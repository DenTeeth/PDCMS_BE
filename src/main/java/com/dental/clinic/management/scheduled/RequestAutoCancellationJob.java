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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to automatically cancel overdue pending requests.
 * 
 * Business rules:
 * - Overtime requests: Cancel if work_date has passed and status is still PENDING
 * - Time-off requests: Cancel if start_date has passed and status is still PENDING
 * - Registration requests: Cancel if effective_from has passed and status is still PENDING
 * 
 * Runs daily at 1:00 AM Vietnam time
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestAutoCancellationJob {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final TimeOffRequestRepository timeOffRequestRepository;
    private final PartTimeRegistrationRepository partTimeRegistrationRepository;

    /**
     * Run auto-cancellation on application startup.
     * This ensures that any overdue requests in seed data are cancelled immediately after deployment.
     * 
     * Triggered by: ApplicationReadyEvent (after all beans are initialized)
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cancelOverdueRequestsOnStartup() {
        log.info("🚀 Application started - Running auto-cancellation for seed data cleanup...");
        cancelOverdueRequests();
    }

    /**
     * Auto-cancel overdue pending requests
     * Runs at 6:00 AM every day (Vietnam time)
     */
    @Scheduled(cron = "0 0 6 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void cancelOverdueRequestsScheduled() {
        log.info("==== Scheduled job: Starting auto-cancellation of overdue pending requests ====");
        cancelOverdueRequests();
    }

    /**
     * Core logic to cancel overdue requests.
     * Can be called by scheduled job or on startup.
     */
    private void cancelOverdueRequests() {
        
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();

            // 1. Cancel overdue overtime requests
            int overtimeCancelled = cancelOverdueOvertimeRequests(today, now);

            // 2. Cancel overdue time-off requests
            int timeOffCancelled = cancelOverdueTimeOffRequests(today, now);

            // 3. Cancel overdue registration requests
            int registrationCancelled = cancelOverdueRegistrationRequests(today, now);

            log.info("✅ Auto-cancellation completed: {} overtime, {} time-off, {} registration requests cancelled",
                    overtimeCancelled, timeOffCancelled, registrationCancelled);

        } catch (Exception e) {
            log.error("❌ Error during auto-cancellation: {}", e.getMessage(), e);
            throw e; // Re-throw to ensure transaction rollback
        }
    }

    /**
     * Cancel overtime requests where work_date has passed and status is PENDING
     */
    private int cancelOverdueOvertimeRequests(LocalDate today, LocalDateTime now) {
        List<OvertimeRequest> overdueRequests = overtimeRequestRepository
                .findByStatusAndWorkDateBefore(RequestStatus.PENDING, today);

        int count = 0;
        for (OvertimeRequest request : overdueRequests) {
            request.setStatus(RequestStatus.CANCELLED);
            request.setCancellationReason("Tự động hủy: Đã quá thời hạn xử lý (quá ngày làm việc yêu cầu)");
            request.setApprovedAt(now);
            overtimeRequestRepository.save(request);
            count++;
            log.info("Auto-cancelled overtime request {} for employee {} (work_date: {})",
                    request.getRequestId(),
                    request.getEmployee().getEmployeeId(),
                    request.getWorkDate());
        }

        if (count > 0) {
            log.info("Cancelled {} overdue overtime requests", count);
        }
        
        return count;
    }

    /**
     * Cancel time-off requests where start_date has passed and status is PENDING
     */
    private int cancelOverdueTimeOffRequests(LocalDate today, LocalDateTime now) {
        List<TimeOffRequest> overdueRequests = timeOffRequestRepository
                .findByStatusAndStartDateBefore(TimeOffStatus.PENDING, today);

        int count = 0;
        for (TimeOffRequest request : overdueRequests) {
            request.setStatus(TimeOffStatus.CANCELLED);
            request.setCancellationReason("Tự động hủy: Đã quá thời hạn xử lý (quá ngày bắt đầu yêu cầu)");
            request.setApprovedAt(now);
            timeOffRequestRepository.save(request);
            count++;
            log.info("Auto-cancelled time-off request {} for employee {} (start_date: {})",
                    request.getRequestId(),
                    request.getEmployeeId(),
                    request.getStartDate());
        }

        if (count > 0) {
            log.info("Cancelled {} overdue time-off requests", count);
        }
        
        return count;
    }

    /**
     * Cancel registration requests where effective_from has passed and status is PENDING
     */
    private int cancelOverdueRegistrationRequests(LocalDate today, LocalDateTime now) {
        List<PartTimeRegistration> overdueRequests = partTimeRegistrationRepository
                .findByStatusAndEffectiveFromBefore(RegistrationStatus.PENDING, today);

        int count = 0;
        for (PartTimeRegistration request : overdueRequests) {
            request.setStatus(RegistrationStatus.CANCELLED);
            request.setReason("Tự động hủy: Đã quá thời hạn xử lý (quá ngày bắt đầu hiệu lực)");
            request.setProcessedAt(now);
            request.setIsActive(false);
            partTimeRegistrationRepository.save(request);
            count++;
            log.info("Auto-cancelled registration {} for employee {} (effective_from: {})",
                    request.getRegistrationId(),
                    request.getEmployeeId(),
                    request.getEffectiveFrom());
        }

        if (count > 0) {
            log.info("Cancelled {} overdue registration requests", count);
        }
        
        return count;
    }
}
