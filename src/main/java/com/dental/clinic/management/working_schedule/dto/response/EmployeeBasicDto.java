package com.dental.clinic.management.working_schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("employee_id")
    private Integer employeeId;

    @JsonProperty("full_name")
    private String fullName;
}
