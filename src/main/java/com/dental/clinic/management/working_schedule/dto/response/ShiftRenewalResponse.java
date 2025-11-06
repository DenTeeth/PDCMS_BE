package com.dental.clinic.management.working_schedule.dto.response;

import com.dental.clinic.management.working_schedule.enums.RenewalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for shift renewal request.
 * Used for both GET pending renewals and PATCH respond actions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRenewalResponse {
    private String renewalId;
    private Integer expiringRegistrationId;
    private Integer employeeId;
    private String employeeName;
    private RenewalStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;

    /**
     * Reason for declining (only set when status = DECLINED).
     */
    private String declineReason;

    // Additional information about the expiring FIXED registration
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String workShiftName; // e.g., "Ca sÃƒÂ¡ng" (from work_shift table)
    private String shiftDetails; // e.g., "Monday, Wednesday (Ca sÃƒÂ¡ng)"

    /**
     * Dynamic message for FE to display.
     * Format: "HÃ¡Â»Â£p Ã„â€˜Ã¡Â»â€œng/LÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c cÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh [shiftName] cÃ¡Â»Â§a bÃ¡ÂºÂ¡n sÃ¡ÂºÂ½ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n vÃƒÂ o
     * ngÃƒÂ y [date].
     * BÃ¡ÂºÂ¡n cÃƒÂ³ muÃ¡Â»â€˜n gia hÃ¡ÂºÂ¡n thÃƒÂªm [1 thÃƒÂ¡ng] khÃƒÂ´ng?"
     */
    private String message;
}
