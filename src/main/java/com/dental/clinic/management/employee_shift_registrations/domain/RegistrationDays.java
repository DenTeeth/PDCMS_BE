package com.dental.clinic.management.employee_shift_registrations.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "registration_days")
public class RegistrationDays {

    @EmbeddedId
    private RegistrationDaysId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", insertable = false, updatable = false)
    private EmployeeShiftRegistration registration;

    // Constructors
    public RegistrationDays() {
    }

    public RegistrationDays(RegistrationDaysId id) {
        this.id = id;
    }

    // Getters and Setters
    public RegistrationDaysId getId() {
        return id;
    }

    public void setId(RegistrationDaysId id) {
        this.id = id;
    }

    public EmployeeShiftRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(EmployeeShiftRegistration registration) {
        this.registration = registration;
    }

    @Override
    public String toString() {
        return "RegistrationDays{" +
                "id=" + id +
                '}';
    }
}
