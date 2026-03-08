package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.infrastructure.client.SentimentAnalyzerClient;
import com.syedsadiquh.coreservice.infrastructure.client.dto.SentimentRequest;
import com.syedsadiquh.coreservice.infrastructure.client.dto.SentimentResponse;
import com.syedsadiquh.coreservice.journal.enums.SentimentLabel;
import com.syedsadiquh.coreservice.journal.event.JournalEntryCreatedEvent;
import com.syedsadiquh.coreservice.journal.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Asynchronous listener that consumes {@link JournalEntryCreatedEvent} events
 * and calls the ML sentiment analysis service via Feign.
 *
 * <p><b>Latency Protection:</b> This listener runs on the {@code mlTaskExecutor}
 * thread pool, ensuring the main HTTP request thread is never blocked by
 * serverless cold-starts or ML service latency.</p>
 *
 * <p><b>Idempotency:</b> If the ML service is unavailable or times out, the
 * sentiment fields simply remain null. Spring Modulith's event publication
 * store will re-deliver failed events on restart.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisListener {

    private final SentimentAnalyzerClient sentimentAnalyzerClient;
    private final JournalEntryRepository journalEntryRepository;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    /**
     * Handles the journal entry created event asynchronously.
     * Uses Spring Modulith's @ApplicationModuleListener which ensures
     * the event is processed after the publishing transaction commits.
     */
    @ApplicationModuleListener
    @Async
    public void onJournalEntryCreated(JournalEntryCreatedEvent event) {
        log.info("Received sentiment analysis request for entry: {}", event.entryId());

        SentimentRequest request = new SentimentRequest(
                event.entryId().toString(),
                event.content()
        );

        SentimentResponse response = callWithRetry(request);

        if (response != null) {
            updateSentiment(event.entryId(), response);
        } else {
            log.warn("Sentiment analysis failed after {} retries for entry: {}. " +
                    "Sentiment will remain null and can be retried on restart.",
                    MAX_RETRIES, event.entryId());
        }
    }

    /**
     * Calls the ML service with exponential backoff retry.
     * Handles cold-start timeouts gracefully.
     */
    private SentimentResponse callWithRetry(SentimentRequest request) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                SentimentResponse response = sentimentAnalyzerClient.analyze(request);
                log.info("Sentiment analysis successful for entry: {} (attempt {})",
                        request.getEntryId(), attempt);
                return response;
            } catch (Exception e) {
                log.warn("Sentiment analysis attempt {}/{} failed for entry: {} — {}",
                        attempt, MAX_RETRIES, request.getEntryId(), e.getMessage());

                if (attempt < MAX_RETRIES) {
                    try {
                        long delay = RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Writes the sentiment score and label back to the journal entry.
     * This is an idempotent update — safe to re-run.
     */
    @Transactional
    protected void updateSentiment(UUID entryId, SentimentResponse response) {
        journalEntryRepository.findById(entryId).ifPresent(entry -> {
            entry.setSentimentScore(response.getScore());
            try {
                entry.setSentimentLabel(SentimentLabel.valueOf(response.getSentiment().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown sentiment label '{}' for entry: {}. Defaulting to NEUTRAL.",
                        response.getSentiment(), entryId);
                entry.setSentimentLabel(SentimentLabel.NEUTRAL);
            }
            journalEntryRepository.save(entry);
            log.info("Sentiment updated for entry: {} — {} ({})",
                    entryId, entry.getSentimentLabel(), entry.getSentimentScore());
        });
    }
}

