package org.example.thuan_security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.model.Permissions;
import org.example.thuan_security.model.Roles;
import org.example.thuan_security.repository.PermissionsRepository;
import org.example.thuan_security.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionsRepository permissionRepository;

    public Roles createRole(String name, String description) {
        Roles role = new Roles();
        role.setName(name);
        return roleRepository.save(role);
    }

    public Roles updateRole(Long id, String name) {
        Roles role = roleRepository.findById(id).orElseThrow(() -> new RuntimeException("Role not found"));
        role.setName(name);
        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    public void assignPermissionsToRole(Long roleId, Long permissionIds) {
        Roles role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));


        Permissions permission = permissionRepository.findById(permissionIds)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        log.info(permission.getName());


        Set<String> permissionsSet = role.getPermissions();
        if (permissionsSet == null) {
            permissionsSet = new HashSet<>();
        }

        permissionsSet.add(permission.getName());

        role.setPermissions(permissionsSet);


        roleRepository.save(role);


    }

}

