package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a fixed shift registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFixedRegistrationRequest {

    @NotNull(message = "Mã nhân viên là bắt buộc")
    private Integer employeeId;

    @NotBlank(message = "Mã ca làm việc là bắt buộc")
    private String workShiftId;

    @NotEmpty(message = "Các ngày trong tuần không được để trống")
    private List<Integer> daysOfWeek;

    @NotNull(message = "Ngày hiệu lực từ là bắt buộc")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo; // null = permanent
}
