package com.syedsadiquh.analyticsservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    /**
     * Forwards the current request's Bearer token to downstream core-service calls.
     * Ensures analytics-service never stores or reuses JWTs — every call gets the
     * live token from the original request context.
     */
    @Bean
    public RequestInterceptor jwtForwardingInterceptor() {
        return template -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                String auth = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (auth != null) {
                    template.header(HttpHeaders.AUTHORIZATION, auth);
                }
            }
        };
    }
}
