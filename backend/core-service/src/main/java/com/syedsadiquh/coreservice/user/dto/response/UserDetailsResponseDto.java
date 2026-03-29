package com.syedsadiquh.coreservice.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsResponseDto {
    private UUID id;
    private String username;
    private String name;
    private String email;
    private TenantResponseDto defaultTenant;
    private Boolean active;
    private String avatarUrl;
    private String countryCode;
    private String phone;
    private String timezone;
    private String address;
}
