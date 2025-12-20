package com.syedsadiquh.userservice.controller;

import com.syedsadiquh.userservice.dto.TokenResponse;
import com.syedsadiquh.userservice.dto.request.LoginRequestDto;
import com.syedsadiquh.userservice.dto.request.RegisterRequestDto;
import com.syedsadiquh.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequestDto loginRequestDto){
        TokenResponse token = authService.login(loginRequestDto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDto registerRequestDto){
        authService.register(registerRequestDto);
        return ResponseEntity.ok("User Registered Successfully");
    }

}
