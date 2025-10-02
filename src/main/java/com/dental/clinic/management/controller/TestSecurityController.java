package com.dental.clinic.management.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.utils.annotation.ApiMessage;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

/**
 * Test controller để kiểm tra RBAC và JWT authentication.
 * Chỉ sử dụng cho test, có thể xóa trong production.
 */
@RestController
@RequestMapping("/api/v1/test-security")
public class TestSecurityController {

    @GetMapping("/public")
    @ApiMessage("Public endpoint - không cần auth")
    public String publicEndpoint() {
        return "This is public endpoint";
    }

    @GetMapping("/authenticated")
    @ApiMessage("Endpoint cần authentication")
    public String authenticatedEndpoint(Authentication auth) {
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return "User: " + auth.getName() + ", Authorities: " + authorities;
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @GetMapping("/admin-only")
    @ApiMessage("Chỉ ADMIN mới truy cập được")
    public String adminOnlyEndpoint() {
        return "Admin only endpoint accessed successfully";
    }

    @PreAuthorize("hasRole('" + USER + "')")
    @GetMapping("/user-only")
    @ApiMessage("Chỉ USER mới truy cập được")
    public String userOnlyEndpoint() {
        return "User only endpoint accessed successfully";
    }

    @PreAuthorize("hasAuthority('" + READ_ALL_EMPLOYEES + "')")
    @GetMapping("/read-employees")
    @ApiMessage("Cần quyền READ_ALL_EMPLOYEES")
    public String readEmployeesEndpoint() {
        return "Read employees permission granted";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('" + CREATE_EMPLOYEE + "')")
    @GetMapping("/create-employee")
    @ApiMessage("ADMIN hoặc có quyền CREATE_EMPLOYEE")
    public String createEmployeeEndpoint() {
        return "Create employee permission granted";
    }
}