package com.syedsadiquh.gatewayservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services")
@Getter
@Setter
public class UriConfiguration {
    private String coreService;
    private String paymentService;
    private String analyticsService;
    private String notificationService;
}
