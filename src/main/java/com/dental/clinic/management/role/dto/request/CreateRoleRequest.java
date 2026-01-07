package com.dental.clinic.management.role.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateRoleRequest {

    @NotBlank(message = "Mã vai trò là bắt buộc")
    @Size(max = 50, message = "Mã vai trò không được vượt quá 50 ký tự")
    private String roleId;

    @NotBlank(message = "Tên vai trò là bắt buộc")
    @Size(max = 50, message = "Tên vai trò không được vượt quá 50 ký tự")
    private String roleName;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @NotNull(message = "Mã vai trò cơ sở là bắt buộc")
    private Integer baseRoleId;

    private Boolean requiresSpecialization = false;

    public CreateRoleRequest() {
    }

    public CreateRoleRequest(String roleId, String roleName, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
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

    public Integer getBaseRoleId() {
        return baseRoleId;
    }

    public void setBaseRoleId(Integer baseRoleId) {
        this.baseRoleId = baseRoleId;
    }
}
