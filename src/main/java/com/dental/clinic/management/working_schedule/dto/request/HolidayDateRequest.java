package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for creating/updating a holiday date.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDateRequest {

    @NotNull(message = "Ngày lễ là bắt buộc")
    private LocalDate holidayDate;

    @NotBlank(message = "Mã định nghĩa là bắt buộc")
    private String definitionId;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}
