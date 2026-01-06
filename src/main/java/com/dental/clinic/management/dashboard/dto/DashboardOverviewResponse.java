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
        private Long completed;
        private Long cancelled;
        private Long noShow;
        private Double completionRate;
    }
}
