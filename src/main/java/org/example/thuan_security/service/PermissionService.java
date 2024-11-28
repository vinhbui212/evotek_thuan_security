package org.example.thuan_security.service;

import lombok.RequiredArgsConstructor;
import org.example.thuan_security.model.Permissions;
import org.example.thuan_security.repository.PermissionsRepository;
import org.springframework.stereotype.Service;

import java.security.Permission;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionsRepository permissionRepository;

    public Permissions createPermission(String name) {
        Permissions permission = new Permissions();
        permission.setName(name);
        return permissionRepository.save(permission);
    }

    public Permissions updatePermission(Long id, String name) {
        Permissions permission = permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Permission not found"));
        permission.setName(name);
        return permissionRepository.save(permission);
    }

    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }

    public List<Permissions> getAllPermissions() {
        return permissionRepository.findAll();
    }
}

