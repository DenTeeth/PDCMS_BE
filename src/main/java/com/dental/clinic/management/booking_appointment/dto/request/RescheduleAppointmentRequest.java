package com.dental.clinic.management.booking_appointment.dto.request;

import com.dental.clinic.management.booking_appointment.enums.AppointmentReasonCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for rescheduling appointment (Cancel old + Create new).
 * API: POST /api/v1/appointments/{appointmentCode}/reschedule
 *
 * Business: Cancel old appointment and create new one with new
 * time/doctor/room.
 * Patient and services remain the same (or can be changed via newServiceIds).
 */
public class RescheduleAppointmentRequest {

    // ==================== NEW APPOINTMENT INFO ====================

    /**
     * New doctor employee code.
     * Required.
     */
    @NotBlank(message = "New employee code is required")
    private String newEmployeeCode;

    /**
     * New room code.
     * Required.
     */
    @NotBlank(message = "New room code is required")
    private String newRoomCode;

    /**
     * New appointment start time.
     * Must not be in the past.
     * Required.
     */
    @NotNull(message = "New start time is required")
    private LocalDateTime newStartTime;

    /**
     * New participant employee codes (nurses, assistants).
     * Optional - can be empty list.
     */
    private List<String> newParticipantCodes;

    /**
     * New service IDs.
     * Optional - if null or empty, reuse old appointment's services.
     * If provided, will replace old services entirely.
     */
    private List<Integer> newServiceIds;

    // ==================== OLD APPOINTMENT CANCELLATION INFO ====================

    /**
     * Reason code for canceling old appointment.
     * Required.
     */
    @NotNull(message = "Reason code is required")
    private AppointmentReasonCode reasonCode;

    /**
     * Additional notes for cancellation.
     * Optional.
     */
    private String cancelNotes;

    public RescheduleAppointmentRequest() {
    }

    public RescheduleAppointmentRequest(String newEmployeeCode, String newRoomCode,
            LocalDateTime newStartTime, List<String> newParticipantCodes,
            List<Integer> newServiceIds, AppointmentReasonCode reasonCode,
            String cancelNotes) {
        this.newEmployeeCode = newEmployeeCode;
        this.newRoomCode = newRoomCode;
        this.newStartTime = newStartTime;
        this.newParticipantCodes = newParticipantCodes;
        this.newServiceIds = newServiceIds;
        this.reasonCode = reasonCode;
        this.cancelNotes = cancelNotes;
    }

    public String getNewEmployeeCode() {
        return newEmployeeCode;
    }

    public void setNewEmployeeCode(String newEmployeeCode) {
        this.newEmployeeCode = newEmployeeCode;
    }

    public String getNewRoomCode() {
        return newRoomCode;
    }

    public void setNewRoomCode(String newRoomCode) {
        this.newRoomCode = newRoomCode;
    }

    public LocalDateTime getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(LocalDateTime newStartTime) {
        this.newStartTime = newStartTime;
    }

    public List<String> getNewParticipantCodes() {
        return newParticipantCodes;
    }

    public void setNewParticipantCodes(List<String> newParticipantCodes) {
        this.newParticipantCodes = newParticipantCodes;
    }

    public List<Integer> getNewServiceIds() {
        return newServiceIds;
    }

    public void setNewServiceIds(List<Integer> newServiceIds) {
        this.newServiceIds = newServiceIds;
    }

    public AppointmentReasonCode getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(AppointmentReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getCancelNotes() {
        return cancelNotes;
    }

    public void setCancelNotes(String cancelNotes) {
        this.cancelNotes = cancelNotes;
    }
}
