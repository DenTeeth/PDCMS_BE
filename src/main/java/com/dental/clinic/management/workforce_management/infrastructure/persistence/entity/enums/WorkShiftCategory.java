package com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.enums;

/**
 * Enum representing work shift categories.
 * Used to classify shifts based on time of day and additional benefits.
 */
public enum WorkShiftCategory {
    /**
     * Normal work shift - standard working hours.
     * Ca làm việc tiêu chuẩn.
     */
    NORMAL,

    /**
     * Night shift - includes night shift allowance.
     * Ca làm đêm - thêm phụ cấp.
     */
    NIGHT
}