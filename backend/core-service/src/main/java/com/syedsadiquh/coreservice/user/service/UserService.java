package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.UpdateUserAvatarRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.UpdateUserRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.UserDetailsResponseDto;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    BaseResponse<UserDetailsResponseDto> getUserDetails(UUID userId);

    BaseResponse<UserDetailsResponseDto> updateUserDetails(UUID uuid, UpdateUserRequestDto updateUserRequestDto);

    BaseResponse<String> updateUserAvatar(UUID uuid, UpdateUserAvatarRequestDto requestDto);
}
