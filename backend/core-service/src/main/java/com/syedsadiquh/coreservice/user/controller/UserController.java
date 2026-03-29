package com.syedsadiquh.coreservice.user.controller;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.response.UserDetailsResponseDto;
import com.syedsadiquh.coreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDetailsResponseDto>> getCurrentUserDetails(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getCurrentUserDetails(UUID.fromString(jwt.getSubject())));
    }
}
