package com.dental.clinic.management.working_schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for basic work shift information.
 * Used in employee shift responses to provide minimal shift details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkShiftBasicDto {

    @JsonProperty("work_shift_id")
    private String workShiftId;

    @JsonProperty("shift_name")
    private String shiftName;
}
