package com.syedsadiquh.notification_service.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

public interface KafkaConsumerService {
    void consumeOnboardingEmail(String email, Acknowledgment ack);
}
