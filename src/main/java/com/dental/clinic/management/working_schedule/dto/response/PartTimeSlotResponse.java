package com.dental.clinic.management.working_schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartTimeSlotResponse {

    private Long slotId;
    private String workShiftId;
    private String workShiftName;
    private String dayOfWeek;
    private Integer quota;
    private Long registered; // Count of active registrations
    private Boolean isActive;
}
