package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.working_schedule.service.LeaveBalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Job 4: Annual leave balance reset.
 *
 * Runs on January 1st at 00:01 AM every year.
 * Resets and creates new annual leave balances for all active employees.
 */
@Component
public class AnnualLeaveBalanceResetJob {

    private static final Logger log = LoggerFactory.getLogger(AnnualLeaveBalanceResetJob.class);

    private final LeaveBalanceService leaveBalanceService;

    public AnnualLeaveBalanceResetJob(LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

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
            int balancesProcessed = leaveBalanceService.annualReset(currentYear);

            log.info("=== Annual Leave Balance Reset Job Completed Successfully ===");
            log.info("Total balances created/updated: {}", balancesProcessed);

        } catch (Exception e) {
            log.error("Error in Annual Leave Balance Reset Job", e);
            throw new RuntimeException("Failed to reset annual leave balances", e);
        }
    }
}
