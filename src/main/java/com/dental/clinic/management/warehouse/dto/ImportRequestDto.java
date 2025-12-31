package com.dental.clinic.management.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO cho API Nhập Kho
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequestDto {

    @NotNull(message = "Mã nhà cung cấp là bắt buộc")
    private Long supplierId;

    private String notes;

    @NotNull(message = "Danh sách vật tư là bắt buộc")
    private List<ImportItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportItemDto {

        @NotNull(message = "Mã vật tư chính là bắt buộc")
        private Long itemMasterId;

        @NotNull(message = "Số lô là bắt buộc")
        private String lotNumber;

        @NotNull(message = "Số lượng là bắt buộc")
        @Min(value = 1, message = "Số lượng phải ít nhất là 1")
        private Integer quantity;

        private LocalDate expiryDate; // Bắt buộc nếu warehouseType=COLD && isTool=false

        @NotNull(message = "Giá nhập là bắt buộc")
        @Min(value = 0, message = "Giá không được âm")
        private BigDecimal importPrice;

        private String notes;
    }
}
