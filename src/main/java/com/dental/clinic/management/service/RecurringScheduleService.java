package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.domain.RecurringSchedule;
import com.dental.clinic.management.domain.WorkShift;
import com.dental.clinic.management.domain.enums.EmploymentType;
import com.dental.clinic.management.dto.request.CreateRecurringScheduleRequest;
import com.dental.clinic.management.dto.request.UpdateRecurringScheduleRequest;
import com.dental.clinic.management.dto.response.RecurringScheduleResponse;
import com.dental.clinic.management.exception.*;
import com.dental.clinic.management.mapper.RecurringScheduleMapper;
import com.dental.clinic.management.repository.EmployeeRepository;
import com.dental.clinic.management.repository.RecurringScheduleRepository;
import com.dental.clinic.management.repository.WorkShiftRepository;
import org.springframework.data.domain.Page;
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
 * Service for managing recurring schedules (weekly patterns for full-time employees).
 * 
 * Business Rules:
 * 1. Only FULL_TIME employees can have recurring schedules
 * 2. Can use predefined shift OR custom times (not both)
 * 3. Custom times must follow same rules: 3-8h, 08:00-21:00
 * 4. No conflicts on same day of week for same employee
 * 5. Used to auto-generate employee_schedules daily
 */
@Service
public class RecurringScheduleService {

    private static final LocalTime MIN_WORKING_HOUR = LocalTime.of(8, 0);
    private static final LocalTime MAX_WORKING_HOUR = LocalTime.of(21, 0);
    private static final int MIN_DURATION_HOURS = 3;
    private static final int MAX_DURATION_HOURS = 8;

    private final RecurringScheduleRepository recurringRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final RecurringScheduleMapper mapper;

    public RecurringScheduleService(RecurringScheduleRepository recurringRepository,
                                   EmployeeRepository employeeRepository,
                                   WorkShiftRepository workShiftRepository,
                                   RecurringScheduleMapper mapper) {
        this.recurringRepository = recurringRepository;
        this.employeeRepository = employeeRepository;
        this.workShiftRepository = workShiftRepository;
        this.mapper = mapper;
    }

    /**
     * Create new recurring schedule.
     * 
     * @param request Create recurring schedule request
     * @return Created recurring schedule response
     * @throws NotFullTimeEmployeeException if employee is not FULL_TIME
     * @throws BadRequestAlertException if both shiftId and custom times provided
     * @throws BadRequestAlertException if neither shiftId nor custom times provided
     * @throws ScheduleConflictException if conflicts with existing recurring schedule
     */
    @Transactional
    public RecurringScheduleResponse createRecurringSchedule(CreateRecurringScheduleRequest request) {
        // Validation 1: Check employee exists and is FULL_TIME
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new BadRequestAlertException(
                "Không tìm thấy nhân viên với ID: " + request.getEmployeeId(),
                "recurring_schedule", "employee_not_found"
            ));

        if (employee.getEmploymentType() != EmploymentType.FULL_TIME) {
            throw new NotFullTimeEmployeeException(
                employee.getEmployeeCode(),
                employee.getEmploymentType() != null ? employee.getEmploymentType().name() : "UNKNOWN"
            );
        }

        // Validation 2: Must provide EITHER shiftId OR custom times (not both, not neither)
        boolean hasShiftId = request.getShiftId() != null && !request.getShiftId().trim().isEmpty();
        boolean hasCustomTimes = request.getStartTime() != null && request.getEndTime() != null;

        if (hasShiftId && hasCustomTimes) {
            throw new BadRequestAlertException(
                "Không thể cung cấp đồng thời shiftId và giờ tùy chỉnh. Chọn một trong hai",
                "recurring_schedule", "ambiguous_time"
            );
        }

        if (!hasShiftId && !hasCustomTimes) {
            throw new BadRequestAlertException(
                "Phải cung cấp shiftId HOẶC giờ tùy chỉnh (startTime + endTime)",
                "recurring_schedule", "missing_time"
            );
        }

        // Resolve times for validation
        LocalTime startTime;
        LocalTime endTime;
        WorkShift workShift = null;

        if (hasShiftId) {
            // Use predefined shift
            workShift = workShiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new BadRequestAlertException(
                    "Không tìm thấy ca làm việc với ID: " + request.getShiftId(),
                    "recurring_schedule", "shift_not_found"
                ));

            startTime = workShift.getStartTime();
            endTime = workShift.getEndTime();
        } else {
            // Use custom times - validate them
            startTime = request.getStartTime();
            endTime = request.getEndTime();
            validateTimeRange(startTime, endTime);
        }

        // Validation 3: Check for conflicts on same day of week
        List<RecurringSchedule> conflicts = recurringRepository.findConflictingSchedules(
            request.getEmployeeId(),
            request.getDayOfWeek(),
            startTime,
            endTime,
            null  // No exclusion for new schedules
        );

        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException(
                request.getDayOfWeek().name(),
                startTime,
                endTime
            );
        }

        // Generate recurring code: REC_YYYYMMDD_SEQ
        String recurringCode = generateRecurringCode();

        // Create entity
        RecurringSchedule schedule = new RecurringSchedule();
        schedule.setRecurringId(UUID.randomUUID().toString());
        schedule.setRecurringCode(recurringCode);
        schedule.setEmployeeId(request.getEmployeeId());
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setShiftId(hasShiftId ? request.getShiftId() : null);
        schedule.setShiftType(workShift != null ? workShift.getShiftType() : null);
        schedule.setStartTime(hasCustomTimes ? request.getStartTime() : null);
        schedule.setEndTime(hasCustomTimes ? request.getEndTime() : null);
        schedule.setIsActive(true);
        schedule.setNotes(request.getNotes());

        // Save
        RecurringSchedule saved = recurringRepository.save(schedule);

        return mapper.toResponse(saved);
    }

    /**
     * Update existing recurring schedule.
     * Cannot change employeeId or dayOfWeek (delete and recreate instead).
     * 
     * @param recurringId Recurring schedule ID
     * @param request Update request
     * @return Updated recurring schedule response
     */
    @Transactional
    public RecurringScheduleResponse updateRecurringSchedule(String recurringId, UpdateRecurringScheduleRequest request) {
        // Find existing schedule
        RecurringSchedule schedule = recurringRepository.findById(recurringId)
            .orElseThrow(() -> new BadRequestAlertException(
                "Không tìm thấy lịch cố định với ID: " + recurringId,
                "recurring_schedule", "not_found"
            ));

        // Validation: Must provide EITHER shiftId OR custom times
        boolean hasShiftId = request.getShiftId() != null && !request.getShiftId().trim().isEmpty();
        boolean hasCustomTimes = request.getStartTime() != null && request.getEndTime() != null;

        if (hasShiftId && hasCustomTimes) {
            throw new BadRequestAlertException(
                "Không thể cung cấp đồng thời shiftId và giờ tùy chỉnh",
                "recurring_schedule", "ambiguous_time"
            );
        }

        // Resolve times for validation
        LocalTime startTime;
        LocalTime endTime;
        WorkShift workShift = null;

        if (hasShiftId) {
            workShift = workShiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new BadRequestAlertException(
                    "Không tìm thấy ca làm việc với ID: " + request.getShiftId(),
                    "recurring_schedule", "shift_not_found"
                ));

            startTime = workShift.getStartTime();
            endTime = workShift.getEndTime();
        } else if (hasCustomTimes) {
            startTime = request.getStartTime();
            endTime = request.getEndTime();
            validateTimeRange(startTime, endTime);
        } else {
            // Keep existing times
            if (schedule.getWorkShift() != null) {
                startTime = schedule.getWorkShift().getStartTime();
                endTime = schedule.getWorkShift().getEndTime();
            } else {
                startTime = schedule.getStartTime();
                endTime = schedule.getEndTime();
            }
        }

        // Check for conflicts (exclude current schedule)
        List<RecurringSchedule> conflicts = recurringRepository.findConflictingSchedules(
            schedule.getEmployeeId(),
            schedule.getDayOfWeek(),
            startTime,
            endTime,
            recurringId  // Exclude self
        );

        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException(
                schedule.getDayOfWeek().name(),
                startTime,
                endTime
            );
        }

        // Update fields
        if (hasShiftId) {
            schedule.setShiftId(request.getShiftId());
            schedule.setShiftType(workShift.getShiftType());
            schedule.setStartTime(null);
            schedule.setEndTime(null);
        } else if (hasCustomTimes) {
            schedule.setShiftId(null);
            schedule.setShiftType(null);
            schedule.setStartTime(request.getStartTime());
            schedule.setEndTime(request.getEndTime());
        }

        schedule.setNotes(request.getNotes());

        // Save
        RecurringSchedule updated = recurringRepository.save(schedule);

        return mapper.toResponse(updated);
    }

    /**
     * Toggle active status of recurring schedule.
     * 
     * @param recurringId Recurring schedule ID
     * @param isActive New active status
     * @return Updated recurring schedule response
     */
    @Transactional
    public RecurringScheduleResponse toggleRecurringSchedule(String recurringId, boolean isActive) {
        RecurringSchedule schedule = recurringRepository.findById(recurringId)
            .orElseThrow(() -> new BadRequestAlertException(
                "Không tìm thấy lịch cố định với ID: " + recurringId,
                "recurring_schedule", "not_found"
            ));

        schedule.setIsActive(isActive);
        RecurringSchedule updated = recurringRepository.save(schedule);

        return mapper.toResponse(updated);
    }

    /**
     * Get recurring schedule by ID.
     * 
     * @param recurringId Recurring schedule ID
     * @return Recurring schedule response
     */
    @Transactional(readOnly = true)
    public RecurringScheduleResponse getRecurringScheduleById(String recurringId) {
        RecurringSchedule schedule = recurringRepository.findById(recurringId)
            .orElseThrow(() -> new BadRequestAlertException(
                "Không tìm thấy lịch cố định với ID: " + recurringId,
                "recurring_schedule", "not_found"
            ));

        return mapper.toResponse(schedule);
    }

    /**
     * Get all recurring schedules for an employee.
     * 
     * @param employeeId Employee ID
     * @param includeInactive Include inactive schedules
     * @param page Page number
     * @param size Page size
     * @return Page of recurring schedules
     */
    @Transactional(readOnly = true)
    public Page<RecurringScheduleResponse> getAllRecurringSchedulesByEmployee(
            String employeeId, boolean includeInactive, int page, int size) {
        
        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new BadRequestAlertException(
                "Không tìm thấy nhân viên với ID: " + employeeId,
                "recurring_schedule", "employee_not_found"
            );
        }

        // Pagination setup
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dayOfWeek"));

        Page<RecurringSchedule> schedules;
        
        if (includeInactive) {
            schedules = recurringRepository.findByEmployeeIdOrderByDayOfWeekAsc(employeeId, pageable);
        } else {
            List<RecurringSchedule> activeList = recurringRepository
                .findByEmployeeIdAndIsActiveOrderByDayOfWeekAsc(employeeId, true);
            
            // Convert list to page manually
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), activeList.size());
            
            if (start > activeList.size()) {
                schedules = Page.empty(pageable);
            } else {
                List<RecurringSchedule> pageContent = activeList.subList(start, end);
                schedules = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, activeList.size()
                );
            }
        }

        return schedules.map(mapper::toResponse);
    }

    /**
     * Validate time range for custom times.
     * Same rules as work shifts: 3-8 hours, 08:00-21:00.
     * 
     * @param startTime Start time
     * @param endTime End time
     */
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        // Rule 1: End time must be after start time
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new InvalidShiftDurationException(
                String.format("Giờ kết thúc (%s) phải sau giờ bắt đầu (%s)", endTime, startTime)
            );
        }

        // Rule 2: Within clinic operating hours
        if (startTime.isBefore(MIN_WORKING_HOUR)) {
            throw new InvalidWorkingHoursException(
                String.format("Giờ bắt đầu (%s) phải từ %s trở về sau", startTime, MIN_WORKING_HOUR)
            );
        }

        if (endTime.isAfter(MAX_WORKING_HOUR)) {
            throw new InvalidWorkingHoursException(
                String.format("Giờ kết thúc (%s) phải trước %s", endTime, MAX_WORKING_HOUR)
            );
        }

        // Rule 3: Duration must be 3-8 hours
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();

        if (hours < MIN_DURATION_HOURS || hours > MAX_DURATION_HOURS) {
            throw new InvalidShiftDurationException(
                String.format("Thời lượng ca làm việc: %d giờ. Yêu cầu: %d-%d giờ",
                    hours, MIN_DURATION_HOURS, MAX_DURATION_HOURS)
            );
        }
    }

    /**
     * Generate unique recurring code.
     * Format: REC_YYYYMMDD_SEQ
     * 
     * @return Generated recurring code
     */
    private String generateRecurringCode() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = today.format(formatter);

        // Get count for today
        long count = recurringRepository.count();
        String seqPart = String.format("%03d", (count % 1000) + 1);

        return "REC_" + datePart + "_" + seqPart;
    }
}
