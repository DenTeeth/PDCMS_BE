package com.dental.clinic.management.workforce_management.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entity representing employee shift registration.
 */
@Entity
@Table(name = "employee_shift_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShiftRegistration {

    @Id
    @Column(name = "registration_id", length = 20)
    @NotNull
    private String registrationId;

    @Column(name = "employee_id", nullable = false, length = 20)
    @NotNull
    private String employeeId;

    @Column(name = "slot_id", nullable = false, length = 20)
    @NotNull
    private String slotId;

    @Column(name = "effective_from", nullable = false)
    @NotNull
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}