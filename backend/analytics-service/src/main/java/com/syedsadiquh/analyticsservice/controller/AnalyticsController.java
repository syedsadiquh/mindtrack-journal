package com.syedsadiquh.analyticsservice.controller;

import com.syedsadiquh.analyticsservice.dto.BaseResponse;
import com.syedsadiquh.analyticsservice.dto.response.AnalyticsSummaryResponse;
import com.syedsadiquh.analyticsservice.dto.response.EmotionDistributionItem;
import com.syedsadiquh.analyticsservice.dto.response.MoodCalendarEntry;
import com.syedsadiquh.analyticsservice.dto.response.SentimentTrendPoint;
import com.syedsadiquh.analyticsservice.dto.response.WritingStreakResponse;
import com.syedsadiquh.analyticsservice.service.AnalyticsRefreshService;
import com.syedsadiquh.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsRefreshService analyticsRefreshService;

    /**
     * Triggers a full data pull from core-service and re-computes all analytics
     * for the authenticated user. Use this when we don't want to wait for the
     * nightly cron job.
     * <p>
     * POST /api/v1/analytics/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<Void>> refresh(@AuthenticationPrincipal Jwt jwt) {
        analyticsRefreshService.refreshForUser(userId(jwt));
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .success(true)
                .message("Analytics refreshed successfully")
                .build());
    }

    /**
     * Sentiment trend over time.
     * <p>
     * GET /api/v1/analytics/sentiment/trends?period=DAILY|WEEKLY|MONTHLY&from=YYYY-MM-DD&to=YYYY-MM-DD
     */
    @GetMapping("/sentiment/trends")
    public ResponseEntity<BaseResponse<List<SentimentTrendPoint>>> getSentimentTrends(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "DAILY") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();
        LocalDate resolvedFrom = from != null ? from : defaultFrom(period, resolvedTo);

        List<SentimentTrendPoint> data = analyticsService.getSentimentTrends(
                userId(jwt), period, resolvedFrom, resolvedTo);

        return ResponseEntity.ok(BaseResponse.<List<SentimentTrendPoint>>builder()
                .success(true)
                .message("Sentiment trends fetched")
                .data(data)
                .build());
    }

    /**
     * Mood calendar heatmap for a given month.
     * <p>
     * GET /api/v1/analytics/mood/calendar?year=2025&month=4
     */
    @GetMapping("/mood/calendar")
    public ResponseEntity<BaseResponse<List<MoodCalendarEntry>>> getMoodCalendar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {

        LocalDate now = LocalDate.now();
        int resolvedYear  = year  > 0 ? year  : now.getYear();
        int resolvedMonth = month > 0 ? month : now.getMonthValue();

        List<MoodCalendarEntry> data = analyticsService.getMoodCalendar(
                userId(jwt), resolvedYear, resolvedMonth);

        return ResponseEntity.ok(BaseResponse.<List<MoodCalendarEntry>>builder()
                .success(true)
                .message("Mood calendar fetched")
                .data(data)
                .build());
    }

    /**
     * Overall analytics summary including sentiment totals and streaks.
     * <p>
     * GET /api/v1/analytics/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<BaseResponse<AnalyticsSummaryResponse>> getSummary(
            @AuthenticationPrincipal Jwt jwt) {

        AnalyticsSummaryResponse data = analyticsService.getSummary(userId(jwt));
        return ResponseEntity.ok(BaseResponse.<AnalyticsSummaryResponse>builder()
                .success(true)
                .message("Analytics summary fetched")
                .data(data)
                .build());
    }

    /**
     * Emotion distribution breakdown for a date range.
     * <p>
     * GET /api/v1/analytics/emotions/distribution?from=YYYY-MM-DD&to=YYYY-MM-DD
     */
    @GetMapping("/emotions/distribution")
    public ResponseEntity<BaseResponse<List<EmotionDistributionItem>>> getEmotionDistribution(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();
        LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(30);

        List<EmotionDistributionItem> data = analyticsService.getEmotionDistribution(
                userId(jwt), resolvedFrom, resolvedTo);

        return ResponseEntity.ok(BaseResponse.<List<EmotionDistributionItem>>builder()
                .success(true)
                .message("Emotion distribution fetched")
                .data(data)
                .build());
    }

    /**
     * Writing streak stats.
     * <p>
     * GET /api/v1/analytics/writing/streak
     */
    @GetMapping("/writing/streak")
    public ResponseEntity<BaseResponse<WritingStreakResponse>> getWritingStreak(
            @AuthenticationPrincipal Jwt jwt) {

        WritingStreakResponse data = analyticsService.getWritingStreak(userId(jwt));
        return ResponseEntity.ok(BaseResponse.<WritingStreakResponse>builder()
                .success(true)
                .message("Writing streak fetched")
                .data(data)
                .build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private LocalDate defaultFrom(String period, LocalDate to) {
        return switch (period.toUpperCase()) {
            case "WEEKLY"  -> to.minusWeeks(12);
            case "MONTHLY" -> to.minusMonths(12);
            default        -> to.minusDays(30);
        };
    }
}
