package com.dental.clinic.management.patient.enums;

/**
 * Simplified unified booking block reasons (Based on FE feedback).
 * 
 * Business Rules:
 * - BR-005: Automatic temporary block after 3 consecutive no-shows
 * - BR-043: Duplicate patient detection (unrelated to booking blocks)
 * - BR-044: Manual blacklist by staff for serious violations
 * 
 * Purpose: Consolidate is_booking_blocked and is_blacklisted into single flag
 * 
 * CONSOLIDATED: Reduced from 10 to 5 reasons for better UX
 */
public enum BookingBlockReason {
    
    // ===== TEMPORARY BLOCKS (BR-005) =====
    /**
     * 🟠 BR-005: Patient has 3 consecutive no-shows
     * Can be unblocked when patient shows up for next appointment
     */
    EXCESSIVE_NO_SHOWS("Bỏ hẹn quá nhiều", true, false),
    
    // ===== PERMANENT BLACKLIST =====
    /**
     * 🔴 Payment issues: debt default, refuses to pay, payment disputes
     * Consolidates: DEBT_DEFAULT
     */
    PAYMENT_ISSUES("Vấn đề thanh toán", false, true),
    
    /**
     * 🔴 Staff abuse: verbal/physical abuse, harassment, disruptive behavior
     * Consolidates: STAFF_ABUSE + DISRUPTIVE_BEHAVIOR
     */
    STAFF_ABUSE("Bạo lực/quấy rối nhân viên", false, true),
    
    /**
     * 🔴 Policy violations: excessive no-shows, repeated rule violations
     * Used for manual blacklist due to policy violations (not automatic)
     */
    POLICY_VIOLATION("Vi phạm quy định", false, true),
    
    /**
     * 🔴 Other serious reasons: property damage, intoxication, frivolous lawsuits, etc.
     * Consolidates: PROPERTY_DAMAGE + INTOXICATION + FRIVOLOUS_LAWSUIT + OTHER_SERIOUS
     * Manager must document details in bookingBlockNotes
     */
    OTHER_SERIOUS("Lý do nghiêm trọng khác", false, true);

    private final String displayName;
    private final boolean isTemporary; // Can be auto-unblocked
    private final boolean isBlacklisted; // Permanent ban, requires manual unban

    BookingBlockReason(String displayName, boolean isTemporary, boolean isBlacklisted) {
        this.displayName = displayName;
        this.isTemporary = isTemporary;
        this.isBlacklisted = isBlacklisted;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public boolean isBlacklisted() {
        return isBlacklisted;
    }

    /**
     * Get all blacklist reasons (permanent bans)
     */
    public static BookingBlockReason[] getBlacklistReasons() {
        return new BookingBlockReason[]{
            PAYMENT_ISSUES,
            STAFF_ABUSE,
            POLICY_VIOLATION,
            OTHER_SERIOUS
        };
    }

    /**
     * Get all temporary block reasons
     */
    public static BookingBlockReason[] getTemporaryBlockReasons() {
        return new BookingBlockReason[]{
            EXCESSIVE_NO_SHOWS
        };
    }
}
