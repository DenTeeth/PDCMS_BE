package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalTime;

/**
 * DTO for updating an existing work shift.
 * All fields are optional.
 * Category is removed - auto-updated based on time changes.
 */
public class UpdateWorkShiftRequest {

    @Size(max = 100, message = "TÃƒÂªn ca lÃƒÂ m viÃ¡Â»â€¡c khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c vÃ†Â°Ã¡Â»Â£t quÃƒÂ¡ 100 kÃƒÂ½ tÃ¡Â»Â±")
    private String shiftName;

    private LocalTime startTime;

    private LocalTime endTime;

    // Category is removed - auto-updated when time changes
}
