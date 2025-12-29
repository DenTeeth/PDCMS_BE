package com.dental.clinic.management.working_schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Business Rule #33: Schedule Finalization Deadline
 * 
 * Rule: Work schedules must be finalized by Friday 5 PM for the following week
 * - Deadline: Friday 17:00 (5 PM) each week
 * - Target period: Monday to Sunday of the FOLLOWING week
 * - After deadline, schedule changes for next week are blocked (read-only)
 * - Applies to: EmployeeShift creation/updates, FixedShiftRegistration, OvertimeRequest
 * 
 * Example Timeline:
 * - Friday, Dec 8, 5 PM: Deadline to finalize schedule for Dec 11-17
 * - After Dec 8, 5 PM: Cannot modify schedule for Dec 11-17
 * - Can still modify schedule for Dec 18+ (future weeks)
 * 
 * Implementation Strategy:
 * 1. validateScheduleDeadline() - Called before any schedule modification
 * 2. Calculate deadline date (last Friday 5 PM)
 * 3. Calculate target week start (Monday after deadline)
 * 4. Check if modification date falls in locked week
 * 5. Throw ErrorResponseException if deadline passed
 * 
 * Integration Points:
 * - EmployeeShiftService.createShift() - before save
 * - EmployeeShiftService.updateShift() - before update
 * - EmployeeShiftService.deleteShift() - before delete
 * - FixedShiftRegistrationService - before modifications
 * - OvertimeRequestService - before creating requests for next week
 * 
 * Configuration:
 * - DEADLINE_DAY: Friday (DayOfWeek.FRIDAY)
 * - DEADLINE_TIME: 17:00 (5 PM)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleFinalizationService {

    @SuppressWarnings("unused")
    private static final DayOfWeek DEADLINE_DAY = DayOfWeek.FRIDAY;
    private static final LocalTime DEADLINE_TIME = LocalTime.of(17, 0); // 5 PM
    @SuppressWarnings("unused")
    private static final int ADVANCE_WEEKS = 1; // Finalize 1 week in advance

    /**
     * Validate schedule finalization deadline.
     * Ensures modifications for next week are only allowed before Friday 5 PM.
     * 
     * @param targetDate Date being modified in the schedule
     * @throws ErrorResponseException if deadline has passed for the target week
     */
    @Transactional(readOnly = true)
    public void validateScheduleDeadline(LocalDate targetDate) {
        LocalDateTime now = LocalDateTime.now();
        validateScheduleDeadline(targetDate, now);
    }

    /**
     * Validate schedule finalization deadline with custom current time.
     * Used for testing and batch operations.
     * 
     * @param targetDate Date being modified in the schedule
     * @param currentDateTime Current date/time
     * @throws ErrorResponseException if deadline has passed for the target week
     */
    @Transactional(readOnly = true)
    public void validateScheduleDeadline(LocalDate targetDate, LocalDateTime currentDateTime) {
        log.debug("Validating schedule deadline for target date {} at current time {}", 
                targetDate, currentDateTime);

        // 1. Calculate the deadline for the week containing targetDate
        LocalDateTime deadline = calculateDeadlineForWeek(targetDate);

        // 2. Check if current time is past the deadline
        if (currentDateTime.isAfter(deadline)) {
            String message = String.format(
                    "Schedule finalization deadline has passed. " +
                    "Changes for the week of %s must be made before %s. " +
                    "Current time: %s. Please contact administrator for emergency changes.",
                    getWeekStartDate(targetDate),
                    deadline.toLocalDate().atTime(DEADLINE_TIME),
                    currentDateTime
            );

            log.warn("Schedule deadline violation: target={}, deadline={}, current={}", 
                    targetDate, deadline, currentDateTime);

            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message);
            problemDetail.setTitle("Đã Quá Hạn Chốt Lịch");
            problemDetail.setProperty("targetDate", targetDate.toString());
            problemDetail.setProperty("deadline", deadline.toString());
            problemDetail.setProperty("currentDateTime", currentDateTime.toString());
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, problemDetail, null);
        }

        log.debug("Schedule deadline check passed for target date {}", targetDate);
    }

    /**
     * Calculate finalization deadline for a given week.
     * Deadline is Friday 5 PM of the PREVIOUS week.
     * 
     * @param targetDate Any date in the target week
     * @return Deadline datetime (Friday 5 PM of previous week)
     */
    private LocalDateTime calculateDeadlineForWeek(LocalDate targetDate) {
        // Get Monday of the target week
        LocalDate weekStart = getWeekStartDate(targetDate);
        
        // Go back to previous Friday
        LocalDate deadlineDate = weekStart.minusDays(3); // Monday - 3 = Friday of previous week
        
        return deadlineDate.atTime(DEADLINE_TIME);
    }

    /**
     * Get Monday of the week containing the given date.
     * 
     * @param date Any date
     * @return Monday of that week
     */
    private LocalDate getWeekStartDate(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    /**
     * Check if a date's schedule can still be modified.
     * Used by FE to enable/disable edit buttons.
     * 
     * @param targetDate Date to check
     * @return true if modifications are allowed
     */
    @Transactional(readOnly = true)
    public boolean canModifySchedule(LocalDate targetDate) {
        try {
            validateScheduleDeadline(targetDate);
            return true;
        } catch (ErrorResponseException e) {
            return false;
        }
    }

    /**
     * Check if a date range's schedule can be modified.
     * Used for bulk operations.
     * 
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return true if ALL dates in range can be modified
     */
    @Transactional(readOnly = true)
    public boolean canModifyScheduleRange(LocalDate startDate, LocalDate endDate) {
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!canModifySchedule(current)) {
                return false;
            }
            current = current.plusDays(1);
        }
        return true;
    }

    /**
     * Get next finalization deadline.
     * Used by FE to display countdown timer.
     * 
     * @return Next Friday 5 PM
     */
    @Transactional(readOnly = true)
    public LocalDateTime getNextDeadline() {
        LocalDate today = LocalDate.now();
        LocalDate nextFriday = today.with(DayOfWeek.FRIDAY);
        
        // If today is Friday and past 5 PM, or after Friday, get next Friday
        if (today.getDayOfWeek() == DayOfWeek.FRIDAY && LocalTime.now().isAfter(DEADLINE_TIME)) {
            nextFriday = nextFriday.plusWeeks(1);
        } else if (today.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            nextFriday = nextFriday.plusWeeks(1);
        }
        
        return nextFriday.atTime(DEADLINE_TIME);
    }

    /**
     * Get schedule modification status for a date.
     * Comprehensive info for FE display.
     * 
     * @param targetDate Date to check
     * @return Schedule status
     */
    @Transactional(readOnly = true)
    public ScheduleModificationStatus getScheduleStatus(LocalDate targetDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = calculateDeadlineForWeek(targetDate);
        boolean canModify = !now.isAfter(deadline);
        
        return ScheduleModificationStatus.builder()
                .targetDate(targetDate)
                .weekStartDate(getWeekStartDate(targetDate))
                .deadline(deadline)
                .currentDateTime(now)
                .canModify(canModify)
                .isLocked(!canModify)
                .message(canModify 
                        ? "Schedule can be modified until " + deadline
                        : "Schedule is locked. Deadline passed on " + deadline)
                .build();
    }

    /**
     * DTO for schedule modification status.
     */
    @lombok.Data
    @lombok.Builder
    public static class ScheduleModificationStatus {
        private LocalDate targetDate;
        private LocalDate weekStartDate;
        private LocalDateTime deadline;
        private LocalDateTime currentDateTime;
        private boolean canModify;
        private boolean isLocked;
        private String message;
    }
}
