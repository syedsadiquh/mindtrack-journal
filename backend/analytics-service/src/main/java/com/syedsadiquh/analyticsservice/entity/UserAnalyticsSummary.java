package com.syedsadiquh.analyticsservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_analytics_summary")
public class UserAnalyticsSummary {

    // Keycloak sub claim - no auto-generation, caller supplies this.
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "total_entries")
    @Builder.Default
    private Long totalEntries = 0L;

    @Column(name = "avg_sentiment_score")
    private Double avgSentimentScore;

    @Column(name = "positive_count")
    @Builder.Default
    private Long positiveCount = 0L;

    @Column(name = "negative_count")
    @Builder.Default
    private Long negativeCount = 0L;

    @Column(name = "neutral_count")
    @Builder.Default
    private Long neutralCount = 0L;

    @Column(name = "most_frequent_emotion", length = 50)
    private String mostFrequentEmotion;

    @Column(name = "current_streak")
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "longest_streak")
    @Builder.Default
    private Integer longestStreak = 0;

    @Column(name = "last_entry_date")
    private LocalDate lastEntryDate;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;
}
