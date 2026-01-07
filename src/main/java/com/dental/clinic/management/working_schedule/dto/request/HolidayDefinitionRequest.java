package com.dental.clinic.management.working_schedule.dto.request;

import com.dental.clinic.management.working_schedule.enums.HolidayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating/updating holiday definition.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDefinitionRequest {

    @NotBlank(message = "Tên ngày lễ là bắt buộc")
    @Size(max = 100, message = "Tên ngày lễ không được vượt quá 100 ký tự")
    private String holidayName;

    @NotNull(message = "Loại ngày lễ là bắt buộc")
    private HolidayType holidayType;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}
