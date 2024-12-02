package org.example.thuan_security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.model.Permissions;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.PermissionsRepository;
import org.example.thuan_security.repository.UserRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserRepository accountRepository;
    private final PermissionsRepository permissionRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return checkUserPermission(authentication.getName(), targetDomainObject, permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return checkUserPermission(authentication.getName(), targetId, permission);
    }

    private boolean checkUserPermission(String email, Object resource, Object permissionScope) {
        Users user = accountRepository.findByEmail(email);
        if (user == null) {
            log.warn("User with email {} not found", email);
            return false;
        }

        List<Permissions> permissionsByName = permissionRepository.findAllByScope(permissionScope.toString());
        if (permissionsByName.isEmpty()) {
            log.warn("No permissions found for name {}", permissionScope);
        }

        List<Permissions> permissionsByResource = permissionRepository.findAllByResource(resource.toString());
        if (permissionsByResource.isEmpty()) {
            log.warn("No permissions found for resource {}", resource);
        }

        return !permissionsByName.isEmpty() && !permissionsByResource.isEmpty();
    }

}


