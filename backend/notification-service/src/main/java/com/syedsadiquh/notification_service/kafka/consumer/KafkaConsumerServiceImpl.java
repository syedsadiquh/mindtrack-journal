package com.syedsadiquh.notification_service.kafka.consumer;

import com.syedsadiquh.notification_service.kafka.events.UserRegistered;
import com.syedsadiquh.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topics.onboarding}"
    )
    @Override
    public void consumeOnboardingEmail(String rawUserRegistered, Acknowledgment ack) {
        UserRegistered userRegistered;
        try {
            userRegistered = objectMapper.readValue(rawUserRegistered, UserRegistered.class);
        } catch (Exception e) {
            log.error("Failed to parse UserRegistered object from Kafka message: {}", rawUserRegistered, e);
            return;
        }

        try {
            log.info("Received onboarding email topic for: {}", userRegistered.email());

            notificationService.sendTestNotification(userRegistered.email());

            ack.acknowledge();
            log.info("Onboarding email sent successfully to: {}", userRegistered.email());
        } catch (Exception e) {
            log.error("Failed to send onboarding email to: {}", userRegistered.email(), e);
        }
    }
}
