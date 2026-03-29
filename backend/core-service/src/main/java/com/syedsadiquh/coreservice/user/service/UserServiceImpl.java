package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.response.TenantResponseDto;
import com.syedsadiquh.coreservice.user.dto.response.UserDetailsResponseDto;
import com.syedsadiquh.coreservice.user.entity.User;
import com.syedsadiquh.coreservice.user.exception.UserException;
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
}
