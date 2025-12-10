package com.dental.clinic.management.patient.enums;

/**
 * BR-044: Predefined reasons for adding patients to blacklist (CONSOLIDATED).
 * 
 * Business Rule: When adding a patient to "Blacklist", staff MUST select
 * one of these predefined reasons (no free-text allowed).
 * 
 * Purpose: Standardize blacklist reasons for reporting and accountability.
 * 
 * SIMPLIFIED: Reduced from 8 to 4 reasons based on FE feedback for better UX
 */
public enum PatientBlacklistReason {
    
    /**
     * 🔴 Payment issues: debt default, refuses to pay, payment disputes
     */
    PAYMENT_ISSUES("Vấn đề thanh toán"),
    
    /**
     * 🔴 Staff abuse: verbal/physical abuse, harassment, disruptive behavior
     */
    STAFF_ABUSE("Bạo lực/quấy rối nhân viên"),
    
    /**
     * 🔴 Policy violations: excessive cancellations, repeated rule violations
     */
    POLICY_VIOLATION("Vi phạm quy định"),
    
    /**
     * 🔴 Other serious reasons: property damage, intoxication, frivolous lawsuits, etc.
     * Manager must document details in notes field
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
