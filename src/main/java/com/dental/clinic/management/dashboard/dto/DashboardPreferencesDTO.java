package com.dental.clinic.management.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for dashboard preferences request/response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPreferencesDTO {
    private Integer id;
    private Integer userId;
    private String layout; // JSON string
    private String visibleWidgets; // JSON array
    private String defaultDateRange;
    private Boolean autoRefresh;
    private Integer refreshInterval;
    private String chartTypePreference;
}
