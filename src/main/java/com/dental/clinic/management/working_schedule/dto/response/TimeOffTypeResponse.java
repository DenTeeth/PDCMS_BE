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
    private Boolean requiresBalance; // true = cÃ¡ÂºÂ§n check sÃ¡Â»â€˜ dÃ†Â° phÃƒÂ©p, false = khÃƒÂ´ng cÃ¡ÂºÂ§n
    private Double defaultDaysPerYear; // SÃ¡Â»â€˜ ngÃƒÂ y phÃƒÂ©p mÃ¡ÂºÂ·c Ã„â€˜Ã¡Â»â€¹nh mÃ¡Â»â€”i nÃ„Æ’m (dÃƒÂ¹ng cho annual reset)
    private Boolean isPaid; // true = cÃƒÂ³ lÃ†Â°Ã†Â¡ng, false = khÃƒÂ´ng lÃ†Â°Ã†Â¡ng
    private Boolean requiresApproval; // true = cÃ¡ÂºÂ§n duyÃ¡Â»â€¡t, false = khÃƒÂ´ng cÃ¡ÂºÂ§n
    private Boolean isActive; // true = Ã„â€˜ang hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng, false = Ã„â€˜ÃƒÂ£ vÃƒÂ´ hiÃ¡Â»â€¡u hÃƒÂ³a
}
