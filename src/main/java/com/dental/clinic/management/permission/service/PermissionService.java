package com.dental.clinic.management.permission.service;

import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.exception.PermissionNotFoundException;
import com.dental.clinic.management.permission.domain.Permission;
import com.dental.clinic.management.permission.dto.request.CreatePermissionRequest;
import com.dental.clinic.management.permission.dto.request.UpdatePermissionRequest;
import com.dental.clinic.management.permission.dto.response.PermissionInfoResponse;
import com.dental.clinic.management.permission.mapper.PermissionMapper;
import com.dental.clinic.management.permission.repository.PermissionRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;


import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionService(
            PermissionRepository permissionRepository,
            PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public Page<PermissionInfoResponse> getAllPermissions(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Permission> permissionPage = permissionRepository.findAll(pageable);
        return permissionPage.map(permissionMapper::toPermissionInfoResponse);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public List<PermissionInfoResponse> getAllActivePermissions() {
        List<Permission> permissions = permissionRepository.findAllActivePermissions();
        return permissionMapper.toPermissionInfoResponseList(permissions);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public PermissionInfoResponse getPermissionById(String permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found with ID: " + permissionId));

        return permissionMapper.toPermissionInfoResponse(permission);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public List<PermissionInfoResponse> getPermissionsByModule(String module) {
        List<Permission> permissions = permissionRepository.findByModuleAndIsActive(module, true);
        return permissionMapper.toPermissionInfoResponseList(permissions);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public PermissionInfoResponse createPermission(CreatePermissionRequest request) {
        // Check if permission name already exists
        if (permissionRepository.existsByPermissionName(request.getPermissionName())) {
            throw new BadRequestAlertException(
                    "Permission with name '" + request.getPermissionName() + "' already exists",
                    "permission",
                    "permissionnameexists");
        }

        // Use permission name as the ID (e.g., CREATE_CONTACT, UPDATE_CONTACT)
        String permissionId = request.getPermissionName();

        // Create new permission
        Permission permission = new Permission(
                permissionId,
                request.getPermissionName(),
                request.getModule(),
                request.getDescription());

        Permission savedPermission = permissionRepository.save(permission);
        return permissionMapper.toPermissionInfoResponse(savedPermission);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public PermissionInfoResponse updatePermission(String permissionId, UpdatePermissionRequest request) {
        Permission existingPermission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found with ID: " + permissionId));

        // Check if new permission name already exists (if being changed)
        if (request.getPermissionName() != null &&
                !request.getPermissionName().equals(existingPermission.getPermissionName()) &&
                permissionRepository.existsByPermissionName(request.getPermissionName())) {
            throw new BadRequestAlertException(
                    "Permission with name '" + request.getPermissionName() + "' already exists",
                    "permission",
                    "permissionnameexists");
        }

        // Update fields if provided
        if (request.getPermissionName() != null) {
            existingPermission.setPermissionName(request.getPermissionName());
        }
        if (request.getModule() != null) {
            existingPermission.setModule(request.getModule());
        }
        if (request.getDescription() != null) {
            existingPermission.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            existingPermission.setIsActive(request.getIsActive());
        }

        Permission updatedPermission = permissionRepository.save(existingPermission);
        return permissionMapper.toPermissionInfoResponse(updatedPermission);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public void deletePermission(String permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found with ID: " + permissionId));

        // Soft delete by setting isActive to false
        permission.setIsActive(false);
        permissionRepository.save(permission);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public void hardDeletePermission(String permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found with ID: " + permissionId));

        permissionRepository.delete(permission);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public boolean existsByPermissionName(String permissionName) {
        return permissionRepository.existsByPermissionName(permissionName);
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public Map<String, List<PermissionInfoResponse>> getPermissionsGroupedByModule() {
        List<Permission> permissions = permissionRepository.findAllActivePermissions();
        return permissions.stream()
                .map(permissionMapper::toPermissionInfoResponse)
                .collect(Collectors.groupingBy(PermissionInfoResponse::getModule));
    }
}
