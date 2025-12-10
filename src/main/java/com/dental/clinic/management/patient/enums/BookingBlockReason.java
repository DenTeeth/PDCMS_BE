package com.dental.clinic.management.patient.enums;

/**
 * Unified booking block reasons for both temporary and permanent restrictions.
 * 
 * Business Rules:
 * - BR-005: Automatic temporary block after 3 consecutive no-shows
 * - BR-043: Automatic blacklist after 3 appointment cancellations within 30 days
 * - BR-044: Manual blacklist by staff for serious violations
 * 
 * Purpose: Consolidate is_booking_blocked and is_blacklisted into single flag
 */
public enum BookingBlockReason {
    
    // ===== TEMPORARY BLOCKS (BR-005) =====
    /**
     * BR-005: Patient has 3 consecutive no-shows
     * Can be unblocked when patient shows up for next appointment
     */
    EXCESSIVE_NO_SHOWS("3 lần không đến liên tiếp", true, false),
    
    // ===== AUTOMATIC BLACKLIST (BR-043) =====
    /**
     * BR-043: Patient cancelled 3 appointments within 30 days
     * Automatically added to blacklist, requires manual unban
     */
    EXCESSIVE_CANCELLATIONS("Hủy lịch quá nhiều (3 lần trong 30 ngày)", false, true),
    
    // ===== MANUAL BLACKLIST (BR-044) =====
    /**
     * BR-044: Patient verbally or physically abused staff members
     */
    STAFF_ABUSE("Xúc phạm nhân viên", false, true),
    
    /**
     * BR-044: Patient has unpaid bills or refuses to pay
     */
    DEBT_DEFAULT("Bùng nợ", false, true),
    
    /**
     * BR-044: Patient threatened legal action frivolously
     */
    FRIVOLOUS_LAWSUIT("Doạ kiện không có cơ sở", false, true),
    
    /**
     * BR-044: Patient damaged clinic property
     */
    PROPERTY_DAMAGE("Phá hoại tài sản phòng khám", false, true),
    
    /**
     * BR-044: Patient showed up intoxicated or under influence
     */
    INTOXICATION("Vi phạm quy định (say rượu/ma túy)", false, true),
    
    /**
     * BR-044: Patient repeatedly creates disturbances
     */
    DISRUPTIVE_BEHAVIOR("Gây rối trật tự liên tục", false, true),
    
    /**
     * BR-044: Patient violated clinic policies multiple times
     */
    POLICY_VIOLATION("Vi phạm nội quy nhiều lần", false, true),
    
    /**
     * BR-044: Other serious reason (Manager must document separately)
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
            EXCESSIVE_CANCELLATIONS,
            STAFF_ABUSE,
            DEBT_DEFAULT,
            FRIVOLOUS_LAWSUIT,
            PROPERTY_DAMAGE,
            INTOXICATION,
            DISRUPTIVE_BEHAVIOR,
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
