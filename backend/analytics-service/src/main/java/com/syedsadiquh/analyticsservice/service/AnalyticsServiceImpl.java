package com.syedsadiquh.analyticsservice.service;

import com.syedsadiquh.analyticsservice.dto.response.AnalyticsSummaryResponse;
import com.syedsadiquh.analyticsservice.dto.response.EmotionDistributionItem;
import com.syedsadiquh.analyticsservice.dto.response.MoodCalendarEntry;
import com.syedsadiquh.analyticsservice.dto.response.SentimentTrendPoint;
import com.syedsadiquh.analyticsservice.dto.response.WritingStreakResponse;
import com.syedsadiquh.analyticsservice.entity.DailySentimentCache;
import com.syedsadiquh.analyticsservice.repository.DailySentimentCacheRepository;
import com.syedsadiquh.analyticsservice.repository.UserAnalyticsSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final DailySentimentCacheRepository dailyRepo;
    private final UserAnalyticsSummaryRepository summaryRepo;

    // Sentiment Trends
    @Override
    public List<SentimentTrendPoint> getSentimentTrends(UUID userId, String period,
                                                        LocalDate from, LocalDate to) {
        List<DailySentimentCache> records = dailyRepo.findByUserIdAndDateRange(userId, from, to);
        return switch (period.toUpperCase()) {
            case "WEEKLY"  -> aggregateWeekly(records);
            case "MONTHLY" -> aggregateMonthly(records);
            default        -> aggregateDaily(records);
        };
    }

    private List<SentimentTrendPoint> aggregateDaily(List<DailySentimentCache> records) {
        return records.stream()
                .map(r -> SentimentTrendPoint.builder()
                        .period(r.getEntryDate().toString())
                        .entryCount((long) r.getEntryCount())
                        .avgSentimentScore(r.getAvgSentimentScore())
                        .sentimentLabel(r.getDominantLabel())
                        .dominantEmotion(r.getDominantEmotion())
                        .build())
                .toList();
    }

    private List<SentimentTrendPoint> aggregateWeekly(List<DailySentimentCache> records) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        Map<LocalDate, List<DailySentimentCache>> byWeek = records.stream()
                .collect(Collectors.groupingBy(r ->
                        r.getEntryDate().with(weekFields.dayOfWeek(), 1)));  // ISO Monday

        return byWeek.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> toTrendPoint(e.getKey().toString(), e.getValue()))
                .toList();
    }

    private List<SentimentTrendPoint> aggregateMonthly(List<DailySentimentCache> records) {
        Map<String, List<DailySentimentCache>> byMonth = records.stream()
                .collect(Collectors.groupingBy(r ->
                        r.getEntryDate().getYear() + "-" +
                        String.format("%02d", r.getEntryDate().getMonthValue())));

        return byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> toTrendPoint(e.getKey(), e.getValue()))
                .toList();
    }

    private SentimentTrendPoint toTrendPoint(String period, List<DailySentimentCache> group) {
        long entryCount = group.stream().mapToLong(DailySentimentCache::getEntryCount).sum();
        Double avgScore = group.stream()
                .filter(r -> r.getAvgSentimentScore() != null)
                .mapToDouble(DailySentimentCache::getAvgSentimentScore)
                .average()
                .stream().boxed().findFirst().orElse(null);
        String dominantLabel = modeOf(group, DailySentimentCache::getDominantLabel);
        String dominantEmotion = modeOf(group, DailySentimentCache::getDominantEmotion);
        return SentimentTrendPoint.builder()
                .period(period)
                .entryCount(entryCount)
                .avgSentimentScore(avgScore)
                .sentimentLabel(dominantLabel)
                .dominantEmotion(dominantEmotion)
                .build();
    }

    private String modeOf(List<DailySentimentCache> group,
                          java.util.function.Function<DailySentimentCache, String> extractor) {
        return group.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Mood Calendar
    @Override
    public List<MoodCalendarEntry> getMoodCalendar(UUID userId, int year, int month) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

        return dailyRepo.findByUserIdAndDateRange(userId, from, to).stream()
                .map(r -> MoodCalendarEntry.builder()
                        .date(r.getEntryDate().toString())
                        .entryCount(r.getEntryCount())
                        .sentimentScore(r.getAvgSentimentScore())
                        .sentimentLabel(r.getDominantLabel())
                        .dominantEmotion(r.getDominantEmotion())
                        .build())
                .toList();
    }

    // Summary
    @Override
    public AnalyticsSummaryResponse getSummary(UUID userId) {
        return summaryRepo.findById(userId)
                .map(s -> AnalyticsSummaryResponse.builder()
                        .totalEntries(s.getTotalEntries())
                        .avgSentimentScore(s.getAvgSentimentScore())
                        .positiveCount(s.getPositiveCount())
                        .negativeCount(s.getNegativeCount())
                        .neutralCount(s.getNeutralCount())
                        .mostFrequentEmotion(s.getMostFrequentEmotion())
                        .currentStreak(s.getCurrentStreak())
                        .longestStreak(s.getLongestStreak())
                        .build())
                .orElse(AnalyticsSummaryResponse.builder()
                        .totalEntries(0L)
                        .positiveCount(0L)
                        .negativeCount(0L)
                        .neutralCount(0L)
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());
    }

    // Emotion Distribution
    @Override
    public List<EmotionDistributionItem> getEmotionDistribution(UUID userId, LocalDate from, LocalDate to) {
        List<DailySentimentCache> records = dailyRepo.findByUserIdAndDateRange(userId, from, to);

        Map<String, Long> counts = records.stream()
                .filter(r -> r.getDominantEmotion() != null)
                .collect(Collectors.groupingBy(DailySentimentCache::getDominantEmotion, Collectors.counting()));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        List<EmotionDistributionItem> items = new ArrayList<>();
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> items.add(EmotionDistributionItem.builder()
                        .emotion(e.getKey())
                        .count(e.getValue())
                        .percentage(total > 0 ? Math.round((e.getValue() * 100.0 / total) * 100.0) / 100.0 : 0.0)
                        .build()));
        return items;
    }

    // Writing Streak
    @Override
    public WritingStreakResponse getWritingStreak(UUID userId) {
        return summaryRepo.findById(userId)
                .map(s -> WritingStreakResponse.builder()
                        .currentStreak(s.getCurrentStreak())
                        .longestStreak(s.getLongestStreak())
                        .lastEntryDate(s.getLastEntryDate())
                        .build())
                .orElse(WritingStreakResponse.builder()
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());
    }
}
