package com.syedsadiquh.userservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegisterRequestDto {
    @NotEmpty
    private String username;
    @NotEmpty
    private String email;
    @NotEmpty
    private String password;
    @NotEmpty
    private String firstName;
    private String lastName;
}
