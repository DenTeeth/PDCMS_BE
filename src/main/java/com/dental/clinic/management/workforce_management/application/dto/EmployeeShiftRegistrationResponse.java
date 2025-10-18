package com.dental.clinic.management.workforce_management.application.dto;

import com.dental.clinic.management.workforce_management.domain.model.RegistrationDayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

/**
 * Response DTO for employee shift registration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShiftRegistrationResponse {

    private Long registrationId;
    private Long employeeId;
    private String slotId;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
    private Set<RegistrationDayOfWeek> registrationDays;
}
