package com.dental.clinic.management.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for dashboard saved view request/response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSavedViewDTO {
    private Integer id;
    private Integer userId;
    private String viewName;
    private String description;
    private Boolean isPublic;
    private String filters; // JSON string
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
