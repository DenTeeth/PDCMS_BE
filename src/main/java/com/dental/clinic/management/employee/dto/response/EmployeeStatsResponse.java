package com.dental.clinic.management.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeStatsResponse {
    private Long totalEmployees;
    private Long activeEmployees;
    private Long inactiveEmployees;
}
