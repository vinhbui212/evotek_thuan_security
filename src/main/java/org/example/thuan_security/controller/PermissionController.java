package org.example.thuan_security.controller;

import lombok.RequiredArgsConstructor;
import org.example.thuan_security.model.Permissions;
import org.example.thuan_security.service.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PreAuthorize("hasPermission('permission','CREATE')")
    @PostMapping
    public ResponseEntity<Permissions> createPermission(@RequestParam String name,
    @RequestParam String scope, @RequestParam String resource ) {
        Permissions permission = permissionService.createPermission(name, scope, resource);
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }

    @PreAuthorize("hasPermission('permission','UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<Permissions> updatePermission(@PathVariable Long id, @RequestParam String name,@RequestParam String scope, @RequestParam String resource) {
        Permissions updatedPermission = permissionService.updatePermission(id, name, scope, resource);
        return ResponseEntity.ok(updatedPermission);
    }

    @PreAuthorize("hasPermission('permission','DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Permission deleted successfully");
    }

    @PreAuthorize("hasPermission('permission','READ')")
    @GetMapping
    public ResponseEntity<List<Permissions>> getAllPermissions() {
        List<Permissions> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }


}

