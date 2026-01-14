package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.employee_shift.CannotCancelBatchShiftException;
import com.dental.clinic.management.exception.employee_shift.CannotCancelCompletedShiftException;
import com.dental.clinic.management.exception.employee_shift.ExceedsMaxHoursException;
import com.dental.clinic.management.exception.employee_shift.InvalidStatusTransitionException;
import com.dental.clinic.management.exception.employee_shift.PastDateNotAllowedException;
import com.dental.clinic.management.exception.employee_shift.RelatedResourceNotFoundException;
import com.dental.clinic.management.exception.employee_shift.ShiftFinalizedException;
import com.dental.clinic.management.exception.employee_shift.ShiftNotFoundException;
import com.dental.clinic.management.exception.employee_shift.TimeOverlapConflictException;
import com.dental.clinic.management.utils.IdGenerator;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.CreateShiftRequestDto;
import com.dental.clinic.management.working_schedule.dto.request.UpdateShiftRequestDto;
import com.dental.clinic.management.working_schedule.dto.response.EmployeeShiftResponseDto;
import com.dental.clinic.management.working_schedule.dto.response.ShiftSummaryResponseDto;
import com.dental.clinic.management.working_schedule.enums.ShiftSource;
import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.dental.clinic.management.working_schedule.mapper.EmployeeShiftMapper;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing employee shifts.
 * Handles shift creation, updates, cancellation, and calendar queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeShiftService {

    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeShiftMapper employeeShiftMapper;
    private final IdGenerator idGenerator;

    // ISSUE #53: Holiday Validation
    private final com.dental.clinic.management.utils.validation.HolidayValidator holidayValidator;

    /**
     * Get shift calendar for an employee with optional filters.
     *
     * @param employeeId           employee ID to view shifts for (null = all
     *                             employees if has permission)
     * @param startDate            start date filter
     * @param endDate              end date filter
     * @param status               optional status filter
     * @param currentEmployeeId    ID of the authenticated user
     * @param hasViewAllPermission whether user has VIEW_SCHEDULE_ALL permission
     * @param pageable             pagination parameters
     * @return paginated list of shifts
     */
    @PreAuthorize("hasAnyAuthority('VIEW_SCHEDULE_ALL', 'VIEW_SCHEDULE_OWN')")
    public Page<EmployeeShiftResponseDto> getShiftCalendar(
            Integer employeeId,
            LocalDate startDate,
            LocalDate endDate,
            ShiftStatus status,
            Integer currentEmployeeId,
            boolean hasViewAllPermission,
            Pageable pageable) {

        // Kiểm tra quyền: người dùng chỉ có thể xem ca của mình trừ khi có
        // quyền VIEW_SCHEDULE_ALL
        if (!hasViewAllPermission && !employeeId.equals(currentEmployeeId)) {
            throw new RelatedResourceNotFoundException("Bạn chỉ có thể xem lịch làm việc của chính mình");
        }

        // Truy vấn ca làm việc với bộ lọc
        List<EmployeeShift> allShifts;
        if (employeeId != null) {
            // Lấy ca làm việc của nhân viên cụ thể
            allShifts = employeeShiftRepository.findByEmployeeAndDateRange(
                    employeeId, startDate, endDate);
        } else {
            // Lấy tất cả ca làm việc trong khoảng thời gian (chỉ được phép với VIEW_SCHEDULE_ALL)
            if (!hasViewAllPermission) {
                throw new RelatedResourceNotFoundException("Bạn chỉ có thể xem lịch làm việc của chính mình");
            }
            allShifts = employeeShiftRepository.findByDateRangeAndStatus(startDate, endDate, null);
        }

        // Áp dụng bộ lọc trạng thái nếu được cung cấp
        if (status != null) {
            allShifts = allShifts.stream()
                    .filter(shift -> shift.getStatus() == status)
                    .collect(Collectors.toList());
        }

        // Áp dụng phân trang
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allShifts.size());
        List<EmployeeShift> pageShifts = allShifts.subList(start, end);

        // Convert to DTOs
        List<EmployeeShiftResponseDto> dtoList = pageShifts.stream()
                .map(employeeShiftMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, allShifts.size());
    }

    /**
     * Get shift summary for an employee grouped by date.
     *
     * @param employeeId employee ID (null = all employees if has permission)
     * @param startDate  start date
     * @param endDate    end date
     * @return list of daily shift summaries
     */
    @PreAuthorize("hasAuthority('VIEW_SCHEDULE_ALL')")
    public List<ShiftSummaryResponseDto> getShiftSummary(
            Integer employeeId,
            LocalDate startDate,
            LocalDate endDate) {

        // Kiểm tra nhân viên có tồn tại không nếu có employeeId
        if (employeeId != null && !employeeRepository.existsById(employeeId)) {
            throw new RelatedResourceNotFoundException("Nhân viên không tồn tại");
        }

        // Lấy tất cả ca làm việc trong khoảng thời gian
        List<EmployeeShift> shifts;
        if (employeeId != null) {
            // Lấy ca làm việc của nhân viên cụ thể
            shifts = employeeShiftRepository.findByEmployeeAndDateRange(
                    employeeId, startDate, endDate);
        } else {
            // Lấy tất cả ca làm việc (cho tất cả nhân viên)
            shifts = employeeShiftRepository.findByDateRangeAndStatus(startDate, endDate, null);
        }

        // Nhóm theo ngày
        Map<LocalDate, List<EmployeeShift>> shiftsByDate = shifts.stream()
                .collect(Collectors.groupingBy(EmployeeShift::getWorkDate));

        // Tạo tóm tắt cho từng ngày
        return shiftsByDate.entrySet().stream()
                .map(entry -> {
                    // Nhóm ca làm việc theo trạng thái
                    Map<ShiftStatus, Long> statusBreakdown = entry.getValue().stream()
                            .collect(Collectors.groupingBy(
                                    EmployeeShift::getStatus,
                                    Collectors.counting()));

                    return ShiftSummaryResponseDto.builder()
                            .workDate(entry.getKey())
                            .totalShifts((long) entry.getValue().size())
                            .statusBreakdown(statusBreakdown)
                            .build();
                })
                .sorted(Comparator.comparing(ShiftSummaryResponseDto::getWorkDate))
                .collect(Collectors.toList());
    }

    /**
     * Get detailed information about a specific shift.
     *
     * @param employeeShiftId      shift ID
     * @param currentEmployeeId    ID of the authenticated user
     * @param hasViewAllPermission whether user has VIEW_SCHEDULE_ALL permission
     * @return shift details
     */
    @PreAuthorize("hasAnyAuthority('VIEW_SCHEDULE_ALL', 'VIEW_SCHEDULE_OWN')")
    public EmployeeShiftResponseDto getShiftDetail(
            String employeeShiftId,
            Integer currentEmployeeId,
            boolean hasViewAllPermission) {

        // Tìm ca làm việc
        EmployeeShift shift = employeeShiftRepository.findById(employeeShiftId)
                .orElseThrow(
                        () -> new ShiftNotFoundException("Không tìm thấy ca làm việc, hoặc bạn không có quyền xem."));

        // Kiểm tra quyền: người dùng chỉ có thể xem ca của mình trừ khi có
        // quyền VIEW_SCHEDULE_ALL
        if (!hasViewAllPermission && !shift.getEmployee().getEmployeeId().equals(currentEmployeeId)) {
            throw new ShiftNotFoundException("Không tìm thấy ca làm việc, hoặc bạn không có quyền xem.");
        }

        return employeeShiftMapper.toResponseDto(shift);
    }

    /**
     * Create a manual shift entry.
     *
     * @param request   shift creation request
     * @param createdBy ID of the user creating the shift
     * @return created shift details
     */
    @PreAuthorize("hasAuthority('MANAGE_FIXED_REGISTRATIONS')")
    @Transactional
    public EmployeeShiftResponseDto createManualShift(CreateShiftRequestDto request, Integer createdBy) {

        // Kiểm tra nhân viên có tồn tại không
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RelatedResourceNotFoundException("Nhân viên không tồn tại"));

        // Kiểm tra ca làm việc có tồn tại không
        WorkShift workShift = workShiftRepository.findById(request.getWorkShiftId())
                .orElseThrow(() -> new RelatedResourceNotFoundException("Ca làm việc không tồn tại"));

        // Kiểm tra tính hợp lệ khi tạo ca (kiểm tra xung đột)
        validateShiftCreation(employee.getEmployeeId(), request.getWorkDate(), workShift.getWorkShiftId());

        // Tạo ID ca làm việc
        String employeeShiftId = idGenerator.generateId("EMS");

        // Tạo ca làm việc mới
        EmployeeShift newShift = new EmployeeShift();
        newShift.setEmployeeShiftId(employeeShiftId);
        newShift.setEmployee(employee);
        newShift.setWorkShift(workShift);
        newShift.setWorkDate(request.getWorkDate());
        newShift.setStatus(ShiftStatus.SCHEDULED);
        newShift.setSource(ShiftSource.MANUAL_ENTRY);
        newShift.setIsOvertime(false);
        newShift.setCreatedBy(createdBy);
        newShift.setNotes(request.getNotes());

        // Lưu ca làm việc
        EmployeeShift savedShift = employeeShiftRepository.save(newShift);
        log.info("Created manual shift: {} for employee: {}", employeeShiftId, employee.getEmployeeId());

        return employeeShiftMapper.toResponseDto(savedShift);
    }

    /**
     * Update an existing shift.
     *
     * @param employeeShiftId shift ID to update
     * @param request         update request
     * @return updated shift details
     */
    @PreAuthorize("hasAuthority('MANAGE_FIXED_REGISTRATIONS')")
    @Transactional
    public EmployeeShiftResponseDto updateShift(String employeeShiftId, UpdateShiftRequestDto request) {

        // Tìm ca làm việc
        EmployeeShift shift = employeeShiftRepository.findById(employeeShiftId)
                .orElseThrow(() -> new ShiftNotFoundException("Không tìm thấy ca làm việc"));

        // Kiểm tra ca đã được hoàn thành chưa (không thể cập nhật ca đã hoàn thành hoặc đã hủy)
        if (shift.getStatus() == ShiftStatus.COMPLETED || shift.getStatus() == ShiftStatus.CANCELLED) {
            throw new ShiftFinalizedException("Không thể cập nhật ca làm đã hoàn thành hoặc đã bị hủy.");
        }

        // Cập nhật trạng thái nếu được cung cấp và hợp lệ
        if (request.getStatus() != null) {
            // Không thể cập nhật thủ công trạng thái thành ON_LEAVE
            if (request.getStatus() == ShiftStatus.ON_LEAVE) {
                throw new InvalidStatusTransitionException(
                        "Không thể cập nhật thủ công trạng thái thành ON_LEAVE. Vui lòng tạo yêu cầu nghỉ phép.");
            }
            validateStatusTransition(shift.getStatus(), request.getStatus());
            shift.setStatus(request.getStatus());
        }

        // Cập nhật ghi chú nếu được cung cấp
        if (request.getNotes() != null) {
            shift.setNotes(request.getNotes());
        }

        // Lưu thay đổi
        EmployeeShift updatedShift = employeeShiftRepository.save(shift);
        log.info("Updated shift: {} to status: {}", employeeShiftId, updatedShift.getStatus());

        return employeeShiftMapper.toResponseDto(updatedShift);
    }

    /**
     * Cancel a shift.
     *
     * @param employeeShiftId shift ID to cancel
     */
    @PreAuthorize("hasAuthority('MANAGE_FIXED_REGISTRATIONS')")
    @Transactional
    public void cancelShift(String employeeShiftId) {

        // Tìm ca làm việc
        EmployeeShift shift = employeeShiftRepository.findById(employeeShiftId)
                .orElseThrow(() -> new ShiftNotFoundException("Không tìm thấy ca làm việc"));

        // Kiểm tra việc hủy có được phép không
        if (shift.getStatus() == ShiftStatus.COMPLETED) {
            throw new CannotCancelCompletedShiftException("Không thể hủy ca làm đã được hoàn thành.");
        }

        // Kiểm tra đã bị hủy chưa (tính bất biến)
        if (shift.getStatus() == ShiftStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Ca làm việc này đã bị hủy trước đó.");
        }

        if (shift.getSource() == ShiftSource.BATCH_JOB || shift.getSource() == ShiftSource.REGISTRATION_JOB) {
            throw new CannotCancelBatchShiftException(
                    "Không thể hủy ca làm mặc định của nhân viên Full-time. Vui lòng tạo yêu cầu nghỉ phép.");
        }

        // Hủy ca làm việc
        shift.setStatus(ShiftStatus.CANCELLED);
        employeeShiftRepository.save(shift);
        log.info("Cancelled shift: {}", employeeShiftId);
    }

    /**
     * Validate that a shift can be created without conflicts.
     *
     * @param employeeId  employee ID
     * @param workDate    work date
     * @param workShiftId work shift ID
     */
    private void validateShiftCreation(Integer employeeId, LocalDate workDate, String workShiftId) {

        // Kiểm tra ngày làm việc có trong quá khứ không
        if (workDate.isBefore(LocalDate.now())) {
            throw new PastDateNotAllowedException(workDate);
        }

        // ISSUE #53: Kiểm tra ngày làm việc KHÔNG phải là ngày nghỉ lễ
        holidayValidator.validateNotHoliday(workDate, "ca làm việc");

        // Lấy chi tiết ca làm việc mới
        WorkShift newWorkShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new RelatedResourceNotFoundException("Ca làm việc không tồn tại"));

        // Lấy tất cả ca làm việc đang hoạt động của nhân viên này trong ngày
        List<EmployeeShift> existingShifts = employeeShiftRepository.findActiveShiftsByEmployeeAndDate(
                employeeId, workDate);

        // Tính thời lượng ca mới tính bằng phút
        long newShiftMinutes = Duration.between(newWorkShift.getStartTime(), newWorkShift.getEndTime()).toMinutes();

        // Tính tổng số giờ hiện có
        long existingTotalMinutes = 0;
        for (EmployeeShift existingShift : existingShifts) {
            WorkShift existingWorkShift = existingShift.getWorkShift();

            // Kiểm tra các khoảng thời gian có trùng lặp không
            if (isTimeOverlap(newWorkShift.getStartTime(), newWorkShift.getEndTime(),
                    existingWorkShift.getStartTime(), existingWorkShift.getEndTime())) {
                throw new TimeOverlapConflictException(
                        newWorkShift.getStartTime(), newWorkShift.getEndTime(),
                        existingWorkShift.getStartTime(), existingWorkShift.getEndTime());
            }

            // Tính thời lượng ca hiện có
            long shiftMinutes = Duration.between(existingWorkShift.getStartTime(),
                    existingWorkShift.getEndTime()).toMinutes();
            existingTotalMinutes += shiftMinutes;
        }

        // Kiểm tra tổng số giờ có vượt quá giới hạn 8 giờ không
        long totalMinutes = newShiftMinutes + existingTotalMinutes;
        long totalHours = totalMinutes / 60;

        if (totalHours > 8) {
            throw new ExceedsMaxHoursException(workDate, (int) totalHours);
        }
    }

    /**
     * Check if two time ranges overlap.
     *
     * @param start1 start time of first range
     * @param end1   end time of first range
     * @param start2 start time of second range
     * @param end2   end time of second range
     * @return true if ranges overlap
     */
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Two time ranges overlap if:
        // - start1 < end2 AND end1 > start2
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Validate status transition is allowed.
     *
     * @param currentStatus current shift status
     * @param newStatus     new status to transition to
     */
    private void validateStatusTransition(ShiftStatus currentStatus, ShiftStatus newStatus) {

        // Cannot change completed shifts
        if (currentStatus == ShiftStatus.COMPLETED) {
            throw new InvalidStatusTransitionException(
                    "Không thể thay đổi trạng thái của ca làm việc đã hoàn thành");
        }

        // Cannot change cancelled shifts
        if (currentStatus == ShiftStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Không thể thay đổi trạng thái của ca làm việc đã hủy");
        }
    }

    /**
     * Create employee shifts automatically from a registration (supports both
     * PART_TIME and FIXED types).
     * This is the generic method that handles shift generation for all registration
     * types.
     *
     * @param employeeId           Employee ID
     * @param workShiftId          Work shift ID
     * @param effectiveFrom        Registration start date
     * @param effectiveTo          Registration end date (nullable for indefinite)
     * @param daysOfWeek           List of day numbers (1=Monday, 2=Tuesday, ...,
     *                             7=Sunday)
     * @param source               Registration source type (PART_TIME_FLEX,
     *                             FULL_TIME, PART_TIME_FIXED)
     * @param sourceRegistrationId Original registration ID for tracking
     * @param createdBy            User who created/approved the registration
     * @return List of created shifts
     */
    @Transactional
    public List<EmployeeShift> createShiftsForRegistration(
            Integer employeeId,
            String workShiftId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            List<Integer> daysOfWeek,
            String source,
            Long sourceRegistrationId,
            Integer createdBy) {

        log.info(" Creating shifts for employee {} from {} to {} (source: {})",
                employeeId, effectiveFrom, effectiveTo, source);

        // Validate employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RelatedResourceNotFoundException("Nhân viên không tồn tại"));

        // Validate work shift exists
        WorkShift workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new RelatedResourceNotFoundException("Ca làm việc không tồn tại"));

        // Validate daysOfWeek
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            throw new IllegalArgumentException("Danh sách ngày làm việc không được rỗng");
        }

        // Calculate working days based on effectiveFrom, effectiveTo, and daysOfWeek
        List<LocalDate> workingDays = calculateWorkingDays(effectiveFrom, effectiveTo, daysOfWeek);

        // ISSUE #53: Filter out holidays
        List<LocalDate> nonHolidayWorkingDays = holidayValidator.filterOutHolidays(workingDays);
        int holidaysFiltered = workingDays.size() - nonHolidayWorkingDays.size();

        if (holidaysFiltered > 0) {
            log.info("🎊 Filtered out {} holidays from {} total working days",
                    holidaysFiltered, workingDays.size());
        }

        log.info("📅 Calculated {} working days from date range (after filtering holidays)",
                nonHolidayWorkingDays.size());

        List<EmployeeShift> createdShifts = new java.util.ArrayList<>();
        int skippedCount = 0;

        for (LocalDate workDate : nonHolidayWorkingDays) {
            // Check if shift already exists (avoid duplicates)
            boolean exists = employeeShiftRepository.existsByEmployeeAndDateAndShift(
                    employeeId, workDate, workShiftId);

            if (exists) {
                log.debug("⏭ Shift already exists for employee {} on {} - skipping", employeeId, workDate);
                skippedCount++;
                continue;
            }

            // Generate shift ID with format: EMS + YYMMDD + SEQ
            String employeeShiftId = idGenerator.generateId("EMS");

            // Create new shift
            EmployeeShift newShift = new EmployeeShift();
            newShift.setEmployeeShiftId(employeeShiftId);
            newShift.setEmployee(employee);
            newShift.setWorkShift(workShift);
            newShift.setWorkDate(workDate);
            newShift.setStatus(ShiftStatus.SCHEDULED);
            newShift.setSource(ShiftSource.REGISTRATION_JOB);
            newShift.setSourceRegistrationId(sourceRegistrationId); // Link to registration
            newShift.setIsOvertime(false);
            newShift.setCreatedBy(createdBy);
            newShift.setNotes(String.format("Tạo tự động từ %s registration #%d", source, sourceRegistrationId));

            EmployeeShift savedShift = employeeShiftRepository.save(newShift);
            createdShifts.add(savedShift);

            log.debug(" Created shift {} for date {}", employeeShiftId, workDate);
        }

        log.info(" Shift generation complete: {} created, {} skipped for employee {}",
                createdShifts.size(), skippedCount, employeeId);
        return createdShifts;
    }

    /**
     * Calculate working days from date range and days of week.
     *
     * @param effectiveFrom Start date
     * @param effectiveTo   End date (nullable for indefinite, will use 1 year
     *                      default)
     * @param daysOfWeek    List of day numbers (1=Monday, 2=Tuesday, ..., 7=Sunday)
     * @return List of dates matching the criteria
     */
    private List<LocalDate> calculateWorkingDays(LocalDate effectiveFrom, LocalDate effectiveTo,
            List<Integer> daysOfWeek) {
        List<LocalDate> workingDays = new java.util.ArrayList<>();

        // If effectiveTo is null, default to 1 year from effectiveFrom (prevent
        // infinite loop)
        LocalDate endDate = (effectiveTo != null) ? effectiveTo : effectiveFrom.plusYears(1);

        // Validate date range
        if (endDate.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu");
        }

        // Convert day numbers to java.time.DayOfWeek
        List<java.time.DayOfWeek> targetDays = daysOfWeek.stream()
                .map(this::convertDayNumberToDayOfWeek)
                .collect(Collectors.toList());

        // Loop through date range and collect matching days
        LocalDate currentDate = effectiveFrom;
        while (!currentDate.isAfter(endDate)) {
            if (targetDays.contains(currentDate.getDayOfWeek())) {
                workingDays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        return workingDays;
    }

    /**
     * Convert day number to java.time.DayOfWeek.
     *
     * @param dayNumber 1=Monday, 2=Tuesday, ..., 7=Sunday
     * @return DayOfWeek enum
     */
    private java.time.DayOfWeek convertDayNumberToDayOfWeek(Integer dayNumber) {
        return switch (dayNumber) {
            case 1 -> java.time.DayOfWeek.MONDAY;
            case 2 -> java.time.DayOfWeek.TUESDAY;
            case 3 -> java.time.DayOfWeek.WEDNESDAY;
            case 4 -> java.time.DayOfWeek.THURSDAY;
            case 5 -> java.time.DayOfWeek.FRIDAY;
            case 6 -> java.time.DayOfWeek.SATURDAY;
            case 7 -> java.time.DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Số ngày không hợp lệ: " + dayNumber + " (phải từ 1-7)");
        };
    }

    /**
     * Create employee shifts for an approved part-time registration.
     * Generates individual shift records for each working day.
     *
     * This is called automatically when a manager approves a part-time
     * registration.
     *
     * @deprecated Use {@link #createShiftsForRegistration} instead for better
     *             flexibility
     * @param employeeId  Employee ID who registered
     * @param workShiftId Work shift ID from the slot
     * @param workingDays List of dates to create shifts for
     * @param managerId   Manager who approved (recorded as createdBy)
     * @return List of created shifts
     */
    @Deprecated
    @Transactional
    public List<EmployeeShift> createShiftsForApprovedRegistration(
            Integer employeeId,
            String workShiftId,
            List<LocalDate> workingDays,
            Integer managerId) {

        log.info("Creating {} shifts for employee {} after registration approval",
                workingDays.size(), employeeId);

        // Validate employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RelatedResourceNotFoundException("Nhân viên không tồn tại"));

        // Validate work shift exists
        WorkShift workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new RelatedResourceNotFoundException("Ca làm việc không tồn tại"));

        List<EmployeeShift> createdShifts = new java.util.ArrayList<>();

        for (LocalDate workDate : workingDays) {
            // Check if shift already exists (avoid duplicates)
            boolean exists = employeeShiftRepository.existsByEmployeeAndDateAndShift(
                    employeeId, workDate, workShiftId);

            if (exists) {
                log.warn("Shift already exists for employee {} on {} - skipping", employeeId, workDate);
                continue;
            }

            // Generate shift ID with format: EMS + YYMMDD + SEQ
            String employeeShiftId = idGenerator.generateId("EMS");

            // Create new shift
            EmployeeShift newShift = new EmployeeShift();
            newShift.setEmployeeShiftId(employeeShiftId);
            newShift.setEmployee(employee);
            newShift.setWorkShift(workShift);
            newShift.setWorkDate(workDate);
            newShift.setStatus(ShiftStatus.SCHEDULED);
            newShift.setSource(ShiftSource.REGISTRATION_JOB); // Mark as created from registration
            newShift.setIsOvertime(false);
            newShift.setCreatedBy(managerId);
            newShift.setNotes("Tạo tự động từ đăng ký bán thời gian");

            EmployeeShift savedShift = employeeShiftRepository.save(newShift);
            createdShifts.add(savedShift);

            log.debug("Created shift {} for date {}", employeeShiftId, workDate);
        }

        log.info("Successfully created {} shifts for employee {}", createdShifts.size(), employeeId);
        return createdShifts;
    }

    /**
     * Check if an employee shift exists for a specific employee, date, and work
     * shift.
     * Used to prevent duplicate shift creation during registration approval.
     *
     * @param employeeId  Employee ID
     * @param workDate    Work date
     * @param workShiftId Work shift ID
     * @return true if shift exists, false otherwise
     */
    public boolean existsByEmployeeAndDateAndShift(Integer employeeId, LocalDate workDate, String workShiftId) {
        return employeeShiftRepository.existsByEmployeeAndDateAndShift(employeeId, workDate, workShiftId);
    }

    /**
     * Check if any shifts exist for a specific registration.
     * Used to skip backfill for registrations that already have shifts.
     *
     * @param source   Source type (e.g., "PART_TIME_FLEX", "FULL_TIME") - not used
     *                 but kept for API compatibility
     * @param sourceId Source registration ID
     * @return true if at least one shift exists with this source registration ID
     */
    public boolean existsShiftsForSource(String source, Long sourceId) {
        log.debug("Checking if shifts exist for sourceRegistrationId: {}", sourceId);
        List<EmployeeShift> shifts = employeeShiftRepository.findBySourceRegistrationId(sourceId);
        return shifts != null && !shifts.isEmpty();
    }

    /**
     * Delete all shifts for a specific registration.
     * Used when regenerating shifts for a registration.
     *
     * @param source   Source type (e.g., "PART_TIME_FLEX", "FULL_TIME") - not used
     *                 but kept for API compatibility
     * @param sourceId Source registration ID
     * @return Number of shifts deleted
     */
    @Transactional
    public int deleteShiftsForSource(String source, Long sourceId) {
        log.info("Deleting shifts for sourceRegistrationId: {}", sourceId);
        List<EmployeeShift> shiftsToDelete = employeeShiftRepository.findBySourceRegistrationId(sourceId);

        if (shiftsToDelete == null || shiftsToDelete.isEmpty()) {
            log.debug("No shifts found to delete for sourceRegistrationId: {}", sourceId);
            return 0;
        }

        int count = shiftsToDelete.size();
        employeeShiftRepository.deleteAll(shiftsToDelete);
        log.info("Deleted {} shifts for sourceRegistrationId: {}", count, sourceId);

        return count;
    }
}
