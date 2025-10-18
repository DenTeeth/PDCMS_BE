package com.dental.clinic.management.workforce_management.application.usecase;

import com.dental.clinic.management.exception.WorkShiftNotFoundException;
import com.dental.clinic.management.workforce_management.application.dto.CreateWorkShiftRequest;
import com.dental.clinic.management.workforce_management.application.dto.WorkShiftResponse;
import com.dental.clinic.management.workforce_management.domain.service.WorkShiftService;
import com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.WorkShift;
import com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.enums.WorkShiftCategory;
import com.dental.clinic.management.workforce_management.infrastructure.persistence.mapper.WorkShiftMapper;
import com.dental.clinic.management.workforce_management.infrastructure.persistence.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case implementation for managing work shifts.
 * Implements all business logic and validation rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkShiftUseCase implements WorkShiftService {

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

    @Override
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

    @Override
    public List<WorkShiftResponse> getAllActiveWorkShifts() {
        List<WorkShift> workShifts = workShiftRepository.findByIsActive(true);
        return workShifts.stream()
                .map(workShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WorkShiftResponse getWorkShiftById(String workShiftId) {
        WorkShift workShift = workShiftRepository.findByWorkShiftIdAndIsActive(workShiftId, true)
                .orElseThrow(() -> new WorkShiftNotFoundException("Work shift not found: " + workShiftId));
        return workShiftMapper.toResponse(workShift);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('UPDATE_WORK_SHIFTS')")
    public WorkShiftResponse updateWorkShift(String workShiftId, CreateWorkShiftRequest request) {
        log.info("Updating work shift: {}", workShiftId);

        WorkShift existingWorkShift = workShiftRepository.findByWorkShiftIdAndIsActive(workShiftId, true)
                .orElseThrow(() -> new WorkShiftNotFoundException("Work shift not found: " + workShiftId));

        // Validation 1: Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Validation 2: Validate duration (3-8 hours)
        double duration = calculateDuration(request.getStartTime(), request.getEndTime());
        validateDuration(duration);

        // Validation 3: Validate working hours (8:00 - 21:00)
        validateWorkingHours(request.getStartTime(), request.getEndTime());

        // Validation 4: Validate night shift category
        validateNightShiftCategory(request.getStartTime(), request.getCategory());

        // Update entity
        existingWorkShift.setShiftName(request.getShiftName());
        existingWorkShift.setStartTime(request.getStartTime());
        existingWorkShift.setEndTime(request.getEndTime());
        existingWorkShift.setCategory(request.getCategory());

        WorkShift savedWorkShift = workShiftRepository.save(existingWorkShift);

        log.info("Successfully updated work shift: {}", savedWorkShift.getWorkShiftId());
        return workShiftMapper.toResponse(savedWorkShift);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('DELETE_WORK_SHIFTS')")
    public void deactivateWorkShift(String workShiftId) {
        log.info("Deactivating work shift: {}", workShiftId);

        WorkShift workShift = workShiftRepository.findByWorkShiftIdAndIsActive(workShiftId, true)
                .orElseThrow(() -> new WorkShiftNotFoundException("Work shift not found: " + workShiftId));

        workShift.setIsActive(false);
        workShiftRepository.save(workShift);

        log.info("Successfully deactivated work shift: {}", workShiftId);
    }

    // Validation methods
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private void validateDuration(double duration) {
        if (duration < MIN_DURATION_HOURS || duration > MAX_DURATION_HOURS) {
            throw new IllegalArgumentException(
                    String.format("Work shift duration must be between %.1f and %.1f hours, but was %.1f hours",
                            MIN_DURATION_HOURS, MAX_DURATION_HOURS, duration));
        }
    }

    private void validateWorkingHours(LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(CLINIC_OPEN) || endTime.isAfter(CLINIC_CLOSE)) {
            throw new IllegalArgumentException(
                    String.format("Work shift must be within clinic hours (%s - %s)",
                            CLINIC_OPEN, CLINIC_CLOSE));
        }
    }

    private void validateNightShiftCategory(LocalTime startTime, WorkShiftCategory category) {
        boolean isNightShift = startTime.isAfter(NIGHT_SHIFT_START) || startTime.equals(NIGHT_SHIFT_START);
        if (isNightShift && category != WorkShiftCategory.NIGHT) {
            throw new IllegalArgumentException("Shifts starting at or after 18:00 must be categorized as NIGHT shift");
        }
        if (!isNightShift && category == WorkShiftCategory.NIGHT) {
            throw new IllegalArgumentException("Shifts starting before 18:00 cannot be categorized as NIGHT shift");
        }
    }

    private double calculateDuration(LocalTime startTime, LocalTime endTime) {
        long startSeconds = startTime.toSecondOfDay();
        long endSeconds = endTime.toSecondOfDay();

        // Handle case where shift crosses midnight
        if (endSeconds <= startSeconds) {
            endSeconds += 24 * 3600; // Add 24 hours
        }

        long durationSeconds = endSeconds - startSeconds;
        double durationHours = durationSeconds / 3600.0; // Convert to hours

        // Subtract lunch break (12:00-13:00) if shift spans across it
        if (!startTime.isAfter(LUNCH_BREAK_START) && !endTime.isBefore(LUNCH_BREAK_END)) {
            durationHours -= LUNCH_BREAK_HOURS; // Subtract 1 hour for lunch break
        }

        return durationHours;
    }

    private String generateWorkShiftId(LocalTime startTime, LocalTime endTime) {
        String timePrefix = String.format("%02d%02d_%02d%02d",
                startTime.getHour(), startTime.getMinute(),
                endTime.getHour(), endTime.getMinute());

        // Find existing shifts with same time pattern
        List<WorkShift> existingShifts = workShiftRepository.findByWorkShiftIdStartingWith("WKS_" + timePrefix);

        int sequenceNumber = existingShifts.size() + 1;

        return String.format("WKS_%s_%03d", timePrefix, sequenceNumber);
    }
}
