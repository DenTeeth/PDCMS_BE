package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating export transaction.
 * Uses FEFO logic - batches must be selected by frontend or auto-selected by
 * backend.
 */
public class CreateExportTransactionRequest {

    @NotEmpty(message = "Danh sách lô hàng không được để trống")
    @Valid
    private List<ExportBatchRequest> batches;

    @NotNull(message = "Ngày xuất kho không được để trống")
    private LocalDateTime transactionDate;

    @NotNull(message = "Người thực hiện không được để trống")
    private Long performedBy;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;

    @Size(max = 200, message = "Lý do xuất kho không được vượt quá 200 ký tự")
    private String exportReason;
}
