package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.enums.EmploymentType;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.working_schedule.domain.PartTimeRegistration;
import com.dental.clinic.management.working_schedule.domain.PartTimeSlot;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.CreateRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateEffectiveToRequest;
import com.dental.clinic.management.working_schedule.dto.response.AvailableSlotResponse;
import com.dental.clinic.management.working_schedule.dto.response.RegistrationResponse;
import com.dental.clinic.management.working_schedule.exception.*;
import com.dental.clinic.management.working_schedule.repository.PartTimeRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeSlotRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeShiftRegistrationService {

    private final PartTimeRegistrationRepository registrationRepository;
    private final PartTimeSlotRepository slotRepository;
    private final WorkShiftRepository workShiftRepository;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final EntityManager entityManager;

    /**
     * Get available slots for employee to claim.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_AVAILABLE_SLOTS')")
    public List<AvailableSlotResponse> getAvailableSlots() {
        Integer employeeId = getCurrentEmployeeId();
        log.info("Fetching available slots for employee {}", employeeId);

        // Get slots employee already registered (from part_time_registrations)
        List<Long> registeredSlotIds = registrationRepository.findByEmployeeIdAndIsActive(employeeId, true)
                .stream()
                .map(PartTimeRegistration::getPartTimeSlotId)
                .collect(Collectors.toList());

        // Get all active slots
        return slotRepository.findAll().stream()
                .filter(slot -> slot.getIsActive())
                .filter(slot -> !registeredSlotIds.contains(slot.getSlotId()))
                .map(slot -> {
                    long registered = slotRepository.countActiveRegistrations(slot.getSlotId());
                    int remaining = slot.getQuota() - (int) registered;

                    if (remaining <= 0) {
                        return null; // Slot is full
                    }

                    WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId()).orElse(null);
                    String shiftName = workShift != null ? workShift.getShiftName() : "Unknown";

                    return AvailableSlotResponse.builder()
                            .slotId(slot.getSlotId())
                            .shiftName(shiftName)
                            .dayOfWeek(slot.getDayOfWeek())
                            .remaining(remaining)
                            .build();
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    /**
     * Claim a slot (create registration) with pessimistic locking.
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREATE_REGISTRATION')")
    public RegistrationResponse claimSlot(CreateRegistrationRequest request) {
        Integer employeeId = getCurrentEmployeeId();
        log.info("Employee {} claiming slot {}", employeeId, request.getPartTimeSlotId());

        // Validate employee exists and is PART_TIME_FLEX
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalStateException("Employee not found: " + employeeId));

        // Only PART_TIME_FLEX employees can claim flexible slots
        if (employee.getEmploymentType() != EmploymentType.PART_TIME_FLEX) {
            log.warn("Employee {} with type {} attempted to claim flexible slot",
                    employeeId, employee.getEmploymentType());
            throw new IllegalArgumentException(
                    "Chỉ nhân viên PART_TIME_FLEX mới có thể đăng ký ca linh hoạt. " +
                            "Nhân viên FULL_TIME và PART_TIME_FIXED phải sử dụng đăng ký ca cố định.");
        }

        // Validate effectiveFrom not in past
        if (request.getEffectiveFrom().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Effective from date cannot be in the past");
        }

        // START TRANSACTION WITH PESSIMISTIC LOCK
        PartTimeSlot slot = entityManager.find(PartTimeSlot.class, request.getPartTimeSlotId(),
                LockModeType.PESSIMISTIC_WRITE);

        if (slot == null || !slot.getIsActive()) {
            throw new SlotNotFoundException(request.getPartTimeSlotId());
        }

        // Check quota
        long currentRegistered = slotRepository.countActiveRegistrations(slot.getSlotId());
        if (currentRegistered >= slot.getQuota()) {
            WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId()).orElse(null);
            String shiftName = workShift != null ? workShift.getShiftName() : "Unknown";
            throw new SlotIsFullException(slot.getSlotId(), shiftName, slot.getDayOfWeek());
        }

        // Check for registration conflicts
        List<PartTimeRegistration> activeRegistrations = registrationRepository
                .findByEmployeeIdAndIsActive(employeeId, true);

        for (PartTimeRegistration existingReg : activeRegistrations) {
            // Check 1: Can't register the same slot twice
            if (existingReg.getPartTimeSlotId().equals(slot.getSlotId())) {
                throw new RegistrationConflictException(employeeId);
            }

            // Check 2: Can't register conflicting time slots (same day + same shift)
            PartTimeSlot existingSlot = slotRepository.findById(existingReg.getPartTimeSlotId()).orElse(null);
            if (existingSlot != null) {
                boolean sameDayAndShift = existingSlot.getDayOfWeek().equals(slot.getDayOfWeek()) &&
                        existingSlot.getWorkShiftId().equals(slot.getWorkShiftId());
                if (sameDayAndShift) {
                    throw new RegistrationConflictException(employeeId);
                }
            }
        }

        // Calculate effectiveTo (3 months from effectiveFrom)
        LocalDate effectiveTo = request.getEffectiveFrom().plusMonths(3);

        // Create registration in part_time_registrations (Schema V14 - Luồng 2)
        PartTimeRegistration registration = PartTimeRegistration.builder()
                .employeeId(employeeId)
                .partTimeSlotId(slot.getSlotId())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(effectiveTo)
                .isActive(true)
                .build();

        PartTimeRegistration saved = registrationRepository.save(registration);
        log.info("Registration {} created for employee {} in part_time_registrations",
                saved.getRegistrationId(), employeeId);

        return buildResponse(saved, slot);
    }

    /**
     * Get all registrations (admin sees all, employee sees own).
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('UPDATE_REGISTRATIONS_ALL', 'VIEW_REGISTRATION_OWN')")
    public List<RegistrationResponse> getRegistrations(Integer filterEmployeeId) {
        boolean isAdmin = SecurityUtil.hasCurrentUserRole("ADMIN") ||
                SecurityUtil.hasCurrentUserPermission("UPDATE_REGISTRATIONS_ALL");

        log.info("Fetching registrations - admin: {}, filter: {}", isAdmin, filterEmployeeId);

        List<PartTimeRegistration> registrations;

        if (isAdmin && filterEmployeeId != null) {
            // Admin with filter sees ALL registrations (active + cancelled) for that
            // employee
            registrations = registrationRepository.findByEmployeeId(filterEmployeeId);
        } else if (isAdmin) {
            registrations = registrationRepository.findAll();
        } else {
            Integer currentEmployeeId = getCurrentEmployeeId();
            registrations = registrationRepository.findByEmployeeIdAndIsActive(currentEmployeeId, true);
        }

        return registrations.stream()
                .map(reg -> {
                    PartTimeSlot slot = slotRepository.findById(reg.getPartTimeSlotId()).orElse(null);
                    return buildResponse(reg, slot);
                })
                .collect(Collectors.toList());
    }

    /**
     * Cancel registration (soft delete).
     */
    @Transactional
    @PreAuthorize("hasAnyAuthority('UPDATE_REGISTRATIONS_ALL', 'CANCEL_REGISTRATION_OWN')")
    public void cancelRegistration(Integer registrationId) {
        boolean isAdmin = SecurityUtil.hasCurrentUserRole("ADMIN") ||
                SecurityUtil.hasCurrentUserPermission("UPDATE_REGISTRATIONS_ALL");
        Integer currentEmployeeId = getCurrentEmployeeId();

        log.info("Cancelling registration {} by employee {}", registrationId, currentEmployeeId);

        PartTimeRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId.toString()));

        // Check ownership if not admin
        if (!isAdmin && !registration.getEmployeeId().equals(currentEmployeeId)) {
            throw new RegistrationNotFoundException(registrationId.toString()); // Hide existence
        }

        // Check if already cancelled
        if (!registration.getIsActive()) {
            throw new RegistrationNotFoundException(registrationId.toString()); // Already cancelled
        }

        registration.setIsActive(false);
        registration.setEffectiveTo(LocalDate.now());
        registration.setUpdatedAt(LocalDateTime.now());
        registrationRepository.save(registration);

        log.info("Registration {} cancelled in part_time_registrations", registrationId);
    }

    /**
     * Update effectiveTo (admin only).
     */
    @Transactional
    @PreAuthorize("hasAuthority('UPDATE_REGISTRATIONS_ALL')")
    public RegistrationResponse updateEffectiveTo(Integer registrationId, UpdateEffectiveToRequest request) {
        log.info("Updating effectiveTo for registration {}", registrationId);

        PartTimeRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId.toString()));

        registration.setEffectiveTo(request.getEffectiveTo());
        registration.setUpdatedAt(LocalDateTime.now());
        PartTimeRegistration updated = registrationRepository.save(registration);

        PartTimeSlot slot = slotRepository.findById(updated.getPartTimeSlotId()).orElse(null);
        return buildResponse(updated, slot);
    }

    /**
     * Get current employee ID from security context.
     */
    private Integer getCurrentEmployeeId() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        return accountRepository.findOneByUsername(username)
                .map(account -> account.getEmployee().getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found for user: " + username));
    }

    private RegistrationResponse buildResponse(PartTimeRegistration registration, PartTimeSlot slot) {
        String shiftName = "Unknown";
        String dayOfWeek = "Unknown";

        if (slot != null) {
            WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId()).orElse(null);
            shiftName = workShift != null ? workShift.getShiftName() : "Unknown";
            dayOfWeek = slot.getDayOfWeek();
        }

        return RegistrationResponse.builder()
                .registrationId(registration.getRegistrationId())
                .employeeId(registration.getEmployeeId())
                .partTimeSlotId(registration.getPartTimeSlotId())
                .workShiftName(shiftName)
                .dayOfWeek(dayOfWeek)
                .effectiveFrom(registration.getEffectiveFrom())
                .effectiveTo(registration.getEffectiveTo())
                .isActive(registration.getIsActive())
                .build();
    }
}
