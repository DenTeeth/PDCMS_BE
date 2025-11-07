package com.dental.clinic.management.account.dto.response;

import java.util.List;

/**
 * User permissions response containing only authorization data.
 */
public class UserPermissionsResponse {

    private String username;
    private List<String> permissions;

    public UserPermissionsResponse() {
    }

    public UserPermissionsResponse(String username, List<String> permissions) {
        this.username = username;
        this.permissions = permissions;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
