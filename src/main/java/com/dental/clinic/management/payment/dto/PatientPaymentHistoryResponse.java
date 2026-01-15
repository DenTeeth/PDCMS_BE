package com.dental.clinic.management.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for patient payment history (invoice list)
 * GET /api/v1/invoices/patient/{patientCode}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientPaymentHistoryResponse {
    
    private List<InvoiceHistoryItem> invoices;
    private PaginationInfo pagination;
    private PaymentSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer pageSize;
        private Long totalItems;
        private Integer totalPages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummary {
        private Long totalInvoices;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal remainingAmount;
        private Long unpaidInvoices;
    }
}
