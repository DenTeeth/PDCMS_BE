package com.dental.clinic.management.working_schedule.service;

import org.springframework.web.ErrorResponseException;
import org.springframework.http.ProblemDetail;
import com.dental.clinic.management.working_schedule.domain.OvertimeRequest;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.enums.RequestStatus;
import com.dental.clinic.management.working_schedule.repository.OvertimeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;

/**
 * Business Rule #41: Daily Overtime Limit (4 Hours)
 * 
 * Rule: An employee cannot work more than 4 hours of overtime in a single day
 * - Maximum daily overtime: 4 hours
 * - Calculated by summing shift durations for APPROVED + PENDING overtime requests on the same date
 * - Applies per employee per day (work_date)
 * - Blocks new overtime requests if adding them would exceed 4 hours
 * 
 * Implementation Strategy:
 * 1. validateDailyOvertimeLimit() - Called when creating/updating OvertimeRequest
 * 2. Calculate total existing overtime hours for the employee on the date
 *    - Include APPROVED requests (confirmed overtime)
 *    - Include PENDING requests (to prevent spam)
 *    - Exclude REJECTED/CANCELLED requests
 * 3. Calculate duration of new/updated overtime shift
 * 4. Validate: existingHours + newShiftHours <= 4
 * 5. Throw ErrorResponseException if limit exceeded
 * 
 * Shift Duration Calculation:
 * - WorkShift has start_time and end_time (LocalTime)
 * - Duration = end_time - start_time
 * - Example: 18:00 - 22:00 = 4 hours
 * 
 * Integration Points:
 * - OvertimeRequestService.createOvertimeRequest() - before save
 * - OvertimeRequestService.updateOvertimeRequest() - before update
 * 
 * Database Schema:
 * - overtime_requests.work_date: LocalDate (date of overtime work)
 * - overtime_requests.work_shift_id: FK to work_shifts
 * - work_shifts.start_time: LocalTime (shift start)
 * - work_shifts.end_time: LocalTime (shift end)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyOvertimeLimitService {

    private static final int MAX_DAILY_OVERTIME_HOURS = 4;
    
    private final OvertimeRequestRepository overtimeRequestRepository;

    /**
     * Validate daily overtime limit.
     * Ensures employee doesn't exceed 4 hours of overtime per day.
     * 
     * @param employeeId Employee requesting overtime
     * @param workDate Date of overtime work
     * @param newShift Work shift for the new/updated overtime
     * @param excludeRequestId Request ID to exclude (null for new requests, ID for updates)
     * @throws ErrorResponseException if daily limit exceeded
     */
    @Transactional(readOnly = true)
    public void validateDailyOvertimeLimit(
            Integer employeeId,
            LocalDate workDate,
            WorkShift newShift,
            String excludeRequestId) {

        log.info("Validating daily overtime limit for employee {} on {}", employeeId, workDate);

        // 1. Calculate existing overtime hours on this date
        double existingHours = calculateExistingOvertimeHours(employeeId, workDate, excludeRequestId);
        
        // 2. Calculate new shift duration
        double newShiftHours = calculateShiftDuration(newShift);
        
        // 3. Calculate total overtime hours
        double totalHours = existingHours + newShiftHours;

        log.debug("Employee {} overtime on {}: existing={}h, new={}h, total={}h (limit: {}h)",
                employeeId, workDate, existingHours, newShiftHours, totalHours, MAX_DAILY_OVERTIME_HOURS);

        // 4. Validate against limit
        if (totalHours > MAX_DAILY_OVERTIME_HOURS) {
            String message = String.format(
                    "Daily overtime limit exceeded. Employee already has %.1f hours of overtime on %s. " +
                    "Adding this shift (%.1f hours) would result in %.1f hours total, " +
                    "which exceeds the daily limit of %d hours.",
                    existingHours,
                    workDate,
                    newShiftHours,
                    totalHours,
                    MAX_DAILY_OVERTIME_HOURS
            );
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
            problemDetail.setTitle("Daily Overtime Limit Exceeded");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, problemDetail, null);
        }

        log.info("Daily overtime limit validation passed for employee {}", employeeId);
    }

    /**
     * Calculate existing overtime hours for an employee on a specific date.
     * Includes APPROVED and PENDING requests (excludes specific request if updating).
     * 
     * @param employeeId Employee ID
     * @param workDate Work date
     * @param excludeRequestId Request ID to exclude (for updates)
     * @return Total overtime hours
     */
    private double calculateExistingOvertimeHours(
            Integer employeeId,
            LocalDate workDate,
            String excludeRequestId) {

        // Get all overtime requests for employee on this date
        List<OvertimeRequest> existingRequests = overtimeRequestRepository
                .findByEmployeeIdAndWorkDate(employeeId, workDate);

        // Filter and sum durations
        return existingRequests.stream()
                .filter(request -> {
                    // Exclude the request being updated
                    if (excludeRequestId != null && excludeRequestId.equals(request.getRequestId())) {
                        return false;
                    }
                    
                    // Only count APPROVED and PENDING requests
                    RequestStatus status = request.getStatus();
                    return status == RequestStatus.APPROVED || status == RequestStatus.PENDING;
                })
                .mapToDouble(request -> calculateShiftDuration(request.getWorkShift()))
                .sum();
    }

    /**
     * Calculate shift duration in hours.
     * 
     * @param workShift Work shift
     * @return Duration in hours (decimal)
     */
    private double calculateShiftDuration(WorkShift workShift) {
        if (workShift == null || workShift.getStartTime() == null || workShift.getEndTime() == null) {
            log.warn("Invalid work shift - cannot calculate duration");
            return 0.0;
        }

        LocalTime start = workShift.getStartTime();
        LocalTime end = workShift.getEndTime();
        
        Duration duration = Duration.between(start, end);
        
        // Handle shifts that cross midnight
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        
        // Convert to hours (decimal)
        return duration.toMinutes() / 60.0;
    }

    /**
     * Get daily overtime summary for an employee on a specific date.
     * Useful for reporting and FE display.
     * 
     * @param employeeId Employee ID
     * @param workDate Work date
     * @return Daily overtime summary
     */
    @Transactional(readOnly = true)
    public DailyOvertimeSummary getDailyOvertimeSummary(Integer employeeId, LocalDate workDate) {
        double existingHours = calculateExistingOvertimeHours(employeeId, workDate, null);
        double remainingHours = Math.max(0, MAX_DAILY_OVERTIME_HOURS - existingHours);
        
        return DailyOvertimeSummary.builder()
                .employeeId(employeeId)
                .workDate(workDate)
                .existingOvertimeHours(existingHours)
                .remainingOvertimeHours(remainingHours)
                .maxDailyOvertimeHours(MAX_DAILY_OVERTIME_HOURS)
                .isLimitReached(existingHours >= MAX_DAILY_OVERTIME_HOURS)
                .build();
    }

    /**
     * Check if employee can add more overtime on a specific date.
     * 
     * @param employeeId Employee ID
     * @param workDate Work date
     * @param shiftDurationHours Duration of new shift in hours
     * @return true if adding the shift would not exceed limit
     */
    @Transactional(readOnly = true)
    public boolean canAddOvertimeShift(Integer employeeId, LocalDate workDate, double shiftDurationHours) {
        double existingHours = calculateExistingOvertimeHours(employeeId, workDate, null);
        return (existingHours + shiftDurationHours) <= MAX_DAILY_OVERTIME_HOURS;
    }

    /**
     * DTO for daily overtime summary.
     */
    @lombok.Builder
    @lombok.Data
    public static class DailyOvertimeSummary {
        private Integer employeeId;
        private LocalDate workDate;
        private double existingOvertimeHours;
        private double remainingOvertimeHours;
        private int maxDailyOvertimeHours;
        private boolean isLimitReached;
    }
}
