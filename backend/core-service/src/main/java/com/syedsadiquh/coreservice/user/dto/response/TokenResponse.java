package com.syedsadiquh.coreservice.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String refreshToken;
    @JsonProperty("expires_in")
    private String expiresIn;
    private String defaultTenantSlug;
}

