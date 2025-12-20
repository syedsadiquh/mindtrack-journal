package com.syedsadiquh.userservice.service;

import com.syedsadiquh.userservice.dto.TokenResponse;
import com.syedsadiquh.userservice.dto.request.LoginRequestDto;
import com.syedsadiquh.userservice.dto.request.RegisterRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;

    // LOGIN USER
    public TokenResponse login(LoginRequestDto request) {
        return keycloakService.login(request);
    }

    // REGISTER NEW USER
    @Transactional
    public void register(RegisterRequestDto request) {
        try {
            keycloakService.register(request);
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
}
