package com.syedsadiquh.notification_service.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.syedsadiquh.notification_service.exception.SendEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final Resend resend;

    @Value("${resend.from.onboarding}")
    private String onboardingEmail;

    @Override
    public void sendNotification(String email) {
        log.info("Sending test email notification to: {}", email);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(onboardingEmail)
                .to(email)
                .subject("Test Email")
                .html("<h1>This is a test email</h1>")
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("Email sent successfully: {}", data.getId());
        } catch (ResendException e) {
            throw new SendEmailException(e.getMessage());
        }
    }

}
