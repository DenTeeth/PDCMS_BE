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

    public UpdateWorkShiftRequest() {
    }

    public UpdateWorkShiftRequest(String shiftName, LocalTime startTime, LocalTime endTime) {
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
