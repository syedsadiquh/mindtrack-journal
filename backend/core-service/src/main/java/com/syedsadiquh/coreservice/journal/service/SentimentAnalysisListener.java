package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.infrastructure.client.SentimentAnalyzerClient;
import com.syedsadiquh.coreservice.infrastructure.client.dto.SentimentRequest;
import com.syedsadiquh.coreservice.infrastructure.client.dto.SentimentResponse;
import com.syedsadiquh.coreservice.journal.entity.SentimentAnalysis;
import com.syedsadiquh.coreservice.journal.event.BlockCreatedEvent;
import com.syedsadiquh.coreservice.journal.event.BlockUpdatedEvent;
import com.syedsadiquh.coreservice.journal.repository.JournalBlockRepository;
import com.syedsadiquh.coreservice.journal.repository.SentimentAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisListener {

    private final SentimentAnalyzerClient sentimentAnalyzerClient;
    private final JournalBlockRepository blockRepository;
    private final SentimentAnalysisRepository sentimentAnalysisRepository;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    @ApplicationModuleListener
    @Async
    public void onBlockCreated(BlockCreatedEvent event) {
        log.info("Received sentiment analysis request for block: {}", event.blockId());
        analyzeSentiment(event.blockId(), event.textContent());
    }

    @ApplicationModuleListener
    @Async
    public void onBlockUpdated(BlockUpdatedEvent event) {
        log.info("Re-analyzing sentiment for updated block: {}", event.blockId());
        analyzeSentiment(event.blockId(), event.textContent());
    }

    private void analyzeSentiment(UUID blockId, String text) {
        SentimentRequest request = new SentimentRequest(blockId.toString(), text);
        SentimentResponse response = callWithRetry(request);

        if (response != null) {
            updateSentiment(blockId, response);
        } else {
            log.warn("Sentiment analysis failed after {} retries for block: {}. " +
                    "Sentiment will remain null and can be retried on restart.",
                    MAX_RETRIES, blockId);
        }
    }

    private SentimentResponse callWithRetry(SentimentRequest request) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                SentimentResponse response = sentimentAnalyzerClient.analyze(request);
                log.info("Sentiment analysis successful for block: {} (attempt {})",
                        request.getEntryId(), attempt);
                return response;
            } catch (Exception e) {
                log.warn("Sentiment analysis attempt {}/{} failed for block: {} — {}",
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

    @Transactional("journalTransactionManager")
    protected void updateSentiment(UUID blockId, SentimentResponse response) {
        blockRepository.findById(blockId).ifPresent(block -> {
            SentimentAnalysis analysis = sentimentAnalysisRepository.findByBlockId(blockId)
                    .orElseGet(() -> SentimentAnalysis.builder()
                            .block(block)
                            .analysedBy("SYSTEM")
                            .createdAt(LocalDateTime.now())
                            .deleted(false)
                            .build());

            analysis.setSentimentLabel(response.getSentiment() != null
                    ? response.getSentiment().toUpperCase() : "NEUTRAL");

            // Normalise ML score from [0, 1] → [-1, +1] for consistent storage.
            // If the ML service already returns [-1, +1], this formula is idempotent
            // for the midpoint (0.5 → 0.0) and preserves polarity.
            float rawScore = (float) response.getScore();
            float normalisedScore = (rawScore * 2) - 1;
            analysis.setSentimentScore(normalisedScore);

            analysis.setAnalysedAt(LocalDateTime.now());
            analysis.setUpdatedAt(LocalDateTime.now());

            sentimentAnalysisRepository.save(analysis);
            log.info("Sentiment updated for block: {} — {} ({})",
                    blockId, analysis.getSentimentLabel(), analysis.getSentimentScore());
        });
    }
}
