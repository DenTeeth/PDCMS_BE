package com.dental.clinic.management.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter criteria for dashboard statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFilter {
    private Integer employeeId;
    private Integer patientId;
    private Integer serviceId;
    
    /**
     * Check if any filter is applied
     */
    public boolean hasFilters() {
        return employeeId != null || patientId != null || serviceId != null;
    }
}
