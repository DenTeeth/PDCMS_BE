package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.*;
import com.dental.clinic.management.utils.IdGenerator;
import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.working_schedule.domain.EmployeeLeaveBalance;
import com.dental.clinic.management.working_schedule.domain.LeaveBalanceHistory;
import com.dental.clinic.management.working_schedule.domain.TimeOffRequest;
import com.dental.clinic.management.working_schedule.domain.TimeOffType;
import com.dental.clinic.management.working_schedule.dto.request.CreateTimeOffRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateTimeOffStatusRequest;
import com.dental.clinic.management.working_schedule.dto.response.TimeOffRequestResponse;
import com.dental.clinic.management.working_schedule.enums.BalanceChangeReason;
import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import com.dental.clinic.management.working_schedule.mapper.TimeOffRequestMapper;
import com.dental.clinic.management.working_schedule.repository.EmployeeLeaveBalanceRepository;
import com.dental.clinic.management.working_schedule.repository.LeaveBalanceHistoryRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffRequestRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for managing time-off requests
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TimeOffRequestService {

        private final TimeOffRequestRepository requestRepository;
        private final TimeOffTypeRepository typeRepository;
        private final EmployeeRepository employeeRepository;
        private final AccountRepository accountRepository;
        private final TimeOffRequestMapper requestMapper;
        private final IdGenerator idGenerator;
        private final EmployeeLeaveBalanceRepository balanceRepository;
        private final LeaveBalanceHistoryRepository historyRepository;

        /**
         * GET /api/v1/time-off-requests
         * Lấy danh sách yêu cầu nghỉ phép với phân trang và bộ lọc
         */
        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_TIMEOFF_ALL + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_TIMEOFF_OWN + "')")
        public Page<TimeOffRequestResponse> getAllRequests(
                        Integer employeeId,
                        TimeOffStatus status,
                        LocalDate startDate,
                        LocalDate endDate,
                        Pageable pageable) {

                log.debug("Request to get all time-off requests with filters");

                // LUỒNG 1: Admin hoặc người dùng có quyền xem tất cả
                if (SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) ||
                                SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.VIEW_TIMEOFF_ALL)) {

                        log.info("User has VIEW_TIMEOFF_ALL permission, fetching with filters");
                        return requestRepository.findWithFilters(employeeId, status, startDate, endDate, pageable)
                                        .map(requestMapper::toResponse);
                }
                // LUỒNG 2: Nhân viên chỉ có quyền VIEW_TIMEOFF_OWN
                else {
                        String username = SecurityUtil.getCurrentUserLogin()
                                        .orElseThrow(() -> new RuntimeException("User not authenticated"));

                        Integer currentEmployeeId = accountRepository.findOneByUsername(username)
                                        .map(account -> account.getEmployee().getEmployeeId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Employee not found for user: " + username));

                        log.info("User has VIEW_TIMEOFF_OWN permission, fetching for employee_id: {}",
                                        currentEmployeeId);

                        // Force filter by current employee, ignore employeeId parameter
                        return requestRepository
                                        .findWithFilters(currentEmployeeId, status, startDate, endDate, pageable)
                                        .map(requestMapper::toResponse);
                }
        }

        /**
         * GET /api/v1/time-off-requests/{request_id}
         * Xem chi tiết một yêu cầu nghỉ phép
         */
        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_TIMEOFF_ALL + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_TIMEOFF_OWN + "')")
        public TimeOffRequestResponse getRequestById(String requestId) {
                log.debug("Request to get time-off request: {}", requestId);

                // LUỒNG 1: Admin hoặc người dùng có quyền xem tất cả
                if (SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) ||
                                SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.VIEW_TIMEOFF_ALL)) {

                        log.info("User has VIEW_TIMEOFF_ALL permission, fetching request: {}", requestId);
                        return requestRepository.findByRequestId(requestId)
                                        .map(requestMapper::toResponse)
                                        .orElseThrow(() -> new TimeOffRequestNotFoundException(requestId));
                }
                // LUỒNG 2: Nhân viên chỉ có quyền VIEW_TIMEOFF_OWN (phải là chủ sở hữu)
                else {
                        String username = SecurityUtil.getCurrentUserLogin()
                                        .orElseThrow(() -> new RuntimeException("User not authenticated"));

                        Integer employeeId = accountRepository.findOneByUsername(username)
                                        .map(account -> account.getEmployee().getEmployeeId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Employee not found for user: " + username));

                        log.info("User has VIEW_TIMEOFF_OWN permission, fetching request: {} for employee_id: {}",
                                        requestId, employeeId);

                        return requestRepository.findByRequestIdAndEmployeeId(requestId, employeeId)
                                        .map(requestMapper::toResponse)
                                        .orElseThrow(() -> new TimeOffRequestNotFoundException(requestId,
                                                        "or you don't have permission to view it"));
                }
        }

        /**
         * POST /api/v1/time-off-requests
         * Tạo yêu cầu nghỉ phép mới
         */
        @PreAuthorize("hasAuthority('" + AuthoritiesConstants.CREATE_TIMEOFF + "')")
        @Transactional
        public TimeOffRequestResponse createRequest(CreateTimeOffRequest request) {
                log.debug("Request to create time-off request: {}", request);

                // 1. Validate employee exists
                employeeRepository.findById(request.getEmployeeId())
                                .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

                // 2. Validate time-off type exists and is active
                TimeOffType timeOffType = typeRepository.findByTypeIdAndIsActive(request.getTimeOffTypeId(), true)
                                .orElseThrow(() -> new TimeOffTypeNotFoundException(request.getTimeOffTypeId()));

                // 3. Validate date range
                if (request.getStartDate().isAfter(request.getEndDate())) {
                        throw new InvalidDateRangeException(
                                        "Ngày bắt đầu không được lớn hơn ngày kết thúc. " +
                                                        "Ngày bắt đầu: " + request.getStartDate() + ", Ngày kết thúc: "
                                                        + request.getEndDate());
                }

                // 3.5. Check leave balance CHỈ cho ANNUAL_LEAVE
                // Các loại khác (SICK_LEAVE, UNPAID_PERSONAL) không cần check balance
                if ("ANNUAL_LEAVE".equals(timeOffType.getTypeCode())) {
                        checkLeaveBalance(request.getEmployeeId(), request.getTimeOffTypeId(),
                                        request.getStartDate(), request.getEndDate(), request.getSlotId());
                }

                // 4. Business Rule: If half-day off (slot_id provided), start_date must equal
                // end_date
                if (request.getSlotId() != null && !request.getStartDate().equals(request.getEndDate())) {
                        throw new InvalidDateRangeException(
                                        "Khi nghỉ theo ca, ngày bắt đầu và kết thúc phải giống nhau. " +
                                                        "Ngày bắt đầu: " + request.getStartDate() + ", Ngày kết thúc: "
                                                        + request.getEndDate());
                }

                // 5. Check for conflicting requests
                boolean hasConflict = requestRepository.existsConflictingRequest(
                                request.getEmployeeId(),
                                request.getStartDate(),
                                request.getEndDate(),
                                request.getSlotId());

                if (hasConflict) {
                        List<TimeOffRequest> conflicts = requestRepository.findConflictingRequests(
                                        request.getEmployeeId(),
                                        request.getStartDate(),
                                        request.getEndDate(),
                                        request.getSlotId());

                        if (!conflicts.isEmpty()) {
                                TimeOffRequest conflict = conflicts.get(0);
                                throw new DuplicateTimeOffRequestException(
                                                String.format("Đã tồn tại một yêu cầu nghỉ phép trùng với khoảng thời gian này. "
                                                                +
                                                                "Request ID: %s, Từ ngày: %s, Đến ngày: %s, Trạng thái: %s",
                                                                conflict.getRequestId(),
                                                                conflict.getStartDate(),
                                                                conflict.getEndDate(),
                                                                conflict.getStatus()));
                        }
                }

                // 6. Get current user ID from token for requested_by
                String username = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));

                Integer requestedBy = accountRepository.findOneByUsername(username)
                                .map(account -> account.getEmployee().getEmployeeId())
                                .orElseThrow(() -> new RuntimeException("Employee not found for user: " + username));

                // 7. Generate request ID
                String requestId = idGenerator.generateId("TOR");
                log.info("Generated time-off request ID: {}", requestId);

                // 8. Create and save time-off request
                TimeOffRequest timeOffRequest = TimeOffRequest.builder()
                                .requestId(requestId)
                                .employeeId(request.getEmployeeId())
                                .timeOffTypeId(request.getTimeOffTypeId())
                                .startDate(request.getStartDate())
                                .endDate(request.getEndDate())
                                .slotId(request.getSlotId())
                                .reason(request.getReason())
                                .status(TimeOffStatus.PENDING)
                                .requestedBy(requestedBy)
                                .requestedAt(LocalDateTime.now())
                                .build();

                TimeOffRequest savedRequest = requestRepository.save(timeOffRequest);
                log.info("Created time-off request: {}", savedRequest.getRequestId());

                return requestMapper.toResponse(savedRequest);
        }

        /**
         * PATCH /api/v1/time-off-requests/{request_id}
         * Cập nhật trạng thái yêu cầu (Duyệt/Từ chối/Hủy)
         */
        @Transactional
        public TimeOffRequestResponse updateRequestStatus(String requestId, UpdateTimeOffStatusRequest request) {
                log.debug("Request to update time-off request status: {} to {}", requestId, request.getStatus());

                // 1. Find request
                TimeOffRequest timeOffRequest = requestRepository.findByRequestId(requestId)
                                .orElseThrow(() -> new TimeOffRequestNotFoundException(requestId));

                // 2. Check current status is PENDING
                if (timeOffRequest.getStatus() != TimeOffStatus.PENDING) {
                        throw new InvalidStateTransitionException(
                                        "Không thể cập nhật yêu cầu. Yêu cầu phải ở trạng thái PENDING. " +
                                                        "Trạng thái hiện tại: " + timeOffRequest.getStatus());
                }

                // 3. Handle different status updates
                switch (request.getStatus()) {
                        case APPROVED -> handleApproval(timeOffRequest);
                        case REJECTED -> handleRejection(timeOffRequest, request.getReason());
                        case CANCELLED -> handleCancellation(timeOffRequest, request.getReason());
                        default -> throw new IllegalArgumentException("Invalid status: " + request.getStatus());
                }

                // 4. Save and return
                TimeOffRequest updatedRequest = requestRepository.save(timeOffRequest);
                log.info("Updated time-off request {} to status: {}", requestId, request.getStatus());

                return requestMapper.toResponse(updatedRequest);
        }

        /**
         * Handle APPROVED status
         */
        private void handleApproval(TimeOffRequest timeOffRequest) {
                // Check permission
                if (!SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) &&
                                !SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.APPROVE_TIMEOFF)) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Bạn không có quyền thực hiện hành động này.");
                }

                // Get approver ID
                String username = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));

                Integer approvedBy = accountRepository.findOneByUsername(username)
                                .map(account -> account.getEmployee().getEmployeeId())
                                .orElseThrow(() -> new RuntimeException("Employee not found for user: " + username));

                // Update request
                timeOffRequest.setStatus(TimeOffStatus.APPROVED);
                timeOffRequest.setApprovedBy(approvedBy);
                timeOffRequest.setApprovedAt(LocalDateTime.now());

                // Deduct leave balance if applicable
                deductLeaveBalance(timeOffRequest);

                // TODO: Automatic action - update employee_shifts status to ON_LEAVE
                // This will be implemented when employee_shifts table is available
                log.info("TODO: Update employee_shifts for employee {} from {} to {} for slot {}",
                                timeOffRequest.getEmployeeId(),
                                timeOffRequest.getStartDate(),
                                timeOffRequest.getEndDate(),
                                timeOffRequest.getSlotId());
        }

        /**
         * Handle REJECTED status
         */
        private void handleRejection(TimeOffRequest timeOffRequest, String reason) {
                // Check permission
                if (!SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) &&
                                !SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.REJECT_TIMEOFF)) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Bạn không có quyền thực hiện hành động này.");
                }

                // Reason is required
                if (reason == null || reason.isBlank()) {
                        throw new IllegalArgumentException("Lý do từ chối là bắt buộc.");
                }

                // Get approver ID (person who rejected)
                String username = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));

                Integer approvedBy = accountRepository.findOneByUsername(username)
                                .map(account -> account.getEmployee().getEmployeeId())
                                .orElseThrow(() -> new RuntimeException("Employee not found for user: " + username));

                // Update request
                timeOffRequest.setStatus(TimeOffStatus.REJECTED);
                timeOffRequest.setApprovedBy(approvedBy);
                timeOffRequest.setApprovedAt(LocalDateTime.now());
                timeOffRequest.setRejectedReason(reason);
        }

        /**
         * Handle CANCELLED status
         */
        private void handleCancellation(TimeOffRequest timeOffRequest, String reason) {
                // Reason is required
                if (reason == null || reason.isBlank()) {
                        throw new IllegalArgumentException("Lý do hủy là bắt buộc.");
                }

                // Get current user
                String username = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));

                Integer currentEmployeeId = accountRepository.findOneByUsername(username)
                                .map(account -> account.getEmployee().getEmployeeId())
                                .orElseThrow(() -> new RuntimeException("Employee not found for user: " + username));

                // Check permission
                boolean isOwner = timeOffRequest.getEmployeeId().equals(currentEmployeeId);
                boolean hasOwnPermission = SecurityUtil
                                .hasCurrentUserPermission(AuthoritiesConstants.CANCEL_TIMEOFF_OWN);
                boolean hasPendingPermission = SecurityUtil
                                .hasCurrentUserPermission(AuthoritiesConstants.CANCEL_TIMEOFF_PENDING);
                boolean isAdmin = SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN);

                if (!isAdmin && !hasPendingPermission && !(isOwner && hasOwnPermission)) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Bạn không có quyền thực hiện hành động này.");
                }

                // Update request
                timeOffRequest.setStatus(TimeOffStatus.CANCELLED);
                timeOffRequest.setCancellationReason(reason);
        }

        /**
         * Check if employee has sufficient leave balance
         */
        private void checkLeaveBalance(Integer employeeId, String timeOffTypeId,
                        LocalDate startDate, LocalDate endDate, String slotId) {
                int currentYear = Year.now().getValue();

                // Find balance record for current year
                EmployeeLeaveBalance balance = balanceRepository
                                .findByEmployeeIdAndTimeOffTypeIdAndCycleYear(employeeId, timeOffTypeId, currentYear)
                                .orElseThrow(() -> new InsufficientLeaveBalanceException(
                                                String.format("Không tìm thấy số dư nghỉ phép cho nhân viên %d và loại nghỉ %s trong năm %d",
                                                                employeeId, timeOffTypeId, currentYear)));

                // Calculate days requested
                BigDecimal daysRequested = calculateDaysRequested(startDate, endDate, slotId);

                // Check if sufficient balance
                BigDecimal daysRemaining = balance.getTotalDaysAllowed().subtract(balance.getDaysTaken());

                if (daysRemaining.compareTo(daysRequested) < 0) {
                        throw new InsufficientLeaveBalanceException(
                                        String.format("Số dư nghỉ phép không đủ. Cần: %.1f ngày, Còn lại: %.1f ngày",
                                                        daysRequested.doubleValue(), daysRemaining.doubleValue()));
                }

                log.info("Balance check passed for employee {} - Requested: {} days, Remaining: {} days",
                                employeeId, daysRequested, daysRemaining);
        }

        /**
         * Calculate number of days requested
         */
        private BigDecimal calculateDaysRequested(LocalDate startDate, LocalDate endDate, String slotId) {
                // If slot_id is provided, it's half-day (0.5 days)
                if (slotId != null) {
                        return new BigDecimal("0.5");
                }

                // Otherwise, count full days between start and end (inclusive)
                long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                return BigDecimal.valueOf(daysBetween);
        }

        /**
         * Deduct leave balance when request is approved
         */
        private void deductLeaveBalance(TimeOffRequest timeOffRequest) {
                // Get time-off type to check type code
                TimeOffType timeOffType = typeRepository.findById(timeOffRequest.getTimeOffTypeId())
                                .orElseThrow(() -> new TimeOffTypeNotFoundException(timeOffRequest.getTimeOffTypeId()));

                // CHỈ trừ số dư cho ANNUAL_LEAVE
                // Các loại khác (SICK_LEAVE, UNPAID_PERSONAL) không trừ số dư
                if (!"ANNUAL_LEAVE".equals(timeOffType.getTypeCode())) {
                        log.info("Skipping balance deduction for type: {} ({})",
                                        timeOffType.getTypeCode(), timeOffType.getTypeName());
                        return;
                }

                int currentYear = Year.now().getValue();

                // Find balance record
                EmployeeLeaveBalance balance = balanceRepository
                                .findByEmployeeIdAndTimeOffTypeIdAndCycleYear(
                                                timeOffRequest.getEmployeeId(),
                                                timeOffRequest.getTimeOffTypeId(),
                                                currentYear)
                                .orElseThrow(() -> new InsufficientLeaveBalanceException(
                                                String.format("Không tìm thấy số dư nghỉ phép cho nhân viên %d và loại nghỉ %s trong năm %d",
                                                                timeOffRequest.getEmployeeId(),
                                                                timeOffRequest.getTimeOffTypeId(), currentYear)));

                // Calculate days to deduct
                BigDecimal daysToDeduct = calculateDaysRequested(
                                timeOffRequest.getStartDate(),
                                timeOffRequest.getEndDate(),
                                timeOffRequest.getSlotId());

                // Update days_taken
                balance.setDaysTaken(balance.getDaysTaken().add(daysToDeduct));
                balanceRepository.save(balance);

                // Create history record
                LeaveBalanceHistory history = LeaveBalanceHistory.builder()
                                .balanceId(balance.getBalanceId())
                                .changedBy(timeOffRequest.getApprovedBy())
                                .changeAmount(daysToDeduct.negate()) // Negative for deduction
                                .reason(BalanceChangeReason.APPROVED_REQUEST)
                                .sourceRequestId(timeOffRequest.getRequestId())
                                .notes(String.format("Trừ %.1f ngày nghỉ phép do yêu cầu %s được phê duyệt",
                                                daysToDeduct.doubleValue(), timeOffRequest.getRequestId()))
                                .createdAt(LocalDateTime.now())
                                .build();

                historyRepository.save(history);

                log.info("Deducted {} days from balance {} for request {}",
                                daysToDeduct, balance.getBalanceId(), timeOffRequest.getRequestId());
        }
}
