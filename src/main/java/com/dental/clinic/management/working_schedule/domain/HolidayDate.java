package com.dental.clinic.management.working_schedule.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entity representing a holiday date.
 * Used by scheduled jobs to skip creating shifts on holidays.
 */
@Entity
@Table(name = "holiday_dates", indexes = {
        @Index(name = "idx_holiday_date", columnList = "holiday_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holiday_id")
    private Long holidayId;

    /**
     * The actual date of the holiday.
     */
    @Column(name = "holiday_date", nullable = false, unique = true)
    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    /**
     * Name of the holiday.
     */
    @Column(name = "holiday_name", nullable = false, length = 200)
    @NotBlank(message = "Holiday name is required")
    @Size(max = 200, message = "Holiday name must not exceed 200 characters")
    private String holidayName;

    /**
     * Year this holiday applies to.
     * Helps with querying holidays for a specific year.
     */
    @Column(name = "year", nullable = false)
    @NotNull(message = "Year is required")
    private Integer year;

    /**
     * Optional description.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
