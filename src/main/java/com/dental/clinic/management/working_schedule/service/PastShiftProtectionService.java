package com.dental.clinic.management.working_schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDate;
// import java.time.LocalDateTime;

/**
 * Business Rule #34: Past Shifts Read-Only Enforcement
 * 
 * Rule: Work shifts cannot be modified once their date has passed
 * - Past shifts are READ-ONLY (no create, update, or delete)
 * - "Past" = shift date < current date (before today)
 * - Applies to: EmployeeShift, FixedShiftRegistration, OvertimeRequest
 * - Exception: Admin can override for payroll corrections (not implemented here)
 * 
 * Rationale:
 * - Protects historical attendance records
 * - Prevents retroactive schedule manipulation
 * - Ensures audit trail integrity
 * - Maintains payroll accuracy
 * 
 * Implementation Strategy:
 * 1. validateNotPastShift() - Called before any shift modification
 * 2. Compare shift date with current date
 * 3. Throw ErrorResponseException if shift is in the past
 * 4. Allow current date shifts (today is still editable)
 * 
 * Integration Points:
 * - EmployeeShiftService.createShift() - before save
 * - EmployeeShiftService.updateShift() - before update
 * - EmployeeShiftService.deleteShift() - before delete
 * - FixedShiftRegistrationService - before modifications
 * - OvertimeRequestService - before create/update/delete
 * - TimeOffRequestService - before modifications to past dates
 * 
 * Grace Period:
 * - Current day (today) is still editable
 * - Only dates BEFORE today are locked
 * - No time-of-day checking (full day granularity)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PastShiftProtectionService {

    /**
     * Validate that shift is not in the past.
     * Prevents modifications to historical shifts.
     * 
     * @param shiftDate Date of the shift being modified
     * @throws ErrorResponseException if shift is in the past
     */
    @Transactional(readOnly = true)
    public void validateNotPastShift(LocalDate shiftDate) {
        LocalDate today = LocalDate.now();
        validateNotPastShift(shiftDate, today);
    }

    /**
     * Validate that shift is not in the past with custom current date.
     * Used for testing and batch operations.
     * 
     * @param shiftDate Date of the shift being modified
     * @param currentDate Current date for comparison
     * @throws ErrorResponseException if shift is in the past
     */
    @Transactional(readOnly = true)
    public void validateNotPastShift(LocalDate shiftDate, LocalDate currentDate) {
        log.debug("Validating shift date {} is not before current date {}", shiftDate, currentDate);

        // Allow modifications to current date and future dates
        if (!shiftDate.isBefore(currentDate)) {
            log.debug("Shift date {} is current or future, modification allowed", shiftDate);
            return;
        }

        // Block modifications to past dates
        String message = String.format(
                "Cannot modify past shifts. Shift date %s has already passed (current date: %s). " +
                "Past shifts are read-only to maintain historical accuracy. " +
                "Contact administrator if corrections are needed.",
                shiftDate,
                currentDate
        );

        log.warn("Past shift modification blocked: shift date={}, current date={}", shiftDate, currentDate);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message);
        problemDetail.setTitle("Không Thể Sửa Ca Quá Khứ");
        problemDetail.setProperty("shiftDate", shiftDate.toString());
        problemDetail.setProperty("currentDate", currentDate.toString());
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, problemDetail, null);
    }

    /**
     * Validate that date range doesn't include past dates.
     * Used for bulk operations.
     * 
     * @param startDate Start date of range
     * @param endDate End date of range
     * @throws ErrorResponseException if any date in range is in the past
     */
    @Transactional(readOnly = true)
    public void validateDateRangeNotPast(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        
        if (startDate.isBefore(today)) {
            String message = String.format(
                    "Cannot modify shifts in date range %s to %s. " +
                    "Start date is in the past (current date: %s). " +
                    "Past shifts are read-only.",
                    startDate,
                    endDate,
                    today
            );

            log.warn("Past date range modification blocked: start={}, end={}, current={}", 
                    startDate, endDate, today);

            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message);
            problemDetail.setTitle("Không Thể Sửa Khoảng Ngày Quá Khứ");
            problemDetail.setProperty("startDate", startDate.toString());
            problemDetail.setProperty("endDate", endDate.toString());
            problemDetail.setProperty("currentDate", today.toString());
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, problemDetail, null);
        }

        log.debug("Date range {} to {} is current or future, modification allowed", startDate, endDate);
    }

    /**
     * Check if a shift date can be modified.
     * Used by FE to enable/disable edit buttons.
     * 
     * @param shiftDate Date to check
     * @return true if modifications are allowed (date is today or future)
     */
    @Transactional(readOnly = true)
    public boolean canModifyShift(LocalDate shiftDate) {
        LocalDate today = LocalDate.now();
        return !shiftDate.isBefore(today);
    }

    /**
     * Check if a date is in the past.
     * Simple utility for FE conditional rendering.
     * 
     * @param date Date to check
     * @return true if date is before today
     */
    @Transactional(readOnly = true)
    public boolean isPastDate(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    /**
     * Get shift modification status for a date.
     * Comprehensive info for FE display.
     * 
     * @param shiftDate Date to check
     * @return Shift status
     */
    @Transactional(readOnly = true)
    public ShiftModificationStatus getShiftStatus(LocalDate shiftDate) {
        LocalDate today = LocalDate.now();
        boolean isPast = shiftDate.isBefore(today);
        boolean isToday = shiftDate.isEqual(today);
        boolean isFuture = shiftDate.isAfter(today);
        boolean canModify = !isPast;

        String message;
        if (isPast) {
            message = "Shift is in the past - read-only";
        } else if (isToday) {
            message = "Shift is today - modifications allowed";
        } else {
            message = "Shift is in the future - modifications allowed";
        }

        return ShiftModificationStatus.builder()
                .shiftDate(shiftDate)
                .currentDate(today)
                .isPast(isPast)
                .isToday(isToday)
                .isFuture(isFuture)
                .canModify(canModify)
                .isReadOnly(isPast)
                .message(message)
                .build();
    }

    /**
     * Validate multiple shift dates in batch.
     * Returns list of invalid dates for detailed error reporting.
     * 
     * @param shiftDates List of shift dates to validate
     * @return List of past dates that cannot be modified (empty if all valid)
     */
    @Transactional(readOnly = true)
    public java.util.List<LocalDate> validateBatchShiftDates(java.util.List<LocalDate> shiftDates) {
        LocalDate today = LocalDate.now();
        
        return shiftDates.stream()
                .filter(date -> date.isBefore(today))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * DTO for shift modification status.
     */
    @lombok.Data
    @lombok.Builder
    public static class ShiftModificationStatus {
        private LocalDate shiftDate;
        private LocalDate currentDate;
        private boolean isPast;
        private boolean isToday;
        private boolean isFuture;
        private boolean canModify;
        private boolean isReadOnly;
        private String message;
    }
}
