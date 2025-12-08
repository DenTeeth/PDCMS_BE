package com.dental.clinic.management.working_schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Business Rule #38: Leave Balance Expiry Tracking
 * 
 * Rule: Annual leave balances expire at end of year (December 31)
 * - Unused annual leave does NOT carry over to next year
 * - Balance resets to 0 on January 1
 * - System should warn employees about expiring balances in Q4
 * - Scheduled job runs on Jan 1 to clear expired balances
 * 
 * Leave Balance Lifecycle:
 * 1. Grant: Balances granted at year start or hire date
 * 2. Usage: Deducted when leave requests are approved
 * 3. Warning: System warns in Oct/Nov/Dec about unused balance
 * 4. Expiry: Balance resets to 0 on Dec 31 (processed Jan 1)
 * 5. Reset: New balance granted for new year
 * 
 * Implementation Strategy:
 * 1. Scheduled Job: Runs Jan 1 at 00:01 to reset balances
 * 2. Warning Service: Notifies employees with high unused balance in Q4
 * 3. Expiry Tracking: Logs expired balances for HR reporting
 * 
 * Database Schema (assumed):
 * - leave_balances.employee_id: FK to employees
 * - leave_balances.year: INT (e.g., 2025)
 * - leave_balances.annual_leave_balance: DECIMAL (remaining days)
 * - leave_balances.expired_balance: DECIMAL (balance lost at year-end)
 * 
 * Integration Points:
 * - Scheduled job runs automatically on Jan 1
 * - TimeOffRequestService checks balance before approval
 * - Employee dashboard displays balance + expiry warning
 * 
 * Note: This implementation assumes leave_balances table exists.
 * If schema differs, adjust queries accordingly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceExpiryService {

    // TODO: Add LeaveBalanceRepository when available
    // private final LeaveBalanceRepository leaveBalanceRepository;
    // private final EmployeeRepository employeeRepository;

    /**
     * Scheduled job to expire annual leave balances.
     * Runs on January 1 at 00:01 every year.
     * 
     * Cron: 0 1 0 1 1 * = At 00:01 on January 1st
     */
    @Scheduled(cron = "0 1 0 1 1 *")
    @Transactional
    public void expireAnnualLeaveBalances() {
        log.info("Starting annual leave balance expiry job for year {}", Year.now().getValue());

        try {
            ExpiryReport report = processLeaveBalanceExpiry();
            
            log.info("Annual leave balance expiry completed: {} employees processed, {} balances expired, total {} days lost",
                    report.getEmployeesProcessed(),
                    report.getBalancesExpired(),
                    report.getTotalDaysExpired());

            // TODO: Send report to HR/Admin via email or notification
            
        } catch (Exception e) {
            log.error("Error during annual leave balance expiry", e);
            // Don't throw - we don't want to stop the scheduler
        }
    }

    /**
     * Process leave balance expiry for all employees.
     * Called by scheduled job or manually by admin.
     * 
     * @return Expiry report with statistics
     */
    @Transactional
    public ExpiryReport processLeaveBalanceExpiry() {
        int lastYear = Year.now().getValue() - 1;
        
        log.info("Processing leave balance expiry for year {}", lastYear);

        // TODO: Implement actual database operations
        // Example implementation:
        // 1. Get all employees with balances for last year
        // 2. For each employee:
        //    a. Get remaining annual leave balance
        //    b. If balance > 0, record as expired
        //    c. Reset balance to 0
        //    d. Grant new year's balance
        // 3. Log expiry report

        ExpiryReport report = ExpiryReport.builder()
                .processedYear(lastYear)
                .processedDate(LocalDate.now())
                .employeesProcessed(0) // Placeholder
                .balancesExpired(0) // Placeholder
                .totalDaysExpired(0.0) // Placeholder
                .employeeDetails(new ArrayList<>())
                .build();

        // TODO: Replace with actual implementation
        log.info("Leave balance expiry not yet implemented (placeholder executed)");

        return report;
    }

    /**
     * Check if employee has expiring leave balance.
     * Used for Q4 warnings (October, November, December).
     * 
     * @param employeeId Employee ID
     * @return true if employee has unused annual leave in Q4
     */
    @Transactional(readOnly = true)
    public boolean hasExpiringBalance(Integer employeeId) {
        LocalDate today = LocalDate.now();
        Month currentMonth = today.getMonth();
        
        // Only warn in Q4 (October, November, December)
        if (currentMonth.getValue() < 10) {
            return false;
        }

        // TODO: Implement actual balance check
        // return leaveBalanceRepository.getAnnualLeaveBalance(employeeId, Year.now().getValue()) > 0;
        
        return false; // Placeholder
    }

    /**
     * Get expiring balance warning for employee.
     * Used by FE to display warning banner in Q4.
     * 
     * @param employeeId Employee ID
     * @return Warning details (null if no warning needed)
     */
    @Transactional(readOnly = true)
    public ExpiryWarning getExpiryWarning(Integer employeeId) {
        LocalDate today = LocalDate.now();
        Month currentMonth = today.getMonth();
        
        // Only warn in Q4
        if (currentMonth.getValue() < 10) {
            return null;
        }

        // TODO: Get actual balance from database
        double remainingBalance = 0.0; // Placeholder

        if (remainingBalance <= 0) {
            return null;
        }

        int daysUntilExpiry = LocalDate.of(today.getYear(), 12, 31).getDayOfYear() - today.getDayOfYear();
        
        return ExpiryWarning.builder()
                .employeeId(employeeId)
                .currentYear(today.getYear())
                .remainingBalance(remainingBalance)
                .expiryDate(LocalDate.of(today.getYear(), 12, 31))
                .daysUntilExpiry(daysUntilExpiry)
                .severity(getSeverity(currentMonth))
                .message(String.format(
                        "You have %.1f days of annual leave remaining. " +
                        "This balance will expire on December 31, %d (%d days remaining). " +
                        "Please submit leave requests to use your remaining balance.",
                        remainingBalance,
                        today.getYear(),
                        daysUntilExpiry
                ))
                .build();
    }

    /**
     * Get warning severity based on month.
     * 
     * @param month Current month
     * @return Severity level
     */
    private String getSeverity(Month month) {
        return switch (month) {
            case OCTOBER -> "INFO";
            case NOVEMBER -> "WARNING";
            case DECEMBER -> "CRITICAL";
            default -> "INFO";
        };
    }

    /**
     * Get all employees with expiring balances.
     * Used for HR reporting and batch notifications.
     * 
     * @return List of employees with unused annual leave
     */
    @Transactional(readOnly = true)
    public List<ExpiryWarning> getAllExpiringBalances() {
        LocalDate today = LocalDate.now();
        
        // Only return data in Q4
        if (today.getMonthValue() < 10) {
            return List.of();
        }

        // TODO: Implement actual query
        // List<LeaveBalance> balances = leaveBalanceRepository.findByYearAndBalanceGreaterThan(
        //         Year.now().getValue(), 0.0);
        // 
        // return balances.stream()
        //         .map(balance -> getExpiryWarning(balance.getEmployeeId()))
        //         .filter(Objects::nonNull)
        //         .collect(Collectors.toList());

        return List.of(); // Placeholder
    }

    /**
     * DTO for expiry report.
     */
    @lombok.Data
    @lombok.Builder
    public static class ExpiryReport {
        private int processedYear;
        private LocalDate processedDate;
        private int employeesProcessed;
        private int balancesExpired;
        private double totalDaysExpired;
        private List<EmployeeExpiryDetail> employeeDetails;
    }

    /**
     * DTO for employee expiry detail.
     */
    @lombok.Data
    @lombok.Builder
    public static class EmployeeExpiryDetail {
        private Integer employeeId;
        private String employeeName;
        private double expiredBalance;
        private double newBalance;
    }

    /**
     * DTO for expiry warning.
     */
    @lombok.Data
    @lombok.Builder
    public static class ExpiryWarning {
        private Integer employeeId;
        private int currentYear;
        private double remainingBalance;
        private LocalDate expiryDate;
        private int daysUntilExpiry;
        private String severity; // INFO, WARNING, CRITICAL
        private String message;
    }
}
