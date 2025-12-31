package com.dental.clinic.management.warehouse.dto.request;

import com.dental.clinic.management.warehouse.enums.WarehouseType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItemMasterRequest {

    @NotBlank(message = "Mã vật tư là bắt buộc")
    @Pattern(regexp = "^[A-Z0-9-]{3,20}$", message = "Mã vật tư phải từ 3-20 ký tự, chỉ gồm chữ in hoa, số và gạch ngang")
    private String itemCode;

    @NotBlank(message = "Tên vật tư là bắt buộc")
    @Size(max = 255, message = "Tên vật tư không được vượt quá 255 ký tự")
    private String itemName;

    @NotNull(message = "Mã danh mục là bắt buộc")
    private Long categoryId;

    @NotNull(message = "Loại kho là bắt buộc")
    private WarehouseType warehouseType;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    private Boolean isPrescriptionRequired;

    @Min(value = 1, message = "Hạn sử dụng mặc định phải ít nhất 1 ngày")
    @Max(value = 3650, message = "Hạn sử dụng mặc định không được vượt quá 3650 ngày (10 năm)")
    private Integer defaultShelfLifeDays;

    @NotNull(message = "Mức tồn kho tối thiểu là bắt buộc")
    @Min(value = 0, message = "Mức tồn kho tối thiểu phải >= 0")
    private Integer minStockLevel;

    @NotNull(message = "Mức tồn kho tối đa là bắt buộc")
    @Min(value = 1, message = "Mức tồn kho tối đa phải >= 1")
    private Integer maxStockLevel;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá thị trường hiện tại phải > 0")
    private BigDecimal currentMarketPrice;

    @NotNull(message = "Danh sách đơn vị là bắt buộc")
    @Size(min = 1, message = "Phải có ít nhất một đơn vị")
    @Valid
    private List<UnitRequest> units;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnitRequest {

        @NotBlank(message = "Tên đơn vị là bắt buộc")
        @Size(max = 50, message = "Tên đơn vị không được vượt quá 50 ký tự")
        private String unitName;

        @NotNull(message = "Tỉ lệ chuyển đổi là bắt buộc")
        @Min(value = 1, message = "Tỉ lệ chuyển đổi phải >= 1")
        private Integer conversionRate;

        @NotNull(message = "Cờ đơn vị cơ bản là bắt buộc")
        private Boolean isBaseUnit;

        @Min(value = 1, message = "Thứ tự hiển thị phải >= 1")
        private Integer displayOrder;

        private Boolean isDefaultImportUnit;

        private Boolean isDefaultExportUnit;
    }
}
