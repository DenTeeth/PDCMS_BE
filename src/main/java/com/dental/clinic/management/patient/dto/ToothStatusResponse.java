package com.dental.clinic.management.patient.dto;

import com.dental.clinic.management.patient.domain.ToothConditionEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Tooth Status (API 8.9)
 *
 * @author Dental Clinic System
 * @since API 8.9
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToothStatusResponse {

    private Integer toothStatusId;

    private Integer patientId;

    private String toothNumber;

    private ToothConditionEnum status;

    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
