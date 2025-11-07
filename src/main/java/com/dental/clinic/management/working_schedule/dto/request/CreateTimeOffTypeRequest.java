package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateTimeOffTypeRequest {

    @NotBlank(message = "typeName is required")
    private String typeName;

    @NotBlank(message = "typeCode is required")
    private String typeCode;

    private String description;

    @NotNull(message = "requiresBalance is required")
    private Boolean requiresBalance;

    private Double defaultDaysPerYear;

    @NotNull(message = "isPaid is required")
    private Boolean isPaid;

    @NotNull(message = "requiresApproval is required")
    private Boolean requiresApproval = true;

    @NotNull(message = "isActive is required")
    private Boolean isActive = true;

    public CreateTimeOffTypeRequest() {
        this.requiresApproval = true;
        this.isActive = true;
    }

    public CreateTimeOffTypeRequest(String typeName, String typeCode, String description,
            Boolean requiresBalance, Double defaultDaysPerYear,
            Boolean isPaid, Boolean requiresApproval, Boolean isActive) {
        this.typeName = typeName;
        this.typeCode = typeCode;
        this.description = description;
        this.requiresBalance = requiresBalance;
        this.defaultDaysPerYear = defaultDaysPerYear;
        this.isPaid = isPaid;
        this.requiresApproval = requiresApproval != null ? requiresApproval : true;
        this.isActive = isActive != null ? isActive : true;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequiresBalance() {
        return requiresBalance;
    }

    public void setRequiresBalance(Boolean requiresBalance) {
        this.requiresBalance = requiresBalance;
    }

    public Double getDefaultDaysPerYear() {
        return defaultDaysPerYear;
    }

    public void setDefaultDaysPerYear(Double defaultDaysPerYear) {
        this.defaultDaysPerYear = defaultDaysPerYear;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
