package com.dental.clinic.management.role.controller;

import com.dental.clinic.management.account.service.AccountService;
import com.dental.clinic.management.permission.dto.response.PermissionInfoResponse;
import com.dental.clinic.management.role.dto.response.RoleInfoResponse;
import com.dental.clinic.management.role.service.RoleService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Role Management", description = "APIs for managing roles, role-permission assignments, and user-role assignments")
public class RoleController {

    private final RoleService roleService;
    private final AccountService accountService;

    public RoleController(RoleService roleService, AccountService accountService) {
        this.roleService = roleService;
        this.accountService = accountService;
    }

    @PostMapping("")
    @Operation(summary = "Create new role", description = "Create a new role with specified details")
    @ApiMessage("Create role successfully")
    public ResponseEntity<RoleInfoResponse> createRole(
            @Valid @RequestBody com.dental.clinic.management.role.dto.request.CreateRoleRequest request)
            throws URISyntaxException {
        RoleInfoResponse response = roleService.createRole(request);
        return ResponseEntity.created(new URI("/api/v1/roles/" + response.getRoleId())).body(response);
    }

    @GetMapping("")
    @Operation(summary = "Get all roles", description = "Retrieve all active roles")
    @ApiMessage("Get roles successfully")
    public ResponseEntity<List<RoleInfoResponse>> getAllRoles() {
        List<RoleInfoResponse> response = roleService.getAllRoles();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Get role by ID", description = "Get role details by role ID")
    @ApiMessage("Get role successfully")
    public ResponseEntity<RoleInfoResponse> getRoleById(
            @PathVariable String roleId) {
        RoleInfoResponse response = roleService.getRoleById(roleId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update role", description = "Update role data by role ID")
    @ApiMessage("Update role successfully")
    public ResponseEntity<RoleInfoResponse> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody com.dental.clinic.management.role.dto.response.UpdateRoleRequest request) {
        RoleInfoResponse response = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete role (soft)", description = "Soft delete role by setting isActive to false")
    @ApiMessage("Delete role successfully")
    public ResponseEntity<RoleInfoResponse> deleteRole(
            @PathVariable String roleId) {
        RoleInfoResponse response = roleService.deleteRole(roleId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roleId}/permissions")
    @Operation(summary = "Assign permissions to role", description = "Dynamically assign multiple permissions to a specific role")
    @ApiMessage("Assign permissions to role successfully")
    public ResponseEntity<Void> assignPermissionsToRole(
            @Parameter(description = "Role ID (e.g., ROLE_ADMIN)", required = true) @PathVariable String roleId,
            @Parameter(description = "List of permission IDs to assign", required = true) @RequestBody List<String> permissionIds) {
        roleService.assignPermissionsToRole(roleId, permissionIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Get role permissions", description = "Retrieve all permissions assigned to a specific role")
    @ApiMessage("Get permissions of role successfully")
    public ResponseEntity<List<PermissionInfoResponse>> getRolePermissions(
            @Parameter(description = "Role ID (e.g., ROLE_ADMIN)", required = true) @PathVariable String roleId) {
        List<PermissionInfoResponse> permissions = roleService.getRolePermissions(roleId);
        return ResponseEntity.ok().body(permissions);
    }

    @PostMapping("/accounts/{accountId}")
    @Operation(summary = "Assign roles to user", description = "Assign multiple roles to a user account")
    @ApiMessage("Assign multiple roles to user successfully")
    public ResponseEntity<Void> assignRolesToAccount(
            @Parameter(description = "Account ID (e.g., ACC_ADMIN)", required = true) @PathVariable String accountId,
            @Parameter(description = "List of role IDs to assign", required = true) @RequestBody List<String> roleIds) {
        accountService.assignRolesToAccount(accountId, roleIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "Get user roles", description = "Retrieve all roles assigned to a specific user account")
    @ApiMessage("Get roles of user successfully")
    public ResponseEntity<List<String>> getAccountRoles(
            @Parameter(description = "Account ID (e.g., ACC_ADMIN)", required = true) @PathVariable String accountId) {
        List<String> roles = accountService.getAccountRoles(accountId);
        return ResponseEntity.ok().body(roles);
    }
}
