package com.dental.clinic.management.booking_appointment.dto.response;

import java.util.List;

/**
 * Response DTO for P3.1: Available Time Slots
 *
 * Returns list of available slots with compatible rooms for each slot
 */
public class AvailableTimesResponse {

    /**
     * Total duration needed for all services (including buffer time)
     * Unit: minutes
     * Calculated from: SUM(defaultDurationMinutes + defaultBufferMinutes)
     */
    private Integer totalDurationNeeded;

    /**
     * List of available time slots
     * Each slot includes compatible rooms that are available at that time
     */
    private List<TimeSlotDTO> availableSlots;

    /**
     * Optional: Message if no slots found
     * Example: "Không có phòng nào hỗ trợ các dịch
     * vụ này"
     */
    private String message;

    public AvailableTimesResponse() {
    }

    public AvailableTimesResponse(Integer totalDurationNeeded, List<TimeSlotDTO> availableSlots, String message) {
        this.totalDurationNeeded = totalDurationNeeded;
        this.availableSlots = availableSlots;
        this.message = message;
    }

    public Integer getTotalDurationNeeded() {
        return totalDurationNeeded;
    }

    public void setTotalDurationNeeded(Integer totalDurationNeeded) {
        this.totalDurationNeeded = totalDurationNeeded;
    }

    public List<TimeSlotDTO> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<TimeSlotDTO> availableSlots) {
        this.availableSlots = availableSlots;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
