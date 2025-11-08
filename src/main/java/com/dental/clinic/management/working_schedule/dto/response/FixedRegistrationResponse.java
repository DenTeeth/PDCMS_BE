package com.dental.clinic.management.working_schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for fixed shift registration details.
 */
public class FixedRegistrationResponse {

    @JsonProperty("registrationId")
    private Integer registrationId;

    @JsonProperty("employeeId")
    private Integer employeeId;

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("workShiftId")
    private String workShiftId;

    @JsonProperty("workShiftName")
    private String workShiftName;

    @JsonProperty("daysOfWeek")
    private List<Integer> daysOfWeek; // 1=Monday, 2=Tuesday, ..., 7=Sunday

    @JsonProperty("effectiveFrom")
    private LocalDate effectiveFrom;

    @JsonProperty("effectiveTo")
    private LocalDate effectiveTo;

    @JsonProperty("isActive")
    private Boolean isActive;

    public FixedRegistrationResponse() {
    }

    public FixedRegistrationResponse(Integer registrationId, Integer employeeId, String employeeName,
            String workShiftId, String workShiftName, List<Integer> daysOfWeek,
            LocalDate effectiveFrom, LocalDate effectiveTo, Boolean isActive) {
        this.registrationId = registrationId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.workShiftId = workShiftId;
        this.workShiftName = workShiftName;
        this.daysOfWeek = daysOfWeek;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.isActive = isActive;
    }

    public Integer getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Integer registrationId) {
        this.registrationId = registrationId;
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

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public String getWorkShiftName() {
        return workShiftName;
    }

    public void setWorkShiftName(String workShiftName) {
        this.workShiftName = workShiftName;
    }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<Integer> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
