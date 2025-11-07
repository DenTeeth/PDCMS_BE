package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
/**
 * Request DTO for approving or rejecting a part-time registration.
 * 
 * Used by managers in: PATCH /api/v1/admin/registrations/part-time-flex/{id}/status
 * 
 * Example (Approve):
 * {
 *   "status": "APPROVED"
 * }
 * 
 * Example (Reject):
 * {
 *   "status": "REJECTED",
 *   "reason": "KhÃƒÂ´ng Ã„â€˜Ã¡Â»Â§ nhÃƒÂ¢n sÃ¡Â»Â± trong thÃ¡Â»Âi gian nÃƒÂ y"
 * }
 */
public class UpdateRegistrationStatusRequest {

    /**
     * New status: "APPROVED" or "REJECTED"
     * PENDING is not allowed (can't revert to pending).
     */
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "APPROVED|REJECTED", message = "Status must be either APPROVED or REJECTED")
    private String status;

    /**
     * Rejection reason (REQUIRED if status = REJECTED).
     * Example: "Ã„ÂÃƒÂ£ Ã„â€˜Ã¡Â»Â§ nhÃƒÂ¢n sÃ¡Â»Â± cho ca nÃƒÂ y"
     */
    private String reason;
}
