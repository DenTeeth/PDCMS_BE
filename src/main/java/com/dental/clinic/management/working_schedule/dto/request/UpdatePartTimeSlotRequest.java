package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePartTimeSlotRequest {

    @NotNull(message = "Hạn mức là bắt buộc")
    @Min(value = 1, message = "Hạn mức phải ít nhất là 1")
    private Integer quota;

    @NotNull(message = "Trạng thái hoạt động là bắt buộc")
    private Boolean isActive;
}
