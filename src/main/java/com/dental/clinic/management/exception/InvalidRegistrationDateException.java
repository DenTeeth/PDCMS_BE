package com.dental.clinic.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

/**
 * Exception thrown when schedule registration violates time window rules.
 * 
 * Business Rules:
 * - Minimum: 24 hours in advance
 * - Maximum: 30 days in advance
 * 
 * Rationale:
 * - 24h minimum: Allow clinic to prepare, notify patients
 * - 30d maximum: Prevent excessive forward planning, maintain flexibility
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRegistrationDateException extends RuntimeException {

    private static final int MIN_ADVANCE_DAYS = 1;  // 24 hours
    private static final int MAX_ADVANCE_DAYS = 30;

    public InvalidRegistrationDateException(String message) {
        super(message);
    }

    public InvalidRegistrationDateException(LocalDate workDate, LocalDate currentDate) {
        super(buildMessage(workDate, currentDate));
    }

    private static String buildMessage(LocalDate workDate, LocalDate currentDate) {
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(currentDate, workDate);
        return String.format(
            "Ngày đăng ký không hợp lệ: %s (còn %d ngày). " +
            "Phải đăng ký trước %d-%d ngày.",
            workDate, daysDiff, MIN_ADVANCE_DAYS, MAX_ADVANCE_DAYS
        );
    }

    public InvalidRegistrationDateException(String message, Throwable cause) {
        super(message, cause);
    }

    public static int getMinAdvanceDays() {
        return MIN_ADVANCE_DAYS;
    }

    public static int getMaxAdvanceDays() {
        return MAX_ADVANCE_DAYS;
    }
}
