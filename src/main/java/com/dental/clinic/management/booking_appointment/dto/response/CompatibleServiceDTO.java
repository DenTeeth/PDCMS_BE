package com.dental.clinic.management.booking_appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a service that a room can perform.
 * <p>
 * Used in GET /api/v1/rooms/{roomCode}/services response
 * to show compatible services for a room.
 * </p>
 *
 * <p>
 * <b>Example JSON:</b>
 * </p>
 * 
 * <pre>
 * {
 *   "serviceId": 35,
 *   "serviceCode": "IMPL_SURGERY_KR",
 *   "serviceName": "PhÃ¡ÂºÂ«u thuÃ¡ÂºÂ­t Ã„â€˜Ã¡ÂºÂ·t trÃ¡Â»Â¥ Implant HÃƒÂ n QuÃ¡Â»â€˜c",
 *   "price": 15000000
 * }
 * </pre>
 *
 * @since V16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompatibleServiceDTO {

    /**
     * Service ID (database primary key).
     */
    private Long serviceId;

    /**
     * Service code (business key, e.g., "IMPL_SURGERY_KR").
     */
    private String serviceCode;

    /**
     * Service name (Vietnamese display name).
     */
    private String serviceName;

    /**
     * Service price in VND.
     */
    private BigDecimal price;
}
