package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.working_schedule.domain.ShiftRenewalRequest;
import com.dental.clinic.management.working_schedule.enums.RenewalStatus;
import com.dental.clinic.management.working_schedule.repository.ShiftRenewalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job 5: Expire pending renewal requests that employees haven't responded to.
 *
 * Runs every hour at the top of the hour (00 minutes).
 * Finds renewal requests with status PENDING_ACTION where responseDeadline has
 * passed.
 * Updates their status to EXPIRED and logs the expiration.
 *
 * This ensures that old renewal invitations don't stay in PENDING state forever
 * and allows HR/Admin to take appropriate action.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExpirePendingRenewalsJob {

    private final ShiftRenewalRequestRepository renewalRepository;

    /**
     * Cron: 0 0 * * * ?
     * - Runs at the top of every hour (e.g., 00:00, 01:00, 02:00, etc.)
     * - Format: second minute hour day-of-month month day-of-week
     *
     * Alternative: Run daily at 2:00 AM with cron "0 0 2 * * ?"
     */
    @Scheduled(cron = "0 0 * * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void expirePendingRenewals() {
        log.info("=== Starting Expire Pending Renewals Job ===");

        LocalDateTime now = LocalDateTime.now();
        log.info("Current time: {}", now);

        try {
            // VALIDATION: Check if renewal repository is accessible
            long totalRenewals = renewalRepository.count();
            log.debug("Total renewal requests in database: {}", totalRenewals);

            // 1. Find pending renewals that have passed their response deadline
            List<ShiftRenewalRequest> expiredRenewals = renewalRepository
                    .findExpiredPendingRenewals(now);

            log.info("Found {} pending renewals that have expired", expiredRenewals.size());

            if (expiredRenewals.isEmpty()) {
                log.info("No expired pending renewals found. Job completed successfully.");
                return;
            }

            // 2. Update status to EXPIRED for each
            int successfullyExpired = 0;
            int failedToExpire = 0;

            for (ShiftRenewalRequest renewal : expiredRenewals) {
                try {
                    // VALIDATION: Double-check the deadline is actually passed
                    if (renewal.getExpiresAt() != null &&
                            renewal.getExpiresAt().isAfter(now)) {
                        log.warn("Renewal {} deadline {} is in the future. Skipping.",
                                renewal.getRenewalId(), renewal.getExpiresAt());
                        continue;
                    }

                    // VALIDATION: Check current status is still PENDING_ACTION
                    if (renewal.getStatus() != RenewalStatus.PENDING_ACTION) {
                        log.debug("Renewal {} status changed to {}. Skipping.",
                                renewal.getRenewalId(), renewal.getStatus());
                        continue;
                    }

                    // Update status to EXPIRED
                    renewal.setStatus(RenewalStatus.EXPIRED);
                    renewal.setConfirmedAt(now);

                    // Add notes about automatic expiration
                    String expirationNote = String.format(
                            "Tự động hết hạn vào %s do nhân viên không phản hồi trước deadline %s",
                            now, renewal.getExpiresAt());

                    if (renewal.getMessage() != null && !renewal.getMessage().isBlank()) {
                        renewal.setMessage(renewal.getMessage() + "\n" + expirationNote);
                    } else {
                        renewal.setMessage(expirationNote);
                    }

                    renewalRepository.save(renewal);
                    successfullyExpired++;

                    log.info("Expired renewal {} for registration {} (Employee ID: {}, Deadline was: {})",
                            renewal.getRenewalId(),
                            renewal.getExpiringRegistration() != null
                                    ? renewal.getExpiringRegistration().getRegistrationId()
                                    : "N/A",
                            renewal.getEmployee() != null ? renewal.getEmployee().getEmployeeId() : null,
                            renewal.getExpiresAt());

                } catch (Exception e) {
                    log.error("Failed to expire renewal {}: {}",
                            renewal.getRenewalId(), e.getMessage(), e);
                    failedToExpire++;
                }
            }

            log.info("=== Expire Pending Renewals Job Completed ===");
            log.info("Total expired renewals found: {}", expiredRenewals.size());
            log.info("Successfully expired: {}", successfullyExpired);
            log.info("Failed to expire: {}", failedToExpire);

            // Optional: Send notification to HR/Admin if there are expired renewals
            if (successfullyExpired > 0) {
                log.warn("ACTION REQUIRED: {} renewal requests have expired. " +
                        "HR/Admin should review and take appropriate action.", successfullyExpired);
                // TODO: Implement notification service to alert HR/Admin
                // notificationService.notifyHRAboutExpiredRenewals(successfullyExpired);
            }

        } catch (Exception e) {
            log.error("Error in Expire Pending Renewals Job", e);
            throw new RuntimeException("Failed to expire pending renewals", e);
        }
    }
}
