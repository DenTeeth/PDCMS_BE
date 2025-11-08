package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request DTO for creating adjustment transaction.
 * Used for inventory count corrections, damaged goods, etc.
 */
public class CreateAdjustmentRequest {

    @NotNull(message = "ID lô hàng không được để trống")
    private Long batchId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer newQuantity;

    @NotNull(message = "Ngày giao dịch không được để trống")
    private LocalDateTime transactionDate;

    @NotNull(message = "Người thực hiện không được để trống")
    private Long performedBy;

    @NotBlank(message = "Ghi chú không được để trống")
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
}
