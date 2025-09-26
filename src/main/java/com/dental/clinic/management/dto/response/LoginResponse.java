package com.dental.clinic.management.dto.response;

import java.util.List;

public class LoginResponse {

    private String token;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permissions;

    // Constructors
    public LoginResponse() {
    }

    public LoginResponse(String token, String username, String email,
            List<String> roles, List<String> permissions) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.permissions = permissions;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}
