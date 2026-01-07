package com.dental.clinic.management.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticsResponse {
    private String month;
    private InvoiceStats invoices;
    private PaymentStats payments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceStats {
        private Long total;
        private BigDecimal totalValue;
        private InvoiceByStatus byStatus;
        private InvoiceByType byType;
        private Double paymentRate;
        private BigDecimal debt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceByStatus {
        private StatusCount pendingPayment;
        private StatusCount partialPaid;
        private StatusCount paid;
        private StatusCount cancelled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceByType {
        private StatusCount appointment;
        private StatusCount treatmentPlan;
        private StatusCount supplemental;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private Long count;
        private BigDecimal value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentStats {
        private Long total;
        private BigDecimal totalValue;
        private PaymentByMethod byMethod;
        private List<DailyPayment> byDay;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentByMethod {
        private StatusCount bankTransfer;
        private StatusCount cash;
        private StatusCount card;
        private StatusCount other;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPayment {
        private LocalDate date;
        private Long count;
        private BigDecimal value;
    }
}
