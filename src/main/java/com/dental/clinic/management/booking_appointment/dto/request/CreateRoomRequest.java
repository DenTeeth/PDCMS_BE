package com.dental.clinic.management.booking_appointment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new room")
public class CreateRoomRequest {

    @SuppressWarnings("deprecation")
    @NotBlank(message = "Mã phòng là bắt buộc")
    @Size(max = 20, message = "Mã phòng không được vượt quá 20 ký tự")
    @Schema(description = "Unique room code", example = "P1", required = true)
    private String roomCode;

    @SuppressWarnings("deprecation")
    @NotBlank(message = "Tên phòng là bắt buộc")
    @Size(max = 100, message = "Tên phòng không được vượt quá 100 ký tự")
    @Schema(description = "Room name", example = "Phòng 01", required = true)
    private String roomName;

    @Size(max = 50, message = "Loại phòng không được vượt quá 50 ký tự")
    @Schema(description = "Room type", example = "STANDARD", allowableValues = { "STANDARD", "XRAY", "IMPLANT" })
    private String roomType;
}
