package com.dental.clinic.management.booking_appointment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new room")
public class CreateRoomRequest {

    @NotBlank(message = "Room code is required")
    @Size(max = 20, message = "Room code must not exceed 20 characters")
    @Schema(description = "Unique room code", example = "P1", required = true)
    private String roomCode;

    @NotBlank(message = "Room name is required")
    @Size(max = 100, message = "Room name must not exceed 100 characters")
    @Schema(description = "Room name", example = "PhÃƒÂ²ng 01", required = true)
    private String roomName;

    @Size(max = 50, message = "Room type must not exceed 50 characters")
    @Schema(description = "Room type", example = "STANDARD", allowableValues = { "STANDARD", "XRAY", "IMPLANT" })
    private String roomType;

    public CreateRoomRequest() {
    }

    public CreateRoomRequest(String roomCode, String roomName, String roomType) {
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.roomType = roomType;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
}
