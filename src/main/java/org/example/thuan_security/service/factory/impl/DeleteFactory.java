package org.example.thuan_security.service.factory.impl;

import org.example.thuan_security.model.LoginType;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.service.factory.DeleteStraegy;
import org.example.thuan_security.service.factory.RegisterStrategy;
import org.example.thuan_security.service.keycloak.UserKCLService;
import org.example.thuan_security.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DeleteFactory implements DeleteStraegy {
    @Autowired
    UserKCLService userKCLService;
    @Autowired
    UserService userService;
    @Value("${keycloak.enabled}")
    private boolean isKeycloakEnabled;

    @Override
    public String deleteStraegy(String id, RegisterRequest registerRequest) {
        LoginType loginType = isKeycloakEnabled ? LoginType.KEYCLOAK : LoginType.DB;

        switch (loginType) {
            case KEYCLOAK:
                userKCLService.updateEnabled(id, registerRequest);
                return "Deleted KL";
            case DB:
            default:
                userService.updateEnabled(id, registerRequest);
                return "Deleted DB";

        }
    }
}
