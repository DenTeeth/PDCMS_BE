package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.employee.EmployeeNotFoundException;
import com.dental.clinic.management.exception.time_off.DuplicateTimeOffRequestException;
import com.dental.clinic.management.exception.time_off.InsufficientLeaveBalanceException;
import com.dental.clinic.management.exception.time_off.ShiftNotFoundForLeaveException;
import com.dental.clinic.management.exception.time_off.TimeOffRequestNotFoundException;
import com.dental.clinic.management.exception.time_off.TimeOffTypeNotFoundException;
import com.dental.clinic.management.exception.validation.InvalidRequestException;
import com.dental.clinic.management.exception.validation.InvalidStateTransitionException;
import com.dental.clinic.management.exception.validation.InvalidDateRangeException;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.notification.service.NotificationService;
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
import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import com.dental.clinic.management.working_schedule.mapper.TimeOffRequestMapper;
import com.dental.clinic.management.working_schedule.repository.EmployeeLeaveBalanceRepository;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.working_schedule.repository.FixedShiftRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.LeaveBalanceHistoryRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeSlotRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffRequestRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffTypeRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
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
        private final EmployeeShiftRepository employeeShiftRepository;
        private final FixedShiftRegistrationRepository fixedShiftRegistrationRepository;
        private final PartTimeSlotRepository partTimeSlotRepository;
        private final WorkShiftRepository workShiftRepository;
        private final NotificationService notificationService;
        private final AppointmentRepository appointmentRepository;

        // ISSUE #53: Holiday Validation
        private final com.dental.clinic.management.utils.validation.HolidayValidator holidayValidator;

        @PersistenceContext
        private EntityManager entityManager;

        /**
         * GET /api/v1/time-off-requests
         * Lấy danh sách yêu cầu nghỉ phép với phân trang và bộ lọc
         */
        // ✅ PERMISSIONS: VIEW_LEAVE_ALL (Manager/Admin see all) OR VIEW_LEAVE_OWN (Employee see own)
        // ROLE_ADMIN & ROLE_MANAGER have VIEW_LEAVE_ALL permission
        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_LEAVE_ALL + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_LEAVE_OWN + "')")
        public Page<TimeOffRequestResponse> getAllRequests(
                        Integer employeeId,
                        TimeOffStatus status,
                        LocalDate startDate,
                        LocalDate endDate,
                        Pageable pageable) {

                log.debug("Yêu cầu lấy danh sách nghỉ phép với bộ lọc");

                // LUỒNG 1: Admin hoặc người dùng có quyền xem tất cả
                if (SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) ||
                                SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.VIEW_LEAVE_ALL)) {

                        log.info("Người dùng có quyền VIEW_LEAVE_ALL, bắt đầu truy vấn kèm bộ lọc");
                        return requestRepository.findWithFilters(employeeId, status, startDate, endDate, pageable)
                                        .map(requestMapper::toResponse);
                }
                // LUỒNG 2: Nhân viên chỉ có quyền VIEW_TIMEOFF_OWN
                else {
                        String username = SecurityUtil.getCurrentUserLogin()
                                        .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

                        Integer currentEmployeeId = accountRepository.findOneByUsername(username)
                                        .map(account -> {
                                                if (account.getEmployee() == null) {
                                                        throw new RuntimeException("Tài khoản " + username
                                                                        + " không có nhân viên liên kết.");
                                                }
                                                return account.getEmployee().getEmployeeId();
                                        })
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Không tìm thấy nhân viên cho người dùng: " + username));

                        log.info("Người dùng có quyền VIEW_TIMEOFF_OWN, truy vấn cho employee_id: {}",
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
        // ✅ PERMISSIONS: VIEW_LEAVE_ALL (Manager/Admin see any) OR VIEW_LEAVE_OWN (Employee see own)
        // ROLE_ADMIN & ROLE_MANAGER have VIEW_LEAVE_ALL permission
        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_LEAVE_ALL + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_LEAVE_OWN + "')")
        public TimeOffRequestResponse getRequestById(String requestId) {
                log.debug("Yêu cầu lấy chi tiết nghỉ phép: {}", requestId);

                // LUỒNG 1: Admin hoặc người dùng có quyền xem tất cả
                if (SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) ||
                                SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.VIEW_LEAVE_ALL)) {

                        log.info("Người dùng có quyền VIEW_LEAVE_ALL, lấy yêu cầu: {}", requestId);
                        return requestRepository.findByRequestId(requestId)
                                        .map(requestMapper::toResponse)
                                        .orElseThrow(() -> new TimeOffRequestNotFoundException(requestId));
                }
                // LUỒNG 2: Nhân viên chỉ có quyền VIEW_TIMEOFF_OWN (phải là chủ sở hữu)
                else {
                        String username = SecurityUtil.getCurrentUserLogin()
                                        .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

                        Integer employeeId = accountRepository.findOneByUsername(username)
                                        .map(account -> {
                                                if (account.getEmployee() == null) {
                                                        throw new RuntimeException("Tài khoản " + username
                                                                        + " không có nhân viên liên kết.");
                                                }
                                                return account.getEmployee().getEmployeeId();
                                        })
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Không tìm thấy nhân viên cho người dùng: " + username));

                        log.info("Người dùng có quyền VIEW_TIMEOFF_OWN, lấy yêu cầu: {} cho employee_id: {}",
                                        requestId, employeeId);

                        return requestRepository.findByRequestIdAndEmployeeId(requestId, employeeId)
                                        .map(requestMapper::toResponse)
                                        .orElseThrow(() -> new TimeOffRequestNotFoundException(requestId,
                                                        "hoặc bạn không có quyền xem yêu cầu này"));
                }
        }

        /**
         * POST /api/v1/time-off-requests
         * Tạo yêu cầu nghỉ phép mới
         */
        // ✅ PERMISSION: CREATE_TIME_OFF (Employee creates time-off request)
        // ROLE_ADMIN & ROLE_MANAGER have CREATE_TIME_OFF permission
        @PreAuthorize("hasAuthority('" + AuthoritiesConstants.CREATE_TIME_OFF + "')")
        @Transactional
        public TimeOffRequestResponse createRequest(CreateTimeOffRequest request) {
                log.debug("Yêu cầu tạo nghỉ phép: {}", request);

                // 1. Auto-fill employeeId from JWT if not provided (for employee self-requests)
                final Integer employeeId;
                if (request.getEmployeeId() != null) {
                        // Admin provided employeeId for another employee
                        employeeId = request.getEmployeeId();
                } else {
                        // Get current user's employeeId from JWT token
                        String username = SecurityUtil.getCurrentUserLogin()
                                        .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

                        employeeId = accountRepository.findOneByUsername(username)
                                        .map(account -> {
                                                if (account.getEmployee() == null) {
                                                        throw new RuntimeException(
                                                                        "Tài khoản " + username
                                                                                        + " không có nhân viên liên kết.");
                                                }
                                                return account.getEmployee().getEmployeeId();
                                        })
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Không tìm thấy nhân viên cho người dùng: " + username));

                        log.info("Tự động điền employeeId từ JWT: {}", employeeId);
                }

                // 2. Validate employee exists
                employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

                // 3. Validate time-off type exists and is active
                TimeOffType timeOffType = typeRepository.findByTypeIdAndIsActive(request.getTimeOffTypeId(), true)
                                .orElseThrow(() -> new TimeOffTypeNotFoundException(request.getTimeOffTypeId()));

                // Check if this is emergency leave (bypasses same-day and 24h notice rules)
                boolean isEmergency = "EMERGENCY_LEAVE".equalsIgnoreCase(timeOffType.getTypeCode());
                LocalDate today = LocalDate.now();

                // 3.1 Không cho phép xin nghỉ trong chính ngày (TRỪ Nghỉ khẩn cấp)
                if (!isEmergency && request.getStartDate().isEqual(today)) {
                        throw new InvalidRequestException(
                                        "TIME_OFF_SAME_DAY_NOT_ALLOWED",
                                        "Không thể xin nghỉ trong chính ngày hôm nay (" + today.toString() + "). " +
                                        "Vui lòng chọn từ ngày mai trở đi, hoặc chọn loại 'Nghỉ khẩn cấp' nếu thực sự cần nghỉ hôm nay.");
                }

                // 3.2 Yêu cầu báo trước 24h (TRỪ Nghỉ khẩn cấp)
                LocalDate noticeCutoffDate = today.plusDays(1); // xấp xỉ 24h kể từ hôm nay
                if (!isEmergency && request.getStartDate().isBefore(noticeCutoffDate)) {
                        throw new InvalidRequestException(
                                        "TIME_OFF_NOTICE_24H_REQUIRED",
                                        "Yêu cầu nghỉ phép cần báo trước tối thiểu 24 giờ. Vui lòng chọn ngày khác hoặc dùng loại Nghỉ khẩn cấp.");
                }

                // 3.3 Nghỉ khẩn cấp BẮT BUỘC phải có lý do
                if (isEmergency) {
                        String reason = request.getReason();
                        if (reason == null || reason.trim().isEmpty()) {
                                throw new InvalidRequestException(
                                                "TIME_OFF_EMERGENCY_REASON_REQUIRED",
                                                "Nghỉ khẩn cấp cần nhập lý do/giải trình.");
                        }
                }

                // 4. Validate date range
                if (request.getStartDate().isAfter(request.getEndDate())) {
                        throw new InvalidDateRangeException(
                                        "Ngày bắt đầu không được lớn hơn ngày kết thúc. " +
                                                        "Ngày bắt đầu: " + request.getStartDate() + ", Ngày kết thúc: "
                                                        + request.getEndDate());
                }

                // 5. ISSUE #53 - Validate date range does NOT include holidays
                holidayValidator.validateRangeNotIncludeHolidays(
                                request.getStartDate(),
                                request.getEndDate(),
                                "nghỉ phép");

                // 6. Check leave balance CHỈ cho ANNUAL_LEAVE
                // Các loại khác (SICK_LEAVE, UNPAID_PERSONAL) không cần check balance
                if ("ANNUAL_LEAVE".equals(timeOffType.getTypeCode())) {
                        checkLeaveBalance(employeeId, request.getTimeOffTypeId(),
                                        request.getStartDate(), request.getEndDate(), request.getWorkShiftId());
                }

                // 7. Business Rule: If half-day off (work_shift_id provided), start_date must
                // equal
                // end_date
                if (request.getWorkShiftId() != null && !request.getStartDate().equals(request.getEndDate())) {
                        throw new InvalidDateRangeException(
                                        "Khi nghỉ theo ca, ngày bắt đầu và kết thúc phải giống nhau. " +
                                                        "Ngày bắt đầu: " + request.getStartDate() + ", Ngày kết thúc: "
                                                        + request.getEndDate());
                }

                // 8. [V14 Hybrid] Kiểm tra nhân viên có lịch làm việc không
                // Query từ fixed_shift_registrations VÀ part_time_registrations
                if (request.getWorkShiftId() != null) {
                        // Nghỉ theo ca (half-day)
                        boolean hasShift = checkEmployeeHasShift(
                                        employeeId,
                                        request.getStartDate(),
                                        request.getWorkShiftId());

                        if (!hasShift) {
                                // Lấy shift name để hiển thị message rõ ràng hơn
                                String shiftName = workShiftRepository.findById(request.getWorkShiftId())
                                                .map(ws -> ws.getShiftName())
                                                .orElse(request.getWorkShiftId());

                                throw new ShiftNotFoundForLeaveException(
                                                employeeId,
                                                request.getStartDate().toString(),
                                                request.getWorkShiftId(),
                                                shiftName);
                        }
                } else {
                        // Nghỉ cả ngày (full-day) - kiểm tra tất cả các ngày
                        LocalDate currentDate = request.getStartDate();
                        boolean hasAnyShift = false;

                        while (!currentDate.isAfter(request.getEndDate())) {
                                if (checkEmployeeHasShift(employeeId, currentDate, null)) {
                                        hasAnyShift = true;
                                        break;
                                }
                                currentDate = currentDate.plusDays(1);
                        }

                        if (!hasAnyShift) {
                                throw new ShiftNotFoundForLeaveException(
                                                String.format("Nhân viên không có lịch làm việc vào bất kỳ ngày nào từ %s đến %s. Vui lòng kiểm tra lịch làm việc trước khi đăng ký nghỉ phép.",
                                                                request.getStartDate(),
                                                                request.getEndDate()));
                        }
                }

                // 9. Chặn xin nghỉ nếu bác sĩ đã có lịch hẹn trong khoảng ngày này (chỉ xét trạng thái đang hoạt động)
                List<AppointmentStatus> blockingStatuses = List.of(
                                AppointmentStatus.SCHEDULED,
                                AppointmentStatus.CHECKED_IN,
                                AppointmentStatus.IN_PROGRESS);

                boolean hasAppointments = appointmentRepository.existsByEmployeeIdAndDateRangeAndStatuses(
                                employeeId,
                                request.getStartDate(),
                                request.getEndDate(),
                                blockingStatuses);

                if (hasAppointments) {
                        throw new InvalidRequestException(
                                        "TIME_OFF_OVERLAP_APPOINTMENT",
                                        "Ngày nghỉ trùng với lịch hẹn hiện có. Vui lòng dời các lịch hẹn trước khi xin nghỉ.");
                }

                // 10. Check for conflicting requests
                boolean hasConflict = requestRepository.existsConflictingRequest(
                                employeeId,
                                request.getStartDate(),
                                request.getEndDate(),
                                request.getWorkShiftId());

                if (hasConflict) {
                        List<TimeOffRequest> conflicts = requestRepository.findConflictingRequests(
                                        employeeId,
                                        request.getStartDate(),
                                        request.getEndDate(),
                                        request.getWorkShiftId());

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

                // 11. Get current user ID from token for requested_by
                String username = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

                Integer requestedBy = accountRepository.findOneByUsername(username)
                                .map(account -> {
                                        if (account.getEmployee() == null) {
                                                throw new RuntimeException(
                                                                "Tài khoản " + username + " không có nhân viên liên kết.");
                                        }
                                        return account.getEmployee().getEmployeeId();
                                })
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên cho người dùng: " + username));

                // 7. Generate request ID
                String requestId = idGenerator.generateId("TOR");
                log.info("Sinh mã yêu cầu nghỉ phép: {}", requestId);

                // 8. Create and save time-off request
                TimeOffRequest timeOffRequest = TimeOffRequest.builder()
                                .requestId(requestId)
                                .employeeId(employeeId)
                                .timeOffTypeId(request.getTimeOffTypeId())
                                .startDate(request.getStartDate())
                                .endDate(request.getEndDate())
                                .workShiftId(request.getWorkShiftId())
                                .reason(request.getReason())
                                .status(TimeOffStatus.PENDING)
                                .requestedBy(requestedBy)
                                .requestedAt(LocalDateTime.now())
                                .build();

                TimeOffRequest savedRequest = requestRepository.save(timeOffRequest);
                requestRepository.flush(); // Force flush to DB
                entityManager.clear(); // Clear persistence context to force fresh fetch
                log.info("Tạo yêu cầu nghỉ phép thành công: {}", savedRequest.getRequestId());

                // Reload to fetch relationships (employee, requestedBy, approvedBy)
                TimeOffRequest reloadedRequest = requestRepository.findByRequestId(savedRequest.getRequestId())
                                .orElseThrow(() -> new TimeOffRequestNotFoundException(savedRequest.getRequestId()));

                // Send notification to all ADMIN users
                try {
                        Employee employee = employeeRepository.findById(employeeId)
                                        .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

                        String employeeName = employee.getFirstName() + " " + employee.getLastName();
                        notificationService.createTimeOffRequestNotification(
                                        employeeName,
                                        savedRequest.getRequestId(),
                                        request.getStartDate().toString(),
                                        request.getEndDate().toString());
                        log.info("Đã gửi thông báo nghỉ phép đến tất cả ADMIN");
                } catch (Exception e) {
                        log.error("Gửi thông báo nghỉ phép thất bại: {}", savedRequest.getRequestId(),
                                        e);
                        // Don't fail the request creation if notification fails
                }

                return requestMapper.toResponse(reloadedRequest);
        }

        /**
         * PATCH /api/v1/time-off-requests/{request_id}
         * Cập nhật trạng thái yêu cầu (Duyệt/Từ chối/Hủy)
         */
        @Transactional
        public TimeOffRequestResponse updateRequestStatus(String requestId, UpdateTimeOffStatusRequest request) {
                log.debug("Yêu cầu cập nhật trạng thái nghỉ phép: {} sang {}", requestId, request.getStatus());

                // 1. Find request
                TimeOffRequest timeOffRequest = requestRepository.findByRequestId(requestId)
                                .orElseThrow(() -> new TimeOffRequestNotFoundException(requestId));

                // 2. Check current status is PENDING
                if (timeOffRequest.getStatus() != TimeOffStatus.PENDING) {
                        throw new InvalidStateTransitionException(
                                        "Không thể cập nhật yêu cầu. Yêu cầu phải ở trạng thái PENDING. " +
                                                        "Trạng thái hiện tại: " + timeOffRequest.getStatus());
                }

                // 2.5. CONSTRAINT: Overdue requests can ONLY be cancelled, not approved/rejected
                LocalDate today = LocalDate.now();
                boolean isOverdue = timeOffRequest.getStartDate().isBefore(today);
                if (isOverdue && request.getStatus() != TimeOffStatus.CANCELLED) {
                        throw new IllegalStateException(
                                String.format(
                                        "Không thể %s yêu cầu đã quá hạn. " +
                                        "Ngày bắt đầu: %s, Ngày hiện tại: %s. " +
                                        "Yêu cầu quá hạn chỉ có thể HỦY với lý do 'quá hạn duyệt'.",
                                        request.getStatus() == TimeOffStatus.APPROVED ? "DUYỆT" : "TỪ CHỐI",
                                        timeOffRequest.getStartDate(), today));
                }

                // 3. Handle different status updates
                switch (request.getStatus()) {
                        case APPROVED -> handleApproval(timeOffRequest);
                        case REJECTED -> handleRejection(timeOffRequest, request.getReason());
                        case CANCELLED -> handleCancellation(timeOffRequest, request.getReason());
                        default ->
                                throw new IllegalArgumentException("Trạng thái không hợp lệ: " + request.getStatus());
                }

                // 4. Save and return
                TimeOffRequest updatedRequest = requestRepository.save(timeOffRequest);
                requestRepository.flush(); // Force flush to DB
                entityManager.clear(); // Clear persistence context to force fresh fetch
                log.info("Đã cập nhật yêu cầu nghỉ phép {} sang trạng thái: {}", requestId, request.getStatus());

                // Reload to fetch relationships (employee, requestedBy, approvedBy)
                TimeOffRequest reloadedRequest = requestRepository.findByRequestId(updatedRequest.getRequestId())
                                .orElseThrow(() -> new TimeOffRequestNotFoundException(updatedRequest.getRequestId()));

                return requestMapper.toResponse(reloadedRequest);
        }

        /**
         * Handle APPROVED status
         */
        private void handleApproval(TimeOffRequest timeOffRequest) {
                // Check permission
                if (!SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) &&
                                !SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.APPROVE_TIME_OFF)) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Bạn không có quyền thực hiện hành động này.");
                }

                // CONSTRAINT: Cannot approve if start_date has already passed
                // This prevents approving requests that should have been auto-cancelled
                LocalDate today = LocalDate.now();
                if (timeOffRequest.getStartDate().isBefore(today)) {
                        throw new IllegalStateException(
                                String.format(
                                        "Không thể duyệt yêu cầu nghỉ phép đã quá hạn. " +
                                        "Ngày bắt đầu: %s, Ngày hiện tại: %s. " +
                                        "Yêu cầu này nên được hủy tự động.",
                                        timeOffRequest.getStartDate(), today));
                }

                // Get approver ID
                String username = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

                Integer approvedBy = accountRepository.findOneByUsername(username)
                                .map(account -> {
                                        if (account.getEmployee() == null) {
                                                throw new RuntimeException(
                                                                "Tài khoản " + username + " không có nhân viên liên kết.");
                                        }
                                        return account.getEmployee().getEmployeeId();
                                })
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên cho người dùng: " + username));

                // Update request
                timeOffRequest.setStatus(TimeOffStatus.APPROVED);
                timeOffRequest.setApprovedBy(approvedBy);
                timeOffRequest.setApprovedAt(LocalDateTime.now());

                // Deduct leave balance if applicable (pass approvedBy explicitly)
                deductLeaveBalance(timeOffRequest, approvedBy);

                // Update employee_shifts status to ON_LEAVE
                updateEmployeeShiftsToOnLeave(timeOffRequest);
        }

        /**
         * Handle REJECTED status
         */
        private void handleRejection(TimeOffRequest timeOffRequest, String reason) {
                // Check permission
                if (!SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) &&
                                !SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.APPROVE_TIME_OFF)) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Bạn không có quyền thực hiện hành động này.");
                }

                // Reason is required
                if (reason == null || reason.isBlank()) {
                        throw new IllegalArgumentException("Lý do từ chối là bắt buộc.");
                }

                // Get approver ID (person who rejected)
                String username = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

                Integer approvedBy = accountRepository.findOneByUsername(username)
                                .map(account -> {
                                        if (account.getEmployee() == null) {
                                                throw new RuntimeException(
                                                                "Tài khoản " + username + " không có nhân viên liên kết.");
                                        }
                                        return account.getEmployee().getEmployeeId();
                                })
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên cho người dùng: " + username));

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
                                .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

                Integer currentEmployeeId = accountRepository.findOneByUsername(username)
                                .map(account -> {
                                        if (account.getEmployee() == null) {
                                                throw new RuntimeException(
                                                                "Tài khoản " + username + " không có nhân viên liên kết.");
                                        }
                                        return account.getEmployee().getEmployeeId();
                                })
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên cho người dùng: " + username));

                // Check permission
                boolean isOwner = timeOffRequest.getEmployeeId().equals(currentEmployeeId);
                boolean hasOwnPermission = SecurityUtil
                                .hasCurrentUserPermission(AuthoritiesConstants.CREATE_TIME_OFF);
                boolean hasPendingPermission = SecurityUtil
                                .hasCurrentUserPermission(AuthoritiesConstants.APPROVE_TIME_OFF);
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
                        LocalDate startDate, LocalDate endDate, String workShiftId) {
                int currentYear = Year.now().getValue();

                // Find balance record for current year
                EmployeeLeaveBalance balance;
                try {
                        balance = balanceRepository
                                        .findByEmployeeIdAndTimeOffTypeIdAndYear(employeeId, timeOffTypeId, currentYear)
                                        .orElseThrow(() -> new InvalidRequestException(
                                                        "BALANCE_NOT_FOUND",
                                                        String.format("Không tìm thấy số dư nghỉ phép cho nhân viên %d và loại nghỉ %s trong năm %d",
                                                                        employeeId, timeOffTypeId, currentYear)));
                } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
                        throw new InvalidRequestException(
                                        "DUPLICATE_BALANCE_RECORDS",
                                        String.format("Phát hiện dữ liệu bị trùng lặp cho nhân viên %d và loại nghỉ %s trong năm %d. Vui lòng liên hệ quản trị viên để xử lý.",
                                                        employeeId, timeOffTypeId, currentYear));
                }

                // Calculate days requested
                BigDecimal daysRequested = calculateDaysRequested(startDate, endDate, workShiftId);

                // Check if sufficient balance
                double daysRemaining = balance.getRemaining();

                if (daysRemaining < daysRequested.doubleValue()) {
                        throw new InsufficientLeaveBalanceException(daysRemaining, daysRequested.doubleValue());
                }

                log.info("Đủ số dư phép cho nhân viên {} - Yêu cầu: {} ngày, Còn lại: {} ngày",
                                employeeId, daysRequested, daysRemaining);
        }

        /**
         * Calculate number of days requested
         */
        private BigDecimal calculateDaysRequested(LocalDate startDate, LocalDate endDate, String workShiftId) {
                // If work_shift_id is provided, it's half-day (0.5 days)
                if (workShiftId != null) {
                        return new BigDecimal("0.5");
                }

                // Otherwise, count full days between start and end (inclusive)
                long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                return BigDecimal.valueOf(daysBetween);
        }

        /**
         * Deduct leave balance when request is approved
         * 
         * @param timeOffRequest the approved time-off request
         * @param approvedBy     the employee ID of the approver
         */
        private void deductLeaveBalance(TimeOffRequest timeOffRequest, Integer approvedBy) {
                // Get time-off type to check type code
                TimeOffType timeOffType = typeRepository.findById(timeOffRequest.getTimeOffTypeId())
                                .orElseThrow(() -> new TimeOffTypeNotFoundException(timeOffRequest.getTimeOffTypeId()));

                // CHỈ trừ số dư cho ANNUAL_LEAVE
                // Các loại khác (SICK_LEAVE, UNPAID_PERSONAL) không trừ số dư
                if (!"ANNUAL_LEAVE".equals(timeOffType.getTypeCode())) {
                        log.info("Không trừ phép cho loại: {} ({})",
                                        timeOffType.getTypeCode(), timeOffType.getTypeName());
                        return;
                }

                int currentYear = Year.now().getValue();

                // Find balance record
                EmployeeLeaveBalance balance;
                try {
                        balance = balanceRepository
                                        .findByEmployeeIdAndTimeOffTypeIdAndYear(
                                                        timeOffRequest.getEmployeeId(),
                                                        timeOffRequest.getTimeOffTypeId(),
                                                        currentYear)
                                        .orElseThrow(() -> new InvalidRequestException(
                                                        "BALANCE_NOT_FOUND",
                                                        String.format("Không tìm thấy số dư nghỉ phép cho nhân viên %d và loại nghỉ %s trong năm %d",
                                                                        timeOffRequest.getEmployeeId(),
                                                                        timeOffRequest.getTimeOffTypeId(),
                                                                        currentYear)));
                } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
                        throw new InvalidRequestException(
                                        "DUPLICATE_BALANCE_RECORDS",
                                        String.format("Phát hiện dữ liệu bị trùng lặp cho nhân viên %d và loại nghỉ %s trong năm %d. Vui lòng liên hệ quản trị viên để xử lý.",
                                                        timeOffRequest.getEmployeeId(),
                                                        timeOffRequest.getTimeOffTypeId(), currentYear));
                }

                // Calculate days to deduct
                BigDecimal daysToDeduct = calculateDaysRequested(
                                timeOffRequest.getStartDate(),
                                timeOffRequest.getEndDate(),
                                timeOffRequest.getWorkShiftId());

                // Update used days
                balance.setUsed(balance.getUsed() + daysToDeduct.doubleValue());
                balanceRepository.save(balance);

                // Create history record with proper entity relationships
                LeaveBalanceHistory history = LeaveBalanceHistory.builder()
                                .balance(balance) // Set the entity relationship
                                .changedBy(approvedBy) // Set the employee ID (NOT NULL column)
                                .changeAmount(daysToDeduct.negate().doubleValue()) // Negative for deduction
                                .reason(BalanceChangeReason.APPROVED_REQUEST)
                                .notes(String.format("Trừ %.1f ngày nghỉ phép do yêu cầu %s được phê duyệt",
                                                daysToDeduct.doubleValue(), timeOffRequest.getRequestId()))
                                .build();

                historyRepository.save(history);

                log.info("Đã trừ {} ngày khỏi số dư {} cho yêu cầu {} bởi người duyệt {}",
                                daysToDeduct, balance.getBalanceId(), timeOffRequest.getRequestId(), approvedBy);
        }

        /**
         * [DEPRECATED] Old hourly reminder job - now handled by RequestReminderNotificationJob at 16:00
         * Kept for reference only. This method is no longer scheduled.
         */
        // @Scheduled(cron = "0 5 * * * *")
        // public void notifyPendingTimeOffWithin24h() {
        //     // This job has been replaced by RequestReminderNotificationJob
        //     // which runs at 16:00 daily and handles weekend logic
        // }

        /**
         * Scheduled: Tự động hủy các yêu cầu PENDING tới ngày bắt đầu mà chưa được duyệt.
         * Chạy lúc 6h sáng mỗi ngày.
         * NOTE: RequestAutoCancellationJob also handles this centrally at 6:00 AM.
         * This method is kept as a backup.
         */
        @Scheduled(cron = "0 0 6 * * *")
        @Transactional
        public void autoCancelPendingOnStartDate() {
                LocalDate today = LocalDate.now();
                List<TimeOffRequest> pendingToday = requestRepository.findByStatusAndStartDate(TimeOffStatus.PENDING, today);

                for (TimeOffRequest req : pendingToday) {
                        req.setStatus(TimeOffStatus.CANCELLED);
                        req.setCancellationReason("Tự động hủy vì quá hạn duyệt (đến ngày bắt đầu)");
                        log.warn("Tự động hủy nghỉ phép {} cho nhân viên {} (bắt đầu: {})", req.getRequestId(),
                                        req.getEmployeeId(), req.getStartDate());
                }

                if (!pendingToday.isEmpty()) {
                        requestRepository.saveAll(pendingToday);
                }
        }

        /**
         * Update employee shifts to ON_LEAVE status when time-off is approved.
         *
         * @param timeOffRequest the approved time-off request
         */
        private void updateEmployeeShiftsToOnLeave(TimeOffRequest timeOffRequest) {
                try {
                        // Determine which shift to update based on workShiftId
                        // If workShiftId is null, update all shifts in the date range
                        // If workShiftId is specified, only update that specific shift
                        String shiftId = timeOffRequest.getWorkShiftId();

                        int updatedCount = employeeShiftRepository.updateShiftStatus(
                                        timeOffRequest.getEmployeeId(),
                                        timeOffRequest.getStartDate(),
                                        timeOffRequest.getEndDate(),
                                        shiftId, // null means all shifts
                                        ShiftStatus.ON_LEAVE);

                        log.info("Đã cập nhật {} ca làm sang ON_LEAVE cho nhân viên {} từ {} đến {} (work_shift: {})",
                                        updatedCount,
                                        timeOffRequest.getEmployeeId(),
                                        timeOffRequest.getStartDate(),
                                        timeOffRequest.getEndDate(),
                                        shiftId != null ? shiftId : "ALL");
                } catch (Exception e) {
                        log.error("Cập nhật ca làm sang ON_LEAVE thất bại cho yêu cầu {}: {}",
                                        timeOffRequest.getRequestId(), e.getMessage(), e);
                        // Don't fail the entire transaction, just log the error
                }
        }

        /**
         * [V14 Hybrid] Check if employee has a scheduled shift for the given date and
         * work shift.
         * This method queries BOTH fixed_shift_registrations AND
         * part_time_registrations.
         *
         * @param employeeId  the employee ID
         * @param date        the date to check
         * @param workShiftId the work shift ID (can be null to check any shift)
         * @return true if employee has a shift, false otherwise
         */
        private boolean checkEmployeeHasShift(Integer employeeId, LocalDate date, String workShiftId) {
                log.debug("Kiểm tra nhân viên {} có ca làm ngày {} work_shift_id: {}",
                                employeeId, date, workShiftId);

                // Get day of week (MONDAY, TUESDAY, etc.)
                String dayOfWeek = date.getDayOfWeek().name();

                // 1. Check FIXED_SHIFT_REGISTRATIONS (FULL_TIME & PART_TIME_FIXED)
                boolean hasFixedShift = fixedShiftRegistrationRepository.findActiveByEmployeeId(employeeId)
                                .stream()
                                .anyMatch(registration -> {
                                        // Check if date is within effective range
                                        if (date.isBefore(registration.getEffectiveFrom())) {
                                                return false;
                                        }
                                        if (registration.getEffectiveTo() != null
                                                        && date.isAfter(registration.getEffectiveTo())) {
                                                return false;
                                        }

                                        // Check if this day of week is in the registration
                                        boolean hasDayOfWeek = registration.getRegistrationDays()
                                                        .stream()
                                                        .anyMatch(day -> day.getDayOfWeek().equals(dayOfWeek));

                                        if (!hasDayOfWeek) {
                                                return false;
                                        }

                                        // If workShiftId is specified, check if it matches
                                        if (workShiftId != null) {
                                                return registration.getWorkShift().getWorkShiftId().equals(workShiftId);
                                        }

                                        return true;
                                });

                if (hasFixedShift) {
                        log.debug("Nhân viên {} có ca cố định ngày {} work_shift_id: {}",
                                        employeeId, date, workShiftId);
                        return true;
                }

                // 2. Check PART_TIME_REGISTRATIONS (PART_TIME_FLEX)
                // Query part_time_slots to find available slots for this employee
                boolean hasPartTimeShift = partTimeSlotRepository.findByDayOfWeekAndIsActiveTrue(dayOfWeek)
                                .stream()
                                .anyMatch(slot -> {
                                        // Check if workShiftId matches (if specified)
                                        if (workShiftId != null
                                                        && !slot.getWorkShift().getWorkShiftId().equals(workShiftId)) {
                                                return false;
                                        }

                                        // Check if employee has claimed this slot
                                        // This requires checking part_time_registrations table
                                        // We need to check if there's an active registration for this employee and slot
                                        // that covers the given date
                                        return slot.getRegistrations()
                                                        .stream()
                                                        .anyMatch(reg -> {
                                                                if (!reg.getEmployeeId().equals(employeeId)) {
                                                                        return false;
                                                                }
                                                                if (!reg.getIsActive()) {
                                                                        return false;
                                                                }
                                                                if (date.isBefore(reg.getEffectiveFrom())) {
                                                                        return false;
                                                                }
                                                                if (reg.getEffectiveTo() != null
                                                                                && date.isAfter(reg.getEffectiveTo())) {
                                                                        return false;
                                                                }
                                                                return true;
                                                        });
                                });

                if (hasPartTimeShift) {
                        log.debug("Nhân viên {} có ca PART_TIME_FLEX ngày {} work_shift_id: {}",
                                        employeeId, date, workShiftId);
                        return true;
                }

                log.debug("Nhân viên {} không có ca làm ngày {} work_shift_id: {}",
                                employeeId, date, workShiftId);
                return false;
        }
}
