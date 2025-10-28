package com.dental.clinic.management.employee_shifts.dto.response;

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

    private String employeeShiftId;

    /**
     * Work date in format: YYYY-MM-DD
     */
    private String workDate;

    /**
     * Shift status as string (enum name)
     */
    private String status;

    private EmployeeBasicDto employee;

    private WorkShiftBasicDto workShift;

    private String notes;

    /**
     * Source of shift creation (MANUAL_ENTRY, BATCH_JOB, etc.)
     */
    private String source;

    /**
     * Indicates if this is an overtime shift
     */
    private Boolean isOvertime;
}
