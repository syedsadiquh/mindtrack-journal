package com.syedsadiquh.coreservice.user.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syedsadiquh.coreservice.user.dto.request.LoginRequestDto;
import com.syedsadiquh.coreservice.user.dto.request.RegisterRequestDto;
import com.syedsadiquh.coreservice.user.dto.response.TokenResponse;
import com.syedsadiquh.coreservice.user.exception.UserException;
import lombok.RequiredArgsConstructor;
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
                    throw new BadCredentialsException("Invalid email or password.");
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
        user.put("firstName", request.getFirstName());
        user.put("lastName", request.getLastName());

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
            throw new RuntimeException("User created but Keycloak did not return a Location header.");
        }

        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
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

        // B. Fetch the Role Details
        Map role = restClient.get()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .body(Map.class);

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
            throw new RuntimeException("Failed to retrieve admin token from Keycloak");
        }

        return response.accessToken();
    }

    private record KeycloakTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("token_type") String tokenType
    ) {}
}

