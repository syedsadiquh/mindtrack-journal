package com.syedsadiquh.gatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator gatewayRoutes(
            RouteLocatorBuilder builder,
            UriConfiguration uriConfiguration
    ) {
        String coreServiceUri = uriConfiguration.getCoreService();
        String paymentServiceUri = uriConfiguration.getPaymentService();
        String analyticsServiceUri = uriConfiguration.getAnalyticsService();
        String notificationServiceUri = uriConfiguration.getNotificationService();

        return builder.routes()
                .route("core-user-routes", route -> route
                        .path(
                                "/api/v1/auth/**",
                                "/api/v1/admin/**",
                                "/api/v1/users/**",
                                "/api/v1/plans",
                                "/api/v1/plans/**"
                        )
                        .uri(coreServiceUri))
                .route("core-journal-routes", route -> route
                        .path(
                                "/api/v1/journals/**"
                        )
                        .uri(coreServiceUri))
                .route("analytics-routes", route -> route
                        .path(
                                "/api/v1/analytics/**"
                        )
                        .uri(analyticsServiceUri))
                .route("notification-routes", route -> route
                        .path(
                                "/api/v1/notification/**"
                        )
                        .uri(notificationServiceUri))
                .build();
    }
}
