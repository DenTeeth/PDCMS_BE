package com.dental.clinic.management.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Job 4: Annual leave balance reset.
 *
 * Runs on January 1st at 00:01 AM every year.
 * Resets and creates new annual leave balances for all active employees.
 *
 * NOTE: This job requires LeaveBalanceService which should be created in the
 * working_schedule.service package with an annualReset(Integer year) method.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AnnualLeaveBalanceResetJob {

    // TODO: Uncomment when LeaveBalanceService is available
    // private final LeaveBalanceService leaveBalanceService;

    /**
     * Cron: 0 1 0 1 1 ?
     * - Runs at 00:01 AM on January 1st every year
     * - Format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 1 0 1 1 ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void resetAnnualLeaveBalances() {
        log.info("=== Starting Annual Leave Balance Reset Job ===");

        int currentYear = java.time.LocalDate.now().getYear();
        log.info("Creating leave balances for year: {}", currentYear);

        try {
            // TODO: Implement LeaveBalanceService.annualReset() method
            // leaveBalanceService.annualReset(currentYear);

            log.warn("LeaveBalanceService.annualReset() not yet implemented");
            log.info("=== Annual Leave Balance Reset Job Completed (Placeholder) ===");

        } catch (Exception e) {
            log.error("Error in Annual Leave Balance Reset Job", e);
            throw new RuntimeException("Failed to reset annual leave balances", e);
        }
    }
}
