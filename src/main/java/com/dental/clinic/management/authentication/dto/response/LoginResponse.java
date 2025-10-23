
package com.dental.clinic.management.authentication.dto.response;

import java.util.List;
import java.util.Map;

import com.dental.clinic.management.authentication.dto.SidebarItemDTO;
import com.dental.clinic.management.employee.enums.EmploymentType;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Login response với access token trong body và refresh token trong cookie.
 */
// @JsonInclude(JsonInclude.Include.NON_NULL) // Commented out for debugging -
// fields should always be present
public class LoginResponse {

    private String token; // Access token
    private String refreshToken; // Refresh token
    private long tokenExpiresAt; // epoch seconds
    private long refreshTokenExpiresAt; // epoch seconds
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permissions;

    // Grouped permissions by module for efficient FE processing
    private Map<String, List<String>> groupedPermissions;

    // Base role for FE layout selection
    private String baseRole; // 'admin', 'employee', or 'patient'

    // Home path for redirect after login
    private String homePath;

    // Sidebar structure grouped by module
    private Map<String, List<SidebarItemDTO>> sidebar;

    // Employee-specific info
    private EmploymentType employmentType; // FULL_TIME or PART_TIME

    public LoginResponse() {
    }

    public LoginResponse(String token, long tokenExpiresAt, String refreshToken, long refreshTokenExpiresAt,
            String username, String email, List<String> roles, List<String> permissions) {
        this.token = token;
        this.tokenExpiresAt = tokenExpiresAt;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.permissions = permissions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(long tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public long getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public void setRefreshTokenExpiresAt(long refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public String getBaseRole() {
        return baseRole;
    }

    public void setBaseRole(String baseRole) {
        this.baseRole = baseRole;
    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    public Map<String, List<SidebarItemDTO>> getSidebar() {
        return sidebar;
    }

    public void setSidebar(Map<String, List<SidebarItemDTO>> sidebar) {
        this.sidebar = sidebar;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public Map<String, List<String>> getGroupedPermissions() {
        return groupedPermissions;
    }

    public void setGroupedPermissions(Map<String, List<String>> groupedPermissions) {
        this.groupedPermissions = groupedPermissions;
    }
}
