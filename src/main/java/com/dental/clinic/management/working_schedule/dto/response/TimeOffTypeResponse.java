package com.dental.clinic.management.working_schedule.dto.response;

/**
 * Response DTO for TimeOffType
 */
public class TimeOffTypeResponse {

    private String typeId;
    private String typeCode; // ANNUAL_LEAVE, SICK_LEAVE, etc.
    private String typeName;
    private String description;
    private Boolean requiresBalance; // true = cÃ¡ÂºÂ§n check sÃ¡Â»â€˜ dÃ†Â° phÃƒÂ©p, false = khÃƒÂ´ng cÃ¡ÂºÂ§n
    private Double defaultDaysPerYear; // SÃ¡Â»â€˜ ngÃƒÂ y phÃƒÂ©p mÃ¡ÂºÂ·c Ã„â€˜Ã¡Â»â€¹nh mÃ¡Â»â€”i nÃ„Æ’m (dÃƒÂ¹ng
                                       // cho annual reset)
    private Boolean isPaid; // true = cÃƒÂ³ lÃ†Â°Ã†Â¡ng, false = khÃƒÂ´ng lÃ†Â°Ã†Â¡ng
    private Boolean requiresApproval; // true = cÃ¡ÂºÂ§n duyÃ¡Â»â€¡t, false = khÃƒÂ´ng cÃ¡ÂºÂ§n
    private Boolean isActive; // true = Ã„â€˜ang hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng, false = Ã„â€˜ÃƒÂ£ vÃƒÂ´ hiÃ¡Â»â€¡u
                              // hÃƒÂ³a

    public TimeOffTypeResponse() {
    }

    public TimeOffTypeResponse(String typeId, String typeCode, String typeName, String description,
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
}
