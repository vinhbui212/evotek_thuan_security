package org.example.thuan_security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.model.Roles;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.IdentityClient;
import org.example.thuan_security.repository.RoleRepository;
import org.example.thuan_security.repository.UserRepository;
import org.example.thuan_security.request.*;
import org.example.thuan_security.response.LoginResponse;
import org.example.thuan_security.response.UserKCLResponse;
import org.example.thuan_security.service.UserKCLService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserKCLServiceImpl implements UserKCLService {

    private final UserRepository userRepository;
    private final IdentityClient identityClient;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${idp.client-id}")
    @NonFinal
    private String clientId;
    @Value("${idp.client-secret}")
    @NonFinal
    private String secretId;
    @Override
    public UserKCLResponse register(RegisterRequest request) {
        // create account keycloak

        // exchange client token
        var token = identityClient.exchangeToken(TokenExchangeParam.builder()
                        .grant_type("client_credentials")
                        .client_id(clientId)
                        .client_secret(secretId)
                        .scope("openid")
                .build());
        log.info("token: {}", token);

        var creationResponse = identityClient.createUser(
                "Bearer " + token.getAccessToken(),
                UserCreationParam.builder()
                        .username(request.getUsername())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .enabled(true)
                        .emailVerified(false)
                        .credentials(List.of(Credential.builder()
                                .type("password")
                                .temporary(false)
                                .value(request.getPassword())
                                .build()))
                        .build());

        String userId = extractUserId(creationResponse);
        log.info("UserId {}", userId);
        var users = toUsers(request);
        users.setUserId(userId);

        Roles roles = roleRepository.findByName("ROLE_USER");
        String roleName = roles.getName();
        users.setRoles(Collections.singleton(roleName));
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        users=userRepository.save(users);
        log.info(toUserKCLResponse(users).toString());
        return toUserKCLResponse(users);

    }
    private String extractUserId(ResponseEntity<?> response) {
        if (response != null && response.getHeaders() != null && response.getHeaders().containsKey("Location")) {
            String location = response.getHeaders().getFirst("Location");

            if (location != null && !location.isEmpty()) {
                String[] splitedStr = location.split("/");

                if (splitedStr.length > 0) {
                    return splitedStr[splitedStr.length - 1];
                }
            }
        }

        return null;
    }

    public Users toUsers(RegisterRequest requestKCL){
        Users users = new Users();
        users.setFullName(requestKCL.getFirstName()+requestKCL.getLastName());
        users.setEmail(requestKCL.getEmail());
        users.setPassword(requestKCL.getPassword());
        users.setDob(requestKCL.getDob());
        return users;
    }
    public UserKCLResponse toUserKCLResponse(Users users){
        UserKCLResponse response = new UserKCLResponse();
        response.setUserId(users.getUserId());
        response.setFirstName(users.getFullName());
        response.setEmail(users.getEmail());
        response.setDob(users.getDob());
        response.setUsername(users.getUsername());
        response.setUserId(users.getUserId());
        return response;
    }
}
