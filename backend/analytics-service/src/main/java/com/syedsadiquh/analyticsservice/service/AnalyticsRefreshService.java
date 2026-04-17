package com.syedsadiquh.analyticsservice.service;

import com.syedsadiquh.analyticsservice.client.CoreServiceClient;
import com.syedsadiquh.analyticsservice.client.dto.JournalPageDto;
import com.syedsadiquh.analyticsservice.dto.BaseResponse;
import com.syedsadiquh.analyticsservice.entity.DailySentimentCache;
import com.syedsadiquh.analyticsservice.entity.UserAnalyticsSummary;
import com.syedsadiquh.analyticsservice.repository.DailySentimentCacheRepository;
import com.syedsadiquh.analyticsservice.repository.UserAnalyticsSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsRefreshService {

    private final CoreServiceClient coreServiceClient;
    private final DailySentimentCacheRepository dailyRepo;
    private final UserAnalyticsSummaryRepository summaryRepo;

    /**
     * Fetches ALL journal pages for the authenticated user from core-service,
     * re-computes analytics, and persists to analytics_service DB.
     * Must be called within a request context so the JWT is forwarded by FeignConfig.
     */
    @Transactional
    public void refreshForUser(UUID userId) {
        log.info("Starting analytics refresh for user {}", userId);

        List<JournalPageDto> allPages = fetchAllPages();
        log.debug("Fetched {} pages from core-service for user {}", allPages.size(), userId);

        // Daily aggregation
        Map<LocalDate, List<JournalPageDto>> byDate = allPages.stream()
                .collect(Collectors.groupingBy(JournalPageDto::getEntryDate));

        List<DailySentimentCache> dailyRecords = byDate.entrySet().stream()
                .map(e -> buildDailyRecord(userId, e.getKey(), e.getValue()))
                .toList();

        dailyRepo.deleteByUserId(userId);
        dailyRepo.flush();
        dailyRepo.saveAll(dailyRecords);

        // Summary
        List<LocalDate> allDates = byDate.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        UserAnalyticsSummary summary = buildSummary(userId, allPages, allDates);
        summaryRepo.save(summary);

        log.info("Analytics refresh complete for user {} — {} entries, {} unique dates",
                userId, allPages.size(), allDates.size());
    }

    // Fetching

    private List<JournalPageDto> fetchAllPages() {
        BaseResponse<List<JournalPageDto>> resp = coreServiceClient.getAnalyticsFeed(null, null);
        List<JournalPageDto> data = resp != null ? resp.getData() : null;
        return data != null ? data : Collections.emptyList();
    }

    // Computation
    private DailySentimentCache buildDailyRecord(UUID userId, LocalDate date, List<JournalPageDto> pages) {
        List<JournalPageDto> scored = pages.stream()
                .filter(p -> p.getSentimentScore() != null)
                .toList();

        Double avgScore = scored.isEmpty() ? null :
                scored.stream().mapToDouble(JournalPageDto::getSentimentScore).average().orElse(0.0);

        String dominantLabel = pages.stream()
                .filter(p -> p.getSentimentLabel() != null)
                .collect(Collectors.groupingBy(JournalPageDto::getSentimentLabel, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String dominantEmotion = pages.stream()
                .filter(p -> p.getDominantEmotion() != null)
                .collect(Collectors.groupingBy(JournalPageDto::getDominantEmotion, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return DailySentimentCache.builder()
                .userId(userId)
                .entryDate(date)
                .entryCount(pages.size())
                .avgSentimentScore(avgScore)
                .dominantLabel(dominantLabel)
                .dominantEmotion(dominantEmotion)
                .build();
    }

    private UserAnalyticsSummary buildSummary(UUID userId, List<JournalPageDto> allPages, List<LocalDate> allDatesSortedDesc) {
        List<JournalPageDto> scored = allPages.stream()
                .filter(p -> p.getSentimentScore() != null)
                .toList();

        Double avgScore = scored.isEmpty() ? null :
                scored.stream().mapToDouble(JournalPageDto::getSentimentScore).average().orElse(0.0);

        long positive = allPages.stream().filter(p -> "POSITIVE".equals(p.getSentimentLabel())).count();
        long negative = allPages.stream().filter(p -> "NEGATIVE".equals(p.getSentimentLabel())).count();
        long neutral  = allPages.stream().filter(p -> "NEUTRAL".equals(p.getSentimentLabel())).count();

        String mostFrequentEmotion = allPages.stream()
                .filter(p -> p.getDominantEmotion() != null)
                .collect(Collectors.groupingBy(JournalPageDto::getDominantEmotion, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return UserAnalyticsSummary.builder()
                .userId(userId)
                .totalEntries((long) allPages.size())
                .avgSentimentScore(avgScore)
                .positiveCount(positive)
                .negativeCount(negative)
                .neutralCount(neutral)
                .mostFrequentEmotion(mostFrequentEmotion)
                .currentStreak(StreakCalculator.currentStreak(allDatesSortedDesc))
                .longestStreak(StreakCalculator.longestStreak(allDatesSortedDesc))
                .lastEntryDate(allDatesSortedDesc.isEmpty() ? null : allDatesSortedDesc.get(0))
                .computedAt(LocalDateTime.now())
                .build();
    }
}
