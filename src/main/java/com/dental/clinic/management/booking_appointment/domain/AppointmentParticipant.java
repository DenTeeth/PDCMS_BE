package com.dental.clinic.management.booking_appointment.domain;

import com.dental.clinic.management.booking_appointment.enums.AppointmentParticipantRole;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * AppointmentParticipant Entity
 * LÃ†Â°u cÃƒÂ¡c nhÃƒÂ¢n viÃƒÂªn khÃƒÂ¡c tham gia lÃ¡Â»â€¹ch hÃ¡ÂºÂ¹n (ngoÃƒÂ i BÃƒÂ¡c sÃ„Â© chÃƒÂ­nh)
 * VD: PhÃ¡Â»Â¥ tÃƒÂ¡, BÃƒÂ¡c sÃ„Â© phÃ¡Â»Â¥, Quan sÃƒÂ¡t viÃƒÂªn
 *
 * Default role: ASSISTANT khi tÃ¡ÂºÂ¡o appointment mÃ¡Â»â€ºi
 */
@Entity
@Table(name = "appointment_participants")
public class AppointmentParticipant {

    @EmbeddedId
    private AppointmentParticipantId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_role", nullable = false)
    private AppointmentParticipantRole role = AppointmentParticipantRole.ASSISTANT;

    // Constructors
    public AppointmentParticipant() {
    }

    public AppointmentParticipant(Integer appointmentId, Integer employeeId, AppointmentParticipantRole role) {
        this.id = new AppointmentParticipantId(appointmentId, employeeId);
        this.role = role;
    }

    // Getters and Setters
    public AppointmentParticipantId getId() {
        return id;
    }

    public void setId(AppointmentParticipantId id) {
        this.id = id;
    }

    public AppointmentParticipantRole getRole() {
        return role;
    }

    public void setRole(AppointmentParticipantRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AppointmentParticipant))
            return false;
        AppointmentParticipant that = (AppointmentParticipant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AppointmentParticipant{" +
                "appointmentId=" + (id != null ? id.getAppointmentId() : null) +
                ", employeeId=" + (id != null ? id.getEmployeeId() : null) +
                ", role='" + role + '\'' +
                '}';
    }

    /**
     * Composite Primary Key for AppointmentParticipant
     */
    @Embeddable
    public static class AppointmentParticipantId implements Serializable {

        @Column(name = "appointment_id", nullable = false)
        private Integer appointmentId;

        @Column(name = "employee_id", nullable = false)
        private Integer employeeId;

        // Constructors
        public AppointmentParticipantId() {
        }

        public AppointmentParticipantId(Integer appointmentId, Integer employeeId) {
            this.appointmentId = appointmentId;
            this.employeeId = employeeId;
        }

        // Getters and Setters
        public Integer getAppointmentId() {
            return appointmentId;
        }

        public void setAppointmentId(Integer appointmentId) {
            this.appointmentId = appointmentId;
        }

        public Integer getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Integer employeeId) {
            this.employeeId = employeeId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AppointmentParticipantId))
                return false;
            AppointmentParticipantId that = (AppointmentParticipantId) o;
            return Objects.equals(appointmentId, that.appointmentId) &&
                    Objects.equals(employeeId, that.employeeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(appointmentId, employeeId);
        }
    }
}
