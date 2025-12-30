package com.dental.clinic.management.payment.dto;

import com.dental.clinic.management.payment.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Integer paymentId;
    private String paymentCode;
    private Integer invoiceId;
    private String invoiceCode;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private String referenceNumber;
    private String notes;
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
}
