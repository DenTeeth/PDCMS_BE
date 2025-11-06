package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating Item Master.
 * Maps to FE CreateItemMasterDto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemMasterRequest {

    @NotBlank(message = "Tên vật tư không được để trống")
    @Size(max = 100, message = "Tên vật tư không được vượt quá 100 ký tự")
    private String itemName;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @NotNull(message = "Danh mục không được để trống")
    private UUID categoryId;

    @Min(value = 0, message = "Mức tồn kho tối thiểu phải >= 0")
    private Integer minStockLevel;

    @Min(value = 0, message = "Mức tồn kho tối đa phải >= 0")
    private Integer maxStockLevel;
}
