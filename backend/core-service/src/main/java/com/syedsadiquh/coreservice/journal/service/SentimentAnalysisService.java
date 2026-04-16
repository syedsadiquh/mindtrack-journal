package com.syedsadiquh.coreservice.journal.service;

import com.syedsadiquh.coreservice.journal.entity.JournalPage;
import com.syedsadiquh.coreservice.journal.entity.SentimentAnalysis;
import com.syedsadiquh.coreservice.journal.repository.JournalPageRepository;
import com.syedsadiquh.coreservice.journal.repository.SentimentAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisService {

    private final JournalPageRepository pageRepository;
    private final SentimentAnalysisRepository sentimentAnalysisRepository;

    @Transactional("journalTransactionManager")
    public SentimentAnalysis saveAnalysisResult(
            JournalPage page,
            String sentimentLabel,
            Map<String, Double> sentimentScores,
            String dominantEmotion,
            Map<String, Double> emotionVector,
            String modelVersion
    ) {
        SentimentAnalysis analysis = sentimentAnalysisRepository.findByPageId(page.getId())
                .orElseGet(() -> SentimentAnalysis.builder()
                        .page(page)
                        .analysedBy("SYSTEM")
                        .createdAt(LocalDateTime.now())
                        .deleted(false)
                        .build());

        analysis.setSentimentLabel(sentimentLabel.toUpperCase());
        analysis.setSentimentScore(computeNormalisedScore(sentimentScores));
        analysis.setSentimentScores(sentimentScores);
        analysis.setDominantEmotion(dominantEmotion);
        analysis.setEmotionVector(emotionVector);
        analysis.setAiModelVersion(modelVersion);
        analysis.setAnalysedAt(LocalDateTime.now());
        analysis.setUpdatedAt(LocalDateTime.now());

        SentimentAnalysis saved = sentimentAnalysisRepository.save(analysis);
        log.info("Sentiment saved for page: {} — {} emotion={}",
                page.getId(), sentimentLabel, dominantEmotion);
        return saved;
    }

    @Transactional("journalTransactionManager")
    public void saveAnalysisResult(
            UUID pageId,
            String sentimentLabel,
            Map<String, Double> sentimentScores,
            String dominantEmotion,
            Map<String, Double> emotionVector,
            String modelVersion
    ) {
        pageRepository.findById(pageId).ifPresentOrElse(
                page -> saveAnalysisResult(page, sentimentLabel, sentimentScores, dominantEmotion, emotionVector, modelVersion),
                () -> log.warn("Page not found for sentiment result: {}", pageId)
        );
    }

    /**
     * Convert sentiment scores to a single [-1, +1] value: positive - negative.
     */
    static float computeNormalisedScore(Map<String, Double> scores) {
        if (scores == null) return 0f;
        double pos = scores.getOrDefault("POSITIVE", 0.0);
        double neg = scores.getOrDefault("NEGATIVE", 0.0);
        return (float) Math.max(-1.0, Math.min(1.0, pos - neg));
    }
}
