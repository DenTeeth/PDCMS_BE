package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for updating Item Master.
 * All fields are optional - only updates non-null fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemMasterRequest {

    @Size(max = 100, message = "Tên vật tư không được vượt quá 100 ký tự")
    private String itemName;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    private UUID categoryId;

    @Min(value = 0, message = "Mức tồn kho tối thiểu phải >= 0")
    private Integer minStockLevel;

    @Min(value = 0, message = "Mức tồn kho tối đa phải >= 0")
    private Integer maxStockLevel;
}
