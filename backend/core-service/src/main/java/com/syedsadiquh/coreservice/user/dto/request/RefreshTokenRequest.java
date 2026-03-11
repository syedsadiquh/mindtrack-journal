package com.syedsadiquh.coreservice.user.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record RefreshTokenRequest(@NotEmpty String refreshToken) {
}
