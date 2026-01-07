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

    @NotNull(message = "Loại hóa đơn là bắt buộc")
    private InvoiceType invoiceType;

    @NotNull(message = "Mã bệnh nhân là bắt buộc")
    private Integer patientId;

    private Integer appointmentId;

    private Integer treatmentPlanId;

    private Integer phaseNumber;

    private Integer installmentNumber;

    @NotNull(message = "Danh sách mục hàng là bắt buộc")
    private List<InvoiceItemDto> items;

    private LocalDateTime dueDate;

    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemDto {
        @NotNull(message = "Mã dịch vụ là bắt buộc")
        private Integer serviceId;
        private String serviceCode;
        @NotNull(message = "Tên dịch vụ là bắt buộc")
        private String serviceName;
        @NotNull(message = "Số lượng là bắt buộc")
        private Integer quantity;
        @NotNull(message = "Đơn giá là bắt buộc")
        private BigDecimal unitPrice;
        private String notes;
    }
}
