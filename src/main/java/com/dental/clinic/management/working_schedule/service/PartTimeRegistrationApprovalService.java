package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.working_schedule.domain.PartTimeRegistration;
import com.dental.clinic.management.working_schedule.domain.PartTimeSlot;
import com.dental.clinic.management.working_schedule.enums.RegistrationStatus;
import com.dental.clinic.management.working_schedule.exception.RegistrationNotFoundException;
import com.dental.clinic.management.working_schedule.exception.RegistrationInvalidStateException;
import com.dental.clinic.management.working_schedule.exception.QuotaExceededException;
import com.dental.clinic.management.working_schedule.exception.SlotNotFoundException;
import com.dental.clinic.management.working_schedule.repository.PartTimeRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing approval/rejection of part-time registration requests.
 * 
 * NEW SPECIFICATION:
 * - Manager approves/rejects pending registrations
 * - Validates quota before approval
 * - Requires reason for rejection
 * - Only APPROVED registrations count toward quota
 */
@Service
public class PartTimeRegistrationApprovalService {

    private static final Logger log = LoggerFactory.getLogger(PartTimeRegistrationApprovalService.class);

    private final PartTimeRegistrationRepository registrationRepository;
    private final PartTimeSlotRepository slotRepository;
    private final PartTimeSlotAvailabilityService availabilityService;

    public PartTimeRegistrationApprovalService(PartTimeRegistrationRepository registrationRepository,
            PartTimeSlotRepository slotRepository,
            PartTimeSlotAvailabilityService availabilityService) {
        this.registrationRepository = registrationRepository;
        this.slotRepository = slotRepository;
        this.availabilityService = availabilityService;
    }

    /**
     * Approve a pending registration.
     * 
     * Validations:
     * 1. Registration must exist and be PENDING
     * 2. Slot must still be active
     * 3. Quota must not be exceeded for ANY working day
     * 
     * @param registrationId The registration ID
     * @param managerId      The manager approving
     * @throws RegistrationNotFoundException if not found or not pending
     * @throws IllegalStateException         if quota would be exceeded
     */
    public void approveRegistration(Integer registrationId, Integer managerId) {
        log.info("Manager {} approving registration {}", managerId, registrationId);

        // Retry loop for optimistic locking races. We'll attempt the transactional
        // approve up to 3 times.
        int maxAttempts = 3;
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                attemptApproveTransactional(registrationId, managerId);
                // success
                return;
            } catch (OptimisticLockingFailureException e) {
                log.warn("Optimistic locking failure on approve attempt {}/{} for registration {}: {}", attempt,
                        maxAttempts, registrationId, e.getMessage());
                if (attempt >= maxAttempts) {
                    // rethrow as runtime so caller sees failure
                    throw e;
                }
                // small backoff
                try {
                    Thread.sleep(50L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying approval", ie);
                }
                // retry
            }
        }
    }

    /**
     * Single transactional attempt to validate and approve a registration.
     * Keeping this method @Transactional ensures each attempt runs in its own
     * transaction
     * so optimistic locking is effective.
     */
    @Transactional
    protected void attemptApproveTransactional(Integer registrationId, Integer managerId) {
        PartTimeRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId.toString()));

        // Validate status
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new RegistrationInvalidStateException(registrationId, registration.getStatus().name());
        }

        // Validate slot exists and is active
        PartTimeSlot slot = slotRepository.findById(registration.getPartTimeSlotId())
                .orElseThrow(() -> new SlotNotFoundException(registration.getPartTimeSlotId()));

        if (!slot.getIsActive()) {
            throw new RegistrationInvalidStateException(registrationId, "SLOT_INACTIVE");
        }

        // Validate quota for all working days
        validateQuotaBeforeApproval(registration, slot);

        // Approve
        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setProcessedBy(managerId);
        registration.setProcessedAt(LocalDateTime.now());
        registrationRepository.save(registration);

        log.info("Registration {} approved by manager {}", registrationId, managerId);
    }

    /**
     * Reject a pending registration.
     * 
     * @param registrationId The registration ID
     * @param managerId      The manager rejecting
     * @param reason         The rejection reason (REQUIRED)
     * @throws RegistrationNotFoundException if not found or not pending
     * @throws IllegalArgumentException      if reason is empty
     */
    @Transactional
    public void rejectRegistration(Integer registrationId, Integer managerId, String reason) {
        log.info("Manager {} rejecting registration {} with reason: {}", managerId, registrationId, reason);

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        PartTimeRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId.toString()));

        // Validate status
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new RegistrationInvalidStateException(registrationId, registration.getStatus().name());
        }

        // Reject
        registration.setStatus(RegistrationStatus.REJECTED);
        registration.setReason(reason.trim());
        registration.setProcessedBy(managerId);
        registration.setProcessedAt(LocalDateTime.now());
        registrationRepository.save(registration);

        log.info("Registration {} rejected by manager {}", registrationId, managerId);
    }

    /**
     * Validate that approving this registration won't exceed quota on any day.
     * 
     * Logic:
     * 1. Get all working days for this registration
     * 2. For each day, count current approved registrations
     * 3. If any day would exceed quota, throw exception
     * 
     * Example:
     * - Slot quota: 2
     * - Registration: 2025-11-17 to 2025-11-30 (FRIDAY, SATURDAY)
     * - Working days: 11/22, 11/23, 11/29, 11/30
     * - Check each day: if any has 2+ approved, reject
     * 
     * @param registration The registration to approve
     * @param slot         The slot being registered for
     * @throws IllegalStateException if quota would be exceeded
     */
    private void validateQuotaBeforeApproval(PartTimeRegistration registration, PartTimeSlot slot) {
        List<LocalDate> workingDays;
        if (registration.getRequestedDates() != null && !registration.getRequestedDates().isEmpty()) {
            workingDays = java.util.List.copyOf(registration.getRequestedDates());
        } else {
            workingDays = availabilityService.getWorkingDays(
                    slot,
                    registration.getEffectiveFrom(),
                    registration.getEffectiveTo());
        }

        for (LocalDate workingDay : workingDays) {
            long currentRegistered = availabilityService.getRegisteredCountForDate(
                    slot.getSlotId(),
                    workingDay);

            if (currentRegistered >= slot.getQuota()) {
                // throw structured exception so GlobalExceptionHandler returns 409 with details
                throw new QuotaExceededException(slot.getSlotId(), workingDay, currentRegistered, slot.getQuota());
            }
        }

        log.debug("Quota validation passed for registration {}", registration.getRegistrationId());
    }

    /**
     * Get all pending registrations (for manager approval list).
     * Ordered by creation date (oldest first).
     * 
     * @return List of pending registrations
     */
    @Transactional(readOnly = true)
    public List<PartTimeRegistration> getPendingRegistrations() {
        return registrationRepository.findByStatusOrderByCreatedAtAsc(RegistrationStatus.PENDING);
    }

    /**
     * Get pending registrations for a specific slot.
     * 
     * @param slotId The slot ID
     * @return List of pending registrations for that slot
     */
    @Transactional(readOnly = true)
    public List<PartTimeRegistration> getPendingRegistrationsForSlot(Long slotId) {
        return registrationRepository.findByPartTimeSlotIdAndStatus(slotId, RegistrationStatus.PENDING);
    }

    /**
     * Check if a registration can be approved (quota check).
     * Returns true if approval won't exceed quota.
     * 
     * @param registrationId The registration ID
     * @return true if can be approved
     */
    @Transactional(readOnly = true)
    public boolean canApprove(Integer registrationId) {
        try {
            PartTimeRegistration registration = registrationRepository.findById(registrationId)
                    .orElse(null);

            if (registration == null || registration.getStatus() != RegistrationStatus.PENDING) {
                return false;
            }

            PartTimeSlot slot = slotRepository.findById(registration.getPartTimeSlotId())
                    .orElse(null);

            if (slot == null || !slot.getIsActive()) {
                return false;
            }

            validateQuotaBeforeApproval(registration, slot);
            return true;
        } catch (Exception e) {
            log.debug("Cannot approve registration {}: {}", registrationId, e.getMessage());
            return false;
        }
    }
}
