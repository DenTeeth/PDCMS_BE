package com.dental.clinic.management.working_schedule.dto.response;

import lombok.*;

/**
 * Response DTO for TimeOffType
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeOffTypeResponse {

    private String typeId;
    private String typeName;
    private String description;
    private Boolean requiresApproval;
    private Boolean isActive;
}
