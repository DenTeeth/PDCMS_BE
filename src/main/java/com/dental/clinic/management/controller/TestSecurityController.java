package com.dental.clinic.management.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.utils.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

/**
 * Test controller để kiểm tra RBAC và JWT authentication.
 * Chỉ sử dụng cho test, có thể xóa trong production.
 */
@RestController
@RequestMapping("/api/v1/test-security")
@Tag(name = "Security Testing", description = "Test endpoints for RBAC and JWT authentication (for development only)")
public class TestSecurityController {

    @GetMapping("/public")
    @Operation(summary = "Public test endpoint", description = "Test endpoint accessible without authentication")
    @ApiMessage("Public endpoint - không cần auth")
    public String publicEndpoint() {
        return "This is public endpoint";
    }

    @GetMapping("/authenticated")
    @Operation(summary = "Authenticated test endpoint", description = "Test endpoint requiring authentication - shows user and authorities")
    @ApiMessage("Endpoint cần authentication")
    public String authenticatedEndpoint(Authentication auth) {
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return "User: " + auth.getName() + ", Authorities: " + authorities;
    }

    // @PreAuthorize("hasRole('" + ADMIN + "')")
    @GetMapping("/admin-only")
    @Operation(summary = "Admin-only test endpoint", description = "Test endpoint accessible only by users with ADMIN role")
    @ApiMessage("Chỉ ADMIN mới truy cập được")
    public String adminOnlyEndpoint() {
        return "Admin only endpoint accessed successfully";
    }

    // @PreAuthorize("hasRole('" + USER + "')")
    @GetMapping("/user-only")
    @Operation(summary = "User-only test endpoint", description = "Test endpoint accessible only by users with USER role")
    @ApiMessage("Chỉ USER mới truy cập được")
    public String userOnlyEndpoint() {
        return "User only endpoint accessed successfully";
    }

    // @PreAuthorize("hasAuthority('" + READ_ALL_EMPLOYEES + "')")
    @GetMapping("/read-employees")
    @Operation(summary = "Test permission-based access", description = "Test endpoint requiring READ_ALL_EMPLOYEES permission")
    @ApiMessage("Cần quyền READ_ALL_EMPLOYEES")
    public String readEmployeesEndpoint() {
        return "Read employees permission granted";
    }

    // @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('" + CREATE_EMPLOYEE + "')")
    @GetMapping("/create-employee")
    @Operation(summary = "Test combined role/permission access", description = "Test endpoint accessible by ADMIN role OR CREATE_EMPLOYEE permission")
    @ApiMessage("ADMIN hoặc có quyền CREATE_EMPLOYEE")
    public String createEmployeeEndpoint() {
        return "Create employee permission granted";
    }
}