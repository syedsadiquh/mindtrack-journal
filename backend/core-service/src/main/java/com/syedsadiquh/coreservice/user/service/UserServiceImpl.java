package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.UpdateUserRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.TenantResponseDto;
import com.syedsadiquh.coreservice.user.dto.response.UserDetailsResponseDto;
import com.syedsadiquh.coreservice.user.entity.Tenant;
import com.syedsadiquh.coreservice.user.entity.User;
import com.syedsadiquh.coreservice.user.exception.UserException;
import com.syedsadiquh.coreservice.user.repository.TenantRepository;
import com.syedsadiquh.coreservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public BaseResponse<UserDetailsResponseDto> getCurrentUserDetails(UUID userId) {
        try {
            log.info("Getting current user details for userId {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserException("User not found"));

            UserDetailsResponseDto responseDto = UserDetailsResponseDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .email(user.getEmail())
                    .defaultTenant(
                            TenantResponseDto.builder()
                                    .tenantId(user.getDefaultTenant().getId())
                                    .tenantName(user.getDefaultTenant().getName())
                                    .tenantSlug(user.getDefaultTenant().getSlug())
                                    .planTier(user.getDefaultTenant().getPlanTier())
                                    .active(user.getDefaultTenant().getActive())
                                    .build()
                    )
                    .active(user.getActive())
                    .avatarUrl(user.getAvatarUrl())
                    .countryCode(user.getCountryCode())
                    .phone(user.getPhone())
                    .timezone(user.getTimezone())
                    .address(user.getAddress())
                    .build();

            return BaseResponse.<UserDetailsResponseDto>builder()
                    .success(true)
                    .message("User details retrieved successfully")
                    .data(responseDto)
                    .build();
        } catch (Exception e) {
            log.error("Unable to get user details for userId {}. ERROR: {}", userId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public BaseResponse<UserDetailsResponseDto> updateCurrentUserDetails(UUID uuid, UpdateUserRequestDto updateUserRequestDto) {
        try {
            log.info("Updating current user details for userId {}", uuid);
            User user = userRepository.findById(uuid)
                    .orElseThrow(() -> new UserException("User not found"));

            // Update fields
            if (updateUserRequestDto.getName() != null) user.setName(updateUserRequestDto.getName());
            if (updateUserRequestDto.getUsername() != null) {
                User existingUser = userRepository.findByUsername(updateUserRequestDto.getUsername()).orElse(null);
                if (existingUser != null && !existingUser.getId().equals(uuid)) {
                    throw new UserException("Username already taken. Please choose a different username.");
                }
                user.setUsername(updateUserRequestDto.getUsername());
            }
            if (updateUserRequestDto.getEmail() != null) {
                User existingUser = userRepository.findByEmail(updateUserRequestDto.getEmail());
                if (existingUser != null && !existingUser.getId().equals(uuid)) {
                    throw new UserException("Email already linked to another account. Please use a different email.");
                }
                user.setEmail(updateUserRequestDto.getEmail());
            }
            if (updateUserRequestDto.getAvatarUrl() != null) user.setAvatarUrl(updateUserRequestDto.getAvatarUrl());
            if (updateUserRequestDto.getCountryCode() != null) user.setCountryCode(updateUserRequestDto.getCountryCode());
            if (updateUserRequestDto.getPhone() != null) user.setPhone(updateUserRequestDto.getPhone());
            if (updateUserRequestDto.getTimezone() != null) user.setTimezone(updateUserRequestDto.getTimezone());
            if (updateUserRequestDto.getAddress() != null) user.setAddress(updateUserRequestDto.getAddress());
            if (updateUserRequestDto.getActive() != null) user.setActive(updateUserRequestDto.getActive());
            if (updateUserRequestDto.getDefaultTenant() != null) {
                Tenant newTenant = tenantRepository.findById(UUID.fromString(updateUserRequestDto.getDefaultTenant()))
                        .orElseThrow(() -> new UserException("Tenant not found"));
                user.setDefaultTenant(newTenant);
            }

            User updatedUser = userRepository.save(user);

            UserDetailsResponseDto responseDto = UserDetailsResponseDto.builder()
                    .id(updatedUser.getId())
                    .username(updatedUser.getUsername())
                    .name(updatedUser.getName())
                    .email(updatedUser.getEmail())
                    .defaultTenant(
                            TenantResponseDto.builder()
                                    .tenantId(updatedUser.getDefaultTenant().getId())
                                    .tenantName(updatedUser.getDefaultTenant().getName())
                                    .tenantSlug(updatedUser.getDefaultTenant().getSlug())
                                    .planTier(updatedUser.getDefaultTenant().getPlanTier())
                                    .active(updatedUser.getDefaultTenant().getActive())
                                    .build()
                    )
                    .active(updatedUser.getActive())
                    .avatarUrl(updatedUser.getAvatarUrl())
                    .countryCode(updatedUser.getCountryCode())
                    .phone(updatedUser.getPhone())
                    .timezone(updatedUser.getTimezone())
                    .address(updatedUser.getAddress())
                    .build();

            return BaseResponse.<UserDetailsResponseDto>builder()
                    .success(true)
                    .message("User details updated successfully")
                    .data(responseDto)
                    .build();
        } catch (Exception e) {
            log.error("Unable to update user details for userId {}. ERROR: {}", uuid, e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
