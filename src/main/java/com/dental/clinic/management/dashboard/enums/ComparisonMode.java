package com.dental.clinic.management.dashboard.enums;

/**
 * Comparison modes for dashboard statistics
 */
public enum ComparisonMode {
    /**
     * No comparison
     */
    NONE,
    
    /**
     * Compare with previous month
     */
    MONTH,
    
    /**
     * Compare with previous quarter (3 months)
     */
    QUARTER,
    
    /**
     * Compare with previous year (same period last year)
     */
    YEAR;
    
    /**
     * Parse string to ComparisonMode enum
     * Returns NONE if invalid or null
     */
    public static ComparisonMode fromString(String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            return NONE;
        }
        
        try {
            return ComparisonMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
