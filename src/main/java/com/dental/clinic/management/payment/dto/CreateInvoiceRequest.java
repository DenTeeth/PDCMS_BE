package com.dental.clinic.management.payment.dto;

import com.dental.clinic.management.payment.enums.InvoiceType;
import jakarta.validation.constraints.NotNull;
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
public class CreateInvoiceRequest {

    @NotNull(message = "Invoice type is required")
    private InvoiceType invoiceType;

    @NotNull(message = "Patient ID is required")
    private Integer patientId;

    private Integer appointmentId;

    private Integer treatmentPlanId;

    private Integer phaseNumber;

    private Integer installmentNumber;

    @NotNull(message = "Items are required")
    private List<InvoiceItemDto> items;

    private LocalDateTime dueDate;

    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemDto {
        @NotNull(message = "Service ID is required")
        private Integer serviceId;
        private String serviceCode;
        @NotNull(message = "Service name is required")
        private String serviceName;
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;
        private String notes;
    }
}
