package com.dental.clinic.management.booking_appointment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Time Slot DTO
 * Represents a specific time slot with available compatible rooms
 */
public class TimeSlotDTO {

    /**
     * Start time of this slot
     * Format: ISO 8601 (YYYY-MM-DDTHH:mm:ss)
     * Example: "2025-10-30T09:30:00"
     */
    private LocalDateTime startTime;

    /**
     * List of room codes that are:
     * 1. Compatible with ALL requested services (from room_services)
     * 2. Available (not busy) at this time slot
     *
     * Example: ["P-IMPLANT-01", "P-IMPLANT-02"]
     */
    private List<String> availableCompatibleRoomCodes;

    /**
     * Optional: Additional info about this slot
     * Example: "Peak hour - may have delays"
     */
    private String note;

    public TimeSlotDTO() {
    }

    public TimeSlotDTO(LocalDateTime startTime, List<String> availableCompatibleRoomCodes, String note) {
        this.startTime = startTime;
        this.availableCompatibleRoomCodes = availableCompatibleRoomCodes;
        this.note = note;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public List<String> getAvailableCompatibleRoomCodes() {
        return availableCompatibleRoomCodes;
    }

    public void setAvailableCompatibleRoomCodes(List<String> availableCompatibleRoomCodes) {
        this.availableCompatibleRoomCodes = availableCompatibleRoomCodes;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
