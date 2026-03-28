package com.syedsadiquh.coreservice.user.kafka.producer;

public interface NotificationProducerService {
    void sendOnboardingEmail(String email, String name);
}
