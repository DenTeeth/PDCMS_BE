package com.dental.clinic.management.working_schedule.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * Entity representing a day of the week for a fixed shift registration.
 * This is part of the composite pattern for fixed schedules.
 *
 * Schema V14 - LuÃ¡Â»â€œng 1: LÃ¡Â»â€¹ch CÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh
 */
@Entity
@Table(name = "fixed_registration_days")
@IdClass(FixedRegistrationDay.FixedRegistrationDayId.class)
public class FixedRegistrationDay {

    /**
     * Reference to the parent registration.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private FixedShiftRegistration fixedShiftRegistration;

    /**
     * Day of the week (MONDAY, TUESDAY, ..., SUNDAY).
     */
    @Id
    @Column(name = "day_of_week", length = 10, nullable = false)
    @NotBlank(message = "Day of week is required")
    private String dayOfWeek;

    public FixedRegistrationDay() {
    }

    public FixedRegistrationDay(FixedShiftRegistration fixedShiftRegistration, String dayOfWeek) {
        this.fixedShiftRegistration = fixedShiftRegistration;
        this.dayOfWeek = dayOfWeek;
    }

    public FixedShiftRegistration getFixedShiftRegistration() {
        return fixedShiftRegistration;
    }

    public void setFixedShiftRegistration(FixedShiftRegistration fixedShiftRegistration) {
        this.fixedShiftRegistration = fixedShiftRegistration;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    /**
     * Composite primary key class.
     */
    public static class FixedRegistrationDayId implements Serializable {
        private Integer fixedShiftRegistration;
        private String dayOfWeek;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FixedRegistrationDayId that = (FixedRegistrationDayId) o;
            return fixedShiftRegistration.equals(that.fixedShiftRegistration) &&
                    dayOfWeek.equals(that.dayOfWeek);
        }

        @Override
        public int hashCode() {
            return fixedShiftRegistration.hashCode() + dayOfWeek.hashCode();
        }
    }
}
