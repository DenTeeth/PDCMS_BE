package com.dental.clinic.management.permission.dto.request;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing permission
 */
public class UpdatePermissionRequest {

    @Size(max = 100, message = "Tên quyền không được vượt quá 100 ký tự")
    private String permissionName;

    @Size(max = 20, message = "Module không được vượt quá 20 ký tự")
    private String module;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    private Boolean isActive;

    // Constructors
    public UpdatePermissionRequest() {
    }

    public UpdatePermissionRequest(String permissionName, String module, String description, Boolean isActive) {
        this.permissionName = permissionName;
        this.module = module;
        this.description = description;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "UpdatePermissionRequest{" +
                "permissionName='" + permissionName + '\'' +
                ", module='" + module + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}