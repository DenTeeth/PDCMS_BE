package com.dental.clinic.management.working_schedule.dto.response;

import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for TimeOffRequest
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeOffRequestResponse {

    private String requestId;
    private Integer employeeId;
    private String timeOffTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String slotId;
    private String reason;
    private TimeOffStatus status;
    private Integer requestedBy;
    private LocalDateTime requestedAt;
    private Integer approvedBy;
    private LocalDateTime approvedAt;
    private String rejectedReason;
    private String cancellationReason;
}
