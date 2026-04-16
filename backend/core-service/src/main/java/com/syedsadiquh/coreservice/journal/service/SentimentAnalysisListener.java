package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.event.PageAnalysisEvent;
import com.syedsadiquh.coreservice.journal.kafka.SentimentAnalysisProducer;
import com.syedsadiquh.coreservice.journal.repository.JournalPageRepository;
import com.syedsadiquh.coreservice.journal.util.BlockTextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisListener {

    private final JournalPageRepository pageRepository;
    private final SentimentAnalysisProducer sentimentAnalysisProducer;

    @ApplicationModuleListener
    @Async
    @Transactional(value = "journalTransactionManager", propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onPageAnalysisRequested(PageAnalysisEvent event) {
        log.info("Received page analysis event for page: {}", event.pageId());

        String aggregatedText = pageRepository.findById(event.pageId())
                .map(BlockTextUtil::aggregateText)
                .orElse(null);

        if (aggregatedText == null || aggregatedText.isBlank()) {
            log.info("No text content found for page: {}. Skipping analysis.", event.pageId());
            return;
        }

        sentimentAnalysisProducer.requestPageAnalysis(event.pageId(), aggregatedText);
    }
}
