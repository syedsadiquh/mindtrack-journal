package com.syedsadiquh.analyticsservice.service;

import com.syedsadiquh.analyticsservice.entity.UserAnalyticsSummary;
import com.syedsadiquh.analyticsservice.repository.DailySentimentCacheRepository;
import com.syedsadiquh.analyticsservice.repository.UserAnalyticsSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsScheduler {

    private static final int BATCH_SIZE = 100;

    private final UserAnalyticsSummaryRepository summaryRepo;
    private final DailySentimentCacheRepository dailyRepo;

    /**
     * Runs at 02:00 every night.
     *
     * <p>Does NOT call core-service (no JWT available in a scheduled context).
     * Instead it re-computes streaks from the existing {@code daily_sentiment_cache}
     * for every user who has analytics data. This keeps streak values accurate
     * as "today" changes without needing a fresh pull from core-service.
     *
     * <p>To pull fresh journal data, users hit {@code POST /api/v1/analytics/refresh}.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void nightlyStreakRefresh() {
        int totalProcessed = 0;
        int pageNumber = 0;
        Page<UserAnalyticsSummary> page;

        do {
            page = summaryRepo.findAll(PageRequest.of(pageNumber, BATCH_SIZE));
            log.info("Nightly streak refresh — processing page {} ({} users)", pageNumber, page.getNumberOfElements());

            for (UserAnalyticsSummary summary : page.getContent()) {
                UUID userId = summary.getUserId();
                try {
                    refreshStreakForUser(userId);
                } catch (Exception e) {
                    log.error("Nightly streak refresh failed for user {}", userId, e);
                }
                totalProcessed++;
            }
            pageNumber++;
        } while (page.hasNext());

        log.info("Nightly streak refresh complete — processed {} users", totalProcessed);
    }

    private void refreshStreakForUser(UUID userId) {
        List<LocalDate> dates = dailyRepo.findEntryDatesByUserIdDesc(userId);

        int currentStreak = StreakCalculator.currentStreak(dates);
        int longestStreak = StreakCalculator.longestStreak(dates);

        int updated = summaryRepo.updateStreaks(userId, currentStreak, longestStreak, LocalDateTime.now());
        if (updated > 0) {
            log.debug("User {} — currentStreak={}, longestStreak={}", userId, currentStreak, longestStreak);
        }
    }
}
