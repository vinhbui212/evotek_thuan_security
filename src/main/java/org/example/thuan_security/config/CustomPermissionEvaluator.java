package org.example.thuan_security.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.model.Permissions;
import org.example.thuan_security.model.Roles;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.PermissionsRepository;
import org.example.thuan_security.repository.RoleRepository;
import org.example.thuan_security.repository.UserRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserRepository accountRepository;
    private final PermissionsRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        log.info("hasPermission called with: " +
                "authentication: " + authentication.getName() +
                ", targetDomainObject: " + targetDomainObject +
                ", permission: " + permission);
        if (permission instanceof String resourceName) {
            Permissions foundResource = permissionRepository.findByName(resourceName);
            log.info(foundResource.toString());
            if (foundResource == null) {
                return false;
            }
            log.info(String.valueOf(checkUserPermission(authentication.getName(), foundResource.getId(), permission)));
            return checkUserPermission(authentication.getName(), foundResource.getId(), permission);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return checkUserPermission(authentication.getName(), targetId, permission);
    }

    private boolean checkUserPermission(String email, Object resourceId, Object permissionScope) {
        Users user = accountRepository.findByEmail(email);
        if (user == null) {
            return false;
        }


        Set<String> userRoles = user.getRoles();
        if (userRoles.isEmpty()) {
            return false;
        }

        for (String roleName : userRoles) {
            Roles role = roleRepository.findByName(roleName);
            if (role != null && role.getPermissions().contains(permissionScope)) {
                return true;
            }
        }

        return false;
    }

}
