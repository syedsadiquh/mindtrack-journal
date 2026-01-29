package com.syedsadiquh.userservice.controller;

import com.syedsadiquh.userservice.dto.response.TokenResponse;
import com.syedsadiquh.userservice.dto.request.LoginRequestDto;
import com.syedsadiquh.userservice.dto.request.RegisterRequestDto;
import com.syedsadiquh.userservice.dto.response.UserRegisterResponseDto;
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
    public ResponseEntity<BaseResponse<UserRegisterResponseDto>> register(@RequestBody @Valid RegisterRequestDto registerRequestDto){
        BaseResponse<UserRegisterResponseDto> response = authService.register(registerRequestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BaseResponse<String>> deleteUser(@PathVariable String id){
        authService.deleteUser(id);
        return ResponseEntity.ok(new BaseResponse<>("User Deleted Successfully"));
    }

}
