package com.dental.clinic.management.workforce_management.infrastructure.persistence.entity;

import com.dental.clinic.management.workforce_management.domain.model.RegistrationDayOfWeek;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing registration days with composite key.
 */
@Entity
@Table(name = "registration_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDay {

    @EmbeddedId
    private RegistrationDayId id;

    /**
     * Composite key for RegistrationDay.
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationDayId implements java.io.Serializable {

        @Column(name = "registration_id", length = 20)
        @NotNull
        private String registrationId;

        @Enumerated(EnumType.STRING)
        @Column(name = "day_of_week", length = 10)
        @NotNull
        private RegistrationDayOfWeek dayOfWeek;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegistrationDayId that = (RegistrationDayId) o;
            return registrationId.equals(that.registrationId) && dayOfWeek == that.dayOfWeek;
        }

        @Override
        public int hashCode() {
            return registrationId.hashCode() + dayOfWeek.hashCode();
        }
    }
}