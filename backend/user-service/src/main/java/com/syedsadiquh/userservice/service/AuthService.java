package com.syedsadiquh.userservice.service;

import com.syedsadiquh.userservice.dto.TokenResponse;
import com.syedsadiquh.userservice.dto.request.LoginRequestDto;
import com.syedsadiquh.userservice.dto.request.RegisterRequestDto;
import com.syedsadiquh.userservice.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

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
                .body(TokenResponse.class);
    }

    // REGISTER NEW USER
    public void register(RegisterRequestDto request) {
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

        restClient.post()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .onStatus(status -> status.value() == 409, (req, resp) -> {
                    throw new RuntimeException("User already exists!");
                })
                .toBodilessEntity();
        assignRole(request.getUsername(), Role.FREE_USER, adminToken);
    }

    private void assignRole(String username, Role roleName, String adminToken) {
        // A. Find the User's ID
        List<Map> users = restClient.get()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users?username=" + username)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .body(List.class);

        if (users == null || users.isEmpty()) {
            throw new RuntimeException("User created but not found. This should not happen.");
        }
        String userId = (String) users.get(0).get("id");

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

    // Helper: Get Service Account Token
    private String getAdminToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        JsonNode response = restClient.post()
                .uri(keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(JsonNode.class);

        return response.get("access_token").asText();
    }
}
