package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import org.springframework.web.ErrorResponseException;
import org.springframework.http.ProblemDetail;
import com.dental.clinic.management.role.domain.BaseRole;
import com.dental.clinic.management.working_schedule.domain.TimeOffRequest;
import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import com.dental.clinic.management.working_schedule.repository.TimeOffRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business Rule #35 & #36: Dentist Leave Conflict Validation
 * 
 * Rule #35: Restrict concurrent dentist absences
 * - Only 1 dentist can be absent at any time
 * - Prevents overlapping approved leave requests for dentists
 * - Applies to employees with BaseRole = 'employee' and Role = 'dentist'
 * 
 * Rule #36: Block leave on dates with approved leaves
 * - Cannot submit leave if another dentist already has approved leave for overlapping dates
 * - Checks for APPROVED status only (not PENDING or REJECTED)
 * 
 * Implementation Strategy:
 * 1. validateDentistLeaveConflict() - Called when creating/updating TimeOffRequest
 * 2. Checks if employee is a dentist (BaseRole='employee', Role='dentist')
 * 3. Queries existing APPROVED leave requests from other dentists
 * 4. Validates no date range overlap exists
 * 5. Throws ErrorResponseException if conflict detected
 * 
 * Integration Points:
 * - TimeOffRequestService.createTimeOffRequest() - before save
 * - TimeOffRequestService.updateTimeOffRequest() - before update
 * - TimeOffRequestService.approveTimeOffRequest() - before approval
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DentistLeaveConflictService {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Validate dentist leave conflict.
     * Ensures only 1 dentist can be absent at any time.
     * 
     * @param employeeId Employee requesting leave
     * @param startDate Leave start date
     * @param endDate Leave end date
     * @param excludeRequestId Request ID to exclude (null for new requests, ID for updates)
     * @throws ErrorResponseException if conflict detected
     */
    @Transactional(readOnly = true)
    public void validateDentistLeaveConflict(
            Integer employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String excludeRequestId) {

        // 1. Check if employee is a dentist
        if (!isDentist(employeeId)) {
            log.debug("Employee {} is not a dentist, skipping dentist leave conflict check", employeeId);
            return;
        }

        log.info("Validating dentist leave conflict for employee {} from {} to {}", 
                employeeId, startDate, endDate);

        // 2. Find conflicting APPROVED leave requests from other dentists
        List<TimeOffRequest> conflicts = findConflictingDentistLeaveRequests(
                employeeId, 
                startDate, 
                endDate, 
                excludeRequestId
        );

        // 3. If conflicts exist, throw exception
        if (!conflicts.isEmpty()) {
            TimeOffRequest conflict = conflicts.get(0);
            String conflictEmployeeName = conflict.getEmployee() != null 
                    ? conflict.getEmployee().getFullName() 
                    : "Employee #" + conflict.getEmployeeId();
            
            String message = String.format(
                    "Cannot submit leave request. Dentist %s already has approved leave from %s to %s. Only 1 dentist can be absent at a time.",
                    conflictEmployeeName,
                    conflict.getStartDate(),
                    conflict.getEndDate()
            );
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
            problemDetail.setTitle("Dentist Leave Conflict");
            throw new ErrorResponseException(HttpStatus.CONFLICT, problemDetail, null);
        }

        log.info("No dentist leave conflicts found for employee {}", employeeId);
    }

    /**
     * Check if employee is a dentist.
     * Dentist = BaseRole 'employee' AND Role 'dentist'.
     * 
     * @param employeeId Employee ID
     * @return true if employee is a dentist
     */
    private boolean isDentist(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElse(null);

        if (employee == null || employee.getAccount() == null) {
            return false;
        }

        // Check BaseRole = 'employee'
        BaseRole baseRole = employee.getAccount().getRole().getBaseRole();
        if (baseRole == null || !"employee".equalsIgnoreCase(baseRole.getBaseRoleName())) {
            return false;
        }

        // Check Role = 'dentist'
        String roleName = employee.getAccount().getRole().getRoleName();
        return "dentist".equalsIgnoreCase(roleName);
    }

    /**
     * Find conflicting dentist leave requests.
     * Returns APPROVED leave requests from OTHER dentists that overlap with the date range.
     * 
     * @param employeeId Employee requesting leave
     * @param startDate Leave start date
     * @param endDate Leave end date
     * @param excludeRequestId Request ID to exclude (for updates)
     * @return List of conflicting requests
     */
    private List<TimeOffRequest> findConflictingDentistLeaveRequests(
            Integer employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String excludeRequestId) {

        // Get all dentist employee IDs (excluding current employee)
        List<Integer> dentistIds = employeeRepository.findByIsActiveTrue().stream()
                .filter(e -> e.getEmployeeId() != null && !e.getEmployeeId().equals(employeeId))
                .filter(e -> isDentist(e.getEmployeeId()))
                .map(Employee::getEmployeeId)
                .collect(Collectors.toList());

        if (dentistIds.isEmpty()) {
            return List.of();
        }

        // Find overlapping APPROVED requests from other dentists
        return timeOffRequestRepository.findAll().stream()
                .filter(request -> {
                    // Exclude current request if updating
                    if (excludeRequestId != null && excludeRequestId.equals(request.getRequestId())) {
                        return false;
                    }

                    // Only check APPROVED requests
                    if (request.getStatus() != TimeOffStatus.APPROVED) {
                        return false;
                    }

                    // Only check requests from other dentists
                    if (!dentistIds.contains(request.getEmployeeId())) {
                        return false;
                    }

                    // Check date overlap
                    return isDateRangeOverlap(
                            startDate, endDate,
                            request.getStartDate(), request.getEndDate()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if two date ranges overlap.
     * 
     * @param start1 Start date of range 1
     * @param end1 End date of range 1
     * @param start2 Start date of range 2
     * @param end2 End date of range 2
     * @return true if ranges overlap
     */
    private boolean isDateRangeOverlap(
            LocalDate start1, LocalDate end1,
            LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    /**
     * Count approved dentist leave requests for a date range.
     * Useful for reporting and statistics.
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Count of approved dentist leaves in the date range
     */
    @Transactional(readOnly = true)
    public long countApprovedDentistLeaves(LocalDate startDate, LocalDate endDate) {
        // Get all dentist employee IDs
        List<Integer> dentistIds = employeeRepository.findByIsActiveTrue().stream()
                .filter(e -> isDentist(e.getEmployeeId()))
                .map(Employee::getEmployeeId)
                .collect(Collectors.toList());

        if (dentistIds.isEmpty()) {
            return 0;
        }

        // Count APPROVED requests from dentists in date range
        return timeOffRequestRepository.findAll().stream()
                .filter(request -> 
                        request.getStatus() == TimeOffStatus.APPROVED &&
                        dentistIds.contains(request.getEmployeeId()) &&
                        isDateRangeOverlap(startDate, endDate, request.getStartDate(), request.getEndDate())
                )
                .count();
    }
}
