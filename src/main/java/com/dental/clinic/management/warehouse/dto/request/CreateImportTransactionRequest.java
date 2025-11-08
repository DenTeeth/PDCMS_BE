package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating import transaction.
 * Contains multiple items to be imported in a single transaction.
 */
public class CreateImportTransactionRequest {

    @NotEmpty(message = "Danh sách vật tư không được để trống")
    @Valid
    private List<ImportItemRequest> items;

    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long supplierId;

    @NotNull(message = "Ngày nhập kho không được để trống")
    private LocalDateTime transactionDate;

    @NotNull(message = "Người thực hiện không được để trống")
    private Long performedBy;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
}
