package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for individual batch selection in export transaction.
 * Used as nested object in CreateExportTransactionRequest.
 */
public class ExportBatchRequest {

    @NotNull(message = "ID lô hàng không được để trống")
    private Long batchId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private Integer quantity;
}
