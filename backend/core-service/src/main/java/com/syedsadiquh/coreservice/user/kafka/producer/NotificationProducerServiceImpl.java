package com.syedsadiquh.coreservice.user.kafka.producer;

import com.syedsadiquh.coreservice.user.kafka.events.UserRegistered;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducerServiceImpl implements NotificationProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.onboarding}")
    private String onboardingTopic;

    @Override
    public void sendOnboardingEmail(String email, String name) {

        UserRegistered obj = UserRegistered.builder()
                .name(name)
                .email(email)
                .build();

        kafkaTemplate.send(onboardingTopic, objectMapper.writeValueAsString(obj));

        log.info("Onboarding email request for {} sent via kafka", email);
    }

}
