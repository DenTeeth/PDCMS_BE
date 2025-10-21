package com.dental.clinic.management.employee_shifts.dto.response;

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

    private String workShiftId;

    private String shiftName;
}
