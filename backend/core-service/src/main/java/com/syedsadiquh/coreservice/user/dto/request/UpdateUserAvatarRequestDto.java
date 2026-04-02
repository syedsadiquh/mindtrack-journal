package com.syedsadiquh.coreservice.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserAvatarRequestDto {
    @NotNull(message = "Avatar Url must not be null")
    private String avatarUrl;
}
