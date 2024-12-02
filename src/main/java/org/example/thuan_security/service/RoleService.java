package org.example.thuan_security.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.model.Permissions;
import org.example.thuan_security.model.PermissionRole;
import org.example.thuan_security.model.Roles;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.PermissionRoleRepository;
import org.example.thuan_security.repository.PermissionsRepository;
import org.example.thuan_security.repository.RoleRepository;
import org.example.thuan_security.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionsRepository permissionRepository;
    private final UserRepository userRepository;
    private final PermissionRoleRepository permissionRoleRepository;
    public Roles createRole(String name) {
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

    @Transactional
    public void assignPermissionsToRole(Long roleId,Long permissionIds) {
        Roles role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));


            Permissions permission = permissionRepository.findById(permissionIds)
                    .orElseThrow(() -> new RuntimeException("Permission not found"));

            PermissionRole permissionRole = new PermissionRole();
            permissionRole.setRoleId(String.valueOf(role.getId()));
            permissionRole.setPermissionId(String.valueOf(permission.getId()));

            System.out.println("Saving PermissionRole: " + permissionRole);

            permissionRoleRepository.save(permissionRole);

    }


    public void assignRoleToUser(Long id, Long roleId) {
        Users users = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        Roles roles=roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        String roleName = roles.getName();
        users.setRoles(Collections.singleton(roleName));
        userRepository.save(users);

    }
}




//@Service
//@RequiredArgsConstructor
//public class RolePermissionService {
//    private final RoleRepository roleRepository;
//    private final PermissionRepository permissionRepository;
//    private final RolePermissionRepository rolePermissionRepository;
//
//    @PreAuthorize("hasRole('ADMIN')")
//    public boolean assignPermission(String roleId, String permissionId, List<PermissionScope> scopes){
//        roleRepository.findById(roleId).orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND));
//        permissionRepository.findById(roleId).orElseThrow(()-> new AppExceptions(ErrorCode.PERMISSION_NOTFOUND));
//        for(PermissionScope item : scopes){
//            boolean foundRolePermission = rolePermissionRepository
//                    .existsByRoleIdAndPermissionIdAndScope(roleId, permissionId, item);
//            if(!foundRolePermission){
//                rolePermissionRepository.save(RolePermission.builder()
//                        .roleId(roleId)
//                        .permissionId(permissionId)
//                        .scope(item)
//                        .build());
//            }
//        }
//
//        return true;
//    }
//
//    // un assign
//    public boolean unAssignPermission(String roleId, String permissionId) {
//        RolePermission rolePermission = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId)
//                .orElseThrow(() -> new AppExceptions(ErrorCode.ROLE_PERMISSION_NOTFOUND));
//        rolePermissionRepository.delete(rolePermission);
//        return true;
//    }
//}
