package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.overtime.DuplicateOvertimeRequestException;
import com.dental.clinic.management.exception.overtime.InvalidStateTransitionException;
import com.dental.clinic.management.exception.overtime.OvertimeRequestNotFoundException;
import com.dental.clinic.management.exception.overtime.RelatedResourceNotFoundException;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.domain.OvertimeRequest;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.CreateOvertimeRequestDTO;
import com.dental.clinic.management.working_schedule.dto.request.UpdateOvertimeStatusDTO;
import com.dental.clinic.management.working_schedule.dto.response.OvertimeRequestDetailResponse;
import com.dental.clinic.management.working_schedule.dto.response.OvertimeRequestListResponse;
import com.dental.clinic.management.working_schedule.enums.RequestStatus;
import com.dental.clinic.management.working_schedule.enums.ShiftSource;
import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.dental.clinic.management.working_schedule.mapper.OvertimeRequestMapper;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.working_schedule.repository.OvertimeRequestRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing overtime requests.
 * Implements business logic, validation, and permission-based access control.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeRequestService {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final OvertimeRequestMapper overtimeRequestMapper;
    private final EmployeeShiftRepository employeeShiftRepository;

    /**
     * Get all overtime requests with pagination and optional filtering.
     * Access control:
     * - VIEW_OT_ALL: Can see all requests
     * - VIEW_OT_OWN: Can only see own requests
     *
     * @param status   optional status filter
     * @param pageable pagination information
     * @return page of overtime requests
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('VIEW_OT_ALL', 'VIEW_OT_OWN')")
    public Page<OvertimeRequestListResponse> getAllOvertimeRequests(RequestStatus status, Pageable pageable) {
        log.info("Fetching overtime requests with status: {}", status);

        boolean hasViewAll = SecurityUtil.hasCurrentUserPermission("VIEW_OT_ALL");

        if (hasViewAll) {
            // User can see all requests
            log.debug("User has VIEW_OT_ALL permission");
            Page<OvertimeRequest> requests = overtimeRequestRepository.findAllWithOptionalStatus(status, pageable);
            return requests.map(overtimeRequestMapper::toListResponse);
        } else {
            // User can only see their own requests
            log.debug("User has VIEW_OT_OWN permission");
            Employee currentEmployee = getCurrentEmployee();
            Page<OvertimeRequest> requests = overtimeRequestRepository.findByEmployeeIdAndStatus(
                    currentEmployee.getEmployeeId(), status, pageable);
            return requests.map(overtimeRequestMapper::toListResponse);
        }
    }

    /**
     * Get detailed information about a specific overtime request.
     * Access control:
     * - VIEW_OT_ALL: Can see any request
     * - VIEW_OT_OWN: Can only see own requests
     *
     * @param requestId the overtime request ID
     * @return overtime request details
     * @throws OvertimeRequestNotFoundException if request not found
     * @throws AccessDeniedException            if user doesn't have permission
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('VIEW_OT_ALL', 'VIEW_OT_OWN')")
    public OvertimeRequestDetailResponse getOvertimeRequestById(String requestId) {
        log.info("Fetching overtime request: {}", requestId);

        OvertimeRequest request = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new OvertimeRequestNotFoundException(requestId));

        // Permission check for VIEW_OT_OWN users
        if (!SecurityUtil.hasCurrentUserPermission("VIEW_OT_ALL")) {
            Employee currentEmployee = getCurrentEmployee();
            if (!request.isOwnedBy(currentEmployee.getEmployeeId()) &&
                    !request.isRequestedBy(currentEmployee.getEmployeeId())) {
                log.warn("User {} attempted to access overtime request {} without permission",
                        currentEmployee.getEmployeeId(), requestId);
                // Return 404 instead of 403 for security (don't reveal existence)
                throw new OvertimeRequestNotFoundException(requestId);
            }
        }

        return overtimeRequestMapper.toDetailResponse(request);
    }

    /**
     * Create a new overtime request.
     * Two modes:
     * 1. Employee creates for themselves: employeeId in DTO is null, auto-filled from JWT
     * 2. Admin creates for any employee: employeeId must be provided in DTO
     * 
     * Validates:
     * - Employee and WorkShift exist
     * - Work date is not in the past
     * - No conflicting requests (same employee, date, shift with PENDING or
     * APPROVED status)
     *
     * @param dto create overtime request DTO
     * @return created overtime request details
     * @throws RelatedResourceNotFoundException  if employee or shift not found
     * @throws DuplicateOvertimeRequestException if conflicting request exists
     * @throws IllegalArgumentException          if work date is in the past
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREATE_OT')")
    public OvertimeRequestDetailResponse createOvertimeRequest(CreateOvertimeRequestDTO dto) {
        // Determine target employee: use provided employeeId or current user's employeeId
        Integer targetEmployeeId;
        if (dto.getEmployeeId() != null) {
            // Admin mode: creating for specified employee
            targetEmployeeId = dto.getEmployeeId();
            log.info("Creating overtime request for employee {} (admin mode)", targetEmployeeId);
        } else {
            // Employee mode: creating for themselves
            Employee currentEmployee = getCurrentEmployee();
            targetEmployeeId = currentEmployee.getEmployeeId();
            log.info("Creating overtime request for employee {} (self-request mode)", targetEmployeeId);
        }

        log.info("Creating overtime request for employee {} on {} shift {}",
                targetEmployeeId, dto.getWorkDate(), dto.getWorkShiftId());

        // Validation 1: Verify employee exists
        Employee employee = employeeRepository.findById(targetEmployeeId)
                .orElseThrow(() -> new RelatedResourceNotFoundException("Nhân viên", targetEmployeeId));

        // Validation 2: Verify work shift exists
        WorkShift workShift = workShiftRepository.findById(dto.getWorkShiftId())
                .orElseThrow(() -> new RelatedResourceNotFoundException("Ca làm việc", dto.getWorkShiftId()));

        // Validation 3: Work date must not be in the past
        if (dto.getWorkDate().isBefore(LocalDate.now())) {
            log.warn("Cannot create overtime request for past date: {}", dto.getWorkDate());
            throw new IllegalArgumentException("Ngày làm việc không được là ngày trong quá khứ.");
        }

        // Validation 4: Check for conflicting requests
        List<RequestStatus> conflictStatuses = List.of(RequestStatus.PENDING, RequestStatus.APPROVED);
        boolean hasConflict = overtimeRequestRepository.existsConflictingRequest(
                targetEmployeeId, dto.getWorkDate(), dto.getWorkShiftId(), conflictStatuses);

        if (hasConflict) {
            log.warn("Conflicting overtime request exists for employee {} on {} shift {}",
                    targetEmployeeId, dto.getWorkDate(), dto.getWorkShiftId());
            throw new DuplicateOvertimeRequestException(targetEmployeeId, dto.getWorkDate(), dto.getWorkShiftId());
        }

        // Get current user as the requester
        Employee requestedBy = getCurrentEmployee();

        // Create overtime request (ID will be auto-generated via @PrePersist)
        OvertimeRequest overtimeRequest = new OvertimeRequest();
        overtimeRequest.setEmployee(employee);
        overtimeRequest.setRequestedBy(requestedBy);
        overtimeRequest.setWorkDate(dto.getWorkDate());
        overtimeRequest.setWorkShift(workShift);
        overtimeRequest.setReason(dto.getReason());
        overtimeRequest.setStatus(RequestStatus.PENDING);

        OvertimeRequest savedRequest = overtimeRequestRepository.save(overtimeRequest);
        log.info("Successfully created overtime request: {}", savedRequest.getRequestId());

        return overtimeRequestMapper.toDetailResponse(savedRequest);
    }

    /**
     * Update overtime request status (Approve, Reject, or Cancel).
     * Business rules:
     * - Can only update PENDING requests
     * - APPROVED: Requires APPROVE_OT permission
     * - REJECTED: Requires REJECT_OT permission, reason is required
     * - CANCELLED: Requires CANCEL_OT_OWN (for own requests) or CANCEL_OT_PENDING
     * (for managing), reason is required
     * - Auto-creates EmployeeShift when APPROVED (TODO: implement in Phase 6)
     *
     * @param requestId the overtime request ID
     * @param dto       update status DTO
     * @return updated overtime request details
     * @throws OvertimeRequestNotFoundException if request not found
     * @throws InvalidStateTransitionException  if request is not PENDING
     * @throws AccessDeniedException            if user doesn't have required
     *                                          permission
     * @throws IllegalArgumentException         if validation fails
     */
    @Transactional
    public OvertimeRequestDetailResponse updateOvertimeStatus(String requestId, UpdateOvertimeStatusDTO dto) {
        log.info("Updating overtime request {} to status {}", requestId, dto.getStatus());

        // Find the request
        OvertimeRequest request = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new OvertimeRequestNotFoundException(requestId));

        // Validation 1: Request must be PENDING
        if (!request.isPending()) {
            log.warn("Cannot update overtime request {} - current status is {}", requestId, request.getStatus());
            throw new InvalidStateTransitionException(requestId, request.getStatus(), dto.getStatus());
        }

        // Get current user
        Employee currentEmployee = getCurrentEmployee();

        // Process based on target status
        switch (dto.getStatus()) {
            case APPROVED -> handleApproval(request, currentEmployee);
            case REJECTED -> handleRejection(request, dto, currentEmployee);
            case CANCELLED -> handleCancellation(request, dto, currentEmployee);
            default -> throw new IllegalArgumentException("Trạng thái không hợp lệ: " + dto.getStatus());
        }

        OvertimeRequest updatedRequest = overtimeRequestRepository.save(request);
        log.info("Successfully updated overtime request {} to status {}", requestId, dto.getStatus());

        return overtimeRequestMapper.toDetailResponse(updatedRequest);
    }

    /**
     * Handle overtime request approval.
     */
    private void handleApproval(OvertimeRequest request, Employee approvedBy) {
        // Check permission
        if (!SecurityUtil.hasCurrentUserPermission("APPROVE_OT")) {
            log.warn("User {} does not have APPROVE_OT permission", approvedBy.getEmployeeId());
            throw new AccessDeniedException("Bạn không có quyền duyệt yêu cầu OT.");
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedAt(LocalDateTime.now());

        log.info("Overtime request {} approved by employee {}", request.getRequestId(), approvedBy.getEmployeeId());

        // Auto-create EmployeeShift record for overtime
        createEmployeeShiftFromOvertimeApproval(request);
    }

    /**
     * Handle overtime request rejection.
     */
    private void handleRejection(OvertimeRequest request, UpdateOvertimeStatusDTO dto, Employee rejectedBy) {
        // Check permission
        if (!SecurityUtil.hasCurrentUserPermission("REJECT_OT")) {
            log.warn("User {} does not have REJECT_OT permission", rejectedBy.getEmployeeId());
            throw new AccessDeniedException("Bạn không có quyền từ chối yêu cầu OT.");
        }

        // Validate reason is provided
        if (dto.getReason() == null || dto.getReason().isBlank()) {
            throw new IllegalArgumentException("Lý do từ chối là bắt buộc.");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectedReason(dto.getReason());
        request.setApprovedBy(rejectedBy); // Track who rejected it
        request.setApprovedAt(LocalDateTime.now());

        log.info("Overtime request {} rejected by employee {}", request.getRequestId(), rejectedBy.getEmployeeId());
    }

    /**
     * Handle overtime request cancellation.
     */
    private void handleCancellation(OvertimeRequest request, UpdateOvertimeStatusDTO dto, Employee cancelledBy) {
        // Validate reason is provided
        if (dto.getReason() == null || dto.getReason().isBlank()) {
            throw new IllegalArgumentException("Lý do hủy là bắt buộc.");
        }

        // Permission check: 
        // - CANCEL_OT_OWN: Can cancel if they are the employee (assigned to the OT) OR the creator (requestedBy)
        // - CANCEL_OT_PENDING: Can cancel any PENDING request (admin/manager)
        boolean isOwnerOrCreator = request.isOwnedBy(cancelledBy.getEmployeeId()) || 
                                   request.isRequestedBy(cancelledBy.getEmployeeId());
        boolean canCancelOwn = SecurityUtil.hasCurrentUserPermission("CANCEL_OT_OWN") && isOwnerOrCreator;
        boolean canCancelAny = SecurityUtil.hasCurrentUserPermission("CANCEL_OT_PENDING");

        if (!canCancelOwn && !canCancelAny) {
            log.warn("User {} does not have permission to cancel overtime request {}",
                    cancelledBy.getEmployeeId(), request.getRequestId());
            throw new AccessDeniedException("Bạn không có quyền hủy yêu cầu OT này.");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request.setCancellationReason(dto.getReason());

        log.info("Overtime request {} cancelled by employee {}", request.getRequestId(), cancelledBy.getEmployeeId());
    }

    /**
     * Get the current logged-in employee.
     *
     * @return current employee
     * @throws IllegalStateException if user not found or not an employee
     */
    private Employee getCurrentEmployee() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng đang đăng nhập."));

        return employeeRepository.findByAccount_Username(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Người dùng hiện tại không phải là nhân viên."));
    }

    /**
     * Auto-create EmployeeShift record when overtime is approved.
     * This creates the actual scheduled shift for the overtime work.
     *
     * @param request the approved overtime request
     */
    private void createEmployeeShiftFromOvertimeApproval(OvertimeRequest request) {
        try {
            // Check if shift already exists to avoid duplicates
            boolean exists = employeeShiftRepository.existsByEmployeeAndDateAndShift(
                    request.getEmployee().getEmployeeId(),
                    request.getWorkDate(),
                    request.getWorkShift().getWorkShiftId());

            if (exists) {
                log.warn("EmployeeShift already exists for employee {} on {} shift {}. Skipping creation.",
                        request.getEmployee().getEmployeeId(), request.getWorkDate(),
                        request.getWorkShift().getWorkShiftId());
                return;
            }

            // Create new employee shift
            EmployeeShift employeeShift = new EmployeeShift();
            employeeShift.setEmployee(request.getEmployee());
            employeeShift.setWorkDate(request.getWorkDate());
            employeeShift.setWorkShift(request.getWorkShift());
            employeeShift.setSource(ShiftSource.OVERTIME); // Mark as overtime source
            employeeShift.setStatus(ShiftStatus.SCHEDULED);
            employeeShift.setNotes(String.format("Tạo từ yêu cầu OT %s - %s",
                    request.getRequestId(), request.getReason()));

            employeeShiftRepository.save(employeeShift);

            log.info("Created EmployeeShift for overtime request {} - Employee {} on {} shift {}",
                    request.getRequestId(),
                    request.getEmployee().getEmployeeId(),
                    request.getWorkDate(),
                    request.getWorkShift().getWorkShiftId());

        } catch (Exception e) {
            log.error("Failed to create EmployeeShift for overtime request {}: {}",
                    request.getRequestId(), e.getMessage(), e);
            // Don't fail the entire transaction, just log the error
        }
    }
}
