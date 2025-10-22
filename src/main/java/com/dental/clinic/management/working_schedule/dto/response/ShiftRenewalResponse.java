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
    private String message;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;

    // Additional information about the expiring registration
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String shiftDetails; // e.g., "Monday, Wednesday (MORNING)"
}
