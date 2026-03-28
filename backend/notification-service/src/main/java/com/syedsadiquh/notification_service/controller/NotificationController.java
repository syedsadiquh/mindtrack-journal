package com.syedsadiquh.notification_service.controller;

import com.syedsadiquh.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/test")
    public void sendNotification(@RequestParam String email) {
        log.info("Preparing to send test email notification to: {}", email);
        notificationService.sendTestNotification(email);
    }

}
