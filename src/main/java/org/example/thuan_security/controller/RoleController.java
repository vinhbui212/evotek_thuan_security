package org.example.thuan_security.controller;


import lombok.RequiredArgsConstructor;
import org.example.thuan_security.model.Roles;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Roles> createRole(@RequestParam String name) {
        Roles role = roleService.createRole(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Roles> updateRole(@PathVariable Long id, @RequestParam String name) {
        Roles updatedRole = roleService.updateRole(id, name);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Role deleted successfully");
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<String> assignPermissionsToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        roleService.assignPermissionsToRole(roleId, permissionId);
        return ResponseEntity.ok("Permission assigned to role successfully");
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<String> assignRoletoUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        roleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok("Rol assigned to role successfully");
    }
}


