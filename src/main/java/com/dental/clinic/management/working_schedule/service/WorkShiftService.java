package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.exception.*;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.CreateWorkShiftRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateWorkShiftRequest;
import com.dental.clinic.management.working_schedule.dto.response.WorkShiftResponse;
import com.dental.clinic.management.working_schedule.enums.WorkShiftCategory;
import com.dental.clinic.management.working_schedule.mapper.WorkShiftMapper;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import com.dental.clinic.management.working_schedule.utils.WorkShiftIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing work shifts.
 * Implements all business logic and validation rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final WorkShiftMapper workShiftMapper;

    // Clinic working hours: 8:00 AM to 9:00 PM
    private static final LocalTime CLINIC_OPEN = LocalTime.of(8, 0);
    private static final LocalTime CLINIC_CLOSE = LocalTime.of(21, 0);
    private static final LocalTime NIGHT_SHIFT_START = LocalTime.of(18, 0);
    
    // Lunch break: 12:00 PM to 1:00 PM (not counted as work hours)
    private static final LocalTime LUNCH_BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_BREAK_END = LocalTime.of(13, 0);
    private static final double LUNCH_BREAK_HOURS = 1.0;
    
    private static final double MIN_DURATION_HOURS = 3.0;
    private static final double MAX_DURATION_HOURS = 8.0;

    /**
     * Create a new work shift.
     * @param request CreateWorkShiftRequest
     * @return WorkShiftResponse
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREATE_WORK_SHIFTS')")
    public WorkShiftResponse createWorkShift(CreateWorkShiftRequest request) {
        log.info("Creating work shift...");

        // Validation 1: Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Validation 2: Validate duration (3-8 hours)
        double duration = calculateDuration(request.getStartTime(), request.getEndTime());
        validateDuration(duration);

        // Validation 3: Validate working hours (8:00 - 21:00)
        validateWorkingHours(request.getStartTime(), request.getEndTime());

        // Validation 4: Validate night shift category
        validateNightShiftCategory(request.getStartTime(), request.getCategory());

        // Generate work shift ID
        String generatedId = generateWorkShiftId(request.getStartTime(), request.getEndTime());
        log.info("Generated work shift ID: {}", generatedId);

        // Create and save entity
        WorkShift workShift = workShiftMapper.toEntity(request);
        workShift.setWorkShiftId(generatedId);
        
        WorkShift savedWorkShift = workShiftRepository.save(workShift);

        log.info("Successfully created work shift: {}", savedWorkShift.getWorkShiftId());
        return workShiftMapper.toResponse(savedWorkShift);
    }

    /**
     * Update an existing work shift.
     * @param workShiftId Work shift ID
     * @param request UpdateWorkShiftRequest
     * @return WorkShiftResponse
     */
    @Transactional
    @PreAuthorize("hasAuthority('UPDATE_WORK_SHIFTS')")
    public WorkShiftResponse updateWorkShift(String workShiftId, UpdateWorkShiftRequest request) {
        log.info("Updating work shift: {}", workShiftId);

        // Find existing work shift
        WorkShift workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new WorkShiftNotFoundException(workShiftId));

        // Determine final values (use new values if provided, otherwise keep existing)
        LocalTime finalStartTime = request.getStartTime() != null ? request.getStartTime() : workShift.getStartTime();
        LocalTime finalEndTime = request.getEndTime() != null ? request.getEndTime() : workShift.getEndTime();
        WorkShiftCategory finalCategory = request.getCategory() != null ? request.getCategory() : workShift.getCategory();

        // Apply all validations with final values
        validateTimeRange(finalStartTime, finalEndTime);
        
        double duration = calculateDuration(finalStartTime, finalEndTime);
        validateDuration(duration);
        
        validateWorkingHours(finalStartTime, finalEndTime);
        validateNightShiftCategory(finalStartTime, finalCategory);

        // Update entity
        workShiftMapper.updateEntity(workShift, request);
        WorkShift updatedWorkShift = workShiftRepository.save(workShift);

        log.info("Successfully updated work shift: {}", workShiftId);
        return workShiftMapper.toResponse(updatedWorkShift);
    }

    /**
     * Delete (soft delete) a work shift.
     * @param workShiftId Work shift ID
     */
    @Transactional
    @PreAuthorize("hasAuthority('DELETE_WORK_SHIFTS')")
    public void deleteWorkShift(String workShiftId) {
        log.info("Deleting work shift: {}", workShiftId);

        // Find existing work shift
        WorkShift workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new WorkShiftNotFoundException(workShiftId));

        // TODO: Check if work shift is in use (when employee_shifts table is implemented)
        // For now, we'll just do soft delete
        // if (isWorkShiftInUse(workShiftId)) {
        //     throw new WorkShiftInUseException(workShiftId);
        // }

        // Soft delete
        workShift.setIsActive(false);
        workShiftRepository.save(workShift);

        log.info("Successfully deleted work shift: {}", workShiftId);
    }

    /**
     * Get all work shifts, optionally filtered by active status.
     * @param isActive Optional filter by active status
     * @return List of WorkShiftResponse
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_WORK_SHIFTS')")
    public List<WorkShiftResponse> getAllWorkShifts(Boolean isActive) {
        log.info("Fetching all work shifts with isActive filter: {}", isActive);

        List<WorkShift> workShifts;
        if (isActive != null) {
            workShifts = workShiftRepository.findByIsActive(isActive);
        } else {
            workShifts = workShiftRepository.findAll();
        }

        return workShifts.stream()
                .map(workShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get work shift by ID.
     * @param workShiftId Work shift ID
     * @return WorkShiftResponse
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_WORK_SHIFTS')")
    public WorkShiftResponse getWorkShiftById(String workShiftId) {
        log.info("Fetching work shift: {}", workShiftId);

        WorkShift workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new WorkShiftNotFoundException(workShiftId));

        return workShiftMapper.toResponse(workShift);
    }

    // ============================================
    // VALIDATION METHODS
    // ============================================

    /**
     * Validate that end time is after start time.
     */
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidTimeRangeException();
        }
    }

    /**
     * Validate shift duration is between 3-8 hours.
     */
    private void validateDuration(double durationHours) {
        if (durationHours < MIN_DURATION_HOURS || durationHours > MAX_DURATION_HOURS) {
            throw new InvalidShiftDurationException(
                String.format("Thời lượng ca làm việc phải từ %.0f đến %.0f giờ. Thực tế: %.1f giờ",
                    MIN_DURATION_HOURS, MAX_DURATION_HOURS, durationHours)
            );
        }
    }

    /**
     * Validate that shift is within clinic working hours (8:00 - 21:00).
     */
    private void validateWorkingHours(LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(CLINIC_OPEN) || endTime.isAfter(CLINIC_CLOSE)) {
            throw new InvalidWorkingHoursException(
                String.format("Ca làm việc phải nằm trong giờ làm việc của phòng khám (%s - %s)",
                    CLINIC_OPEN, CLINIC_CLOSE)
            );
        }
    }

    /**
     * Validate that shifts starting at or after 18:00 must be NIGHT category.
     */
    private void validateNightShiftCategory(LocalTime startTime, WorkShiftCategory category) {
        if (startTime.compareTo(NIGHT_SHIFT_START) >= 0 && category != WorkShiftCategory.NIGHT) {
            throw new InvalidCategoryException("Work shifts starting at or after 18:00 must be NIGHT category");
        }
    }

    /**
     * Calculate duration in hours between start and end time.
     * Excludes lunch break (11:00-12:00) if the shift spans across it.
     */
    private double calculateDuration(LocalTime startTime, LocalTime endTime) {
        long startSeconds = startTime.toSecondOfDay();
        long endSeconds = endTime.toSecondOfDay();
        
        // Handle case where shift crosses midnight
        if (endSeconds <= startSeconds) {
            endSeconds += 24 * 3600;
        }
        
        long durationSeconds = endSeconds - startSeconds;
        double durationHours = durationSeconds / 3600.0;
        
        // Subtract lunch break if shift spans across it
        // Lunch break: 11:00 - 12:00 (1 hour)
        if (isShiftSpanningLunchBreak(startTime, endTime)) {
            durationHours -= LUNCH_BREAK_HOURS;
        }
        
        return durationHours;
    }

    /**
     * Check if shift spans across the lunch break period (11:00-12:00).
     */
    private boolean isShiftSpanningLunchBreak(LocalTime startTime, LocalTime endTime) {
        // Shift spans lunch break if:
        // - Start time is before or at lunch break start (11:00)
        // - End time is after or at lunch break end (12:00)
        return !startTime.isAfter(LUNCH_BREAK_START) && !endTime.isBefore(LUNCH_BREAK_END);
    }

    /**
     * Generate unique work shift ID based on time and sequence.
     * Format: WKS_{TIME_OF_DAY}_{SEQ}
     * Example: WKS_MORNING_01, WKS_AFTERNOON_02
     */
    private String generateWorkShiftId(LocalTime startTime, LocalTime endTime) {
        // Determine the time of day category from the generator
        String timeOfDay = WorkShiftIdGenerator.extractTimeOfDay(
            WorkShiftIdGenerator.generateShiftId(startTime, endTime, 1)
        );
        
        if (timeOfDay == null) {
            timeOfDay = "SHIFT"; // Fallback
        }
        
        // Find all existing shifts with this time of day prefix
        String prefix = "WKS_" + timeOfDay + "_";
        List<WorkShift> existingShifts = workShiftRepository.findByWorkShiftIdStartingWith(prefix);
        
        // Determine next sequence number
        int maxSequence = 0;
        for (WorkShift shift : existingShifts) {
            String id = shift.getWorkShiftId();
            String sequencePart = id.substring(prefix.length());
            try {
                int sequence = Integer.parseInt(sequencePart);
                maxSequence = Math.max(maxSequence, sequence);
            } catch (NumberFormatException e) {
                // Skip if not a valid number
            }
        }
        
        int nextSequence = maxSequence + 1;
        return WorkShiftIdGenerator.generateShiftId(startTime, endTime, nextSequence);
    }
}
