package com.dental.clinic.management.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
    private String month;
    private String previousMonth;
    private SummaryStats summary;
    private ComparisonStats revenue;
    private ComparisonStats expenses;
    private InvoiceStats invoices;
    private AppointmentStats appointments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryStats {
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
        private Long totalInvoices;
        private Long totalAppointments;
        private Long totalPatients;
        private Long totalEmployees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonStats {
        private BigDecimal current;
        private BigDecimal previous;
        private BigDecimal change;
        private Double changePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceStats {
        private Long total;
        private Long paid;
        private Long pending;
        private Long cancelled;
        private Double paidPercent;
        private BigDecimal debt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentStats {
        private Long total;
        private Long scheduled;        // SCHEDULED - Đã đặt lịch
        private Long checkedIn;        // CHECKED_IN - Đã check-in
        private Long inProgress;       // IN_PROGRESS - Đang điều trị
        private Long completed;        // COMPLETED - Hoàn thành
        private Long cancelled;        // CANCELLED - Đã hủy (>24h)
        private Long cancelledLate;    // CANCELLED_LATE - Hủy muộn (≤24h)
        private Long noShow;           // NO_SHOW - Không đến
        private Double completionRate;
    }
}
