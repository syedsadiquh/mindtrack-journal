package com.syedsadiquh.coreservice.user.service;

import com.nimbusds.jwt.SignedJWT;
import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.LoginRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.RegisterRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.TokenResponse;
import com.syedsadiquh.coreservice.user.dto.response.UserRegisterResponseDto;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakService keycloakService;
    private final SlugService slugService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantMemberRepository tenantMemberRepository;

    @Override
    public TokenResponse login(LoginRequestDto request) {
        try {
            TokenResponse response = keycloakService.login(request);

            User user = userRepository.findByUsername(request.getUsername());

            if (user == null) {
                log.info("User logged in via Keycloak but missing locally. Syncing now...");
                user = syncLocalUserFromToken(request.getUsername(), response.getAccessToken());
            }

            String slug;
            if (hasSystemAdminRole(response.getAccessToken())) {
                slug = "system-admin";
            } else {
                slug = (user.getDefaultTenant() != null)
                        ? user.getDefaultTenant().getSlug()
                        : "onboarding";
            }
            response.setDefaultTenantSlug(slug);

            return response;
        } catch (UserException e) {
            throw new UserException("Login failed: " + e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException("Something went wrong");
        }
    }

    private User syncLocalUserFromToken(String email, String jwtToken) throws ParseException {
        SignedJWT parsedToken = SignedJWT.parse(jwtToken);
        String keycloakId = parsedToken.getJWTClaimsSet().getSubject();
        String firstName = (String) parsedToken.getJWTClaimsSet().getClaim("given_name");
        String lastName = (String) parsedToken.getJWTClaimsSet().getClaim("family_name");

        String baseName = (firstName != null ? firstName : "Personal") + " Workspace";
        String slug = slugService.generateUniqueTenantSlug(baseName);

        Tenant personalTenant = Tenant.builder()
                .name(baseName)
                .slug(slug)
                .planTier(PlanTier.FREE)
                .active(true)
                .createdBy("SYSTEM")
                .createdAt(LocalDateTime.now())
                .build();
        personalTenant = tenantRepository.save(personalTenant);

        User newUser = User.builder()
                .id(UUID.fromString(keycloakId))
                .email(email)
                .username(email)
                .name(firstName + " " + lastName)
                .defaultTenant(personalTenant)
                .active(true)
                .createdBy("SYSTEM")
                .createdAt(LocalDateTime.now())
                .build();
        newUser = userRepository.save(newUser);

        TenantMember membership = TenantMember.builder()
                .id(new TenantMemberId(personalTenant.getId(), newUser.getId()))
                .tenant(personalTenant)
                .user(newUser)
                .role(Role.OWNER)
                .createdBy("SYSTEM")
                .createdAt(LocalDateTime.now())
                .build();
        tenantMemberRepository.save(membership);

        return newUser;
    }

    private boolean hasSystemAdminRole(String token) {
        try {
            SignedJWT parsedToken = SignedJWT.parse(token);
            Map<String, Object> claims = parsedToken.getJWTClaimsSet().getClaims();

            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            if (realmAccess == null) return false;

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) return false;

            return roles.contains("SYS_ADMIN");
        } catch (ParseException e) {
            log.error("Failed to parse JWT for role check", e);
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public BaseResponse<UserRegisterResponseDto> register(RegisterRequestDto request) {
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
                    .createdBy("USER")
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

            UserRegisterResponseDto responseDto = UserRegisterResponseDto.builder()
                    .email(savedUser.getEmail())
                    .userId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .build();
            return new BaseResponse<>(true, "User Registered Successfully", responseDto);

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

    @Transactional
    @Override
    public void deleteUser(String id) {
        try {
            User user = userRepository.findById(UUID.fromString(id)).orElseThrow(
                    () -> new UserException("User with id " + id + " does not exist")
            );

            keycloakService.deleteKeycloakUser(id);

            UUID userId = UUID.fromString(id);
            List<TenantMember> memberships = tenantMemberRepository.findByUserId(userId);

            List<Tenant> ownerTenants = memberships.stream()
                    .filter(tm -> tm.getRole() == Role.OWNER)
                    .map(TenantMember::getTenant)
                    .toList();

            memberships.forEach(membership -> {
                membership.setDeleted(true);
                membership.setDeletedAt(LocalDateTime.now());
            });

            ownerTenants.forEach(tenant -> {
                tenant.setActive(false);
                tenant.setDeleted(true);
            });

            user.setActive(false);
            user.setDeleted(true);

            userRepository.save(user);
            tenantRepository.saveAll(ownerTenants);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
}

