package com.syedsadiquh.notification_service.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

public interface KafkaConsumerService {
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topics.onboarding}"
    )
    void consumeOnboardingEmail(String email, Acknowledgment ack);
}
