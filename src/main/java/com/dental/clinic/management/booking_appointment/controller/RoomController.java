package com.dental.clinic.management.booking_appointment.controller;

import com.dental.clinic.management.booking_appointment.dto.request.CreateRoomRequest;
import com.dental.clinic.management.booking_appointment.dto.request.UpdateRoomRequest;
import com.dental.clinic.management.booking_appointment.dto.response.RoomResponse;
import com.dental.clinic.management.booking_appointment.service.RoomService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

/**
 * REST controller for managing rooms/chairs in the dental clinic.
 * Base path: /api/v1/rooms
 */
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Room Management", description = "APIs for managing dental clinic rooms and chairs")
public class RoomController {

    private final RoomService roomService;

    /**
     * Get all rooms with pagination and filters
     *
     * @param page          page number (default: 0)
     * @param size          page size (default: 10, max: 100)
     * @param sortBy        field to sort by (default: roomId)
     * @param sortDirection sort direction ASC or DESC (default: ASC)
     * @param isActive      filter by active status (optional)
     * @param roomType      filter by room type (optional)
     * @param keyword       search by code or name (optional)
     * @return paginated list of rooms
     */
    @GetMapping
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_ROOM + "')")
    @Operation(summary = "Get all rooms with filters", description = "Retrieve all rooms with pagination, sorting and filters")
    @ApiMessage("Lấy danh sách phòng thành công")
    public ResponseEntity<Page<RoomResponse>> getAllRooms(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "roomId") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDirection,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Filter by room type") @RequestParam(required = false) String roomType,
            @Parameter(description = "Search by code or name") @RequestParam(required = false) String keyword) {

        Page<RoomResponse> rooms = roomService.getAllRooms(page, size, sortBy, sortDirection, isActive, roomType,
                keyword);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get all active rooms (no pagination, for dropdown/select options)
     *
     * @return list of active rooms
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_ROOM + "')")
    @Operation(summary = "Get all active rooms", description = "Retrieve all active rooms without pagination (for dropdowns)")
    @ApiMessage("Lấy danh sách phòng đang hoạt động thành công")
    public ResponseEntity<List<RoomResponse>> getAllActiveRooms() {
        List<RoomResponse> rooms = roomService.getAllActiveRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get room by ID
     *
     * @param roomId room ID
     * @return room details
     */
    @GetMapping("/{roomId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_ROOM + "')")
    @Operation(summary = "Get room by ID", description = "Retrieve room details by room ID")
    @ApiMessage("Lấy thông tin phòng thành công")
    public ResponseEntity<RoomResponse> getRoomById(
            @Parameter(description = "Room ID") @PathVariable String roomId) {

        RoomResponse room = roomService.getRoomById(roomId);
        return ResponseEntity.ok(room);
    }

    /**
     * Create a new room
     *
     * @param request room creation data
     * @return created room
     */
    @PostMapping
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + CREATE_ROOM + "')")
    @Operation(summary = "Create new room", description = "Create a new room/chair in the clinic")
    @ApiMessage("Tạo phòng mới thành công")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse room = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    /**
     * Update a room
     *
     * @param roomId  room ID to update
     * @param request room update data
     * @return updated room
     */
    @PutMapping("/{roomId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + UPDATE_ROOM + "')")
    @Operation(summary = "Update room", description = "Update room information")
    @ApiMessage("Cập nhật thông tin phòng thành công")
    public ResponseEntity<RoomResponse> updateRoom(
            @Parameter(description = "Room ID") @PathVariable String roomId,
            @Valid @RequestBody UpdateRoomRequest request) {

        RoomResponse room = roomService.updateRoom(roomId, request);
        return ResponseEntity.ok(room);
    }

    /**
     * Permanently delete a room (hard delete)
     * WARNING: This action cannot be undone!
     *
     * @param roomId room ID to permanently delete
     * @return no content
     */
    @DeleteMapping("/{roomId}/permanent")
    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Operation(summary = "Permanently delete room", description = "Permanently delete a room from database (Admin only)")
    @ApiMessage("Xóa vĩnh viễn phòng thành công")
    public ResponseEntity<Void> permanentlyDeleteRoom(
            @Parameter(description = "Room ID") @PathVariable String roomId) {

        roomService.permanentlyDeleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Soft delete a room (set isActive = false)
     *
     * @param roomId room ID to delete
     * @return no content
     */
    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + DELETE_ROOM + "')")
    @Operation(summary = "Delete room (soft delete)", description = "Deactivate a room by setting isActive to false")
    @ApiMessage("Xóa phòng thành công")
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "Room ID") @PathVariable String roomId) {

        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}
