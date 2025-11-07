package com.dental.clinic.management.working_schedule.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a specific holiday date.
 * A holiday date belongs to a holiday definition (e.g., "Lunar New Year 2025").
 * Uses composite primary key: (holiday_date + definition_id).
 */
@Entity
@Table(name = "holiday_dates")
@IdClass(HolidayDateId.class)
public class HolidayDate {

    /**
     * The actual date of the holiday (part of composite PK).
     */
    @Id
    @Column(name = "holiday_date", nullable = false)
    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    /**
     * Reference to the holiday definition ID (part of composite PK).
     */
    @Id
    @Column(name = "definition_id", length = 20, nullable = false)
    private String definitionId;

    /**
     * Many-to-One relationship with HolidayDefinition.
     * Uses definitionId as foreign key.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", referencedColumnName = "definition_id", insertable = false, updatable = false)
    private HolidayDefinition holidayDefinition;

    /**
     * Optional description for this specific date.
     * E.g., "First day of Tet", "Victory Day celebration"
     */
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public HolidayDate() {
    }

    public HolidayDate(LocalDate holidayDate, String definitionId, HolidayDefinition holidayDefinition,
            String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.holidayDate = holidayDate;
        this.definitionId = definitionId;
        this.holidayDefinition = holidayDefinition;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public HolidayDefinition getHolidayDefinition() {
        return holidayDefinition;
    }

    public void setHolidayDefinition(HolidayDefinition holidayDefinition) {
        this.holidayDefinition = holidayDefinition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
