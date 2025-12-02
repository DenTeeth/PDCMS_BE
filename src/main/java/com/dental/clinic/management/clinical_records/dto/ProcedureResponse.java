package com.dental.clinic.management.clinical_records.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Clinical Record Procedure (API 8.4)
 * 
 * Maps to clinical_record_procedures table
 * Includes service information via JOIN
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureResponse {

    private Integer procedureId;
    
    private Integer clinicalRecordId;
    
    // Service information (from JOIN)
    private Long serviceId;  // DentalService uses Long
    
    private String serviceName;
    
    private String serviceCode;
    
    // Link to treatment plan
    private Long patientPlanItemId;  // PatientPlanItem uses Long itemId
    
    // Procedure details
    private String procedureDescription;  // REQUIRED field from schema
    
    private String toothNumber;
    
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
