package com.dental.clinic.management.warehouse.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetServiceConsumablesRequest {

    @NotNull(message = "Mã dịch vụ là bắt buộc")
    private Long serviceId;

    @NotEmpty(message = "Danh sách vật tư tiêu hao không được để trống")
    @Valid
    private List<ConsumableItemRequest> consumables;
}
