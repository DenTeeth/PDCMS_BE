package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business Rules Service for Inactive Employee Management
 * 
 * Implements:
 * - Rule #25: Inactive employees automatically removed from future work schedules
 */
@Service
public class InactiveEmployeeCleanupService {

    private static final Logger log = LoggerFactory.getLogger(InactiveEmployeeCleanupService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeShiftRepository employeeShiftRepository;

    /**
     * Rule #25: Remove inactive employees from future shifts
     * 
     * Scheduled to run daily at midnight
     * Removes all future shifts for employees with isActive = false
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void removeInactiveEmployeesFromFutureShifts() {
        log.info("Starting inactive employee cleanup job...");

        LocalDate today = LocalDate.now();
        
        // Find all inactive employees
        List<Employee> inactiveEmployees = employeeRepository.findByIsActiveFalse();

        if (inactiveEmployees.isEmpty()) {
            log.info("No inactive employees found");
            return;
        }

        log.info("Found {} inactive employees. Checking for future shifts...", inactiveEmployees.size());

        CleanupReport report = new CleanupReport();

        // Remove future shifts for each inactive employee
        for (Employee employee : inactiveEmployees) {
            try {
                int removedCount = removeFutureShifts(employee.getEmployeeId(), today);
                
                if (removedCount > 0) {
                    log.info("Removed {} future shift(s) for inactive employee: {} ({} - {})", 
                        removedCount,
                        employee.getEmployeeId(),
                        employee.getFirstName(),
                        employee.getLastName());
                    
                    report.addEmployeeCleanup(
                        employee.getEmployeeId(),
                        employee.getFirstName() + " " + employee.getLastName(),
                        removedCount
                    );
                }
            } catch (Exception e) {
                log.error("Failed to remove shifts for employee {}: {}", 
                    employee.getEmployeeId(), e.getMessage(), e);
                report.addFailure(employee.getEmployeeId(), e.getMessage());
            }
        }

        log.info("Inactive employee cleanup completed. Total employees: {}, Total shifts removed: {}, Failures: {}", 
            report.getCleanedEmployeeCount(), 
            report.getTotalShiftsRemoved(),
            report.getFailureCount());
    }

    /**
     * Remove all future shifts for an employee
     * 
     * @param employeeId ID of the employee
     * @param fromDate Date from which to remove shifts (typically today)
     * @return Number of shifts removed
     */
    @Transactional
    public int removeFutureShifts(Integer employeeId, LocalDate fromDate) {
        if (employeeId == null || fromDate == null) {
            throw new IllegalArgumentException("EmployeeId and fromDate cannot be null");
        }

        // Get all future shifts for the employee
        List<EmployeeShift> futureShifts = employeeShiftRepository
            .findByEmployeeAndDateRange(
                employeeId, 
                fromDate, 
                fromDate.plusYears(10) // Far future date
            );

        if (futureShifts.isEmpty()) {
            return 0;
        }

        // Delete all future shifts
        employeeShiftRepository.deleteAll(futureShifts);

        log.debug("Deleted {} future shift(s) for employee {} starting from {}", 
            futureShifts.size(), employeeId, fromDate);

        return futureShifts.size();
    }

    /**
     * Manually remove future shifts for a specific inactive employee
     * Can be called when employee status changes to inactive
     * 
     * @param employeeId ID of the employee
     * @return Cleanup result
     */
    @Transactional
    public EmployeeCleanupResult removeShiftsForInactiveEmployee(Integer employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        if (Boolean.TRUE.equals(employee.getIsActive())) {
            return new EmployeeCleanupResult(employeeId, 0, false, "Employee is still active");
        }

        LocalDate today = LocalDate.now();
        int removedCount = removeFutureShifts(employeeId, today);

        return new EmployeeCleanupResult(
            employeeId, 
            removedCount, 
            true, 
            removedCount > 0 ? 
                String.format("Removed %d future shift(s)", removedCount) : 
                "No future shifts found"
        );
    }

    /**
     * Check if employee has future shifts
     * 
     * @param employeeId ID of the employee
     * @return true if employee has shifts scheduled in the future
     */
    public boolean hasFutureShifts(Integer employeeId) {
        if (employeeId == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        List<EmployeeShift> futureShifts = employeeShiftRepository
            .findByEmployeeAndDateRange(
                employeeId,
                today,
                today.plusYears(1)
            );

        return !futureShifts.isEmpty();
    }

    /**
     * Cleanup Report - tracks all cleanup operations
     */
    public static class CleanupReport {
        private List<EmployeeCleanup> cleanups = new ArrayList<>();
        private List<EmployeeFailure> failures = new ArrayList<>();

        public void addEmployeeCleanup(Integer employeeId, String employeeName, int shiftsRemoved) {
            cleanups.add(new EmployeeCleanup(employeeId, employeeName, shiftsRemoved));
        }

        public void addFailure(Integer employeeId, String error) {
            failures.add(new EmployeeFailure(employeeId, error));
        }

        public int getCleanedEmployeeCount() {
            return cleanups.size();
        }

        public int getTotalShiftsRemoved() {
            return cleanups.stream().mapToInt(c -> c.shiftsRemoved).sum();
        }

        public int getFailureCount() {
            return failures.size();
        }

        public List<EmployeeCleanup> getCleanups() { return cleanups; }
        public List<EmployeeFailure> getFailures() { return failures; }
    }

    /**
     * Single employee cleanup record
     */
    public static class EmployeeCleanup {
        private Integer employeeId;
        private String employeeName;
        private int shiftsRemoved;

        public EmployeeCleanup(Integer employeeId, String employeeName, int shiftsRemoved) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.shiftsRemoved = shiftsRemoved;
        }

        public Integer getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public int getShiftsRemoved() { return shiftsRemoved; }
    }

    /**
     * Cleanup failure record
     */
    public static class EmployeeFailure {
        private Integer employeeId;
        private String errorMessage;

        public EmployeeFailure(Integer employeeId, String errorMessage) {
            this.employeeId = employeeId;
            this.errorMessage = errorMessage;
        }

        public Integer getEmployeeId() { return employeeId; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Result of manual cleanup for a single employee
     */
    public static class EmployeeCleanupResult {
        private Integer employeeId;
        private int shiftsRemoved;
        private boolean success;
        private String message;

        public EmployeeCleanupResult(Integer employeeId, int shiftsRemoved, boolean success, String message) {
            this.employeeId = employeeId;
            this.shiftsRemoved = shiftsRemoved;
            this.success = success;
            this.message = message;
        }

        public Integer getEmployeeId() { return employeeId; }
        public int getShiftsRemoved() { return shiftsRemoved; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
