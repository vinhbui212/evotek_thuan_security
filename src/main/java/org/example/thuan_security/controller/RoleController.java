package org.example.thuan_security.controller;


import lombok.RequiredArgsConstructor;
import org.example.thuan_security.model.Roles;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasPermission('role','CREATE')")
    @PostMapping
    public ResponseEntity<Roles> createRole(@RequestParam String name) {
        Roles role = roleService.createRole(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @PreAuthorize("hasPermission('role','UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<Roles> updateRole(@PathVariable Long id, @RequestParam String name) {
        Roles updatedRole = roleService.updateRole(id, name);
        return ResponseEntity.ok(updatedRole);
    }

    @PreAuthorize("hasPermission('role','DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Role deleted successfully");
    }

    @PreAuthorize("hasPermission('role','UPDATE')")
    @PostMapping("/{roleId}/assign-permissions")
    public ResponseEntity<Void> assignPermissionsToRole(
            @PathVariable Long roleId, @RequestBody Long permissionIds) {
        roleService.assignPermissionsToRole(roleId, permissionIds);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasPermission('role','UPDATE')")
    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<String> assignRoletoUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        roleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok("Rol assigned to role successfully");
    }
}


