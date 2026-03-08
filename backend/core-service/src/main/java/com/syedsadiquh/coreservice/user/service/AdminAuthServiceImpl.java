package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.AdminRegisterRequestDto;
import com.syedsadiquh.coreservice.user.entity.Tenant;
import com.syedsadiquh.coreservice.user.entity.TenantMember;
import com.syedsadiquh.coreservice.user.entity.TenantMemberId;
import com.syedsadiquh.coreservice.user.entity.User;
import com.syedsadiquh.coreservice.user.enums.PlanTier;
import com.syedsadiquh.coreservice.user.enums.Role;
import com.syedsadiquh.coreservice.user.exception.UserException;
import com.syedsadiquh.coreservice.user.repository.TenantMemberRepository;
import com.syedsadiquh.coreservice.user.repository.TenantRepository;
import com.syedsadiquh.coreservice.user.repository.UserRepository;
import com.syedsadiquh.coreservice.user.utils.KeycloakService;
import com.syedsadiquh.coreservice.user.utils.SlugService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final KeycloakService keycloakService;
    private final SlugService slugService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantMemberRepository tenantMemberRepository;

    @Override
    public BaseResponse<String> register(AdminRegisterRequestDto request) {
        String keycloakId = null;
        try {
            keycloakId = keycloakService.createKeycloakUser(request);
            UUID userId = UUID.fromString(keycloakId);

            Tenant personalTenant = Tenant.builder()
                    .name(request.getFirstName() + "'s Workspace")
                    .slug(slugService.generateUniqueTenantSlug(request.getFirstName() + " Workspace"))
                    .planTier(PlanTier.FREE)
                    .active(true)
                    .createdBy("SYSTEM")
                    .createdAt(LocalDateTime.now())
                    .build();
            Tenant savedTenant = tenantRepository.save(personalTenant);

            User user = User.builder()
                    .id(userId)
                    .defaultTenant(savedTenant)
                    .username(request.getUsername())
                    .name(request.getFirstName() + " " + request.getLastName())
                    .email(request.getEmail())
                    .active(true)
                    .createdBy("SYSTEM")
                    .createdAt(LocalDateTime.now())
                    .build();
            User savedUser = userRepository.save(user);

            TenantMemberId tenantMemberId = new TenantMemberId(savedTenant.getId(), savedUser.getId());
            TenantMember membership = TenantMember.builder()
                    .id(tenantMemberId)
                    .tenant(savedTenant)
                    .user(savedUser)
                    .role(Role.OWNER)
                    .createdBy("SYSTEM")
                    .createdAt(LocalDateTime.now())
                    .build();
            tenantMemberRepository.save(membership);

            return new BaseResponse<>(true, "Admin User Registered Successfully");

        } catch (Exception e) {
            log.error("Registration failed. Initiating Rollback. Error: {}", e.getMessage());
            if (null != keycloakId) {
                try {
                    keycloakService.deleteKeycloakUser(keycloakId);
                    log.info("Rollback successful: Deleted Keycloak user {}", keycloakId);
                } catch (Exception deleteEx) {
                    log.error("CRITICAL: Rollback failed! Ghost user {} exists in Keycloak.", keycloakId);
                }
            }
            throw new UserException("Registration failed: " + e.getMessage());
        }
    }
}

