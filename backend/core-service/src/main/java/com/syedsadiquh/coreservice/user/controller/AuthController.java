package com.syedsadiquh.coreservice.user.controller;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.enums.SystemRole;
import com.syedsadiquh.coreservice.user.dto.request.LoginRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.RegisterRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.TokenResponse;
import com.syedsadiquh.coreservice.user.dto.response.UserRegisterResponseDto;
import com.syedsadiquh.coreservice.user.service.AuthService;
import com.syedsadiquh.coreservice.user.utils.AuthCookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        TokenResponse token = authService.login(loginRequestDto);
        HttpHeaders headers = new HttpHeaders();
        cookieService.writeRefreshCookie(headers, token.getRefreshToken());
        token.setRefreshToken(null);
        return ResponseEntity.ok().headers(headers).body(token);
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
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request) {
        String refreshCookie = readCookie(request, cookieService.cookieName());
        if (refreshCookie == null || refreshCookie.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        TokenResponse token = authService.refreshToken(refreshCookie);
        HttpHeaders headers = new HttpHeaders();
        cookieService.writeRefreshCookie(headers, token.getRefreshToken());
        token.setRefreshToken(null);
        return ResponseEntity.ok().headers(headers).body(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        HttpHeaders headers = new HttpHeaders();
        cookieService.clearRefreshCookie(headers);
        return ResponseEntity.noContent().headers(headers).build();
    }

    private static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
