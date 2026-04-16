package com.syedsadiquh.coreservice.journal.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.sentiment-request}")
    private String sentimentRequestTopic;

    public void requestPageAnalysis(UUID pageId, String text) {
        Map<String, String> payload = Map.of(
                "page_id", pageId.toString(),
                "text", text
        );

        kafkaTemplate.send(sentimentRequestTopic, pageId.toString(), objectMapper.writeValueAsString(payload));
        log.info("Sentiment analysis request sent to Kafka for page: {}", pageId);
    }
}
