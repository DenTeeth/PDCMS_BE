package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating adjustment transaction.
 * Used for inventory count corrections, damaged goods, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdjustmentRequest {

    @NotNull(message = "ID lô hàng không được để trống")
    private UUID batchId;

    @NotNull(message = "Số lượng mới không được để trống")
    @Min(value = 0, message = "Số lượng mới phải >= 0")
    private Integer newQuantity;

    @NotNull(message = "Ngày điều chỉnh không được để trống")
    private LocalDateTime transactionDate;

    @NotNull(message = "Người thực hiện không được để trống")
    private UUID performedBy;

    @NotBlank(message = "Ghi chú không được để trống (bắt buộc cho giao dịch điều chỉnh)")
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
}
