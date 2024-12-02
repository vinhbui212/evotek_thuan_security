package org.example.thuan_security.repository;

import org.example.thuan_security.model.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionsRepository extends JpaRepository<Permissions, Long> {
    Permissions findByName(String name);
    Permissions findByResource(String resource);
    Permissions findByScope(String scope);

    List<Permissions> findAllByScope(String string);

    List<Permissions> findAllByResource(String string);
}
