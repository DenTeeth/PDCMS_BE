package com.dental.clinic.management.patient.dto;

import com.dental.clinic.management.patient.enums.PatientBlacklistReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * BR-044: Request DTO for blacklisting a patient.
 */
@Data
public class BlacklistPatientRequest {
    
    @NotNull(message = "Lý do blacklist không được để trống")
    private PatientBlacklistReason reason;
    
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
}
