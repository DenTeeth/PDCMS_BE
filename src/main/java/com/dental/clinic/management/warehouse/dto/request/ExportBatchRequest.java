package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for individual batch selection in export transaction.
 * Used as nested object in CreateExportTransactionRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportBatchRequest {

    @NotNull(message = "ID lô hàng không được để trống")
    private UUID batchId;

    @NotNull(message = "Số lượng xuất không được để trống")
    @Min(value = 1, message = "Số lượng xuất phải >= 1")
    private Integer quantity;
}
