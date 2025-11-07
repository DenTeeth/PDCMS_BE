package com.dental.clinic.management.booking_appointment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Room information response")
public class RoomResponse {

    @Schema(description = "Room ID", example = "GHE001")
    private String roomId;

    @Schema(description = "Room code", example = "P1")
    private String roomCode;

    @Schema(description = "Room name", example = "PhÃƒÂ²ng 01")
    private String roomName;

    @Schema(description = "Room type", example = "STANDARD")
    private String roomType;

    @Schema(description = "Is room active", example = "true")
    private Boolean isActive;

    @Schema(description = "Created timestamp", example = "2025-10-27T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp", example = "2025-10-27T10:00:00")
    private LocalDateTime updatedAt;

    public RoomResponse() {
    }

    public RoomResponse(String roomId, String roomCode, String roomName, String roomType,
            Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.roomType = roomType;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
