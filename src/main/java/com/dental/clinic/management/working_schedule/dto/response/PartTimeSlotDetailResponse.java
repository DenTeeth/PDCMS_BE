package com.dental.clinic.management.working_schedule.dto.response;

import java.util.List;

public class PartTimeSlotDetailResponse {

    private Long slotId;
    private String workShiftId;
    private String workShiftName;
    private String dayOfWeek;
    private Integer quota;
    private Long registered; // Count of active registrations
    private Boolean isActive;
    private List<RegisteredEmployeeInfo> registeredEmployees;

    public PartTimeSlotDetailResponse() {
    }

    public PartTimeSlotDetailResponse(Long slotId, String workShiftId, String workShiftName, String dayOfWeek,
            Integer quota, Long registered, Boolean isActive, List<RegisteredEmployeeInfo> registeredEmployees) {
        this.slotId = slotId;
        this.workShiftId = workShiftId;
        this.workShiftName = workShiftName;
        this.dayOfWeek = dayOfWeek;
        this.quota = quota;
        this.registered = registered;
        this.isActive = isActive;
        this.registeredEmployees = registeredEmployees;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
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

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Long getRegistered() {
        return registered;
    }

    public void setRegistered(Long registered) {
        this.registered = registered;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<RegisteredEmployeeInfo> getRegisteredEmployees() {
        return registeredEmployees;
    }

    public void setRegisteredEmployees(List<RegisteredEmployeeInfo> registeredEmployees) {
        this.registeredEmployees = registeredEmployees;
    }

    public static class RegisteredEmployeeInfo {
        private Integer employeeId;
        private String employeeCode;
        private String employeeName;
        private String effectiveFrom;
        private String effectiveTo;

        public RegisteredEmployeeInfo() {
        }

        public RegisteredEmployeeInfo(Integer employeeId, String employeeCode, String employeeName,
                String effectiveFrom, String effectiveTo) {
            this.employeeId = employeeId;
            this.employeeCode = employeeCode;
            this.employeeName = employeeName;
            this.effectiveFrom = effectiveFrom;
            this.effectiveTo = effectiveTo;
        }

        public Integer getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Integer employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public void setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getEffectiveFrom() {
            return effectiveFrom;
        }

        public void setEffectiveFrom(String effectiveFrom) {
            this.effectiveFrom = effectiveFrom;
        }

        public String getEffectiveTo() {
            return effectiveTo;
        }

        public void setEffectiveTo(String effectiveTo) {
            this.effectiveTo = effectiveTo;
        }
    }
}
