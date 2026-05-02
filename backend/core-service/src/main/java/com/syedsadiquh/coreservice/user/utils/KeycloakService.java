package com.syedsadiquh.coreservice.user.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syedsadiquh.coreservice.user.dto.request.LoginRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.RegisterRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.TokenResponse;
import com.syedsadiquh.coreservice.user.entity.User;
import com.syedsadiquh.coreservice.user.enums.SystemRole;
import com.syedsadiquh.coreservice.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {
    private final RestClient restClient;

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.credentials.client-secret}")
    private String clientSecret;

    // Token Caching
    private String cachedAdminToken;
    private long tokenExpirationTime;

    // LOGIN USER
    public TokenResponse login(LoginRequestDto request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", request.getUsername());
        formData.add("password", request.getPassword());

        return restClient.post()
                .uri(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .onStatus(status -> status.value() == 401, (req, resp) -> {
                    throw new BadCredentialsException("Invalid username or password.");
                })
                .body(TokenResponse.class);
    }

    // REGISTER NEW USER
    public String createKeycloakUser(RegisterRequestDto request) {
        String adminToken = getAdminToken();

        Map<String, Object> user = new HashMap<>();
        user.put("username", request.getUsername());
        user.put("email", request.getEmail());
        user.put("enabled", true);
        user.put("emailVerified", true);
        user.put("firstName", request.getFirstName());
        user.put("lastName", request.getLastName());
        user.put("requiredActions", Collections.emptyList());

        Map<String, Object> creds = new HashMap<>();
        creds.put("type", "password");
        creds.put("value", request.getPassword());
        creds.put("temporary", false);
        user.put("credentials", List.of(creds));

        ResponseEntity<Void> response = restClient.post()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .onStatus(status -> status.value() == 409, (req, resp) -> {
                    throw new UserException("User already exists in Identity Provider!");
                })
                .toBodilessEntity();

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new RuntimeException("User created but Auth server did not return a Location header.");
        }

        String path = location.getPath();
        String userId = path.substring(path.lastIndexOf('/') + 1);

        // Assign the default USER system role directly using the ID we just parsed
        try {
            assignRole(userId, SystemRole.ROLE_USER.toString(), adminToken);
        } catch (Exception e) {
            log.warn("Failed to assign USER role to {}. Error: {}", request.getUsername(), e.getMessage());
        }

        return userId;
    }

    private void assignRole(String userId, String roleName, String adminToken) {
        // A. Fetch the Role Details (auto-create if missing)
        Map<String, Object> role;
        try {
            role = restClient.get()
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName)
                    .header("Authorization", "Bearer " + adminToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception notFound) {
            log.info("Realm role '{}' missing — creating it.", roleName);
            Map<String, Object> newRole = new HashMap<>();
            newRole.put("name", roleName);
            newRole.put("description", roleName + " realm role (auto-created)");

            restClient.post()
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/roles")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(newRole)
                    .retrieve()
                    .toBodilessEntity();

            role = restClient.get()
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName)
                    .header("Authorization", "Bearer " + adminToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        }

        // B. Assign the Role directly to the user ID
        restClient.post()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList(role))
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteKeycloakUser(String userId) {
        restClient.delete()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
                .header("Authorization", "Bearer " + getAdminToken())
                .retrieve()
                .toBodilessEntity();
    }

    // Safely sync user entity to Keycloak via GET-Modify-PUT pattern
    public void syncUserToKeycloak(User user) {
        String adminToken = getAdminToken();
        String userIdStr = user.getId().toString();

        // 1. Get existing user representation
        Map<String, Object> kcUser = restClient.get()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userIdStr)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (kcUser == null) {
            throw new UserException("Failed to fetch user from Identity Provider for sync.");
        }

        // 2. Modify mapped fields
        kcUser.put("username", user.getUsername());
        kcUser.put("email", user.getEmail());
        kcUser.put("enabled", user.getActive() != null ? user.getActive() : true);

        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            kcUser.put("firstName", user.getFirstName());
        }
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            kcUser.put("lastName", user.getLastName());
        }

        // 3. Put full representation back
        restClient.put()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userIdStr)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(kcUser)
                .retrieve()
                .toBodilessEntity();
    }

    // CACHED Admin Token Retrieval
    private synchronized String getAdminToken() {
        // Return cached token if valid (with a 10-second safety buffer)
        if (cachedAdminToken != null && System.currentTimeMillis() < tokenExpirationTime - 10000) {
            return cachedAdminToken;
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        KeycloakTokenResponse response = restClient.post()
                .uri(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(KeycloakTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new RuntimeException("Failed to retrieve admin token from Auth server");
        }

        cachedAdminToken = response.accessToken();
        // Convert expires_in (seconds) to milliseconds and add to current time
        tokenExpirationTime = System.currentTimeMillis() + (response.expiresIn() * 1000L);

        return cachedAdminToken;
    }

    public TokenResponse refreshToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        return restClient.post()
                .uri(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, resp) -> {
                    throw new UserException("Invalid refresh token.");
                })
                .body(TokenResponse.class);
    }

    private record KeycloakTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("token_type") String tokenType
    ) {}
}