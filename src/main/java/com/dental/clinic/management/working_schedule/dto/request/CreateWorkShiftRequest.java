package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * DTO for creating a new work shift.
 * Note: shiftId and category are auto-generated based on time range.
 * Category is no longer sent in request - it's determined by start time.
 */
public class CreateWorkShiftRequest {

    @NotBlank(message = "TÃƒÂªn ca lÃƒÂ m viÃ¡Â»â€¡c khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    @Size(max = 100, message = "TÃƒÂªn ca lÃƒÂ m viÃ¡Â»â€¡c khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c vÃ†Â°Ã¡Â»Â£t quÃƒÂ¡ 100 kÃƒÂ½ tÃ¡Â»Â±")
    private String shiftName;

    @NotNull(message = "GiÃ¡Â»Â bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng")
    private LocalTime startTime;

    @NotNull(message = "GiÃ¡Â»Â kÃ¡ÂºÂ¿t thÃƒÂºc khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ' trÃ¡Â»â€˜ng")
    private LocalTime endTime;

    public CreateWorkShiftRequest() {
    }

    public CreateWorkShiftRequest(String shiftName, LocalTime startTime, LocalTime endTime) {
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

    // Category is removed - auto-generated based on startTime
    // NORMAL: startTime < 18:00 AND endTime <= 18:00
    // NIGHT: startTime >= 18:00
    // INVALID: shift spans across 18:00 boundary
}
