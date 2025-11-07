package com.dental.clinic.management.working_schedule.domain;

import jakarta.persistence.*;

/**
 * Entity for time_off_types table
 * Represents different types of time-off (annual leave, sick leave, etc.)
 */
@Entity
@Table(name = "time_off_types")
public class TimeOffType {

    @Id
    @Column(name = "type_id", length = 50)
    private String typeId;

    @Column(name = "type_code", nullable = false, unique = true, length = 50)
    private String typeCode; // Unique code: ANNUAL_LEAVE, SICK_LEAVE, UNPAID_LEAVE

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "requires_balance", nullable = false)
    private Boolean requiresBalance = false; // true if this type requires balance tracking

    @Column(name = "default_days_per_year")
    private Double defaultDaysPerYear; // Default number of days allocated per year (e.g., 12 for annual leave)

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = true; // true = paid, false = unpaid

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public TimeOffType() {
    }

    public TimeOffType(String typeId, String typeCode, String typeName, String description,
            Boolean requiresBalance, Double defaultDaysPerYear, Boolean isPaid,
            Boolean requiresApproval, Boolean isActive) {
        this.typeId = typeId;
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.description = description;
        this.requiresBalance = requiresBalance;
        this.defaultDaysPerYear = defaultDaysPerYear;
        this.isPaid = isPaid;
        this.requiresApproval = requiresApproval;
        this.isActive = isActive;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
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

    @Override
    public String toString() {
        return "TimeOffType{" +
                "typeId='" + typeId + '\'' +
                ", typeName='" + typeName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
