package com.dental.clinic.management.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new Service Category
 * Used in API: POST /api/v1/service-categories
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceCategoryRequest {

    @NotBlank(message = "Mã danh mục là bắt buộc")
    @Size(max = 50, message = "Mã danh mục không được vượt quá 50 ký tự")
    private String categoryCode;

    @NotBlank(message = "Tên danh mục là bắt buộc")
    @Size(max = 255, message = "Tên danh mục không được vượt quá 255 ký tự")
    private String categoryName;

    @NotNull(message = "Thứ tự hiển thị là bắt buộc")
    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private Integer displayOrder;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
}
