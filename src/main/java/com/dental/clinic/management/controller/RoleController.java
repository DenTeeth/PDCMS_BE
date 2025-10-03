package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.response.PermissionInfoResponse;
import com.dental.clinic.management.service.RoleService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/{roleId}/permissions")
    @ApiMessage("Assign permissions to role successfully")
    public ResponseEntity<Void> assignPermissionsToRole(
            @PathVariable String roleId,
            @RequestBody List<String> permissionIds) {
        roleService.assignPermissionsToRole(roleId, permissionIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roleId}/permissions")
    @ApiMessage("Get permissions of role successfully")
    public ResponseEntity<List<PermissionInfoResponse>> getRolePermissions(@PathVariable String roleId) {
        List<PermissionInfoResponse> permissions = roleService.getRolePermissions(roleId);
        return ResponseEntity.ok().body(permissions);
    }
}