package com.dental.clinic.management.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatisticsResponse {
    private String month;
    private List<DoctorPerformance> topDoctors;
    private TimeOffStats timeOff;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorPerformance {
        private Long employeeId;
        private String employeeCode;
        private String fullName;
        private Long appointmentCount;
        private BigDecimal totalRevenue;
        private BigDecimal averageRevenuePerAppointment;
        private Long serviceCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeOffStats {
        private Long totalDays;
        private Long totalRequests;
        private TimeOffByType byType;
        private TimeOffByStatus byStatus;
        private List<TopEmployee> topEmployees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeOffByType {
        private TypeStats paidLeave;
        private TypeStats unpaidLeave;
        private TypeStats emergencyLeave;
        private TypeStats sickLeave;
        private TypeStats other;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStats {
        private Long requests;
        private Long days;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeOffByStatus {
        private Long pending;
        private Long approved;
        private Long rejected;
        private Long cancelled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopEmployee {
        private Long employeeId;
        private String employeeCode;
        private String fullName;
        private Long totalDays;
        private Long requests;
    }
}
