package com.syedsadiquh.notification_service.service;

import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void sendNotification(String email);
}
