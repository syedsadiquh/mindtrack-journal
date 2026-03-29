package com.syedsadiquh.notification_service.service;

public interface NotificationService {
    void sendTestNotification(String email);

    void sendOnboardingEmail(String email, String name);
}
