package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.working_schedule.domain.PartTimeRegistration;
import com.dental.clinic.management.working_schedule.domain.PartTimeSlot;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.CreatePartTimeSlotRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdatePartTimeSlotRequest;
import com.dental.clinic.management.working_schedule.dto.response.PartTimeSlotDetailResponse;
import com.dental.clinic.management.working_schedule.dto.response.PartTimeSlotResponse;
import com.dental.clinic.management.working_schedule.exception.QuotaViolationException;
import com.dental.clinic.management.working_schedule.exception.SlotNotFoundException;
import com.dental.clinic.management.working_schedule.repository.PartTimeRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeSlotRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import com.dental.clinic.management.exception.work_shift.WorkShiftNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartTimeSlotService {

    private static final Logger log = LoggerFactory.getLogger(PartTimeSlotService.class);

    private final PartTimeSlotRepository partTimeSlotRepository;
    private final WorkShiftRepository workShiftRepository;
    private final PartTimeRegistrationRepository registrationRepository;
    private final EmployeeRepository employeeRepository;

    public PartTimeSlotService(PartTimeSlotRepository partTimeSlotRepository,
            WorkShiftRepository workShiftRepository,
            PartTimeRegistrationRepository registrationRepository,
            EmployeeRepository employeeRepository) {
        this.partTimeSlotRepository = partTimeSlotRepository;
        this.workShiftRepository = workShiftRepository;
        this.registrationRepository = registrationRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new part-time slot.
     * 
     * NEW SPECIFICATION:
     * - Requires effectiveFrom and effectiveTo
     * - Supports multiple days (comma-separated)
     * - Validates date range
     */
    @Transactional
    @PreAuthorize("hasAuthority('MANAGE_WORK_SLOTS')")
    public PartTimeSlotResponse createSlot(CreatePartTimeSlotRequest request) {
        log.info("Creating part-time slot: shift={}, days={}, effectiveFrom={}, effectiveTo={}, quota={}",
                request.getWorkShiftId(), request.getDayOfWeek(),
                request.getEffectiveFrom(), request.getEffectiveTo(), request.getQuota());

        // Validate work shift exists
        WorkShift workShift = workShiftRepository.findById(request.getWorkShiftId())
                .orElseThrow(() -> new WorkShiftNotFoundException(request.getWorkShiftId()));

        // NEW: Validate date range
        if (request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new IllegalArgumentException("Effective to date must be after effective from date");
        }

        // NEW: Validate effective from is not in the past
        if (request.getEffectiveFrom().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Effective from date cannot be in the past");
        }

        // NEW: Validate day of week format (accept comma-separated values)
        String normalizedDayOfWeek = request.getDayOfWeek().toUpperCase().trim();
        validateDaysOfWeek(normalizedDayOfWeek);

        // Note: We no longer check for unique constraint since slots can have same
        // shift+day
        // but different date ranges. The combination of shift+day+dates should be
        // unique.
        // This validation could be added if needed.

        // Create slot
        PartTimeSlot slot = new PartTimeSlot();
        slot.setWorkShiftId(request.getWorkShiftId());
        slot.setDayOfWeek(normalizedDayOfWeek);
        slot.setEffectiveFrom(request.getEffectiveFrom());
        slot.setEffectiveTo(request.getEffectiveTo());
        slot.setQuota(request.getQuota());
        slot.setIsActive(true);

        PartTimeSlot savedSlot = partTimeSlotRepository.save(slot);
        log.info("Created slot with ID: {} for days {} from {} to {}",
                savedSlot.getSlotId(), normalizedDayOfWeek,
                request.getEffectiveFrom(), request.getEffectiveTo());

        return buildResponse(savedSlot, workShift.getShiftName());
    }

    /**
     * Validate day of week string.
     * Supports single day (FRIDAY) or multiple days (FRIDAY,SATURDAY).
     * 
     * @param dayOfWeek The day of week string to validate
     * @throws IllegalArgumentException if invalid
     */
    private void validateDaysOfWeek(String dayOfWeek) {
        String[] days = dayOfWeek.split(",");
        for (String day : days) {
            String trimmedDay = day.trim();
            if (trimmedDay.isEmpty()) {
                throw new IllegalArgumentException("Day of week cannot be empty");
            }

            try {
                java.time.DayOfWeek.valueOf(trimmedDay);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid day of week: " + trimmedDay +
                        ". Valid values: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
            }

            // Optional: Reject SUNDAY if clinic doesn't work on Sundays
            if ("SUNDAY".equals(trimmedDay)) {
                log.warn("Creating slot for SUNDAY - verify this is intended");
            }
        }
    }

    /**
     * Get all slots with registration counts.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_WORK_SLOTS')")
    public List<PartTimeSlotResponse> getAllSlots() {
        log.info("Fetching all part-time slots");

        return partTimeSlotRepository.findAll().stream()
                .map(slot -> {
                    WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId())
                            .orElse(null);
                    String shiftName = workShift != null ? workShift.getShiftName() : "Unknown";
                    return buildResponse(slot, shiftName);
                })
                .collect(Collectors.toList());
    }

    /**
     * Update slot quota and isActive status.
     */
    @Transactional
    @PreAuthorize("hasAuthority('MANAGE_WORK_SLOTS')")
    public PartTimeSlotResponse updateSlot(Long slotId, UpdatePartTimeSlotRequest request) {
        log.info("Updating slot {}: quota={}, isActive={}", slotId, request.getQuota(), request.getIsActive());

        PartTimeSlot slot = partTimeSlotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException(slotId));

        // Check quota violation - NEW: Use countApprovedRegistrations
        long currentRegistered = partTimeSlotRepository.countApprovedRegistrations(slotId);
        if (request.getQuota() < currentRegistered) {
            throw new QuotaViolationException(slotId, request.getQuota(), currentRegistered);
        }

        slot.setQuota(request.getQuota());
        slot.setIsActive(request.getIsActive());

        PartTimeSlot updatedSlot = partTimeSlotRepository.save(slot);
        log.info("Updated slot {}", slotId);

        WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId()).orElse(null);
        String shiftName = workShift != null ? workShift.getShiftName() : "Unknown";

        return buildResponse(updatedSlot, shiftName);
    }

    /**
     * Get slot detail with list of registered employees.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_WORK_SLOTS')")
    public PartTimeSlotDetailResponse getSlotDetail(Long slotId) {
        log.info("Fetching detail for slot {}", slotId);

        PartTimeSlot slot = partTimeSlotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException(slotId));

        WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId()).orElse(null);
        String shiftName = workShift != null ? workShift.getShiftName() : "Unknown";

        // Get all active registrations for this slot
        List<PartTimeRegistration> registrations = registrationRepository
                .findByPartTimeSlotIdAndIsActive(slotId, true);

        // Build employee info list
        List<PartTimeSlotDetailResponse.RegisteredEmployeeInfo> employeeInfos = registrations.stream()
                .map(reg -> {
                    Employee employee = employeeRepository.findById(reg.getEmployeeId()).orElse(null);
                    return new PartTimeSlotDetailResponse.RegisteredEmployeeInfo(
                            reg.getEmployeeId(),
                            employee != null ? employee.getEmployeeCode() : "Unknown",
                            employee != null ? employee.getFullName() : "Unknown",
                            reg.getEffectiveFrom().toString(),
                            reg.getEffectiveTo().toString());
                })
                .collect(Collectors.toList());

        // NEW: Count only APPROVED registrations
        long registered = partTimeSlotRepository.countApprovedRegistrations(slotId);

        return new PartTimeSlotDetailResponse(
                slot.getSlotId(),
                slot.getWorkShiftId(),
                shiftName,
                slot.getDayOfWeek(),
                slot.getQuota(),
                registered,
                slot.getIsActive(),
                employeeInfos);
    }

    private PartTimeSlotResponse buildResponse(PartTimeSlot slot, String shiftName) {
        // NEW: Count only APPROVED registrations
        long registered = partTimeSlotRepository.countApprovedRegistrations(slot.getSlotId());

        return new PartTimeSlotResponse(
                slot.getSlotId(),
                slot.getWorkShiftId(),
                shiftName,
                slot.getDayOfWeek(),
                slot.getQuota(),
                registered,
                slot.getIsActive(),
                slot.getEffectiveFrom(),
                slot.getEffectiveTo());
    }
}
