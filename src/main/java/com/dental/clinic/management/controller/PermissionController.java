package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.request.CreatePermissionRequest;
import com.dental.clinic.management.dto.request.UpdatePermissionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.dto.response.PermissionInfoResponse;
import com.dental.clinic.management.service.PermissionService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import jakarta.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("")
    @ApiMessage("Get all permissions successfully")
    public ResponseEntity<List<PermissionInfoResponse>> getAllPermissions() {
        List<PermissionInfoResponse> response = permissionService.getAllActivePermissions();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/by-module")
    @ApiMessage("Get permissions grouped by module successfully")
    public ResponseEntity<Map<String, List<PermissionInfoResponse>>> getPermissionsByModule() {
        Map<String, List<PermissionInfoResponse>> response = permissionService.getPermissionsGroupedByModule();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/active")
    @ApiMessage("Get all active permissions successfully")
    public ResponseEntity<List<PermissionInfoResponse>> getAllActivePermissions() {
        List<PermissionInfoResponse> response = permissionService.getAllActivePermissions();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{permissionId}")
    @ApiMessage("Get permission by ID successfully")
    public ResponseEntity<PermissionInfoResponse> getPermissionById(@PathVariable String permissionId) {
        PermissionInfoResponse response = permissionService.getPermissionById(permissionId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/module/{module}")
    @ApiMessage("Get permissions by module successfully")
    public ResponseEntity<List<PermissionInfoResponse>> getPermissionsByModule(@PathVariable String module) {
        List<PermissionInfoResponse> response = permissionService.getPermissionsByModule(module);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("")
    @ApiMessage("Create permission successfully")
    public ResponseEntity<PermissionInfoResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request)
            throws URISyntaxException {
        PermissionInfoResponse response = permissionService.createPermission(request);
        return ResponseEntity.created(new URI("/api/v1/permissions/" + response.getPermissionId())).body(response);
    }

    @PatchMapping("/{permissionId}")
    @ApiMessage("Update permission successfully")
    public ResponseEntity<PermissionInfoResponse> updatePermission(
            @PathVariable String permissionId,
            @Valid @RequestBody UpdatePermissionRequest request) {
        PermissionInfoResponse response = permissionService.updatePermission(permissionId, request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{permissionId}")
    @ApiMessage("Delete permission successfully")
    public ResponseEntity<Void> deletePermission(@PathVariable String permissionId) {
        permissionService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{permissionId}/hard")
    @ApiMessage("Hard delete permission successfully")
    public ResponseEntity<Void> hardDeletePermission(@PathVariable String permissionId) {
        permissionService.hardDeletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{permissionName}")
    @ApiMessage("Check if permission name exists")
    public ResponseEntity<Boolean> existsByPermissionName(@PathVariable String permissionName) {
        boolean exists = permissionService.existsByPermissionName(permissionName);
        return ResponseEntity.ok().body(exists);
    }
}