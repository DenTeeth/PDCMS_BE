package com.dental.clinic.management.workforce_management.application.dto;

import com.dental.clinic.management.workforce_management.domain.model.RegistrationDayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

/**
 * Request DTO for creating employee shift registration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeShiftRegistrationRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Slot ID is required")
    private String slotId;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @NotNull(message = "Registration days are required")
    private Set<RegistrationDayOfWeek> registrationDays;
}