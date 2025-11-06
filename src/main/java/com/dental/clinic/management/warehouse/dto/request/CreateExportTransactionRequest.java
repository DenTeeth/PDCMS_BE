package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating export transaction.
 * Uses FEFO logic - batches must be selected by frontend or auto-selected by
 * backend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExportTransactionRequest {

    @NotEmpty(message = "Danh sách lô xuất không được để trống")
    @Valid
    private List<ExportBatchRequest> batches;

    @NotNull(message = "Ngày xuất không được để trống")
    private LocalDateTime transactionDate;

    @NotNull(message = "Người thực hiện không được để trống")
    private UUID performedBy;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;

    @Size(max = 200, message = "Lý do xuất không được vượt quá 200 ký tự")
    private String exportReason;
}
