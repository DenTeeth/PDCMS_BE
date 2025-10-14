package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.EmployeeSchedule;
import com.dental.clinic.management.domain.enums.EmployeeScheduleStatus;
import com.dental.clinic.management.dto.request.UpdateEmployeeScheduleStatusRequest;
import com.dental.clinic.management.dto.response.EmployeeScheduleResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.mapper.EmployeeScheduleMapper;
import com.dental.clinic.management.repository.EmployeeRepository;
import com.dental.clinic.management.repository.EmployeeScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service for managing employee schedules (attendance tracking).
 *
 * Business Rules:
 * 1. Status transitions: SCHEDULED → PRESENT/LATE/ABSENT/ON_LEAVE
 * 2. Auto-calculate LATE if actualStartTime > startTime
 * 3. Track overtime if actualEndTime > endTime
 * 4. Records used for payroll and HR reporting
 */
@Service
public class EmployeeScheduleService {

    private final EmployeeScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeScheduleMapper mapper;

    public EmployeeScheduleService(EmployeeScheduleRepository scheduleRepository,
            EmployeeRepository employeeRepository,
            EmployeeScheduleMapper mapper) {
        this.scheduleRepository = scheduleRepository;
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }

    /**
     * Update employee schedule status (attendance tracking).
     *
     * @param scheduleId Schedule ID
     * @param request    Update status request
     * @return Updated schedule response
     * @throws BadRequestAlertException if schedule not found or invalid status
     *                                  transition
     */
    @Transactional
    public EmployeeScheduleResponse updateScheduleStatus(String scheduleId,
            UpdateEmployeeScheduleStatusRequest request) {
        // Find schedule
        EmployeeSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy lịch làm việc với ID: " + scheduleId,
                        "employee_schedule", "not_found"));

        // Validation 1: Check status transition validity
        validateStatusTransition(schedule.getStatus(), request.getStatus());

        // Validation 2: For PRESENT and LATE status, actualStartTime is required
        if ((request.getStatus() == EmployeeScheduleStatus.PRESENT ||
                request.getStatus() == EmployeeScheduleStatus.LATE) &&
                request.getActualStartTime() == null) {
            throw new BadRequestAlertException(
                    "Trạng thái PRESENT/LATE yêu cầu giờ check-in thực tế (actualStartTime)",
                    "employee_schedule", "missing_checkin_time");
        }

        // Auto-calculate status based on actual times
        EmployeeScheduleStatus finalStatus = request.getStatus();

        if (request.getActualStartTime() != null && schedule.getStartTime() != null) {
            // If checked in after scheduled start time → LATE
            if (request.getActualStartTime().isAfter(schedule.getStartTime())) {
                finalStatus = EmployeeScheduleStatus.LATE;
            } else if (finalStatus == EmployeeScheduleStatus.SCHEDULED) {
                // If checked in on time and status is still SCHEDULED → PRESENT
                finalStatus = EmployeeScheduleStatus.PRESENT;
            }
        }

        // Update schedule
        schedule.setStatus(finalStatus);
        schedule.setActualStartTime(request.getActualStartTime());
        schedule.setActualEndTime(request.getActualEndTime());

        // Append notes
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            String existingNotes = schedule.getNotes() != null ? schedule.getNotes() : "";
            schedule.setNotes(
                    existingNotes.isEmpty() ? request.getNotes() : existingNotes + "\n" + request.getNotes());
        }

        // Save
        EmployeeSchedule updated = scheduleRepository.save(schedule);

        return mapper.toResponse(updated);
    }

    /**
     * Get employee schedule by ID.
     *
     * @param scheduleId Schedule ID
     * @return Schedule response
     */
    @Transactional(readOnly = true)
    public EmployeeScheduleResponse getEmployeeScheduleById(String scheduleId) {
        EmployeeSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy lịch làm việc với ID: " + scheduleId,
                        "employee_schedule", "not_found"));

        return mapper.toResponse(schedule);
    }

    /**
     * Get all schedules for an employee within date range.
     *
     * @param employeeId Employee ID
     * @param startDate  Start date (inclusive)
     * @param endDate    End date (inclusive)
     * @param page       Page number
     * @param size       Page size
     * @return Page of schedules
     */
    @Transactional(readOnly = true)
    public Page<EmployeeScheduleResponse> getAllSchedulesByEmployee(
            String employeeId, LocalDate startDate, LocalDate endDate,
            int page, int size) {

        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new BadRequestAlertException(
                    "Không tìm thấy nhân viên với ID: " + employeeId,
                    "employee_schedule", "employee_not_found");
        }

        // Pagination setup
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "workDate", "startTime"));

        // Query with date range
        Page<EmployeeSchedule> schedules = scheduleRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(
                        employeeId, startDate, endDate, pageable);

        return schedules.map(mapper::toResponse);
    }

    /**
     * Get schedules for a specific date (all employees).
     * Used for daily attendance tracking.
     *
     * @param workDate Work date
     * @return List of schedules for that date
     */
    @Transactional(readOnly = true)
    public Page<EmployeeScheduleResponse> getSchedulesByDate(LocalDate workDate, int page, int size) {
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 50 : size;
        Pageable pageable = PageRequest.of(page, size);

        // Get all schedules for date
        java.util.List<EmployeeSchedule> allSchedules = scheduleRepository
                .findByWorkDateOrderByEmployeeIdAscStartTimeAsc(workDate);

        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allSchedules.size());

        Page<EmployeeSchedule> schedules;
        if (start > allSchedules.size()) {
            schedules = Page.empty(pageable);
        } else {
            java.util.List<EmployeeSchedule> pageContent = allSchedules.subList(start, end);
            schedules = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, allSchedules.size());
        }

        return schedules.map(mapper::toResponse);
    }

    /**
     * Validate status transition.
     *
     * Valid transitions:
     * - SCHEDULED → PRESENT (normal check-in on time)
     * - SCHEDULED → LATE (check-in late)
     * - SCHEDULED → ABSENT (no check-in)
     * - SCHEDULED → ON_LEAVE (pre-approved absence)
     *
     * Invalid transitions:
     * - Any terminal status → Another status (PRESENT/LATE/ABSENT/ON_LEAVE are
     * final)
     *
     * @param currentStatus Current status
     * @param newStatus     New status
     * @throws BadRequestAlertException if transition is invalid
     */
    private void validateStatusTransition(EmployeeScheduleStatus currentStatus, EmployeeScheduleStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return; // Let other validations handle null
        }

        // Terminal statuses cannot be changed
        if (currentStatus != EmployeeScheduleStatus.SCHEDULED) {
            throw new BadRequestAlertException(
                    String.format("Không thể thay đổi trạng thái từ %s. Trạng thái này đã hoàn tất",
                            currentStatus),
                    "employee_schedule", "invalid_status_transition");
        }

        // SCHEDULED can transition to any status
        // (This is the only valid starting state for transitions)
    }
}
