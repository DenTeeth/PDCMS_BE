package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Room entity.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

       /**
        * Find room by room code
        */
       Optional<Room> findByRoomCode(String roomCode);

       /**
        * Check if room code exists
        */
       boolean existsByRoomCode(String roomCode);

       /**
        * Find all active rooms
        */
       List<Room> findByIsActiveTrue();

       /**
        * Find rooms by type
        */
       List<Room> findByRoomType(String roomType);

       /**
        * Find active rooms by type
        */
       List<Room> findByRoomTypeAndIsActiveTrue(String roomType);

       /**
        * Search rooms by code or name (case-insensitive)
        */
       @Query("SELECT r FROM Room r WHERE " +
                     "LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(r.roomName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       List<Room> searchByCodeOrName(String keyword);

       /**
        * Search active rooms by code or name
        */
       @Query("SELECT r FROM Room r WHERE r.isActive = true AND (" +
                     "LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(r.roomName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
       List<Room> searchActiveByCodeOrName(String keyword);
}
