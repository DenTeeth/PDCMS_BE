package com.dental.clinic.management.warehouse.dto.request;

import com.dental.clinic.management.warehouse.enums.WarehouseType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for updating Category.
 * All fields are optional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String categoryName;

    private WarehouseType warehouseType;

    private UUID parentCategoryId;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}
