package com.dental.clinic.management.payment.dto;

import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.enums.InvoiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Integer invoiceId;
    private String invoiceCode;
    private InvoiceType invoiceType;
    private Integer patientId;
    private String patientName;
    private Integer appointmentId;
    private String appointmentCode;
    private Integer treatmentPlanId;
    private String treatmentPlanCode;
    private Integer phaseNumber;
    private Integer installmentNumber;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingDebt;
    private InvoicePaymentStatus paymentStatus;
    private LocalDateTime dueDate;
    private String notes;
    private String paymentCode; // Mã thanh toán PDCMS123456
    private String qrCodeUrl; // URL QR code VietQR để khách hàng quét thanh toán
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InvoiceItemResponse> items;
    private List<PaymentSummary> payments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemResponse {
        private Integer itemId;
        private Integer serviceId;
        private String serviceCode;
        private String serviceName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummary {
        private Integer paymentId;
        private String paymentCode;
        private BigDecimal amount;
        private String paymentMethod;
        private LocalDateTime paymentDate;
    }
}
