package com.dental.clinic.management.employee.enums;

/**
 * Employment type enum (Schema V14).
 *
 * - FULL_TIME: Uses Fixed Schedule (Luồng 1)
 * - PART_TIME_FIXED: Uses Fixed Schedule (Luồng 1)
 * - PART_TIME_FLEX: Uses Flexible Schedule (Luồng 2)
 */
public enum EmploymentType {
    FULL_TIME, // Dùng Luồng Cố định (fixed_shift_registrations)
    PART_TIME_FIXED, // Dùng Luồng Cố định (fixed_shift_registrations)
    PART_TIME_FLEX, // Dùng Luồng Linh hoạt (part_time_registrations)

    // Legacy support
    @Deprecated
    PART_TIME // Map to PART_TIME_FLEX for backward compatibility
}
