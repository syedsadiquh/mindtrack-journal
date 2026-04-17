package com.syedsadiquh.analyticsservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SentimentTrendPoint {
    /**
     * Period label:
     * - ISO date (DAILY),
     * - ISO week start date (WEEKLY), and
     * - YYYY-MM (MONTHLY).
     */
    private String period;
    private Long entryCount;
    private Double avgSentimentScore;
    private String sentimentLabel;
    private String dominantEmotion;
}
