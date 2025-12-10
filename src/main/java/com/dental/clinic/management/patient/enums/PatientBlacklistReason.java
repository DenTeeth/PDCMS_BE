package com.dental.clinic.management.patient.enums;

/**
 * BR-044: Predefined reasons for adding patients to blacklist.
 * 
 * Business Rule: When adding a patient to "Blacklist", staff MUST select
 * one of these predefined reasons (no free-text allowed).
 * 
 * Purpose: Standardize blacklist reasons for reporting and accountability.
 */
public enum PatientBlacklistReason {
    
    /**
     * Patient verbally or physically abused staff members
     */
    STAFF_ABUSE("Xúc phạm nhân viên"),
    
    /**
     * Patient has unpaid bills or refuses to pay
     */
    DEBT_DEFAULT("Bùng nợ"),
    
    /**
     * Patient threatened legal action frivolously
     */
    FRIVOLOUS_LAWSUIT("Doạ kiện không có cơ sở"),
    
    /**
     * Patient damaged clinic property
     */
    PROPERTY_DAMAGE("Phá hoại tài sản phòng khám"),
    
    /**
     * Patient showed up intoxicated or under influence
     */
    INTOXICATION("Vi phạm quy định (say rượu/ma túy)"),
    
    /**
     * Patient repeatedly creates disturbances
     */
    DISRUPTIVE_BEHAVIOR("Gây rối trật tự liên tục"),
    
    /**
     * Patient violated clinic policies multiple times
     */
    POLICY_VIOLATION("Vi phạm nội quy nhiều lần"),
    
    /**
     * Other serious reason (Manager must document separately)
     */
    OTHER_SERIOUS("Lý do nghiêm trọng khác");

    private final String displayName;

    PatientBlacklistReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if the reason is severe enough to require Manager approval.
     * 
     * Currently, all blacklist actions require Manager/Admin role.
     * This method is for future workflow enhancements.
     */
    public boolean requiresManagerApproval() {
        // All blacklist reasons require Manager approval for now
        return true;
    }
}
