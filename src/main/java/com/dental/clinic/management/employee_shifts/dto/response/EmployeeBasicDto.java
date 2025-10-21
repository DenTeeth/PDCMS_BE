package com.dental.clinic.management.employee_shifts.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for basic employee information.
 * Used in shift responses to provide minimal employee details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBasicDto {

    /**
     * Unique identifier for the employee.
     */
    private Long employeeId;

    private String fullName;
}
