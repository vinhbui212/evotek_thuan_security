package org.example.thuan_security.controller;

import lombok.RequiredArgsConstructor;
import org.example.thuan_security.model.Permissions;
import org.example.thuan_security.service.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<Permissions> createPermission(@RequestParam String name) {
        Permissions permission = permissionService.createPermission(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Permissions> updatePermission(@PathVariable Long id, @RequestParam String name) {
        Permissions updatedPermission = permissionService.updatePermission(id, name);
        return ResponseEntity.ok(updatedPermission);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Permission deleted successfully");
    }

    @GetMapping
    public ResponseEntity<List<Permissions>> getAllPermissions() {
        List<Permissions> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }


}

