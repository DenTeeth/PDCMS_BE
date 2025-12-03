package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    private Integer appointmentId;
    private String appointmentCode;
    private String roomId;
    private String appointmentStartTime;
    private String appointmentEndTime;
    private Integer expectedDurationMinutes;
    private String status;
    private String notes;
}
