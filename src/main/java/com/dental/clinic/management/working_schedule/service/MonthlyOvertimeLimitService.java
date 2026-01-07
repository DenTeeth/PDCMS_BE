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
import java.time.YearMonth;
import java.util.List;
// import java.util.stream.Collectors;

/**
 * Business Rule #42: Monthly Overtime Limit (40 Hours)
 * 
 * Rule: An employee cannot work more than 40 hours of overtime per month
 * - Maximum monthly overtime: 40 hours
 * - Calculated by summing shift durations for APPROVED overtime requests in the calendar month
 * - Month boundary: 1st day to last day of month (e.g., Nov 1 - Nov 30)
 * - PENDING requests are NOT counted (only APPROVED counts against limit)
 * - Blocks new overtime requests if adding them would exceed 40 hours when approved
 * 
 * Implementation Strategy:
 * 1. validateMonthlyOvertimeLimit() - Called when APPROVING OvertimeRequest
 * 2. Calculate total APPROVED overtime hours for employee in the month
 * 3. Calculate duration of the overtime shift being approved
 * 4. Validate: approvedHours + newShiftHours <= 40
 * 5. Throw ErrorResponseException if limit exceeded
 * 
 * Difference from Rule #41 (Daily Limit):
 * - Rule #41: Counts APPROVED + PENDING, validates at request creation
 * - Rule #42: Counts APPROVED only, validates at approval time
 * 
 * Shift Duration Calculation:
 * - Same as Rule #41: Duration = end_time - start_time
 * - Handles shifts crossing midnight
 * 
 * Integration Points:
 * - OvertimeRequestService.approveOvertimeRequest() - before approval
 * 
 * Database Schema:
 * - overtime_requests.work_date: LocalDate (determines which month)
 * - overtime_requests.status: ENUM (PENDING, APPROVED, REJECTED, CANCELLED)
 * - work_shifts.start_time, end_time: LocalTime
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyOvertimeLimitService {

    private static final int MAX_MONTHLY_OVERTIME_HOURS = 40;
    
    private final OvertimeRequestRepository overtimeRequestRepository;

    /**
     * Validate monthly overtime limit.
     * Ensures employee doesn't exceed 40 hours of overtime per month.
     * Should be called when APPROVING an overtime request.
     * 
     * @param employeeId Employee requesting overtime
     * @param workDate Date of overtime work (determines the month)
     * @param newShift Work shift for the overtime being approved
     * @throws ErrorResponseException if monthly limit exceeded
     */
    @Transactional(readOnly = true)
    public void validateMonthlyOvertimeLimit(
            Integer employeeId,
            LocalDate workDate,
            WorkShift newShift) {

        YearMonth yearMonth = YearMonth.from(workDate);
        log.info("Validating monthly overtime limit for employee {} in {}", employeeId, yearMonth);

        // 1. Calculate existing APPROVED overtime hours in the month
        double approvedHours = calculateApprovedOvertimeHours(employeeId, yearMonth);
        
        // 2. Calculate new shift duration
        double newShiftHours = calculateShiftDuration(newShift);
        
        // 3. Calculate total overtime hours (if this request is approved)
        double totalHours = approvedHours + newShiftHours;

        log.debug("Employee {} overtime in {}: approved={}h, new={}h, total={}h (limit: {}h)",
                employeeId, yearMonth, approvedHours, newShiftHours, totalHours, MAX_MONTHLY_OVERTIME_HOURS);

        // 4. Validate against limit
        if (totalHours > MAX_MONTHLY_OVERTIME_HOURS) {
            String message = String.format(
                    "Monthly overtime limit exceeded. Employee already has %.1f hours of approved overtime in %s. " +
                    "Approving this shift (%.1f hours) would result in %.1f hours total, " +
                    "which exceeds the monthly limit of %d hours.",
                    approvedHours,
                    yearMonth.toString(),
                    newShiftHours,
                    totalHours,
                    MAX_MONTHLY_OVERTIME_HOURS
            );
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
            problemDetail.setTitle("Vượt Giới Hạn Thêm Giờ Tháng");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, problemDetail, null);
        }

        log.info("Monthly overtime limit validation passed for employee {}", employeeId);
    }

    /**
     * Calculate APPROVED overtime hours for an employee in a specific month.
     * Only counts APPROVED requests (not PENDING).
     * 
     * @param employeeId Employee ID
     * @param yearMonth Year and month
     * @return Total APPROVED overtime hours in the month
     */
    private double calculateApprovedOvertimeHours(Integer employeeId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get all overtime requests for employee in the month
        List<OvertimeRequest> requests = overtimeRequestRepository.findAll().stream()
                .filter(request -> 
                        request.getEmployee() != null &&
                        request.getEmployee().getEmployeeId().equals(employeeId) &&
                        request.getWorkDate() != null &&
                        !request.getWorkDate().isBefore(startDate) &&
                        !request.getWorkDate().isAfter(endDate)
                )
                .collect(java.util.stream.Collectors.toList());

        // Sum durations of APPROVED requests only
        return requests.stream()
                .filter(request -> request.getStatus() == RequestStatus.APPROVED)
                .mapToDouble(request -> calculateShiftDuration(request.getWorkShift()))
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
     * Get monthly overtime summary for an employee.
     * Useful for reporting and FE dashboard.
     * 
     * @param employeeId Employee ID
     * @param yearMonth Year and month
     * @return Monthly overtime summary
     */
    @Transactional(readOnly = true)
    public MonthlyOvertimeSummary getMonthlyOvertimeSummary(Integer employeeId, YearMonth yearMonth) {
        double approvedHours = calculateApprovedOvertimeHours(employeeId, yearMonth);
        double remainingHours = Math.max(0, MAX_MONTHLY_OVERTIME_HOURS - approvedHours);
        
        // Calculate pending hours (for informational purposes)
        double pendingHours = calculatePendingOvertimeHours(employeeId, yearMonth);
        
        return MonthlyOvertimeSummary.builder()
                .employeeId(employeeId)
                .yearMonth(yearMonth)
                .approvedOvertimeHours(approvedHours)
                .pendingOvertimeHours(pendingHours)
                .remainingOvertimeHours(remainingHours)
                .maxMonthlyOvertimeHours(MAX_MONTHLY_OVERTIME_HOURS)
                .isLimitReached(approvedHours >= MAX_MONTHLY_OVERTIME_HOURS)
                .warningThresholdReached(approvedHours >= MAX_MONTHLY_OVERTIME_HOURS * 0.8) // 80% threshold
                .build();
    }

    /**
     * Calculate PENDING overtime hours for informational display.
     * 
     * @param employeeId Employee ID
     * @param yearMonth Year and month
     * @return Total PENDING overtime hours
     */
    private double calculatePendingOvertimeHours(Integer employeeId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return overtimeRequestRepository.findAll().stream()
                .filter(request -> 
                        request.getEmployee() != null &&
                        request.getEmployee().getEmployeeId().equals(employeeId) &&
                        request.getWorkDate() != null &&
                        !request.getWorkDate().isBefore(startDate) &&
                        !request.getWorkDate().isAfter(endDate) &&
                        request.getStatus() == RequestStatus.PENDING
                )
                .mapToDouble(request -> calculateShiftDuration(request.getWorkShift()))
                .sum();
    }

    /**
     * Check if approving an overtime request would exceed the monthly limit.
     * 
     * @param employeeId Employee ID
     * @param workDate Work date
     * @param shiftDurationHours Duration of shift in hours
     * @return true if approving would NOT exceed limit
     */
    @Transactional(readOnly = true)
    public boolean canApproveOvertimeRequest(Integer employeeId, LocalDate workDate, double shiftDurationHours) {
        YearMonth yearMonth = YearMonth.from(workDate);
        double approvedHours = calculateApprovedOvertimeHours(employeeId, yearMonth);
        return (approvedHours + shiftDurationHours) <= MAX_MONTHLY_OVERTIME_HOURS;
    }

    /**
     * Get overtime statistics for multiple months (for reports).
     * 
     * @param employeeId Employee ID
     * @param startMonth Start month (inclusive)
     * @param endMonth End month (inclusive)
     * @return List of monthly summaries
     */
    @Transactional(readOnly = true)
    public List<MonthlyOvertimeSummary> getOvertimeStatistics(
            Integer employeeId,
            YearMonth startMonth,
            YearMonth endMonth) {
        
        List<MonthlyOvertimeSummary> summaries = new java.util.ArrayList<>();
        YearMonth current = startMonth;
        
        while (!current.isAfter(endMonth)) {
            summaries.add(getMonthlyOvertimeSummary(employeeId, current));
            current = current.plusMonths(1);
        }
        
        return summaries;
    }

    /**
     * DTO for monthly overtime summary.
     */
    @lombok.Builder
    @lombok.Data
    public static class MonthlyOvertimeSummary {
        private Integer employeeId;
        private YearMonth yearMonth;
        private double approvedOvertimeHours;
        private double pendingOvertimeHours;
        private double remainingOvertimeHours;
        private int maxMonthlyOvertimeHours;
        private boolean isLimitReached;
        private boolean warningThresholdReached; // 80% of limit
    }
}
