package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for annual leave balance reset (P5.2)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnualResetRequest {

    @NotNull(message = "Năm chu kỳ là bắt buộc")
    @JsonProperty("cycle_year")
    private Integer cycleYear; // e.g., 2026

    @NotNull(message = "Mã loại nghỉ phép là bắt buộc")
    @JsonProperty("apply_to_type_id")
    private String applyToTypeId; // e.g., "ANNUAL_LEAVE"

    @NotNull(message = "Số ngày mặc định là bắt buộc")
    @Positive(message = "Số ngày mặc định phải là số dương")
    @JsonProperty("default_allowance")
    private Double defaultAllowance; // e.g., 12.0
}
