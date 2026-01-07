package com.dental.clinic.management.role.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateRoleRequest {

    @NotBlank(message = "Tên vai trò là bắt buộc")
    @Size(max = 50, message = "Tên vai trò không được vượt quá 50 ký tự")
    private String roleName;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    private Boolean requiresSpecialization;

    public UpdateRoleRequest() {
    }

    public UpdateRoleRequest(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequiresSpecialization() {
        return requiresSpecialization;
    }

    public void setRequiresSpecialization(Boolean requiresSpecialization) {
        this.requiresSpecialization = requiresSpecialization;
    }
}
