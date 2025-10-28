package com.dental.clinic.management.booking_appointment.service;

import com.dental.clinic.management.booking_appointment.domain.Room;
import com.dental.clinic.management.booking_appointment.dto.request.CreateRoomRequest;
import com.dental.clinic.management.booking_appointment.dto.request.UpdateRoomRequest;
import com.dental.clinic.management.booking_appointment.dto.response.RoomResponse;
import com.dental.clinic.management.booking_appointment.mapper.RoomMapper;
import com.dental.clinic.management.booking_appointment.repository.RoomRepository;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing rooms
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final IdGenerator idGenerator;

    /**
     * Inject IdGenerator into Room entity after bean creation
     */
    @PostConstruct
    public void init() {
        Room.setIdGenerator(idGenerator);
        log.info("IdGenerator injected into Room entity");
    }

    /**
     * Get all rooms with pagination
     */
    @Transactional(readOnly = true)
    public Page<RoomResponse> getAllRooms(int page, int size, String sortBy, String sortDirection) {
        log.debug("Request to get all rooms - page: {}, size: {}", page, size);

        // Validate inputs
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return roomRepository.findAll(pageable)
                .map(roomMapper::toResponse);
    }

    /**
     * Get all active rooms
     */
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllActiveRooms() {
        log.debug("Request to get all active rooms");

        return roomRepository.findByIsActiveTrue().stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get room by ID
     */
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(String roomId) {
        log.debug("Request to get room by ID: {}", roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Room not found with ID: " + roomId,
                        "room",
                        "notfound"));

        return roomMapper.toResponse(room);
    }

    /**
     * Get room by code
     */
    @Transactional(readOnly = true)
    public RoomResponse getRoomByCode(String roomCode) {
        log.debug("Request to get room by code: {}", roomCode);

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Room not found with code: " + roomCode,
                        "room",
                        "notfound"));

        return roomMapper.toResponse(room);
    }

    /**
     * Search rooms by keyword
     */
    @Transactional(readOnly = true)
    public List<RoomResponse> searchRooms(String keyword, boolean activeOnly) {
        log.debug("Request to search rooms with keyword: {}, activeOnly: {}", keyword, activeOnly);

        List<Room> rooms = activeOnly
                ? roomRepository.searchActiveByCodeOrName(keyword)
                : roomRepository.searchByCodeOrName(keyword);

        return rooms.stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get rooms by type
     */
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByType(String roomType, boolean activeOnly) {
        log.debug("Request to get rooms by type: {}, activeOnly: {}", roomType, activeOnly);

        List<Room> rooms = activeOnly
                ? roomRepository.findByRoomTypeAndIsActiveTrue(roomType)
                : roomRepository.findByRoomType(roomType);

        return rooms.stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new room
     */
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        log.debug("Request to create room: {}", request);

        // Check if room code already exists
        if (roomRepository.existsByRoomCode(request.getRoomCode())) {
            throw new BadRequestAlertException(
                    "Room code already exists: " + request.getRoomCode(),
                    "room",
                    "codeexists");
        }

        Room room = roomMapper.toEntity(request);
        room = roomRepository.save(room);

        log.info("Created room with ID: {} and code: {}", room.getRoomId(), room.getRoomCode());

        return roomMapper.toResponse(room);
    }

    /**
     * Update a room
     */
    @Transactional
    public RoomResponse updateRoom(String roomId, UpdateRoomRequest request) {
        log.debug("Request to update room ID: {} with data: {}", roomId, request);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Room not found with ID: " + roomId,
                        "room",
                        "notfound"));

        // Check if new room code already exists (and it's not the current room)
        if (request.getRoomCode() != null &&
                !request.getRoomCode().equals(room.getRoomCode()) &&
                roomRepository.existsByRoomCode(request.getRoomCode())) {
            throw new BadRequestAlertException(
                    "Room code already exists: " + request.getRoomCode(),
                    "room",
                    "codeexists");
        }

        // Update fields if provided
        if (request.getRoomCode() != null) {
            room.setRoomCode(request.getRoomCode());
        }
        if (request.getRoomName() != null) {
            room.setRoomName(request.getRoomName());
        }
        if (request.getRoomType() != null) {
            room.setRoomType(request.getRoomType());
        }
        if (request.getIsActive() != null) {
            room.setIsActive(request.getIsActive());
        }

        room = roomRepository.save(room);

        log.info("Updated room with ID: {}", roomId);

        return roomMapper.toResponse(room);
    }

    /**
     * Delete a room (soft delete by setting isActive = false)
     */
    @Transactional
    public void deleteRoom(String roomId) {
        log.debug("Request to delete room ID: {}", roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Room not found with ID: " + roomId,
                        "room",
                        "notfound"));

        room.setIsActive(false);
        roomRepository.save(room);

        log.info("Soft deleted room with ID: {}", roomId);
    }

    /**
     * Permanently delete a room (hard delete)
     */
    @Transactional
    public void permanentlyDeleteRoom(String roomId) {
        log.debug("Request to permanently delete room ID: {}", roomId);

        if (!roomRepository.existsById(roomId)) {
            throw new BadRequestAlertException(
                    "Room not found with ID: " + roomId,
                    "room",
                    "notfound");
        }

        roomRepository.deleteById(roomId);

        log.info("Permanently deleted room with ID: {}", roomId);
    }
}
