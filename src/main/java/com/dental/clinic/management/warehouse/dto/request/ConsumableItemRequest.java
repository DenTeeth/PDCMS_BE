package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableItemRequest {

    @NotNull(message = "Mã vật tư chính là bắt buộc")
    private Long itemMasterId;

    @NotNull(message = "Số lượng mỗi dịch vụ là bắt buộc")
    @DecimalMin(value = "0.01", message = "Số lượng phải lớn hơn 0")
    private BigDecimal quantityPerService;

    @NotNull(message = "Mã đơn vị là bắt buộc")
    private Long unitId;

    private String notes;
}
