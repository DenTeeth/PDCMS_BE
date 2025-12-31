package com.dental.clinic.management.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing Service Category
 * Used in API: PATCH /api/v1/service-categories/{categoryId}
 * All fields are optional (partial update)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceCategoryRequest {

    @Size(max = 50, message = "Mã danh mục không được vượt quá 50 ký tự")
    private String categoryCode;

    @Size(max = 255, message = "Tên danh mục không được vượt quá 255 ký tự")
    private String categoryName;

    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private Integer displayOrder;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    private Boolean isActive;
}
