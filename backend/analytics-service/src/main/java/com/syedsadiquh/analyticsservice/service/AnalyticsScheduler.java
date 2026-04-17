package com.syedsadiquh.analyticsservice.service;

import com.syedsadiquh.analyticsservice.entity.UserAnalyticsSummary;
import com.syedsadiquh.analyticsservice.repository.UserAnalyticsSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsScheduler {

    private final UserAnalyticsSummaryRepository summaryRepo;
    private final AnalyticsRefreshService analyticsRefreshService;

    /**
     * Runs at 02:00 every night.
     *
     * <p>Pulls fresh journal data from core-service and re-computes full analytics
     * for every user who has analytics data.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void nightlyAnalyticsRefresh() {
        List<UserAnalyticsSummary> allUsers = summaryRepo.findAll();
        log.info("Nightly analytics refresh — processing {} users", allUsers.size());

        for (UserAnalyticsSummary summary : allUsers) {
            UUID userId = summary.getUserId();
            try {
                analyticsRefreshService.refreshForUser(userId);
            } catch (Exception e) {
                log.error("Nightly analytics refresh failed for user {}", userId, e);
            }
        }

        log.info("Nightly analytics refresh complete");
    }
}
