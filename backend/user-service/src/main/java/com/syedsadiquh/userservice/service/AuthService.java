package com.syedsadiquh.userservice.service;

import com.nimbusds.jwt.SignedJWT;
import com.syedsadiquh.userservice.dto.TokenResponse;
import com.syedsadiquh.userservice.dto.request.LoginRequestDto;
import com.syedsadiquh.userservice.dto.request.RegisterRequestDto;
import com.syedsadiquh.userservice.entity.Tenant;
import com.syedsadiquh.userservice.entity.TenantMember;
import com.syedsadiquh.userservice.entity.User;
import com.syedsadiquh.userservice.entity.compositeKey.TenantMemberId;
import com.syedsadiquh.userservice.enums.PlanTier;
import com.syedsadiquh.userservice.enums.Role;
import com.syedsadiquh.userservice.exception.UserException;
import com.syedsadiquh.userservice.repository.TenantMemberRepository;
import com.syedsadiquh.userservice.repository.TenantRepository;
import com.syedsadiquh.userservice.repository.UserRepository;
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
public class AuthService {

    private final KeycloakService keycloakService;
    private final SlugService slugService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantMemberRepository tenantMemberRepository;

    // LOGIN USER
    public TokenResponse login(LoginRequestDto request) {
        try {
            TokenResponse response = keycloakService.login(request);

            User user = userRepository.findByUsername(request.getUsername());

            // JIT SYNC: If missing locally, create them now!
            if (user == null) {
                log.info("User logged in via Keycloak but missing locally. Syncing now...");
                user = syncLocalUserFromToken(request.getUsername(), response.getAccessToken());
            }

            // Determine Landing Page (Slug)
            String slug;
            if (hasSystemAdminRole(response.getAccessToken())) {
                slug = "system-admin"; // Special landing for Super Admins
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

    private User syncLocalUserFromToken(String email, String jwtToken) throws java.text.ParseException {
        // Parse JWT to get the real Keycloak UUID (sub)
        SignedJWT parsedToken = SignedJWT.parse(jwtToken);
        String keycloakId = parsedToken.getJWTClaimsSet().getSubject();
        String firstName = (String) parsedToken.getJWTClaimsSet().getClaim("given_name");
        String lastName = (String) parsedToken.getJWTClaimsSet().getClaim("family_name");

        // 2. Create the "Personal Workspace" (Tenant) FIRST
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

        // Save Tenant immediately so we have an ID
        personalTenant = tenantRepository.save(personalTenant);

        // 3. Create the User (Now linked to the Tenant)
        User newUser = User.builder()
                .id(UUID.fromString(keycloakId)) // Keycloak ID
                .email(email)
                .username(email)
                .name(firstName + " " + lastName)
                .defaultTenant(personalTenant)
                .active(true)
                .createdBy("SYSTEM")
                .createdAt(LocalDateTime.now())
                .build();

        // Save User
        newUser = userRepository.save(newUser);

        // 4. Create the Membership (OWNER)
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

    // Helper to check for Admin Role
    private boolean hasSystemAdminRole(String token) {
        try {
            // 1. Parse the Token
            SignedJWT parsedToken = SignedJWT.parse(token);
            Map<String, Object> claims = parsedToken.getJWTClaimsSet().getClaims();

            // 2. Extract "realm_access" object
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");

            if (realmAccess == null) {
                return false; // No realm roles defined
            }

            // 3. Extract the "roles" list
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");

            if (roles == null) {
                return false;
            }

            // 4. Check for your specific Role Key
            return roles.contains("SYS_ADMIN");

        } catch (java.text.ParseException e) {
            log.error("Failed to parse JWT for role check", e);
            return false;
        } catch (Exception e) {
            // Catch casting errors if the token structure is weird
            return false;
        }
    }

    // REGISTER NEW USER
    public void register(RegisterRequestDto request) {
        String keycloakId = null;
        try {
            keycloakId = keycloakService.createKeycloakUser(request);

            UUID userId = UUID.fromString(keycloakId);

            // Building their personal workspace
            Tenant personalTenant = Tenant.builder()
                    .name(request.getFirstName()+"'s Workspace")
                    .slug(slugService.generateUniqueTenantSlug(request.getFirstName() + " Workspace"))
                    .planTier(PlanTier.FREE)
                    .active(true)
                    .createdBy("SYSTEM")
                    .createdAt(LocalDateTime.now())
                    .build();
            Tenant savedTenant = tenantRepository.save(personalTenant);

            // Building user
            User user = User.builder()
                    .id(userId)
                    .defaultTenant(savedTenant)
                    .username(request.getUsername())
                    .name(request.getFirstName()+ " " +request.getLastName())
                    .email(request.getEmail())
                    .active(true)
                    .createdBy("SYSTEM")
                    .createdAt(LocalDateTime.now())
                    .build();
            User savedUser = userRepository.save(user);

            // establishing roles
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

        } catch (Exception e) {
            log.error("Registration failed. Initiating Rollback. Error: {}", e.getMessage());
            if (null != keycloakId) {
                try {
                    keycloakService.deleteKeycloakUser(keycloakId);
                    log.info("Rollback successful: Deleted Keycloak user {}", keycloakId);
                } catch (Exception deleteEx) {
                    log.error("CRITICAL: Rollback failed! Ghost user {} exists in Keycloak.", keycloakId);
                    // TODO: Later: notify sys_admin to manually fix this.
                }
            }
            throw new UserException("Registration failed: " + e.getMessage());
        }
    }
}
