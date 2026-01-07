package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTimeOffTypeRequest {

    @NotBlank(message = "Tên loại nghỉ phép là bắt buộc")
    private String typeName;

    @NotBlank(message = "Mã loại nghỉ phép là bắt buộc")
    private String typeCode;

    private String description;

    @NotNull(message = "Yêu cầu số dư là bắt buộc")
    private Boolean requiresBalance;

    private Double defaultDaysPerYear;

    @NotNull(message = "Có lương là bắt buộc")
    private Boolean isPaid;

    @Builder.Default
    private Boolean requiresApproval = true;

    @Builder.Default
    private Boolean isActive = true;
}
