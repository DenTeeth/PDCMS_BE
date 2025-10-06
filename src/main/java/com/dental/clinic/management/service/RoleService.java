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

    // --- New dependencies for CRUD ---
    private final com.dental.clinic.management.mapper.RoleMapper roleMapper;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PermissionMapper permissionMapper,
            com.dental.clinic.management.mapper.RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
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

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public com.dental.clinic.management.dto.response.RoleInfoResponse createRole(com.dental.clinic.management.dto.request.CreateRoleRequest request) {
        if (roleRepository.existsById(request.getRoleId()) || roleRepository.existsByRoleName(request.getRoleName())) {
            throw new BadRequestAlertException("Role already exists", "role", "roleexists");
        }
        com.dental.clinic.management.domain.Role role = roleMapper.toRole(request);
        roleRepository.save(role);
        return roleMapper.toRoleInfoResponse(role);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public java.util.List<com.dental.clinic.management.dto.response.RoleInfoResponse> getAllRoles() {
        java.util.List<com.dental.clinic.management.domain.Role> roles = roleRepository.findAllActiveRoles();
        return roleMapper.toRoleInfoResponseList(roles);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public com.dental.clinic.management.dto.response.RoleInfoResponse getRoleById(String roleId) {
        com.dental.clinic.management.domain.Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BadRequestAlertException("Role not found with ID: " + roleId, "role", "rolenotfound"));
        return roleMapper.toRoleInfoResponse(role);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public com.dental.clinic.management.dto.response.RoleInfoResponse updateRole(String roleId, com.dental.clinic.management.dto.request.UpdateRoleRequest request) {
        com.dental.clinic.management.domain.Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BadRequestAlertException("Role not found with ID: " + roleId, "role", "rolenotfound"));
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        roleRepository.save(role);
        return roleMapper.toRoleInfoResponse(role);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public com.dental.clinic.management.dto.response.RoleInfoResponse deleteRole(String roleId) {
        com.dental.clinic.management.domain.Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BadRequestAlertException("Role not found with ID: " + roleId, "role", "rolenotfound"));
        // If already soft-deleted, treat as not found
        if (role.getIsActive() != null && !role.getIsActive()) {
            throw new BadRequestAlertException("Role not found with ID: " + roleId, "role", "rolenotfound");
        }

        // Soft delete: set isActive = false
        role.setIsActive(false);
        roleRepository.save(role);
        return roleMapper.toRoleInfoResponse(role);
    }
}