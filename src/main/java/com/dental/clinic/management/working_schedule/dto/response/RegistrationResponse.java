package com.dental.clinic.management.working_schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    private Integer registrationId; // Changed from String to Integer (Schema V14 - part_time_registrations uses
                                    // SERIAL)
    private Integer employeeId;
    private Long partTimeSlotId;
    private String workShiftName;
    private String dayOfWeek;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
}
