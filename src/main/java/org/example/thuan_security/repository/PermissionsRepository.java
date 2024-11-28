package org.example.thuan_security.repository;

import org.example.thuan_security.model.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionsRepository extends JpaRepository<Permissions, Long> {
    Permissions findByName(String name);
}
