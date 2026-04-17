package com.syedsadiquh.analyticsservice.service;

import com.syedsadiquh.analyticsservice.entity.UserAnalyticsSummary;
import com.syedsadiquh.analyticsservice.repository.DailySentimentCacheRepository;
import com.syedsadiquh.analyticsservice.repository.UserAnalyticsSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsScheduler {

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
    @Transactional
    public void nightlyStreakRefresh() {
        List<UserAnalyticsSummary> allUsers = summaryRepo.findAll();
        log.info("Nightly streak refresh — processing {} users", allUsers.size());

        for (UserAnalyticsSummary summary : allUsers) {
            UUID userId = summary.getUserId();
            try {
                refreshStreakForUser(userId);
            } catch (Exception e) {
                log.error("Nightly streak refresh failed for user {}", userId, e);
            }
        }

        log.info("Nightly streak refresh complete");
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
