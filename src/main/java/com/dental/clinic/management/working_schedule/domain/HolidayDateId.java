package com.dental.clinic.management.working_schedule.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Composite primary key for HolidayDate entity.
 * Combination of (holiday_date, definition_id).
 */
public class HolidayDateId implements Serializable {

    private LocalDate holidayDate;
    private String definitionId;

    public HolidayDateId() {
    }

    public HolidayDateId(LocalDate holidayDate, String definitionId) {
        this.holidayDate = holidayDate;
        this.definitionId = definitionId;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HolidayDateId that = (HolidayDateId) o;
        return Objects.equals(holidayDate, that.holidayDate) && Objects.equals(definitionId, that.definitionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holidayDate, definitionId);
    }
}
