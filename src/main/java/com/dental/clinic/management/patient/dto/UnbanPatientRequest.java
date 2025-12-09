package com.dental.clinic.management.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for patient unban request.
 */
@Data
public class UnbanPatientRequest {

    @NotBlank(message = "Lý do mở khóa không được để trống")
    @Size(min = 10, max = 500, message = "Lý do phải từ 10 đến 500 ký tự")
    private String reason;
}
