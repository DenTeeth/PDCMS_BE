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
    private AlertStats alerts;

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
        private Long totalPatients;          // Total patients in system
        private Long newPatientsThisMonth;   // New patients created in selected period
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
        private Long overdue;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentStats {
        private Long total;
        private Long scheduled;
        private Long completed;
        private Long cancelled;
    }

    /**
     * Alert/Notification system for dashboard metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertStats {
        private Integer totalAlerts;
        private java.util.List<Alert> alerts;
    }

    /**
     * Individual alert details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String type;           // REVENUE_DROP, HIGH_DEBT, HIGH_NO_SHOW, LOW_INVENTORY, EXPIRING_MATERIALS
        private String severity;       // WARNING, CRITICAL
        private String title;          // Short alert title
        private String message;        // Detailed message
        private String value;          // Current value causing alert
        private String threshold;      // Threshold that was exceeded
    }
}
