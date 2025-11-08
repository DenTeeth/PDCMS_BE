package com.dental.clinic.management.working_schedule.dto.response;

import java.util.List;

/**
 * Response DTO for GET /api/v1/admin/leave-balances
 * Returns leave balances for ALL employees (Admin Dashboard)
 */
public class AllEmployeesLeaveBalanceResponse {

    private FilterInfo filter;
    private List<EmployeeBalanceSummary> data;

    public AllEmployeesLeaveBalanceResponse() {
    }

    public AllEmployeesLeaveBalanceResponse(FilterInfo filter, List<EmployeeBalanceSummary> data) {
        this.filter = filter;
        this.data = data;
    }

    public FilterInfo getFilter() {
        return filter;
    }

    public void setFilter(FilterInfo filter) {
        this.filter = filter;
    }

    public List<EmployeeBalanceSummary> getData() {
        return data;
    }

    public void setData(List<EmployeeBalanceSummary> data) {
        this.data = data;
    }

    public static class FilterInfo {
        private Integer cycleYear;
        private String timeOffTypeId;

        public FilterInfo() {
        }

        public FilterInfo(Integer cycleYear, String timeOffTypeId) {
            this.cycleYear = cycleYear;
            this.timeOffTypeId = timeOffTypeId;
        }

        public Integer getCycleYear() {
            return cycleYear;
        }

        public void setCycleYear(Integer cycleYear) {
            this.cycleYear = cycleYear;
        }

        public String getTimeOffTypeId() {
            return timeOffTypeId;
        }

        public void setTimeOffTypeId(String timeOffTypeId) {
            this.timeOffTypeId = timeOffTypeId;
        }
    }

    public static class EmployeeBalanceSummary {
        private Integer employeeId;
        private String employeeName; // Full name for FE display
        private List<BalanceInfo> balances;

        public EmployeeBalanceSummary() {
        }

        public EmployeeBalanceSummary(Integer employeeId, String employeeName, List<BalanceInfo> balances) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.balances = balances;
        }

        public Integer getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Integer employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public List<BalanceInfo> getBalances() {
            return balances;
        }

        public void setBalances(List<BalanceInfo> balances) {
            this.balances = balances;
        }
    }

    public static class BalanceInfo {
        private String timeOffTypeName;
        private Double totalDaysAllowed;
        private Double daysTaken;
        private Double daysRemaining;

        public BalanceInfo() {
        }

        public BalanceInfo(String timeOffTypeName, Double totalDaysAllowed, Double daysTaken, Double daysRemaining) {
            this.timeOffTypeName = timeOffTypeName;
            this.totalDaysAllowed = totalDaysAllowed;
            this.daysTaken = daysTaken;
            this.daysRemaining = daysRemaining;
        }

        public String getTimeOffTypeName() {
            return timeOffTypeName;
        }

        public void setTimeOffTypeName(String timeOffTypeName) {
            this.timeOffTypeName = timeOffTypeName;
        }

        public Double getTotalDaysAllowed() {
            return totalDaysAllowed;
        }

        public void setTotalDaysAllowed(Double totalDaysAllowed) {
            this.totalDaysAllowed = totalDaysAllowed;
        }

        public Double getDaysTaken() {
            return daysTaken;
        }

        public void setDaysTaken(Double daysTaken) {
            this.daysTaken = daysTaken;
        }

        public Double getDaysRemaining() {
            return daysRemaining;
        }

        public void setDaysRemaining(Double daysRemaining) {
            this.daysRemaining = daysRemaining;
        }
    }
}
