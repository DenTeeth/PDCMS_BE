package com.dental.clinic.management.working_schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for employee leave balances (P5.2)
 */
public class EmployeeLeaveBalanceResponse {

    @JsonProperty("employee_id")
    private Integer employeeId;

    @JsonProperty("cycle_year")
    private Integer cycleYear;

    @JsonProperty("balances")
    private List<LeaveBalanceDetailResponse> balances;

    public EmployeeLeaveBalanceResponse() {
    }

    public EmployeeLeaveBalanceResponse(Integer employeeId, Integer cycleYear,
            List<LeaveBalanceDetailResponse> balances) {
        this.employeeId = employeeId;
        this.cycleYear = cycleYear;
        this.balances = balances;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getCycleYear() {
        return cycleYear;
    }

    public void setCycleYear(Integer cycleYear) {
        this.cycleYear = cycleYear;
    }

    public List<LeaveBalanceDetailResponse> getBalances() {
        return balances;
    }

    public void setBalances(List<LeaveBalanceDetailResponse> balances) {
        this.balances = balances;
    }
}
