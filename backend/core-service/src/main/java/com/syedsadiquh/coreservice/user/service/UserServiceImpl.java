package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.UpdateUserAvatarRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.UpdateUserRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.TenantResponseDto;
import com.syedsadiquh.coreservice.user.dto.response.UserDetailsResponseDto;
import com.syedsadiquh.coreservice.user.entity.Tenant;
import com.syedsadiquh.coreservice.user.entity.User;
import com.syedsadiquh.coreservice.user.exception.UserException;
import com.syedsadiquh.coreservice.user.repository.TenantRepository;
import com.syedsadiquh.coreservice.user.repository.UserRepository;
import com.syedsadiquh.coreservice.user.utils.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final KeycloakService keycloakService;

    public BaseResponse<UserDetailsResponseDto> getUserDetails(UUID userId) {
        try {
            log.info("Getting current user details for userId {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserException("User not found"));

            return BaseResponse.<UserDetailsResponseDto>builder()
                    .success(true)
                    .message("User details retrieved successfully")
                    .data(mapToDto(user))
                    .build();
        } catch (Exception e) {
            log.error("Unable to get user details for userId {}. ERROR: {}", userId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public BaseResponse<UserDetailsResponseDto> updateUserDetails(UUID uuid, UpdateUserRequestDto dto) {
        try {
            log.info("Updating current user details for userId {}", uuid);
            User user = userRepository.findById(uuid)
                    .orElseThrow(() -> new UserException("User not found"));

            boolean requiresKeycloakSync = false;

            // Apply updates locally
            if (dto.getFirstName() != null) {
                user.setFirstName(dto.getFirstName());
                requiresKeycloakSync = true;
            }
            if (dto.getLastName() != null) {
                user.setLastName(dto.getLastName());
                requiresKeycloakSync = true;
            }
            if (dto.getActive() != null) {
                user.setActive(dto.getActive());
                requiresKeycloakSync = true;
            }
            if (dto.getUsername() != null) {
                User existingUser = userRepository.findByUsername(dto.getUsername()).orElse(null);
                if (existingUser != null && !existingUser.getId().equals(uuid)) {
                    throw new UserException("Username already taken. Please choose a different username.");
                }
                user.setUsername(dto.getUsername());
                requiresKeycloakSync = true;
            }
            if (dto.getEmail() != null) {
                User existingUser = userRepository.findByEmail(dto.getEmail());
                if (existingUser != null && !existingUser.getId().equals(uuid)) {
                    throw new UserException("Email already linked to another account.");
                }
                user.setEmail(dto.getEmail());
                requiresKeycloakSync = true;
            }

            // Unmapped Keycloak fields
            if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
            if (dto.getCountryCode() != null) user.setCountryCode(dto.getCountryCode());
            if (dto.getPhone() != null) user.setPhone(dto.getPhone());
            if (dto.getTimezone() != null) user.setTimezone(dto.getTimezone());
            if (dto.getAddress() != null) user.setAddress(dto.getAddress());

            // Tenant updates
            if (dto.getDefaultTenant() != null) {
                Tenant newTenant = tenantRepository.findById(UUID.fromString(dto.getDefaultTenant()))
                        .orElseThrow(() -> new UserException("Tenant not found"));
                user.setDefaultTenant(newTenant);
            }

            // Sync structural identity changes to Keycloak BEFORE saving to DB
            if (requiresKeycloakSync) {
                keycloakService.syncUserToKeycloak(user);
            }

            User updatedUser = userRepository.save(user);

            return BaseResponse.<UserDetailsResponseDto>builder()
                    .success(true)
                    .message("User details updated successfully")
                    .data(mapToDto(updatedUser))
                    .build();
        } catch (Exception e) {
            log.error("Unable to update user details for userId {}. ERROR: {}", uuid, e.getMessage());
            throw e;
        }
    }

    public BaseResponse<String> updateUserAvatar(UUID uuid, UpdateUserAvatarRequestDto requestDto) {
        try {
            log.info("Updating avatar for user details for userId {}", uuid);
            User user = userRepository.findById(uuid)
                    .orElseThrow(() -> new UserException("User not found"));

            user.setAvatarUrl(requestDto.getAvatarUrl());

            userRepository.save(user);

            return BaseResponse.<String>builder()
                    .success(true)
                    .message("User avatar updated successfully")
                    .data(requestDto.getAvatarUrl())
                    .build();
        } catch (Exception e) {
            log.error("Unable to update user avatar for userId {}. ERROR: {}", uuid, e.getMessage());
            throw e;
        }
    }

    private UserDetailsResponseDto mapToDto(User user) {
        return UserDetailsResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .defaultTenant(
                        TenantResponseDto.builder()
                                .tenantId(user.getDefaultTenant().getId())
                                .tenantName(user.getDefaultTenant().getName())
                                .tenantSlug(user.getDefaultTenant().getSlug())
                                .planTier(user.getDefaultTenant().getPlan().getTier())
                                .planDisplayName(user.getDefaultTenant().getPlan().getDisplayName())
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
    }
}