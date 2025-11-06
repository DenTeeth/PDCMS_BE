package com.dental.clinic.management.booking_appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for GET /api/v1/rooms/{roomCode}/services.
 * <p>
 * Shows which services can be performed in a specific room.
 * </p>
 *
 * <p>
 * <b>Example JSON:</b>
 * </p>
 *
 * <pre>
 * {
 *   "roomId": "GHE251103004",
 *   "roomCode": "P-04",
 *   "roomName": "PhÃƒÂ²ng Implant",
 *   "compatibleServices": [
 *     {
 *       "serviceId": 35,
 *       "serviceCode": "IMPL_SURGERY_KR",
 *       "serviceName": "PhÃ¡ÂºÂ«u thuÃ¡ÂºÂ­t Ã„â€˜Ã¡ÂºÂ·t trÃ¡Â»Â¥ Implant HÃƒÂ n QuÃ¡Â»â€˜c",
 *       "price": 15000000
 *     },
 *     {
 *       "serviceId": 36,
 *       "serviceCode": "IMPL_BONE_GRAFT",
 *       "serviceName": "GhÃƒÂ©p xÃ†Â°Ã†Â¡ng Ã¡Â»â€¢ rÃ„Æ’ng",
 *       "price": 5000000
 *     }
 *   ]
 * }
 * </pre>
 *
 * @since V16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomServicesResponse {

    /**
     * Room ID (database primary key - VARCHAR).
     */
    private String roomId;

    /**
     * Room code (business key, e.g., "P-04").
     */
    private String roomCode;

    /**
     * Room name (Vietnamese display name).
     */
    private String roomName;

    /**
     * List of services that can be performed in this room.
     * Empty list if no services assigned.
     */
    private List<CompatibleServiceDTO> compatibleServices;
}
