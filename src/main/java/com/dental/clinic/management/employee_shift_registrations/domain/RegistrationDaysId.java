package com.dental.clinic.management.employee_shift_registrations.domain;

import java.io.Serializable;
import java.util.Objects;

import com.dental.clinic.management.employee_shift_registrations.enums.DayOfWeek;

public class RegistrationDaysId implements Serializable {

    private String registrationId;
    private DayOfWeek dayOfWeek;

    // Constructors
    public RegistrationDaysId() {
    }

    public RegistrationDaysId(String registrationId, DayOfWeek dayOfWeek) {
        this.registrationId = registrationId;
        this.dayOfWeek = dayOfWeek;
    }

    // Getters and Setters
    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RegistrationDaysId))
            return false;
        RegistrationDaysId that = (RegistrationDaysId) o;
        return Objects.equals(registrationId, that.registrationId) &&
                dayOfWeek == that.dayOfWeek;
    }

    @Override
    public int hashCode() {
        return Objects.hash(registrationId, dayOfWeek);
    }

    @Override
    public String toString() {
        return "RegistrationDaysId{" +
                "registrationId='" + registrationId + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                '}';
    }
}
