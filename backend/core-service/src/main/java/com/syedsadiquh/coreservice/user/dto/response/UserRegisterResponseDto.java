package com.syedsadiquh.coreservice.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponseDto {
    private UUID userId;
    private String username;
    private String email;
}

