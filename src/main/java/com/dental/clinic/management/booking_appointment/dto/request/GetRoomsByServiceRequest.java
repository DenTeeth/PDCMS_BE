package com.dental.clinic.management.booking_appointment.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for getting rooms compatible with specific services.
 * 
 * NEW: Room filtering by service type (2024-12-29)
 * Use case: When creating appointment for implant service, only show implant rooms
 * 
 * Features:
 * - Returns only rooms that support ALL specified services
 * - Filters out inactive rooms
 * - Returns empty list if no compatible rooms found
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetRoomsByServiceRequest {
    
    /**
     * List of service codes to check compatibility for.
     * Room must support ALL of these services to be included in results.
     * 
     * Example: ["IMPLANT_L1", "IMPLANT_L2"] → Returns rooms that support both implant levels
     */
    @NotEmpty(message = "At least one service code is required")
    private List<String> serviceCodes;
}
