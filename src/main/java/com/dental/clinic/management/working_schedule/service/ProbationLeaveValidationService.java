package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.enums.EmploymentType;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import org.springframework.web.ErrorResponseException;
import org.springframework.http.ProblemDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business Rule #40: Probation Unpaid Leave Enforcement
 * 
 * Rule: Probationary employees cannot use PAID leave (Annual/Sick)
 * - Probationary employees (EmploymentType = PROBATION) can ONLY take UNPAID leave
 * - If they try to use PAID leave types (Annual/Sick), system blocks the request
 * - Unpaid leave is always allowed for probationary employees
 * - Regular employees (FULL_TIME/PART_TIME) can use any leave type
 * 
 * Implementation Strategy:
 * 1. validateProbationLeaveType() - Called when creating/updating TimeOffRequest
 * 2. Checks if employee is on probation (EmploymentType = PROBATION)
 * 3. Checks if leave type is PAID (Annual/Sick)
 * 4. Throws ErrorResponseException if probation + paid leave detected
 * 5. Allows UNPAID leave types always
 * 
 * Database Schema:
 * - employees.employment_type: ENUM('FULL_TIME', 'PART_TIME', 'PROBATION')
 * - time_off_types.is_paid: BOOLEAN (true for Annual/Sick, false for Unpaid)
 * 
 * Integration Points:
 * - TimeOffRequestService.createTimeOffRequest() - before save
 * - TimeOffRequestService.updateTimeOffRequest() - before update
 * 
 * Leave Type Classification:
 * - PAID: Annual Leave, Sick Leave, Maternity Leave, Paternity Leave
 * - UNPAID: Unpaid Leave, Personal Leave (unpaid), Emergency Leave (unpaid)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProbationLeaveValidationService {

    private final EmployeeRepository employeeRepository;

    /**
     * Validate that probationary employees only use unpaid leave.
     * 
     * @param employeeId Employee requesting leave
     * @param isPaidLeave Whether the leave type is paid (true) or unpaid (false)
     * @throws ErrorResponseException if probation employee tries to use paid leave
     */
    @Transactional(readOnly = true)
    public void validateProbationLeaveType(Integer employeeId, Boolean isPaidLeave) {
        
        // 1. Get employee and check employment type
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, 
                            "Không tìm thấy nhân viên với ID: " + employeeId);
                    pd.setTitle("Không Tìm Thấy Nhân Viên");
                    return new ErrorResponseException(HttpStatus.NOT_FOUND, pd, null);
                });

        EmploymentType employmentType = employee.getEmploymentType();
        
        // 2. If not on probation, allow any leave type
        if (employmentType != EmploymentType.PROBATION) {
            log.debug("Employee {} is {} - all leave types allowed", 
                    employeeId, employmentType);
            return;
        }

        // 3. If on probation and requesting paid leave, block it
        if (Boolean.TRUE.equals(isPaidLeave)) {
            log.warn("Probationary employee {} attempted to use paid leave", employeeId);
            String message = String.format(
                    "Nhân viên thử việc không được sử dụng nghỉ phép có lương. " +
                    "Nhân viên %s (ID: %d) đang trong thời gian thử việc và chỉ có thể xin nghỉ không lương. " +
                    "Vui lòng chọn loại nghỉ không lương hoặc đợi cho đến khi kết thúc thời gian thử việc.",
                    employee.getFullName(),
                    employeeId
            );
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message);
            problemDetail.setTitle("Hạn Chế Nghỉ Phép Thử Việc");
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, problemDetail, null);
        }

        // 4. Unpaid leave is allowed
        log.info("Probationary employee {} using unpaid leave - allowed", employeeId);
    }

    /**
     * Check if employee is on probation.
     * Useful for UI conditional rendering.
     * 
     * @param employeeId Employee ID
     * @return true if employee is on probation
     */
    @Transactional(readOnly = true)
    public boolean isEmployeeOnProbation(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .map(employee -> employee.getEmploymentType() == EmploymentType.PROBATION)
                .orElse(false);
    }

    /**
     * Get employee's available leave types based on employment status.
     * Used by FE to filter leave type dropdown.
     * 
     * @param employeeId Employee ID
     * @return LeaveTypeAvailability object
     */
    @Transactional(readOnly = true)
    public LeaveTypeAvailability getAvailableLeaveTypes(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, 
                            "Không tìm thấy nhân viên với ID: " + employeeId);
                    pd.setTitle("Không Tìm Thấy Nhân Viên");
                    return new ErrorResponseException(HttpStatus.NOT_FOUND, pd, null);
                });

        boolean isProbation = employee.getEmploymentType() == EmploymentType.PROBATION;
        
        return LeaveTypeAvailability.builder()
                .employeeId(employeeId)
                .employmentType(employee.getEmploymentType())
                .isProbation(isProbation)
                .canUsePaidLeave(!isProbation)
                .canUseUnpaidLeave(true)
                .message(isProbation 
                        ? "Probationary employees can only use unpaid leave types" 
                        : "All leave types available")
                .build();
    }

    /**
     * DTO for leave type availability response.
     */
    @lombok.Builder
    @lombok.Data
    public static class LeaveTypeAvailability {
        private Integer employeeId;
        private EmploymentType employmentType;
        private boolean isProbation;
        private boolean canUsePaidLeave;
        private boolean canUseUnpaidLeave;
        private String message;
    }
}
