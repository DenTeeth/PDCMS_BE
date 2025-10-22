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
    private String typeCode; // ANNUAL_LEAVE, SICK_LEAVE, etc.
    private String typeName;
    private String description;
    private Boolean isPaid; // true = có lương, false = không lương
    private Boolean requiresApproval;
    private Boolean isActive;
}
