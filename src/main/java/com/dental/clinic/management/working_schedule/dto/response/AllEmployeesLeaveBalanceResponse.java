package com.dental.clinic.management.working_schedule.dto.response;

import java.util.List;

/**
 * Response DTO for GET /api/v1/admin/leave-balances
 * Returns leave balances for ALL employees (Admin Dashboard)
 */
public class AllEmployeesLeaveBalanceResponse {

    private FilterInfo filter;
    private List<EmployeeBalanceSummary> data;

                    public static class FilterInfo {
        private Integer cycleYear;
        private String timeOffTypeId;
    }

                    public static class EmployeeBalanceSummary {
        private Integer employeeId;
        private String employeeName; // Full name for FE display
        private List<BalanceInfo> balances;
    }

                    public static class BalanceInfo {
        private String timeOffTypeName;
        private Double totalDaysAllowed;
        private Double daysTaken;
        private Double daysRemaining;
    }
}
