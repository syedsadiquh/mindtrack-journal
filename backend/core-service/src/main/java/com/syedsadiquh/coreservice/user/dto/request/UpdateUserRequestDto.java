package com.syedsadiquh.coreservice.user.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequestDto {
    private String fullName;
    private String username;
    private String email;
    private String avatarUrl;
    private String countryCode;
    private String phone;
    private String timezone;
    private String address;
    private Boolean active;
    private String defaultTenant;
}
