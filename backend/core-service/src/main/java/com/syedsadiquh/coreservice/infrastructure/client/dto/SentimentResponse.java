package com.syedsadiquh.coreservice.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResponse {
    @JsonProperty("page_id")
    private String pageId;

    @JsonProperty("sentiment_label")
    private String sentimentLabel;

    @JsonProperty("sentiment_scores")
    private Map<String, Double> sentimentScores;

    @JsonProperty("dominant_emotion")
    private String dominantEmotion;

    @JsonProperty("emotion_vector")
    private Map<String, Double> emotionVector;

    @JsonProperty("analyzer_version")
    private String analyzerVersion;
}
