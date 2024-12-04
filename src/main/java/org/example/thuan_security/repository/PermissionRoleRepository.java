package org.example.thuan_security.repository;

import org.example.thuan_security.model.PermissionRole;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PermissionRoleRepository extends JpaRepository<PermissionRole, Long> {
     boolean existsByRoleIdAndPermissionId(String roleId, String permissionId);

}
