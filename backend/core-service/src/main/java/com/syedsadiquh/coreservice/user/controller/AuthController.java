package com.syedsadiquh.coreservice.user.controller;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.enums.SystemRole;
import com.syedsadiquh.coreservice.user.dto.request.LoginRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.RefreshTokenRequest;
import com.syedsadiquh.coreservice.user.dto.request.RegisterRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.TokenResponse;
import com.syedsadiquh.coreservice.user.dto.response.UserRegisterResponseDto;
import com.syedsadiquh.coreservice.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        TokenResponse token = authService.login(loginRequestDto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<UserRegisterResponseDto>> register(
            @RequestBody @Valid RegisterRequestDto registerRequestDto) {
        BaseResponse<UserRegisterResponseDto> response = authService.register(registerRequestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize(SystemRole.HAS_ROLE_ADMIN_OR_SYS_ADMIN)
    public ResponseEntity<BaseResponse<String>> deleteUser(@PathVariable String id) {
        authService.deleteUser(id);
        return ResponseEntity.ok(new BaseResponse<>("User Deleted Successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest tokenDto) {
        TokenResponse token = authService.refreshToken(tokenDto.refreshToken());
        return ResponseEntity.ok(token);
    }
}

