package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for individual item in import transaction.
 * Used as nested object in CreateImportTransactionRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportItemRequest {

    @NotNull(message = "ID vật tư không được để trống")
    private UUID itemMasterId;

    @NotBlank(message = "Số lô không được để trống")
    @Size(max = 50, message = "Số lô không được vượt quá 50 ký tự")
    private String lotNumber;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải >= 1")
    private Integer quantity;

    @NotNull(message = "Đơn giá nhập không được để trống")
    @Min(value = 0, message = "Đơn giá nhập phải >= 0")
    private BigDecimal importPrice;

    private LocalDate expiryDate;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String notes;
}
