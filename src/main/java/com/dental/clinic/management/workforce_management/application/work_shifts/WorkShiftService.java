package com.dental.clinic.management.workforce_management.application.work_shifts;

import com.dental.clinic.management.exception.*;
import com.dental.clinic.management.workforce_management.domain.work_shifts.WorkShift;
import com.dental.clinic.management.workforce_management.domain.work_shifts.enums.WorkShiftCategory;
import com.dental.clinic.management.workforce_management.infrastructure.work_shifts.WorkShiftRepository;
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
        WorkShift workShift = WorkShiftMapper.toEntity(request);
        workShift.setWorkShiftId(generatedId);

        WorkShift savedWorkShift = workShiftRepository.save(workShift);

        log.info("Successfully created work shift: {}", savedWorkShift.getWorkShiftId());
        return workShiftMapper.toResponse(savedWorkShift);
    }

    // ... existing code ...
}
