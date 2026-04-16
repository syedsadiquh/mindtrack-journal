package com.syedsadiquh.coreservice.journal.kafka;

import com.syedsadiquh.coreservice.journal.service.SentimentAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisConsumer {

    private final SentimentAnalysisService sentimentAnalysisService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topics.sentiment-result}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeAnalysisResult(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);

            sentimentAnalysisService.saveAnalysisResult(
                    UUID.fromString(json.get("page_id").asString()),
                    json.get("sentiment_label").asString(),
                    toDoubleMap(json.get("sentiment_scores")),
                    json.get("dominant_emotion").asString(),
                    toDoubleMap(json.get("emotion_vector")),
                    json.get("analyzer_version").asString()
            );
        } catch (Exception e) {
            log.error("Failed to process sentiment analysis result: {}", e.getMessage(), e);
        }
    }

    private Map<String, Double> toDoubleMap(JsonNode node) {
        Map<String, Double> map = new HashMap<>();
        if (node != null && node.isObject()) {
            node.propertyNames().forEach(field ->
                    map.put(field, node.get(field).asDouble())
            );
        }
        return map;
    }
}
