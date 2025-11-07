package com.dental.clinic.management.booking_appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * Request DTO for P3.1: Find Available Time Slots
 *
 * Business Logic:
 * 1. Validate date (not in past)
 * 2. Validate employee, services, participants exist and active
 * 3. Calculate total duration from services
 * 4. Filter compatible rooms (room_services)
 * 5. Find intersection of available time (doctor + assistants + rooms)
 */
public class AvailableTimesRequest {

    /**
     * Date to search for available slots
     * Format: YYYY-MM-DD
     * Validation: Must not be in the past
     */
    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in format YYYY-MM-DD")
    private String date;

    /**
     * Employee code of primary doctor
     * Example: "EMP001", "BS-NGUYEN-VAN-A"
     */
    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    /**
     * List of service codes for this appointment
     * Example: ["SCALING_L1", "FILLING_COMP"]
     * Will be used to:
     * - Calculate total duration (sum of duration + buffer)
     * - Filter compatible rooms (room_services)
     * - Check doctor specialization
     */
    @NotEmpty(message = "At least one service code is required")
    private List<String> serviceCodes;

    /**
     * Optional: List of participant employee codes (assistants)
     * Example: ["PT-001", "PT-002"]
     * Will be used to check their availability in the intersection algorithm
     */
    private List<String> participantCodes;

    public AvailableTimesRequest() {
    }

    public AvailableTimesRequest(String date, String employeeCode, List<String> serviceCodes,
            List<String> participantCodes) {
        this.date = date;
        this.employeeCode = employeeCode;
        this.serviceCodes = serviceCodes;
        this.participantCodes = participantCodes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public List<String> getServiceCodes() {
        return serviceCodes;
    }

    public void setServiceCodes(List<String> serviceCodes) {
        this.serviceCodes = serviceCodes;
    }

    public List<String> getParticipantCodes() {
        return participantCodes;
    }

    public void setParticipantCodes(List<String> participantCodes) {
        this.participantCodes = participantCodes;
    }
}
