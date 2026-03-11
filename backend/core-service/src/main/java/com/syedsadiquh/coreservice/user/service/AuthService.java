package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.LoginRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.RegisterRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.TokenResponse;
import com.syedsadiquh.coreservice.user.dto.response.UserRegisterResponseDto;

public interface AuthService {
    TokenResponse login(LoginRequestDto request);

    BaseResponse<UserRegisterResponseDto> register(RegisterRequestDto request);

    void deleteUser(String id);

    TokenResponse refreshToken(String refreshToken);
}

