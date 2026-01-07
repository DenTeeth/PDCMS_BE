package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adjusting leave balance (P5.2)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustLeaveBalanceRequest {

    @NotNull(message = "Mã nhân viên là bắt buộc")
    @JsonProperty("employee_id")
    private Integer employeeId;

    @NotNull(message = "Mã loại nghỉ phép là bắt buộc")
    @JsonProperty("time_off_type_id")
    private String timeOffTypeId;

    @NotNull(message = "Năm chu kỳ là bắt buộc")
    @JsonProperty("cycle_year")
    private Integer cycleYear;

    @NotNull(message = "Số tiền thay đổi là bắt buộc")
    @JsonProperty("change_amount")
    private Double changeAmount; // Positive to add, negative to subtract

    @JsonProperty("notes")
    private String notes;
}
