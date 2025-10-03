package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.response.PermissionInfoResponse;
import com.dental.clinic.management.service.AccountService;
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
    private final AccountService accountService;

    public RoleController(RoleService roleService, AccountService accountService) {
        this.roleService = roleService;
        this.accountService = accountService;
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

    @PostMapping("/accounts/{accountId}")
    @ApiMessage("Assign multiple roles to user successfully")
    public ResponseEntity<Void> assignRolesToAccount(
            @PathVariable String accountId,
            @RequestBody List<String> roleIds) {
        accountService.assignRolesToAccount(accountId, roleIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/accounts/{accountId}")
    @ApiMessage("Get roles of user successfully")
    public ResponseEntity<List<String>> getAccountRoles(@PathVariable String accountId) {
        List<String> roles = accountService.getAccountRoles(accountId);
        return ResponseEntity.ok().body(roles);
    }
}