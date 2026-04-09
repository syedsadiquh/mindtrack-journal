package com.syedsadiquh.coreservice.journal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentAnalysisResponse {
    private String sentimentLabel;
    private Float sentimentScore;
    private Map<String, Double> emotionVector;
    private LocalDateTime analysedAt;
}
