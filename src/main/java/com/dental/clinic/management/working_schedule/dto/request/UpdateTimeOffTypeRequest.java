package com.dental.clinic.management.working_schedule.dto.request;

/**
 * Update request for TimeOffType - All fields are optional
 * Only send the fields you want to update
 */
public class UpdateTimeOffTypeRequest {

    private String typeName;

    private String typeCode;

    private String description;

    private Boolean requiresBalance;

    private Double defaultDaysPerYear;

    private Boolean isPaid;

    private Boolean requiresApproval;

    private Boolean isActive;

    // Constructors
    public UpdateTimeOffTypeRequest() {
    }

    public UpdateTimeOffTypeRequest(String typeName, String typeCode, String description,
            Boolean requiresBalance, Double defaultDaysPerYear, Boolean isPaid,
            Boolean requiresApproval, Boolean isActive) {
        this.typeName = typeName;
        this.typeCode = typeCode;
        this.description = description;
        this.requiresBalance = requiresBalance;
        this.defaultDaysPerYear = defaultDaysPerYear;
        this.isPaid = isPaid;
        this.requiresApproval = requiresApproval;
        this.isActive = isActive;
    }

    // Getters and Setters
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
