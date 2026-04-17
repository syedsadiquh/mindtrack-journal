package com.syedsadiquh.analyticsservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsSummaryResponse {
    private Long totalEntries;
    private Double avgSentimentScore;
    private Long positiveCount;
    private Long negativeCount;
    private Long neutralCount;
    private String mostFrequentEmotion;
    private Integer currentStreak;
    private Integer longestStreak;
}
