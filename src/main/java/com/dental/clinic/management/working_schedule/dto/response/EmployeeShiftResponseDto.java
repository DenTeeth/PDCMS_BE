package com.dental.clinic.management.working_schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for employee shift response.
 * Contains complete shift assignment details including employee and work shift
 * information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShiftResponseDto {

    @JsonProperty("employee_shift_id")
    private String employeeShiftId;

    /**
     * Work date in format: YYYY-MM-DD
     */
    @JsonProperty("work_date")
    private String workDate;

    /**
     * Shift status as string (enum name)
     */
    @JsonProperty("status")
    private String status;

    @JsonProperty("employee")
    private EmployeeBasicDto employee;

    @JsonProperty("work_shift")
    private WorkShiftBasicDto workShift;

    @JsonProperty("notes")
    private String notes;

    /**
     * Source of shift creation (MANUAL_ENTRY, BATCH_JOB, etc.)
     */
    @JsonProperty("source")
    private String source;

    /**
     * Indicates if this is an overtime shift
     */
    @JsonProperty("is_overtime")
    private Boolean isOvertime;
}
