package com.syedsadiquh.coreservice.user.controller;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.UpdateUserAvatarRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.UpdateUserRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.UserDetailsResponseDto;
import com.syedsadiquh.coreservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDetailsResponseDto>> getUserDetails(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUserDetails(UUID.fromString(jwt.getSubject())));
    }

    @PatchMapping("/me")
    public ResponseEntity<BaseResponse<UserDetailsResponseDto>> updateUserDetails(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateUserRequestDto updateUserRequestDto
    ) {
        return ResponseEntity.ok(userService.updateUserDetails(UUID.fromString(jwt.getSubject()), updateUserRequestDto));
    }

    @PutMapping("/me/update-avatar")
    public ResponseEntity<BaseResponse<String>> updateUserAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @ModelAttribute UpdateUserAvatarRequestDto requestDto
    ) {
        return ResponseEntity.ok(userService.updateUserAvatar(UUID.fromString(jwt.getSubject()), requestDto));
    }
}
