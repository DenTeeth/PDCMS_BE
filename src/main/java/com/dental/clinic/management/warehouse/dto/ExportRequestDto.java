package com.dental.clinic.management.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO cho API Xuất Kho
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequestDto {

    private String notes;

    @NotNull(message = "Danh sách vật tư là bắt buộc")
    private List<ExportItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportItemDto {

        @NotNull(message = "Mã lô hàng là bắt buộc")
        private Long batchId;

        @NotNull(message = "Số lượng là bắt buộc")
        @Min(value = 1, message = "Số lượng phải ít nhất là 1")
        private Integer quantity;

        private String notes;
    }
}
