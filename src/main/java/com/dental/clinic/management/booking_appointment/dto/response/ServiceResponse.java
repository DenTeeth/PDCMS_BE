package com.dental.clinic.management.booking_appointment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for dental service
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dental service response")
public class ServiceResponse {

    @Schema(description = "Service ID", example = "1")
    private Integer serviceId;

    @Schema(description = "Service code", example = "SV-CAOVOI")
    private String serviceCode;

    @Schema(description = "Service name", example = "CÃ¡ÂºÂ¡o vÃƒÂ´i rÃ„Æ’ng vÃƒÂ  Ã„ÂÃƒÂ¡nh bÃƒÂ³ng")
    private String serviceName;

    @Schema(description = "Service description", example = "LÃ¡ÂºÂ¥y sÃ¡ÂºÂ¡ch vÃƒÂ´i rÃ„Æ’ng...")
    private String description;

    @Schema(description = "Default duration in minutes", example = "30")
    private Integer defaultDurationMinutes;

    @Schema(description = "Default buffer time in minutes", example = "10")
    private Integer defaultBufferMinutes;

    @Schema(description = "Service price (VND)", example = "300000")
    private BigDecimal price;

    @Schema(description = "Specialization ID", example = "1")
    private Integer specializationId;

    @Schema(description = "Specialization name", example = "ChÃ¡Â»â€°nh nha")
    private String specializationName;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Created at", example = "2025-10-29T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at", example = "2025-10-29T15:45:00")
    private LocalDateTime updatedAt;
}
