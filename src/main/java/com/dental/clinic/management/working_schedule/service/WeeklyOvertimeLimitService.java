package com.dental.clinic.management.working_schedule.service;

import org.springframework.web.ErrorResponseException;
import org.springframework.http.ProblemDetail;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Business Rule #37: Weekly Working Hours Limit (48 Hours)
 * 
 * Rule: An employee cannot be scheduled for more than 48 hours per week
 * - Maximum weekly hours: 48 hours
 * - Week boundary: Monday to Sunday (ISO-8601 week definition)
 * - Calculated by summing all SCHEDULED shift durations in the week
 * - Includes regular shifts (not just overtime)
 * - Blocks new shift assignments if adding them would exceed 48 hours
 * 
 * Implementation Strategy:
 * 1. validateWeeklyWorkingHoursLimit() - Called when creating/updating EmployeeShift
 * 2. Calculate total SCHEDULED hours for employee in the week containing the date
 * 3. Calculate duration of the new shift being added
 * 4. Validate: scheduledHours + newShiftHours <= 48
 * 5. Throw ErrorResponseException if limit exceeded with warning message
 * 
 * Week Definition:
 * - Week starts on Monday, ends on Sunday (ISO-8601 standard)
 * - Example: Jan 6, 2026 (Monday) to Jan 12, 2026 (Sunday)
 * 
 * Difference from Overtime Rules:
 * - Rule #41 (Daily Overtime): 4 hours/day for overtime requests
 * - Rule #42 (Monthly Overtime): 40 hours/month for overtime requests
 * - Rule #37 (Weekly Hours): 48 hours/week for ALL scheduled shifts
 * 
 * Shift Duration Calculation:
 * - Same as DailyOvertimeLimitService: Duration = end_time - start_time
 * - Handles shifts crossing midnight
 * 
 * Integration Points:
 * - EmployeeShiftService.createManualShift() - before save
 * - EmployeeShiftService.updateShift() - before update
 * - EmployeeShiftService.createShiftsForRegistration() - before batch save
 * 
 * Database Schema:
 * - employee_shifts.work_date: LocalDate (determines which week)
 * - employee_shifts.status: ENUM (SCHEDULED, ON_LEAVE, COMPLETED, ABSENT, CANCELLED)
 * - work_shifts.start_time, end_time: LocalTime
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyOvertimeLimitService {

    private static final int MAX_WEEKLY_WORKING_HOURS = 48;
    
    private final EmployeeShiftRepository employeeShiftRepository;

    /**
     * Validate weekly working hours limit.
     * Ensures employee doesn't exceed 48 hours of scheduled work per week.
     * 
     * @param employeeId Employee ID
     * @param workDate Date of the shift (determines which week)
     * @param newShift Work shift for the new/updated shift
     * @param excludeShiftId Shift ID to exclude (null for new shifts, ID for updates)
     * @throws ErrorResponseException if weekly limit exceeded
     */
    @Transactional(readOnly = true)
    public void validateWeeklyWorkingHoursLimit(
            Integer employeeId,
            LocalDate workDate,
            WorkShift newShift,
            String excludeShiftId) {

        log.info("Validating weekly working hours limit for employee {} on {}", employeeId, workDate);

        // 1. Get week boundaries (Monday to Sunday)
        LocalDate weekStart = workDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = workDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        log.debug("Week boundaries: {} to {}", weekStart, weekEnd);

        // 2. Calculate existing scheduled hours in this week
        double existingHours = calculateScheduledHoursInWeek(employeeId, weekStart, weekEnd, excludeShiftId);
        
        // 3. Calculate new shift duration
        double newShiftHours = calculateShiftDuration(newShift);
        
        // 4. Calculate total scheduled hours
        double totalHours = existingHours + newShiftHours;

        log.debug("Employee {} week {}-{}: existing={}h, new={}h, total={}h (limit: {}h)",
                employeeId, weekStart, weekEnd, existingHours, newShiftHours, totalHours, MAX_WEEKLY_WORKING_HOURS);

        // 5. Validate against limit
        if (totalHours > MAX_WEEKLY_WORKING_HOURS) {
            String message = String.format(
                    "⚠️ Cảnh báo: Vượt giới hạn giờ làm việc tuần. " +
                    "Nhân viên đã được xếp lịch %.1f giờ trong tuần %s đến %s. " +
                    "Thêm ca này (%.1f giờ) sẽ tổng cộng %.1f giờ, " +
                    "vượt quá giới hạn %d giờ/tuần.",
                    existingHours,
                    weekStart,
                    weekEnd,
                    newShiftHours,
                    totalHours,
                    MAX_WEEKLY_WORKING_HOURS
            );
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
            problemDetail.setTitle("Vượt Giới Hạn 48 Giờ/Tuần");
            problemDetail.setProperty("employeeId", employeeId);
            problemDetail.setProperty("weekStart", weekStart.toString());
            problemDetail.setProperty("weekEnd", weekEnd.toString());
            problemDetail.setProperty("existingHours", existingHours);
            problemDetail.setProperty("newShiftHours", newShiftHours);
            problemDetail.setProperty("totalHours", totalHours);
            problemDetail.setProperty("maxWeeklyHours", MAX_WEEKLY_WORKING_HOURS);
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, problemDetail, null);
        }

        log.info("Weekly working hours validation passed for employee {}", employeeId);
    }

    /**
     * Calculate existing scheduled hours for an employee in a specific week.
     * Only counts SCHEDULED shifts (excludes CANCELLED, ABSENT).
     * 
     * @param employeeId Employee ID
     * @param weekStart Start of week (Monday)
     * @param weekEnd End of week (Sunday)
     * @param excludeShiftId Shift ID to exclude (for updates)
     * @return Total scheduled hours in the week
     */
    private double calculateScheduledHoursInWeek(
            Integer employeeId,
            LocalDate weekStart,
            LocalDate weekEnd,
            String excludeShiftId) {

        // Get all shifts for employee in the week
        List<EmployeeShift> shifts = employeeShiftRepository.findAll().stream()
                .filter(shift -> 
                        shift.getEmployee() != null &&
                        shift.getEmployee().getEmployeeId().equals(employeeId) &&
                        shift.getWorkDate() != null &&
                        !shift.getWorkDate().isBefore(weekStart) &&
                        !shift.getWorkDate().isAfter(weekEnd) &&
                        // Only count SCHEDULED shifts (not CANCELLED or ABSENT)
                        shift.getStatus() == com.dental.clinic.management.working_schedule.enums.ShiftStatus.SCHEDULED
                )
                .collect(java.util.stream.Collectors.toList());

        // Filter and sum durations
        return shifts.stream()
                .filter(shift -> {
                    // Exclude the shift being updated
                    if (excludeShiftId != null && excludeShiftId.equals(shift.getEmployeeShiftId())) {
                        return false;
                    }
                    return true;
                })
                .mapToDouble(shift -> calculateShiftDuration(shift.getWorkShift()))
                .sum();
    }

    /**
     * Calculate shift duration in hours.
     * Same logic as DailyOvertimeLimitService.
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
     * Get weekly working hours summary for an employee.
     * Useful for reporting and FE dashboard.
     * 
     * @param employeeId Employee ID
     * @param date Any date in the week
     * @return Weekly summary
     */
    @Transactional(readOnly = true)
    public WeeklyWorkingHoursSummary getWeeklySummary(Integer employeeId, LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        double scheduledHours = calculateScheduledHoursInWeek(employeeId, weekStart, weekEnd, null);
        double remainingHours = Math.max(0, MAX_WEEKLY_WORKING_HOURS - scheduledHours);
        
        return WeeklyWorkingHoursSummary.builder()
                .employeeId(employeeId)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .scheduledHours(scheduledHours)
                .remainingHours(remainingHours)
                .maxWeeklyHours(MAX_WEEKLY_WORKING_HOURS)
                .isLimitReached(scheduledHours >= MAX_WEEKLY_WORKING_HOURS)
                .warningThresholdReached(scheduledHours >= MAX_WEEKLY_WORKING_HOURS * 0.9) // 90% threshold
                .build();
    }

    /**
     * Check if employee can add more hours in a specific week.
     * 
     * @param employeeId Employee ID
     * @param workDate Date in the week
     * @param shiftDurationHours Duration of new shift in hours
     * @return true if adding the shift would not exceed limit
     */
    @Transactional(readOnly = true)
    public boolean canAddShift(Integer employeeId, LocalDate workDate, double shiftDurationHours) {
        LocalDate weekStart = workDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = workDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        double existingHours = calculateScheduledHoursInWeek(employeeId, weekStart, weekEnd, null);
        return (existingHours + shiftDurationHours) <= MAX_WEEKLY_WORKING_HOURS;
    }

    /**
     * DTO for weekly working hours summary.
     */
    @lombok.Builder
    @lombok.Data
    public static class WeeklyWorkingHoursSummary {
        private Integer employeeId;
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private double scheduledHours;
        private double remainingHours;
        private int maxWeeklyHours;
        private boolean isLimitReached;
        private boolean warningThresholdReached; // 90% of limit
    }
}
