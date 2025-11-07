package com.dental.clinic.management.working_schedule.dto.request;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for updating a fixed shift registration.
 */
public class UpdateFixedRegistrationRequest {

    private String workShiftId; // Optional

    private List<Integer> daysOfWeek; // Optional

    private LocalDate effectiveFrom; // Optional

    private LocalDate effectiveTo; // Optional (null = permanent)
}
