package com.dental.clinic.management.warehouse.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for single item unit conversion
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversionItemRequest {

    @NotNull(message = "Mã vật tư chính là bắt buộc")
    @Positive(message = "Mã vật tư chính phải là số dương")
    private Long itemMasterId;

    @NotNull(message = "Mã đơn vị nguồn là bắt buộc")
    @Positive(message = "Mã đơn vị nguồn phải là số dương")
    private Long fromUnitId;

    @NotNull(message = "Mã đơn vị đích là bắt buộc")
    @Positive(message = "Mã đơn vị đích phải là số dương")
    private Long toUnitId;

    @NotNull(message = "Số lượng là bắt buộc")
    @Positive(message = "Số lượng phải là số dương")
    private Double quantity;
}
