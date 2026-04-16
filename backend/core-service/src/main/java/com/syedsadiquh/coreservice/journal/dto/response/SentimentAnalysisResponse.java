package com.syedsadiquh.coreservice.journal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SentimentAnalysisResponse {
    private String sentimentLabel;
    private Float sentimentScore;
    private Map<String, Double> sentimentScores;
    private String dominantEmotion;
    private Map<String, Double> emotionVector;
    private LocalDateTime analysedAt;
}
