package com.dental.clinic.management.working_schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO for creating a manual employee shift.
 */
public class CreateShiftRequestDto {

    @NotNull(message = "employee_id khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @JsonProperty("employee_id")
    private Integer employeeId;

    @NotNull(message = "work_date khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @JsonProperty("work_date")
    private LocalDate workDate;

    @NotNull(message = "work_shift_id khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @JsonProperty("work_shift_id")
    private String workShiftId;

    @JsonProperty("notes")
    private String notes;
}
