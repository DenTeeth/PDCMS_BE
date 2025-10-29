package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.exception.*;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.CreateWorkShiftRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateWorkShiftRequest;
import com.dental.clinic.management.working_schedule.dto.response.WorkShiftResponse;
import com.dental.clinic.management.working_schedule.enums.WorkShiftCategory;
import com.dental.clinic.management.working_schedule.exception.TimeOfDayMismatchException;
import com.dental.clinic.management.working_schedule.mapper.WorkShiftMapper;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeSlotRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import com.dental.clinic.management.working_schedule.utils.WorkShiftIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final EmployeeShiftRepository employeeShiftRepository;
    private final PartTimeSlotRepository partTimeSlotRepository;
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
     * Category is auto-generated based on time range.
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

        // Validation 4: Prevent shifts spanning across 18:00 boundary
        validateShiftDoesNotSpanBoundary(request.getStartTime(), request.getEndTime());

        // Auto-generate category based on time range
        WorkShiftCategory autoCategory = determineCategoryByTime(request.getStartTime(), request.getEndTime());
        log.info("Auto-generated category: {} for shift {}-{}", autoCategory, request.getStartTime(), request.getEndTime());

        // Generate work shift ID
        String generatedId = generateWorkShiftId(request.getStartTime(), request.getEndTime());
        log.info("Generated work shift ID: {}", generatedId);

        // Create and save entity
        WorkShift workShift = workShiftMapper.toEntity(request);
        workShift.setWorkShiftId(generatedId);
        workShift.setCategory(autoCategory); // Set auto-generated category
        
        WorkShift savedWorkShift = workShiftRepository.save(workShift);

        log.info("Successfully created work shift: {} with category: {}", savedWorkShift.getWorkShiftId(), savedWorkShift.getCategory());
        return workShiftMapper.toResponse(savedWorkShift);
    }

    /**
     * Update an existing work shift.
     * Category is auto-updated based on time changes.
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

        // Check if time is being changed
        boolean isTimeChanging = request.getStartTime() != null || request.getEndTime() != null;
        
        // Prevent time changes if work shift is in use by employee schedules OR registrations
        if (isTimeChanging && isWorkShiftInUse(workShiftId)) {
            String usageDetails = getWorkShiftUsageDetails(workShiftId);
            throw new ShiftInUseException(workShiftId, usageDetails);
        }

        // Determine final values (use new values if provided, otherwise keep existing)
        LocalTime finalStartTime = request.getStartTime() != null ? request.getStartTime() : workShift.getStartTime();
        LocalTime finalEndTime = request.getEndTime() != null ? request.getEndTime() : workShift.getEndTime();

        // Apply all validations with final values
        validateTimeRange(finalStartTime, finalEndTime);
        
        double duration = calculateDuration(finalStartTime, finalEndTime);
        validateDuration(duration);
        
        validateWorkingHours(finalStartTime, finalEndTime);
        validateShiftDoesNotSpanBoundary(finalStartTime, finalEndTime);

        // Auto-update category if time changed
        if (isTimeChanging) {
            WorkShiftCategory newCategory = determineCategoryByTime(finalStartTime, finalEndTime);
            
            // Prevent category changes that would conflict with shift ID semantic meaning
            if (workShift.getCategory() != newCategory) {
                throw new CategoryChangeForbiddenException(
                    workShiftId, 
                    workShift.getCategory().toString(), 
                    newCategory.toString()
                );
            }
            
            // Prevent time-of-day changes that conflict with shift ID prefix
            // Example: WKS_MORNING_03 cannot be updated to afternoon hours (14:00-18:00)
            String expectedTimeOfDay = WorkShiftIdGenerator.extractTimeOfDay(workShiftId);
            String actualTimeOfDay = determineTimeOfDayFromStartTime(finalStartTime);
            
            if (expectedTimeOfDay != null && !expectedTimeOfDay.equals(actualTimeOfDay)) {
                throw new TimeOfDayMismatchException(workShiftId, expectedTimeOfDay, actualTimeOfDay);
            }
            
            log.info("Category remains {} after time update", workShift.getCategory());
        }

        // Update entity
        workShiftMapper.updateEntity(workShift, request);
        
        WorkShift updatedWorkShift = workShiftRepository.save(workShift);

        log.info("Successfully updated work shift: {} with category: {}", workShiftId, updatedWorkShift.getCategory());
        return workShiftMapper.toResponse(updatedWorkShift);
    }

    /**
     * Delete (soft delete) a work shift.
     * Prevents deletion if work shift is in use by employee schedules.
     * @param workShiftId Work shift ID
     */
    @Transactional
    @PreAuthorize("hasAuthority('DELETE_WORK_SHIFTS')")
    public void deleteWorkShift(String workShiftId) {
        log.info("Deleting work shift: {}", workShiftId);

        // Find existing work shift
        WorkShift workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new WorkShiftNotFoundException(workShiftId));

        // Check if work shift is in use by employee schedules OR registrations
        if (isWorkShiftInUse(workShiftId)) {
            String usageDetails = getWorkShiftUsageDetails(workShiftId);
            throw new ShiftInUseException(workShiftId, usageDetails);
        }

        // Soft delete
        workShift.setIsActive(false);
        workShiftRepository.save(workShift);

        log.info("Successfully deleted work shift: {}", workShiftId);
    }

    /**
     * Reactivate a soft-deleted work shift (Issue 7).
     * @param workShiftId Work shift ID
     * @return WorkShiftResponse
     */
    @Transactional
    @PreAuthorize("hasAuthority('UPDATE_WORK_SHIFTS')")
    public WorkShiftResponse reactivateWorkShift(String workShiftId) {
        log.info("Reactivating work shift: {}", workShiftId);

        // Find existing work shift (including inactive ones)
        WorkShift workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new WorkShiftNotFoundException(workShiftId));

        // Check if already active
        if (Boolean.TRUE.equals(workShift.getIsActive())) {
            throw new IllegalStateException(
                String.format("Work shift %s is already active", workShiftId)
            );
        }

        // Reactivate
        workShift.setIsActive(true);
        WorkShift reactivatedWorkShift = workShiftRepository.save(workShift);

        log.info("Successfully reactivated work shift: {}", workShiftId);
        return workShiftMapper.toResponse(reactivatedWorkShift);
    }

    /**
     * Get all work shifts with advanced filtering, searching, and sorting.
     * @param isActive Optional filter by active status
     * @param category Optional filter by category (NORMAL/NIGHT)
     * @param search Optional search keyword for shift name
     * @param sortBy Optional sort field (startTime, category, shiftName)
     * @param sortDirection Optional sort direction (ASC/DESC), defaults to ASC
     * @return List of WorkShiftResponse
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_WORK_SHIFTS')")
    public List<WorkShiftResponse> getAllWorkShifts(
            Boolean isActive, 
            WorkShiftCategory category,
            String search,
            String sortBy,
            String sortDirection) {
        
        log.info("Fetching work shifts - isActive: {}, category: {}, search: {}, sortBy: {}, sortDirection: {}", 
                 isActive, category, search, sortBy, sortDirection);

        // Build dynamic query using Specification (Issue 11, 12)
        Specification<WorkShift> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by isActive
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }
            
            // Filter by category
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            
            // Search by shift name (case-insensitive)
            if (search != null && !search.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("shiftName")), 
                    "%" + search.toLowerCase() + "%"
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Execute query
        List<WorkShift> workShifts = workShiftRepository.findAll(spec);
        
        // Convert to response DTOs
        List<WorkShiftResponse> responses = workShifts.stream()
                .map(workShiftMapper::toResponse)
                .collect(Collectors.toList());
        
        // Apply sorting (Issue 10, 12)
        responses = applySorting(responses, sortBy, sortDirection);
        
        log.info("Retrieved {} work shifts", responses.size());
        return responses;
    }

    /**
     * Apply sorting to work shift responses.
     * Default: Sort by startTime ASC, then category (NORMAL before NIGHT).
     * 
     * @param responses List of work shift responses
     * @param sortBy Sort field (startTime, category, shiftName, or null for default)
     * @param sortDirection Sort direction (ASC/DESC, or null for ASC)
     * @return Sorted list
     */
    private List<WorkShiftResponse> applySorting(
            List<WorkShiftResponse> responses, 
            String sortBy, 
            String sortDirection) {
        
        boolean isAscending = sortDirection == null || sortDirection.equalsIgnoreCase("ASC");
        
        if (sortBy == null || sortBy.equalsIgnoreCase("startTime")) {
            // Default: Sort by startTime, then category
            Comparator<WorkShiftResponse> comparator = Comparator
                    .comparing(WorkShiftResponse::getStartTime)
                    .thenComparing(WorkShiftResponse::getCategory);
            
            return isAscending 
                    ? responses.stream().sorted(comparator).collect(Collectors.toList())
                    : responses.stream().sorted(comparator.reversed()).collect(Collectors.toList());
            
        } else if (sortBy.equalsIgnoreCase("category")) {
            // Sort by category, then startTime
            Comparator<WorkShiftResponse> comparator = Comparator
                    .comparing(WorkShiftResponse::getCategory)
                    .thenComparing(WorkShiftResponse::getStartTime);
            
            return isAscending 
                    ? responses.stream().sorted(comparator).collect(Collectors.toList())
                    : responses.stream().sorted(comparator.reversed()).collect(Collectors.toList());
            
        } else if (sortBy.equalsIgnoreCase("shiftName")) {
            // Sort by shift name
            Comparator<WorkShiftResponse> comparator = Comparator
                    .comparing(WorkShiftResponse::getShiftName);
            
            return isAscending 
                    ? responses.stream().sorted(comparator).collect(Collectors.toList())
                    : responses.stream().sorted(comparator.reversed()).collect(Collectors.toList());
        }
        
        // Fallback: return as-is
        return responses;
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
     * Validate that shifts do not span across the 18:00 boundary.
     * A shift is either fully NORMAL (ends <= 18:00) or fully NIGHT (starts >= 18:00).
     * Shifts spanning across 18:00 are ambiguous and not allowed.
     * 
     * @param startTime Shift start time
     * @param endTime Shift end time
     * @throws InvalidCategoryException if shift spans the 18:00 boundary
     */
    private void validateShiftDoesNotSpanBoundary(LocalTime startTime, LocalTime endTime) {
        boolean startsBeforeBoundary = startTime.isBefore(NIGHT_SHIFT_START);
        boolean endsAfterBoundary = endTime.isAfter(NIGHT_SHIFT_START);
        
        if (startsBeforeBoundary && endsAfterBoundary) {
            throw new InvalidCategoryException(
                String.format("Ca làm việc không được vượt qua ranh giới 18:00. " +
                              "Ca của bạn: %s - %s. " +
                              "Vui lòng tạo ca THƯỜNG (kết thúc trước hoặc đúng 18:00) " +
                              "hoặc ca ĐÊM (bắt đầu từ 18:00 trở đi).", 
                              startTime, endTime)
            );
        }
    }

    /**
     * Determine time-of-day category from start time for shift ID validation.
     * Uses the same logic as WorkShiftIdGenerator.
     * - MORNING: starts between 08:00-11:59
     * - AFTERNOON: starts between 12:00-17:59
     * - EVENING: starts between 18:00-20:59
     * 
     * @param startTime Shift start time
     * @return Time of day category (MORNING, AFTERNOON, EVENING)
     */
    private String determineTimeOfDayFromStartTime(LocalTime startTime) {
        LocalTime afternoonStart = LocalTime.of(12, 0);
        LocalTime eveningStart = LocalTime.of(18, 0);
        
        if (startTime.compareTo(eveningStart) >= 0) {
            return "EVENING";
        } else if (startTime.compareTo(afternoonStart) >= 0) {
            return "AFTERNOON";
        } else {
            return "MORNING";
        }
    }

    /**
     * Determine the appropriate category based on time range.
     * NIGHT if starts >= 18:00, otherwise NORMAL.
     * This should only be called after validateShiftDoesNotSpanBoundary.
     * 
     * @param startTime Shift start time
     * @param endTime Shift end time
     * @return Auto-determined WorkShiftCategory
     */
    private WorkShiftCategory determineCategoryByTime(LocalTime startTime, LocalTime endTime) {
        return startTime.compareTo(NIGHT_SHIFT_START) >= 0 
                ? WorkShiftCategory.NIGHT 
                : WorkShiftCategory.NORMAL;
    }

    /**
     * Check if a work shift template is currently in use by employee schedules or part-time slots.
     * V2: Checks BOTH employee_shifts (full-time schedules) AND part_time_slots (which link to registrations).
     * 
     * @param workShiftId Work shift ID
     * @return true if work shift is in use by any schedule or slot
     */
    private boolean isWorkShiftInUse(String workShiftId) {
        boolean usedBySchedules = employeeShiftRepository.existsByWorkShiftId(workShiftId);
        boolean usedBySlots = partTimeSlotRepository.existsByWorkShiftId(workShiftId);
        return usedBySchedules || usedBySlots;
    }

    /**
     * Get detailed usage count for a work shift (schedules + slots).
     * V2: Shows full-time schedules and part-time slots count.
     * 
     * @param workShiftId Work shift ID
     * @return Usage count message showing schedules and slots
     */
    private String getWorkShiftUsageDetails(String workShiftId) {
        long scheduleCount = employeeShiftRepository.countByWorkShiftId(workShiftId);
        long slotCount = partTimeSlotRepository.countByWorkShiftId(workShiftId);
        
        if (scheduleCount > 0 && slotCount > 0) {
            return scheduleCount + " lịch làm việc và " + slotCount + " slot bán thời gian";
        } else if (scheduleCount > 0) {
            return scheduleCount + " lịch làm việc";
        } else {
            return slotCount + " slot bán thời gian";
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
