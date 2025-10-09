package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.WorkShift;
import com.dental.clinic.management.domain.enums.WorkShiftType;
import com.dental.clinic.management.dto.request.CreateWorkShiftRequest;
import com.dental.clinic.management.dto.request.UpdateWorkShiftRequest;
import com.dental.clinic.management.dto.response.WorkShiftResponse;
import com.dental.clinic.management.exception.*;
import com.dental.clinic.management.mapper.WorkShiftMapper;
import com.dental.clinic.management.repository.RecurringScheduleRepository;
import com.dental.clinic.management.repository.WorkShiftRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing work shifts.
 * 
 * Business Rules:
 * - Duration: 3-8 hours
 * - Working hours: 08:00 - 21:00
 * - No overlapping shifts
 * - Cannot delete shifts in use
 */
@Service
public class WorkShiftService {

    private static final LocalTime MIN_WORKING_HOUR = LocalTime.of(8, 0);
    private static final LocalTime MAX_WORKING_HOUR = LocalTime.of(21, 0);
    private static final int MIN_DURATION_HOURS = 3;
    private static final int MAX_DURATION_HOURS = 8;

    private final WorkShiftRepository workShiftRepository;
    private final RecurringScheduleRepository recurringScheduleRepository;
    private final WorkShiftMapper workShiftMapper;

    public WorkShiftService(WorkShiftRepository workShiftRepository,
                           RecurringScheduleRepository recurringScheduleRepository,
                           WorkShiftMapper workShiftMapper) {
        this.workShiftRepository = workShiftRepository;
        this.recurringScheduleRepository = recurringScheduleRepository;
        this.workShiftMapper = workShiftMapper;
    }

    /**
     * Create new work shift with validation.
     * 
     * @param request Create shift request
     * @return Created shift response
     * @throws InvalidShiftDurationException if duration not 3-8 hours
     * @throws InvalidWorkingHoursException if outside 08:00-21:00
     * @throws ScheduleConflictException if overlaps with existing shift
     * @throws BadRequestAlertException if shiftCode already exists
     */
    @Transactional
    public WorkShiftResponse createWorkShift(CreateWorkShiftRequest request) {
        // Validation 1: Check shift code uniqueness
        if (workShiftRepository.existsByShiftCode(request.getShiftCode())) {
            throw new BadRequestAlertException(
                String.format("Mã ca làm việc '%s' đã tồn tại", request.getShiftCode()),
                "work_shift", "shift_code_exists"
            );
        }

        // Validation 2: Check shift name uniqueness
        if (workShiftRepository.existsByShiftNameIgnoreCase(request.getShiftName())) {
            throw new BadRequestAlertException(
                String.format("Tên ca làm việc '%s' đã tồn tại", request.getShiftName()),
                "work_shift", "shift_name_exists"
            );
        }

        // Validation 3: Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Validation 4: Check for overlapping shifts
        List<WorkShift> overlappingShifts = workShiftRepository.findOverlappingShifts(
            request.getStartTime(), request.getEndTime()
        );
        if (!overlappingShifts.isEmpty()) {
            throw new ScheduleConflictException(
                String.format("Ca làm việc trùng với ca '%s' (%s - %s)",
                    overlappingShifts.get(0).getShiftName(),
                    overlappingShifts.get(0).getStartTime(),
                    overlappingShifts.get(0).getEndTime())
            );
        }

        // Create entity
        WorkShift workShift = new WorkShift();
        workShift.setShiftId(UUID.randomUUID().toString());
        workShift.setShiftCode(request.getShiftCode());
        workShift.setShiftName(request.getShiftName());
        workShift.setShiftType(request.getShiftType());
        workShift.setStartTime(request.getStartTime());
        workShift.setEndTime(request.getEndTime());
        workShift.setIsActive(true);
        workShift.setNotes(request.getNotes());

        // Save
        WorkShift saved = workShiftRepository.save(workShift);

        // Convert to response
        return workShiftMapper.toResponse(saved);
    }

    /**
     * Update existing work shift.
     * 
     * @param shiftId Shift ID
     * @param request Update shift request
     * @return Updated shift response
     */
    @Transactional
    public WorkShiftResponse updateWorkShift(String shiftId, UpdateWorkShiftRequest request) {
        // Find existing shift
        WorkShift workShift = workShiftRepository.findById(shiftId)
            .orElseThrow(() -> new BadRequestAlertException(
                String.format("Không tìm thấy ca làm việc với ID: %s", shiftId),
                "work_shift", "not_found"
            ));

        // Validation: Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Validation: Check for overlapping shifts (exclude current shift)
        List<WorkShift> overlappingShifts = workShiftRepository.findOverlappingShifts(
            request.getStartTime(), request.getEndTime()
        ).stream()
         .filter(s -> !s.getShiftId().equals(shiftId))
         .collect(Collectors.toList());

        if (!overlappingShifts.isEmpty()) {
            throw new ScheduleConflictException(
                String.format("Ca làm việc trùng với ca '%s'",
                    overlappingShifts.get(0).getShiftName())
            );
        }

        // Update fields
        workShift.setShiftName(request.getShiftName());
        workShift.setShiftType(request.getShiftType());
        workShift.setStartTime(request.getStartTime());
        workShift.setEndTime(request.getEndTime());
        workShift.setIsActive(request.getIsActive());
        workShift.setNotes(request.getNotes());

        // Save
        WorkShift updated = workShiftRepository.save(workShift);

        return workShiftMapper.toResponse(updated);
    }

    /**
     * Get all work shifts (active and inactive).
     * 
     * @return List of all shifts
     */
    @Transactional(readOnly = true)
    public Page<WorkShiftResponse> getAllWorkShifts(boolean includeInactive, int page, int size) {
        // Validate pagination
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startTime"));

        List<WorkShift> allShifts;
        
        if (includeInactive) {
            allShifts = workShiftRepository.findAllByOrderByStartTimeAsc();
        } else {
            allShifts = workShiftRepository.findByIsActiveTrueOrderByStartTimeAsc();
        }

        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allShifts.size());
        
        if (start > allShifts.size()) {
            return Page.empty(pageable);
        }
        
        List<WorkShift> pageContent = allShifts.subList(start, end);
        Page<WorkShift> shiftPage = new PageImpl<>(pageContent, pageable, allShifts.size());
        
        return shiftPage.map(workShiftMapper::toResponse);
    }

    /**
     * Get work shift by ID.
     * 
     * @param shiftId Shift ID
     * @return Shift response
     */
    @Transactional(readOnly = true)
    public WorkShiftResponse getWorkShiftById(String shiftId) {
        WorkShift workShift = workShiftRepository.findById(shiftId)
            .orElseThrow(() -> new BadRequestAlertException(
                String.format("Không tìm thấy ca làm việc với ID: %s", shiftId),
                "work_shift", "not_found"
            ));

        return workShiftMapper.toResponse(workShift);
    }

    /**
     * Delete work shift (soft delete - set inactive).
     * Cannot delete if shift is in use by recurring schedules.
     * 
     * @param shiftId Shift ID
     * @throws WorkShiftInUseException if shift is being used
     */
    @Transactional
    public void deleteWorkShift(String shiftId) {
        // Find shift
        WorkShift workShift = workShiftRepository.findById(shiftId)
            .orElseThrow(() -> new BadRequestAlertException(
                String.format("Không tìm thấy ca làm việc với ID: %s", shiftId),
                "work_shift", "not_found"
            ));

        // Check if shift is being used
        int recurringCount = recurringScheduleRepository.findByShiftIdAndIsActive(shiftId, true).size();
        
        if (recurringCount > 0) {
            throw new WorkShiftInUseException(
                workShift.getShiftCode(), recurringCount, 0
            );
        }

        // Soft delete - set inactive
        workShift.setIsActive(false);
        workShiftRepository.save(workShift);
    }

    /**
     * Validate time range for shift.
     * 
     * Business Rules:
     * - Duration: 3-8 hours
     * - Start time: >= 08:00
     * - End time: <= 21:00
     * - End time > Start time
     * 
     * @param startTime Start time
     * @param endTime End time
     * @throws InvalidShiftDurationException if duration invalid
     * @throws InvalidWorkingHoursException if outside working hours
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
}
