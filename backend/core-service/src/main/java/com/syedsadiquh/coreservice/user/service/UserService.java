package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.UpdateUserRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.UserDetailsResponseDto;

import java.util.UUID;

public interface UserService {

    BaseResponse<UserDetailsResponseDto> getCurrentUserDetails(UUID userId);

    BaseResponse<UserDetailsResponseDto> updateCurrentUserDetails(UUID uuid, UpdateUserRequestDto updateUserRequestDto);
}
