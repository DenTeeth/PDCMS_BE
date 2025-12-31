package com.dental.clinic.management.permission.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new permission
 */
public class CreatePermissionRequest {

    @NotBlank(message = "Tên quyền là bắt buộc")
    @Size(max = 100, message = "Tên quyền không được vượt quá 100 ký tự")
    private String permissionName;

    @NotBlank(message = "Module là bắt buộc")
    @Size(max = 20, message = "Module không được vượt quá 20 ký tự")
    private String module;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    // Constructors
    public CreatePermissionRequest() {
    }

    public CreatePermissionRequest(String permissionName, String module, String description) {
        this.permissionName = permissionName;
        this.module = module;
        this.description = description;
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

    @Override
    public String toString() {
        return "CreatePermissionRequest{" +
                "permissionName='" + permissionName + '\'' +
                ", module='" + module + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}