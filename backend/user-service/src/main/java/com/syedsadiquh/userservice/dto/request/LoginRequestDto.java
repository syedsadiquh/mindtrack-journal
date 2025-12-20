package com.syedsadiquh.userservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotEmpty
    String username;
    @NotEmpty
    String password;
}
