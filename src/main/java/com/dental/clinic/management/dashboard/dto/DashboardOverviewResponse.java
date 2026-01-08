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
    private KPIStats kpis;
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

    /**
     * Key Performance Indicators (KPIs)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPIStats {
        // Average Revenue Per Appointment - Doanh thu trung bình mỗi cuộc hẹn
        private BigDecimal arpa;
        
        // Patient Retention Rate - Tỷ lệ giữ chân bệnh nhân (%)
        private Double patientRetentionRate;
        
        // Appointment Utilization Rate - Tỷ lệ sử dụng lịch hẹn (%)
        // (Số ca hoàn thành / Tổng số ca) * 100
        private Double appointmentUtilizationRate;
        
        // Revenue per Doctor - Doanh thu trung bình mỗi bác sĩ
        private BigDecimal revenuePerDoctor;
        
        // Profit Margin - Tỷ lệ lợi nhuận (%)
        // (Lợi nhuận / Doanh thu) * 100
        private Double profitMarginPercent;
        
        // Average Cost per Service - Chi phí trung bình mỗi dịch vụ
        private BigDecimal avgCostPerService;
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
