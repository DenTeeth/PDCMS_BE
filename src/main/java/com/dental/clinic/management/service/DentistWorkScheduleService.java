package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.DentistWorkSchedule;
import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.domain.enums.DentistWorkScheduleStatus;
import com.dental.clinic.management.domain.enums.EmploymentType;
import com.dental.clinic.management.dto.request.CancelDentistScheduleRequest;
import com.dental.clinic.management.dto.request.CreateDentistScheduleRequest;
import com.dental.clinic.management.dto.request.UpdateDentistScheduleRequest;
import com.dental.clinic.management.dto.response.DentistScheduleResponse;
import com.dental.clinic.management.exception.*;
import com.dental.clinic.management.mapper.DentistScheduleMapper;
import com.dental.clinic.management.repository.DentistWorkScheduleRepository;
import com.dental.clinic.management.repository.EmployeeRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing dentist work schedules (part-time flexible schedules).
 *
 * Business Rules:
 * 1. Only PART_TIME dentists can register schedules
 * 2. Maximum 2 schedules per day
 * 3. Must register at least 24 hours in advance
 * 4. Cannot register more than 30 days in advance
 * 5. Duration: minimum 2 hours, recommended 3-4 hours
 * 6. Working hours: 08:00 - 21:00
 * 7. No overlapping schedules for same dentist
 * 8. Cannot cancel BOOKED schedules
 *
 * Payment commitment: "Đăng ký lên ngồi là phải trả tiền"
 */
@Service
public class DentistWorkScheduleService {

    private static final LocalTime MIN_WORKING_HOUR = LocalTime.of(8, 0);
    private static final LocalTime MAX_WORKING_HOUR = LocalTime.of(21, 0);
    private static final int MIN_DURATION_HOURS = 2;
    private static final int MAX_SCHEDULES_PER_DAY = 2;
    private static final int MIN_ADVANCE_DAYS = 1; // 24 hours
    private static final int MAX_ADVANCE_DAYS = 30;

    private final DentistWorkScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final DentistScheduleMapper mapper;

    public DentistWorkScheduleService(DentistWorkScheduleRepository scheduleRepository,
            EmployeeRepository employeeRepository,
            DentistScheduleMapper mapper) {
        this.scheduleRepository = scheduleRepository;
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }

    /**
     * Create new dentist work schedule.
     *
     * @param request Create schedule request
     * @return Created schedule response
     * @throws NotPartTimeDentistException      if dentist is not PART_TIME
     * @throws MaxSchedulesExceededException    if already has 2 schedules on that
     *                                          day
     * @throws InvalidRegistrationDateException if registration window violated
     * @throws InvalidWorkingHoursException     if outside 08:00-21:00
     * @throws InvalidShiftDurationException    if duration < 2 hours
     * @throws ScheduleConflictException        if overlaps with existing schedule
     */
    @Transactional
    public DentistScheduleResponse createDentistSchedule(CreateDentistScheduleRequest request) {
        // Validation 1: Check dentist exists and is PART_TIME
        Employee dentist = employeeRepository.findById(request.getDentistId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy nhân viên với ID: " + request.getDentistId(),
                        "dentist_schedule", "dentist_not_found"));

        if (dentist.getEmploymentType() != EmploymentType.PART_TIME) {
            throw new NotPartTimeDentistException(
                    dentist.getEmployeeCode(),
                    dentist.getEmploymentType() != null ? dentist.getEmploymentType().name() : "UNKNOWN");
        }

        // Validation 2: Check max 2 schedules per day
        long existingCount = scheduleRepository.countActiveSchedulesByDentistAndDate(
                request.getDentistId(),
                request.getWorkDate());

        if (existingCount >= MAX_SCHEDULES_PER_DAY) {
            throw new MaxSchedulesExceededException(request.getWorkDate(), (int) existingCount);
        }

        // Validation 3: Check registration date window (24h - 30d)
        validateRegistrationDateWindow(request.getWorkDate());

        // Validation 4: Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Validation 5: Check for conflicts
        List<DentistWorkSchedule> conflicts = scheduleRepository.findConflictingSchedules(
                request.getDentistId(),
                request.getWorkDate(),
                request.getStartTime(),
                request.getEndTime(),
                null // No exclusion for new schedules
        );

        if (!conflicts.isEmpty()) {
            DentistWorkSchedule conflict = conflicts.get(0);
            throw new ScheduleConflictException(
                    request.getWorkDate(),
                    request.getStartTime(),
                    request.getEndTime(),
                    conflict.getScheduleCode());
        }

        // Generate schedule code: SCH_YYYYMMDD_SEQ
        String scheduleCode = generateScheduleCode(request.getWorkDate(), request.getDentistId());

        // Create entity
        DentistWorkSchedule schedule = new DentistWorkSchedule();
        schedule.setScheduleId(UUID.randomUUID().toString());
        schedule.setScheduleCode(scheduleCode);
        schedule.setDentistId(request.getDentistId());
        schedule.setWorkDate(request.getWorkDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setStatus(DentistWorkScheduleStatus.AVAILABLE);
        schedule.setNotes(request.getNotes());

        // Save
        DentistWorkSchedule saved = scheduleRepository.save(schedule);

        return mapper.toResponse(saved);
    }

    /**
     * Update existing dentist work schedule.
     * Can only update AVAILABLE schedules.
     *
     * @param scheduleId Schedule ID
     * @param request    Update request
     * @return Updated schedule response
     */
    @Transactional
    public DentistScheduleResponse updateDentistSchedule(String scheduleId, UpdateDentistScheduleRequest request) {
        // Find existing schedule
        DentistWorkSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy lịch làm việc với ID: " + scheduleId,
                        "dentist_schedule", "not_found"));

        // Validation 1: Check ownership (SECURITY: Only owner or admin can update)
        Employee currentEmployee = getCurrentAuthenticatedEmployee();
        validateScheduleOwnership(schedule, currentEmployee);

        // Validation 2: Can only update AVAILABLE schedules
        if (schedule.getStatus() != DentistWorkScheduleStatus.AVAILABLE) {
            throw new BadRequestAlertException(
                    String.format("Không thể cập nhật lịch có trạng thái: %s. Chỉ cập nhật được lịch AVAILABLE",
                            schedule.getStatus()),
                    "dentist_schedule", "invalid_status");
        }

        // Validation 3: Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Validation 4: Check for conflicts (exclude current schedule)
        List<DentistWorkSchedule> conflicts = scheduleRepository.findConflictingSchedules(
                schedule.getDentistId(),
                schedule.getWorkDate(),
                request.getStartTime(),
                request.getEndTime(),
                scheduleId // Exclude self
        );

        if (!conflicts.isEmpty()) {
            DentistWorkSchedule conflict = conflicts.get(0);
            throw new ScheduleConflictException(
                    schedule.getWorkDate(),
                    request.getStartTime(),
                    request.getEndTime(),
                    conflict.getScheduleCode());
        }

        // Update fields
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setNotes(request.getNotes());

        // Save
        DentistWorkSchedule updated = scheduleRepository.save(schedule);

        return mapper.toResponse(updated);
    }

    /**
     * Cancel dentist work schedule.
     * Cannot cancel BOOKED schedules (must reschedule patients first).
     *
     * @param scheduleId Schedule ID
     * @param request    Cancel request with reason
     */
    @Transactional
    public void cancelDentistSchedule(String scheduleId, CancelDentistScheduleRequest request) {
        // Find schedule
        DentistWorkSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy lịch làm việc với ID: " + scheduleId,
                        "dentist_schedule", "not_found"));

        // Validation 1: Check ownership (SECURITY: Only owner or admin can cancel)
        Employee currentEmployee = getCurrentAuthenticatedEmployee();
        validateScheduleOwnership(schedule, currentEmployee);

        // Validation 2: Cannot cancel BOOKED schedules
        if (schedule.getStatus() == DentistWorkScheduleStatus.BOOKED) {
            throw new ScheduleAlreadyBookedException(schedule.getScheduleCode(), 1);
        }

        // Validation 3: Cannot cancel already CANCELLED or EXPIRED schedules
        if (schedule.getStatus() == DentistWorkScheduleStatus.CANCELLED) {
            throw new BadRequestAlertException(
                    "Lịch làm việc đã được hủy trước đó",
                    "dentist_schedule", "already_cancelled");
        }

        if (schedule.getStatus() == DentistWorkScheduleStatus.EXPIRED) {
            throw new BadRequestAlertException(
                    "Không thể hủy lịch đã hết hạn",
                    "dentist_schedule", "expired");
        }

        // Update status and notes
        schedule.setStatus(DentistWorkScheduleStatus.CANCELLED);
        schedule.setNotes(
                (schedule.getNotes() != null ? schedule.getNotes() + "\n" : "") +
                        "Lý do hủy: " + request.getCancelReason());

        scheduleRepository.save(schedule);
    }

    /**
     * Get dentist schedule by ID.
     *
     * @param scheduleId Schedule ID
     * @return Schedule response
     */
    @Transactional(readOnly = true)
    public DentistScheduleResponse getDentistScheduleById(String scheduleId) {
        DentistWorkSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy lịch làm việc với ID: " + scheduleId,
                        "dentist_schedule", "not_found"));

        return mapper.toResponse(schedule);
    }

    /**
     * Get all schedules for a dentist within date range.
     *
     * @param dentistId Dentist employee ID
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @param page      Page number
     * @param size      Page size
     * @return Page of schedules
     */
    @Transactional(readOnly = true)
    public Page<DentistScheduleResponse> getAllSchedulesByDentist(
            String employeeCode, LocalDate startDate, LocalDate endDate,
            int page, int size) {

        // Get dentist by employeeCode and convert to dentistId (UUID)
        Employee dentist = employeeRepository.findOneByEmployeeCode(employeeCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy nhân viên với Code: " + employeeCode,
                        "dentist_schedule", "dentist_not_found"));

        String dentistId = dentist.getEmployeeId(); // Get the UUID

        // Pagination setup
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "workDate", "startTime"));

        // Query with date range using dentistId (UUID)
        Page<DentistWorkSchedule> schedules = scheduleRepository.findByDentistIdAndWorkDateBetween(
                dentistId, startDate, endDate, pageable);

        return schedules.map(mapper::toResponse);
    }

    /**
     * Get available schedules for booking (calendar view).
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return List of available schedules
     */
    @Transactional(readOnly = true)
    public Page<DentistScheduleResponse> getAvailableSchedules(LocalDate startDate, LocalDate endDate, int page,
            int size) {
        // Validate pagination
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 20 : size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "workDate", "startTime"));

        List<DentistWorkSchedule> allSchedules = scheduleRepository
                .findByWorkDateBetweenAndStatusOrderByWorkDateAscStartTimeAsc(
                        startDate, endDate, DentistWorkScheduleStatus.AVAILABLE);

        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allSchedules.size());

        if (start > allSchedules.size()) {
            return Page.empty(pageable);
        }

        List<DentistWorkSchedule> pageContent = allSchedules.subList(start, end);
        Page<DentistWorkSchedule> schedulePage = new PageImpl<>(pageContent, pageable, allSchedules.size());

        return schedulePage.map(mapper::toResponse);
    }

    /**
     * Validate registration date window.
     * Must register 24 hours - 30 days in advance.
     *
     * @param workDate Work date to validate
     * @throws InvalidRegistrationDateException if outside valid window
     */
    private void validateRegistrationDateWindow(LocalDate workDate) {
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.plusDays(MIN_ADVANCE_DAYS);
        LocalDate maxDate = today.plusDays(MAX_ADVANCE_DAYS);

        if (workDate.isBefore(minDate)) {
            throw new InvalidRegistrationDateException(
                    String.format("Ngày làm việc %s quá gần. Phải đăng ký trước ít nhất %d ngày (từ %s)",
                            workDate, MIN_ADVANCE_DAYS, minDate));
        }

        if (workDate.isAfter(maxDate)) {
            throw new InvalidRegistrationDateException(
                    String.format("Ngày làm việc %s quá xa. Chỉ đăng ký được tối đa %d ngày (đến %s)",
                            workDate, MAX_ADVANCE_DAYS, maxDate));
        }
    }

    /**
     * Validate time range for schedule.
     *
     * Business Rules:
     * - Duration: minimum 2 hours
     * - Start time: >= 08:00
     * - End time: <= 21:00
     * - End time > Start time
     *
     * @param startTime Start time
     * @param endTime   End time
     * @throws InvalidShiftDurationException if duration < 2 hours
     * @throws InvalidWorkingHoursException  if outside working hours
     */
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        // Rule 1: End time must be after start time
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new InvalidShiftDurationException(
                    String.format("Giờ kết thúc (%s) phải sau giờ bắt đầu (%s)", endTime, startTime));
        }

        // Rule 2: Within clinic operating hours
        if (startTime.isBefore(MIN_WORKING_HOUR)) {
            throw new InvalidWorkingHoursException(
                    String.format("Giờ bắt đầu (%s) phải từ %s trở về sau", startTime, MIN_WORKING_HOUR));
        }

        if (endTime.isAfter(MAX_WORKING_HOUR)) {
            throw new InvalidWorkingHoursException(
                    String.format("Giờ kết thúc (%s) phải trước %s", endTime, MAX_WORKING_HOUR));
        }

        // Rule 3: Minimum duration check
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();

        if (hours < MIN_DURATION_HOURS) {
            throw new InvalidShiftDurationException(
                    String.format("Thời lượng ca làm việc: %d giờ. Tối thiểu: %d giờ. Khuyến nghị: 3-4 giờ",
                            hours, MIN_DURATION_HOURS));
        }
    }

    /**
     * Generate unique schedule code.
     * Format: SCH_YYYYMMDD_SEQ
     *
     * @param workDate  Work date
     * @param dentistId Dentist ID
     * @return Generated schedule code
     */
    private String generateScheduleCode(LocalDate workDate, String dentistId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = workDate.format(formatter);

        // Get sequence number for this dentist on this date
        long count = scheduleRepository.countActiveSchedulesByDentistAndDate(dentistId, workDate);
        String seqPart = String.format("%03d", count + 1);

        return "SCH_" + datePart + "_" + seqPart;
    }

    /**
     * Get current authenticated employee from security context.
     * Used for owner validation.
     *
     * @return Current authenticated employee
     * @throws BadRequestAlertException if not authenticated or employee not found
     */
    private Employee getCurrentAuthenticatedEmployee() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy thông tin người dùng đăng nhập",
                        "dentist_schedule", "not_authenticated"));

        return employeeRepository.findByAccount_Username(username)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy thông tin nhân viên cho tài khoản: " + username,
                        "dentist_schedule", "employee_not_found"));
    }

    /**
     * Validate schedule ownership.
     * Only owner or ADMIN can modify schedule.
     *
     * @param schedule        Schedule to validate
     * @param currentEmployee Current authenticated employee
     * @throws EmployeeNotAuthorizedException if not owner and not admin
     */
    private void validateScheduleOwnership(DentistWorkSchedule schedule, Employee currentEmployee) {
        boolean isAdmin = SecurityUtil.hasCurrentUserRole("ADMIN");
        boolean isOwner = schedule.getDentistId().equals(currentEmployee.getEmployeeId());

        if (!isOwner && !isAdmin) {
            throw new EmployeeNotAuthorizedException(
                    currentEmployee.getEmployeeCode(),
                    "Bạn không có quyền chỉnh sửa lịch của bác sĩ khác");
        }
    }
}
