package com.syedsadiquh.coreservice.user.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syedsadiquh.coreservice.user.dto.request.LoginRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.RegisterRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.TokenResponse;
import com.syedsadiquh.coreservice.user.enums.SystemRole;
import com.syedsadiquh.coreservice.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
                    throw new UserException("User already exists!");
                })
                .toBodilessEntity();

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new RuntimeException("User created but Auth server did not return a Location header.");
        }

        String path = location.getPath();
        String userId = path.substring(path.lastIndexOf('/') + 1);

        // Clear any realm-default required actions (e.g. VERIFY_PROFILE) that Keycloak
        // auto-attaches on creation — otherwise login fails with "Account is not fully set up".
        try {
            Map<String, Object> clear = new HashMap<>();
            clear.put("requiredActions", Collections.emptyList());
            clear.put("emailVerified", true);
            restClient.put()
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(clear)
                    .retrieve()
                    .toBodilessEntity();

            // Debug: read back to see what Keycloak kept
            Map fetched = restClient.get()
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
                    .header("Authorization", "Bearer " + adminToken)
                    .retrieve()
                    .body(Map.class);
            log.info("KC user after PUT: requiredActions={}, emailVerified={}, enabled={}",
                    fetched != null ? fetched.get("requiredActions") : null,
                    fetched != null ? fetched.get("emailVerified") : null,
                    fetched != null ? fetched.get("enabled") : null);
        } catch (Exception e) {
            log.warn("Failed to clear requiredActions for {}: {}", userId, e.getMessage());
        }

        // Assign the default USER system role in Keycloak
        try {
            assignRole(request.getUsername(), SystemRole.ROLE_USER, adminToken);
        } catch (Exception e) {
            log.warn("Failed to assign USER role to {}. They may need manual role assignment. Error: {}",
                    request.getUsername(), e.getMessage());
        }

        return userId;
    }

    private void assignRole(String username, String roleName, String adminToken) {
        // A. Find the User's ID
        List<Map> users = restClient.get()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users?username=" + username)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .body(List.class);

        if (users == null || users.isEmpty()) {
            throw new RuntimeException("User created but not found. This should not happen.");
        }
        String userId = (String) users.getFirst().get("id");

        // B. Fetch the Role Details (auto-create if missing so the realm self-heals)
        Map role;
        try {
            role = restClient.get()
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName)
                    .header("Authorization", "Bearer " + adminToken)
                    .retrieve()
                    .body(Map.class);
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
                    .body(Map.class);
        }

        // C. Assign the Role
        restClient.post()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList(role))
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteKeycloakUser(String userId) {
        String adminToken = getAdminToken();
        restClient.delete()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + userId)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .toBodilessEntity();
    }

    private String getAdminToken() {
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

        return response.accessToken();
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

