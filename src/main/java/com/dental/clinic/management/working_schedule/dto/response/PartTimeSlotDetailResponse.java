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

                    public static class RegisteredEmployeeInfo {
        private Integer employeeId;
        private String employeeCode;
        private String employeeName;
        private String effectiveFrom;
        private String effectiveTo;
    }
}
