package com.dental.clinic.management.working_schedule.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for time_off_types table
 * Represents different types of time-off (annual leave, sick leave, etc.)
 */
@Entity
@Table(name = "time_off_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeOffType {

    @Id
    @Column(name = "type_id", length = 50)
    private String typeId;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "description")
    private String description;

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Override
    public String toString() {
        return "TimeOffType{" +
                "typeId='" + typeId + '\'' +
                ", typeName='" + typeName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
