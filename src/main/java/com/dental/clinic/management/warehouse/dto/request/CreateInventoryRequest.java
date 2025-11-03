package com.dental.clinic.management.warehouse.dto.request;

import com.dental.clinic.management.warehouse.domain.Inventory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating inventory items.
 * Simplified version - removed pricing, unit measure, and certification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequest {

    @NotNull(message = "Nhà cung c?p không du?c d? tr?ng")
    private Long supplierId;

    @NotBlank(message = "Tên v?t tu không du?c d? tr?ng")
    @Size(max = 255, message = "Tên v?t tu không du?c vu?t quá 255 ký t?")
    private String itemName;

    @NotNull(message = "Lo?i kho không du?c d? tr?ng")
    private Inventory.WarehouseType warehouseType;

    @Size(max = 100, message = "Tên nhóm không du?c vu?t quá 100 ký t?")
    private String category;

    @NotNull(message = "S? lu?ng t?n kho không du?c d? tr?ng")
    @Min(value = 1, message = "S? lu?ng t?n kho ph?i l?n hon 0")
    private Integer stockQuantity;

    @Min(value = 0, message = "M?c t?n kho t?i thi?u không du?c âm")
    private Integer minStockLevel;

    @Min(value = 0, message = "M?c t?n kho t?i da không du?c âm")
    private Integer maxStockLevel;

    private LocalDate expiryDate;

    private Inventory.Status status;

    @Size(max = 1000, message = "Ghi chú không du?c vu?t quá 1000 ký t?")
    private String notes;
}
