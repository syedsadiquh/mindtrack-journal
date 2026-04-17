package com.syedsadiquh.analyticsservice.service;

import com.syedsadiquh.analyticsservice.dto.response.AnalyticsSummaryResponse;
import com.syedsadiquh.analyticsservice.dto.response.EmotionDistributionItem;
import com.syedsadiquh.analyticsservice.dto.response.MoodCalendarEntry;
import com.syedsadiquh.analyticsservice.dto.response.SentimentTrendPoint;
import com.syedsadiquh.analyticsservice.dto.response.WritingStreakResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AnalyticsService {

    List<SentimentTrendPoint> getSentimentTrends(UUID userId, String period, LocalDate from, LocalDate to);

    List<MoodCalendarEntry> getMoodCalendar(UUID userId, int year, int month);

    AnalyticsSummaryResponse getSummary(UUID userId);

    List<EmotionDistributionItem> getEmotionDistribution(UUID userId, LocalDate from, LocalDate to);

    WritingStreakResponse getWritingStreak(UUID userId);
}
