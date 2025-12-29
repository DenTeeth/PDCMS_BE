package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.working_schedule.domain.OvertimeRequest;
import com.dental.clinic.management.working_schedule.domain.PartTimeRegistration;
import com.dental.clinic.management.working_schedule.domain.TimeOffRequest;
import com.dental.clinic.management.working_schedule.enums.RequestStatus;
import com.dental.clinic.management.working_schedule.enums.RegistrationStatus;
import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import com.dental.clinic.management.working_schedule.repository.OvertimeRequestRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to automatically delete old completed/cancelled/rejected requests.
 * 
 * Business rules:
 * - Delete requests that are older than a certain period after completion/cancellation/rejection
 * - Default: Delete after 1 month (30 days) to save storage
 * - Only delete requests in final states: APPROVED, REJECTED, CANCELLED
 * - Keep PENDING requests (they will be auto-cancelled by RequestAutoCancellationJob)
 * 
 * Runs daily at 2:00 AM Vietnam time
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestAutoCleanupJob {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final TimeOffRequestRepository timeOffRequestRepository;
    private final PartTimeRegistrationRepository partTimeRegistrationRepository;

    // Delete requests older than 30 days
    private static final int CLEANUP_DAYS_THRESHOLD = 30;

    /**
     * Auto-delete old completed/cancelled/rejected requests
     * Runs at 2:00 AM every day (Vietnam time)
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void cleanupOldRequests() {
        log.info("==== Starting auto-cleanup of old requests (older than {} days) ====", CLEANUP_DAYS_THRESHOLD);
        
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(CLEANUP_DAYS_THRESHOLD);
            LocalDateTime cutoffDateTime = cutoffDate.atStartOfDay();

            // 1. Delete old overtime requests
            int overtimeDeleted = deleteOldOvertimeRequests(cutoffDate, cutoffDateTime);

            // 2. Delete old time-off requests
            int timeOffDeleted = deleteOldTimeOffRequests(cutoffDate, cutoffDateTime);

            // 3. Delete old registration requests
            int registrationDeleted = deleteOldRegistrationRequests(cutoffDate, cutoffDateTime);

            log.info("==== Auto-cleanup completed ====");
            log.info("Summary: {} overtime, {} time-off, {} registration requests deleted",
                    overtimeDeleted, timeOffDeleted, registrationDeleted);

        } catch (Exception e) {
            log.error("Error during auto-cleanup job: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete old overtime requests
     * Criteria:
     * - Status is APPROVED, REJECTED, or CANCELLED
     * - work_date is before cutoffDate (older than 30 days)
     * OR
     * - approved_at/updated_at is before cutoffDateTime for cancelled/rejected requests
     */
    private int deleteOldOvertimeRequests(LocalDate cutoffDate, LocalDateTime cutoffDateTime) {
        // Find approved requests with old work dates
        List<OvertimeRequest> approvedOldRequests = overtimeRequestRepository
                .findByStatusAndWorkDateBefore(RequestStatus.APPROVED, cutoffDate);

        // Find rejected requests that were processed long ago
        List<OvertimeRequest> rejectedOldRequests = overtimeRequestRepository
                .findByStatusAndApprovedAtBefore(RequestStatus.REJECTED, cutoffDateTime);

        // Find cancelled requests that were processed long ago
        List<OvertimeRequest> cancelledOldRequests = overtimeRequestRepository
                .findByStatusAndApprovedAtBefore(RequestStatus.CANCELLED, cutoffDateTime);

        int count = 0;
        
        // Delete approved old requests
        for (OvertimeRequest request : approvedOldRequests) {
            try {
                overtimeRequestRepository.delete(request);
                count++;
                log.debug("Deleted old overtime request {} (status: {}, work_date: {})",
                        request.getRequestId(), request.getStatus(), request.getWorkDate());
            } catch (Exception e) {
                log.error("Failed to delete overtime request {}: {}", 
                        request.getRequestId(), e.getMessage());
            }
        }

        // Delete rejected old requests
        for (OvertimeRequest request : rejectedOldRequests) {
            try {
                overtimeRequestRepository.delete(request);
                count++;
                log.debug("Deleted old overtime request {} (status: {}, approved_at: {})",
                        request.getRequestId(), request.getStatus(), request.getApprovedAt());
            } catch (Exception e) {
                log.error("Failed to delete overtime request {}: {}", 
                        request.getRequestId(), e.getMessage());
            }
        }

        // Delete cancelled old requests
        for (OvertimeRequest request : cancelledOldRequests) {
            try {
                overtimeRequestRepository.delete(request);
                count++;
                log.debug("Deleted old overtime request {} (status: {}, approved_at: {})",
                        request.getRequestId(), request.getStatus(), request.getApprovedAt());
            } catch (Exception e) {
                log.error("Failed to delete overtime request {}: {}", 
                        request.getRequestId(), e.getMessage());
            }
        }

        if (count > 0) {
            log.info("Deleted {} old overtime requests", count);
        }
        
        return count;
    }

    /**
     * Delete old time-off requests
     */
    private int deleteOldTimeOffRequests(LocalDate cutoffDate, LocalDateTime cutoffDateTime) {
        // Find approved requests with old end dates
        List<TimeOffRequest> approvedOldRequests = timeOffRequestRepository
                .findByStatusAndEndDateBefore(TimeOffStatus.APPROVED, cutoffDate);

        // Find rejected requests that were processed long ago
        List<TimeOffRequest> rejectedOldRequests = timeOffRequestRepository
                .findByStatusAndApprovedAtBefore(TimeOffStatus.REJECTED, cutoffDateTime);

        // Find cancelled requests that were processed long ago
        List<TimeOffRequest> cancelledOldRequests = timeOffRequestRepository
                .findByStatusAndApprovedAtBefore(TimeOffStatus.CANCELLED, cutoffDateTime);

        int count = 0;
        
        // Delete approved old requests
        for (TimeOffRequest request : approvedOldRequests) {
            try {
                timeOffRequestRepository.delete(request);
                count++;
                log.debug("Deleted old time-off request {} (status: {}, end_date: {})",
                        request.getRequestId(), request.getStatus(), request.getEndDate());
            } catch (Exception e) {
                log.error("Failed to delete time-off request {}: {}", 
                        request.getRequestId(), e.getMessage());
            }
        }

        // Delete rejected old requests
        for (TimeOffRequest request : rejectedOldRequests) {
            try {
                timeOffRequestRepository.delete(request);
                count++;
                log.debug("Deleted old time-off request {} (status: {}, approved_at: {})",
                        request.getRequestId(), request.getStatus(), request.getApprovedAt());
            } catch (Exception e) {
                log.error("Failed to delete time-off request {}: {}", 
                        request.getRequestId(), e.getMessage());
            }
        }

        // Delete cancelled old requests
        for (TimeOffRequest request : cancelledOldRequests) {
            try {
                timeOffRequestRepository.delete(request);
                count++;
                log.debug("Deleted old time-off request {} (status: {}, approved_at: {})",
                        request.getRequestId(), request.getStatus(), request.getApprovedAt());
            } catch (Exception e) {
                log.error("Failed to delete time-off request {}: {}", 
                        request.getRequestId(), e.getMessage());
            }
        }

        if (count > 0) {
            log.info("Deleted {} old time-off requests", count);
        }
        
        return count;
    }

    /**
     * Delete old registration requests
     */
    private int deleteOldRegistrationRequests(LocalDate cutoffDate, LocalDateTime cutoffDateTime) {
        // Find approved requests with old effective_to dates and inactive
        List<PartTimeRegistration> approvedOldRequests = partTimeRegistrationRepository
                .findByStatusAndEffectiveToBeforeAndIsActive(RegistrationStatus.APPROVED, cutoffDate, false);

        // Find rejected requests that were processed long ago
        List<PartTimeRegistration> rejectedOldRequests = partTimeRegistrationRepository
                .findByStatusAndProcessedAtBefore(RegistrationStatus.REJECTED, cutoffDateTime);

        // Find cancelled requests that were processed long ago
        List<PartTimeRegistration> cancelledOldRequests = partTimeRegistrationRepository
                .findByStatusAndProcessedAtBefore(RegistrationStatus.CANCELLED, cutoffDateTime);

        int count = 0;
        
        // Delete approved old requests
        for (PartTimeRegistration request : approvedOldRequests) {
            try {
                partTimeRegistrationRepository.delete(request);
                count++;
                log.debug("Deleted old registration {} (status: {}, effective_to: {})",
                        request.getRegistrationId(), request.getStatus(), request.getEffectiveTo());
            } catch (Exception e) {
                log.error("Failed to delete registration {}: {}", 
                        request.getRegistrationId(), e.getMessage());
            }
        }

        // Delete rejected old requests
        for (PartTimeRegistration request : rejectedOldRequests) {
            try {
                partTimeRegistrationRepository.delete(request);
                count++;
                log.debug("Deleted old registration {} (status: {}, processed_at: {})",
                        request.getRegistrationId(), request.getStatus(), request.getProcessedAt());
            } catch (Exception e) {
                log.error("Failed to delete registration {}: {}", 
                        request.getRegistrationId(), e.getMessage());
            }
        }

        // Delete cancelled old requests
        for (PartTimeRegistration request : cancelledOldRequests) {
            try {
                partTimeRegistrationRepository.delete(request);
                count++;
                log.debug("Deleted old registration {} (status: {}, processed_at: {})",
                        request.getRegistrationId(), request.getStatus(), request.getProcessedAt());
            } catch (Exception e) {
                log.error("Failed to delete registration {}: {}", 
                        request.getRegistrationId(), e.getMessage());
            }
        }

        if (count > 0) {
            log.info("Deleted {} old registration requests", count);
        }
        
        return count;
    }
}
