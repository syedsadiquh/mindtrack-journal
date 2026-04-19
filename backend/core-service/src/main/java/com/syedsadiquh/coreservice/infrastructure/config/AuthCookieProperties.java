package com.syedsadiquh.coreservice.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.cookie")
public class AuthCookieProperties {
    private String name = "refresh_token";
    private String path = "/api/v1/auth";
    private String domain = "";
    private boolean secure = false;
    private String sameSite = "Lax";
    private long maxAgeSeconds = 1800;
}
