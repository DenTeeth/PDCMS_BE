package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.working_schedule.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.working_schedule.enums.RenewalStatus;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.ShiftRenewalRequestRepository;
import com.dental.clinic.management.working_schedule.service.ShiftRenewalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Job 3: Auto-detect expiring part-time registrations and create renewal
 * requests.
 *
 * Runs daily at 01:00 AM.
 * Finds registrations expiring in 7 days and creates renewal invitations.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DailyRenewalDetectionJob {

    private final EmployeeShiftRegistrationRepository registrationRepository;
    private final ShiftRenewalRequestRepository renewalRepository;
    private final ShiftRenewalService renewalService;

    private static final int DAYS_BEFORE_EXPIRY = 7;

    /**
     * Cron: 0 0 1 * * ?
     * - Runs at 01:00 AM every day
     * - Format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 1 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void detectExpiringRegistrations() {
        log.info("=== Starting Daily Renewal Detection Job ===");

        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(DAYS_BEFORE_EXPIRY);

        log.info("Looking for registrations expiring on: {}", expiryDate);

        try {
            // VALIDATION: Check if registration repository is accessible
            long totalRegistrations = registrationRepository.count();
            log.info("Validation passed: {} total registrations in database", totalRegistrations);

            if (totalRegistrations == 0) {
                log.info("No registrations found in database. Job completed with no actions.");
                return;
            }

            // 1. Find registrations expiring in 7 days
            List<EmployeeShiftRegistration> expiringRegistrations = registrationRepository
                    .findRegistrationsExpiringOn(expiryDate);

            log.info("Found {} registrations expiring on {}", expiringRegistrations.size(), expiryDate);

            if (expiringRegistrations.isEmpty()) {
                log.info("No expiring registrations found. Job completed successfully.");
                return;
            }

            // 2. Create renewal requests for each
            int renewalsCreated = 0;
            int skippedAlreadyExists = 0;
            int skippedDueToErrors = 0;

            for (EmployeeShiftRegistration registration : expiringRegistrations) {
                try {
                    // VALIDATION: Check if registration has valid ID
                    String registrationId = registration.getRegistrationId();
                    if (registrationId == null || registrationId.isBlank()) {
                        log.warn("Registration has invalid ID. Skipping.");
                        skippedDueToErrors++;
                        continue;
                    }

                    // VALIDATION: Check if employee ID exists
                    if (registration.getEmployeeId() == null) {
                        log.warn("Registration {} has no employee ID. Skipping.", registrationId);
                        skippedDueToErrors++;
                        continue;
                    }

                    // Check if renewal already exists
                    boolean alreadyExists = renewalRepository.existsByRegistrationIdAndStatus(
                            registrationId,
                            RenewalStatus.PENDING_ACTION);

                    if (alreadyExists) {
                        log.debug("Renewal already exists for registration {}. Skipping.", registrationId);
                        skippedAlreadyExists++;
                        continue;
                    }

                    // Create renewal request
                    renewalService.createRenewalRequest(registrationId);
                    renewalsCreated++;

                    log.info("Created renewal request for registration {} (Employee ID: {})",
                            registrationId, registration.getEmployeeId());

                } catch (Exception e) {
                    log.error("Failed to create renewal for registration {}: {}",
                            registration.getRegistrationId(), e.getMessage(), e);
                    skippedDueToErrors++;
                }
            }

            log.info("=== Daily Renewal Detection Job Completed ===");
            log.info("Total expiring registrations: {}", expiringRegistrations.size());
            log.info("Renewals created: {}", renewalsCreated);
            log.info("Skipped (already exists): {}", skippedAlreadyExists);
            log.info("Skipped (errors): {}", skippedDueToErrors);

        } catch (Exception e) {
            log.error("Error in Daily Renewal Detection Job", e);
            throw new RuntimeException("Failed to detect expiring registrations", e);
        }
    }
}
