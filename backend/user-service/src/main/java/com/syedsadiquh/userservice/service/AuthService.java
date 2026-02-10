package com.syedsadiquh.userservice.service;

import com.syedsadiquh.userservice.controller.BaseResponse;
import com.syedsadiquh.userservice.dto.request.LoginRequestDto;
import com.syedsadiquh.userservice.dto.request.RegisterRequestDto;
import com.syedsadiquh.userservice.dto.response.TokenResponse;
import com.syedsadiquh.userservice.dto.response.UserRegisterResponseDto;
import jakarta.transaction.Transactional;

public interface AuthService {
    TokenResponse login(LoginRequestDto request);

    BaseResponse<UserRegisterResponseDto> register(RegisterRequestDto request);

    void deleteUser(String id);
}
