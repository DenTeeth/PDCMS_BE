package com.dental.clinic.management.service;

import com.dental.clinic.management.dto.response.PermissionInfoResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.mapper.PermissionMapper;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

import com.dental.clinic.management.domain.Permission;
import com.dental.clinic.management.domain.Role;
import com.dental.clinic.management.repository.PermissionRepository;
import com.dental.clinic.management.repository.RoleRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PermissionMapper permissionMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public void assignPermissionsToRole(String roleId, List<String> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Role not found with ID: " + roleId,
                        "role",
                        "rolenotfound"));

        Set<Permission> permissions = new HashSet<>();
        for (String permissionId : permissionIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new BadRequestAlertException(
                            "Permission not found with ID: " + permissionId,
                            "permission",
                            "permissionnotfound"));
            permissions.add(permission);
        }

        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public List<PermissionInfoResponse> getRolePermissions(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Role not found with ID: " + roleId,
                        "role",
                        "rolenotfound"));

        List<Permission> permissions = role.getPermissions().stream().toList();
        return permissionMapper.toPermissionInfoResponseList(permissions);
    }
}