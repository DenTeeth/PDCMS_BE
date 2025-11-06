package com.dental.clinic.management.working_schedule.exception;

import java.time.LocalDate;
import java.util.List;

/**
 * Exception thrown when employee already has an approved registration that conflicts
 * with the requested dates.
 * Enhanced with conflicting dates and existing registration ID for better error context.
 */
public class RegistrationConflictException extends RuntimeException {
    private final List<LocalDate> conflictingDates;
    private final Integer existingRegistrationId;

    public RegistrationConflictException(List<LocalDate> conflictingDates, Integer existingRegistrationId) {
        super(String.format("Bạn đã có đăng ký được duyệt cho ca làm việc này vào các ngày: %s (Registration ID: %d)",
                formatDates(conflictingDates), existingRegistrationId));
        this.conflictingDates = conflictingDates;
        this.existingRegistrationId = existingRegistrationId;
    }

    // Legacy constructor for backward compatibility
    public RegistrationConflictException(Integer employeeId) {
        super("Bạn đã có đăng ký ca làm việc active khác trùng giờ. Vui lòng hủy đăng ký cũ trước.");
        this.conflictingDates = null;
        this.existingRegistrationId = null;
    }

    private static String formatDates(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return "";
        }
        return String.join(", ", dates.stream()
                .map(LocalDate::toString)
                .toArray(String[]::new));
    }

    public List<LocalDate> getConflictingDates() {
        return conflictingDates;
    }

    public Integer getExistingRegistrationId() {
        return existingRegistrationId;
    }
}
