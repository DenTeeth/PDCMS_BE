package com.dental.clinic.management.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for appointment heatmap data
 * Shows appointment distribution by day of week and hour of day
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentHeatmapResponse {
    private String startDate;
    private String endDate;
    private List<HeatmapCell> data;
    private Statistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapCell {
        private String dayOfWeek; // MON, TUE, WED, THU, FRI, SAT, SUN
        private Integer hour; // 0-23
        private Long count; // Number of appointments
        private Double percentage; // Percentage of total appointments
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long totalAppointments;
        private String busiestDay;
        private Integer busiestHour;
        private Long peakAppointments;
        private Double averageAppointmentsPerSlot;
    }
}
