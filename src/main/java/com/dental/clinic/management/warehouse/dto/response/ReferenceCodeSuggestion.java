package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Reference Code Suggestion DTO
 * Used for auto-complete and dropdown suggestions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceCodeSuggestion {
    
    /**
     * The reference code value (e.g., APT-2025-001, REQ-2025-050)
     */
    private String code;
    
    /**
     * Display label with additional context
     * e.g., "APT-2025-001 (Nguyễn Văn A - 2025-12-25)"
     */
    private String label;
    
    /**
     * Type of reference: APPOINTMENT, REQUEST, CUSTOM
     */
    private String type;
    
    /**
     * Date when this reference was last used
     */
    private LocalDate lastUsedDate;
    
    /**
     * Number of times this reference code has been used
     */
    private Integer usageCount;
    
    /**
     * Related appointment ID (if type is APPOINTMENT)
     */
    private Long relatedAppointmentId;
    
    /**
     * Patient name (if linked to appointment)
     */
    private String patientName;
}
