package com.dental.clinic.management.payment.dto;

import com.dental.clinic.management.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
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
public class CreatePaymentRequest {

    @NotNull(message = "Mã hóa đơn là bắt buộc")
    private Integer invoiceId;

    @NotNull(message = "Số tiền là bắt buộc")
    private BigDecimal amount;

    @NotNull(message = "Phương thức thanh toán là bắt buộc")
    private PaymentMethod paymentMethod;

    private LocalDateTime paymentDate;

    private String referenceNumber;

    private String notes;
}
