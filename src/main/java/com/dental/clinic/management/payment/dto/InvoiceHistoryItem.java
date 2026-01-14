package com.dental.clinic.management.payment.dto;

import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Simplified invoice item for payment history list
 * Contains essential information for displaying in patient payment history
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceHistoryItem {
    
    private String invoiceCode;
    private String patientCode;
    private String patientName;
    private String appointmentCode;
    private String treatmentPlanCode;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private InvoicePaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime issuedDate;
    private LocalDateTime dueDate;
    private LocalDateTime lastPaymentDate;
    private String notes;
    private List<InvoiceItemSummary> items;
    private List<PaymentInfo> payments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemSummary {
        private Integer itemId;
        private String serviceName;
        private String serviceCode;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private BigDecimal totalPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private Integer paymentId;
        private BigDecimal amount;
        private PaymentMethod paymentMethod;
        private LocalDateTime paymentDate;
        private String notes;
    }
}
