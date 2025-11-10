package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.DentalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DentalService entity (Booking Appointment Module)
 * 
 * Note: This is the OLD repository for booking_appointment module.
 * The NEW Service module has its own repository: com.dental.clinic.management.service.repository.DentalServiceRepository
 * 
 * This repository handles services WITH specialization relationship (for appointment validation).
 * The new repository handles services WITH category relationship (for public API).
 */
@Repository("bookingDentalServiceRepository")
public interface DentalServiceRepository extends JpaRepository<DentalService, Integer> {

    /**
     * Find service by code
     */
    Optional<DentalService> findByServiceCode(String serviceCode);

    /**
     * Find services by list of service codes (for Availability API)
     * Used in: AvailabilityService.getAvailableDoctors(), getAvailableRooms()
     */
    @Query("SELECT s FROM BookingDentalService s WHERE s.serviceCode IN :serviceCodes")
    List<DentalService> findByServiceCodeIn(@Param("serviceCodes") List<String> serviceCodes);

    /**
     * Find services by codes with specialization (JOIN FETCH to avoid N+1)
     * Used in: AppointmentCreationService.validateServices()
     */
    @Query("SELECT s FROM BookingDentalService s " +
           "LEFT JOIN FETCH s.specialization " +
           "WHERE s.serviceCode IN :serviceCodes AND s.isActive = true")
    List<DentalService> findByServiceCodeInWithSpecialization(@Param("serviceCodes") List<String> serviceCodes);

    /**
     * Find all active services with specialization (for dropdown)
     */
    @Query("SELECT s FROM BookingDentalService s " +
           "LEFT JOIN FETCH s.specialization " +
           "WHERE s.isActive = true " +
           "ORDER BY s.serviceName")
    List<DentalService> findAllActiveWithSpecialization();

    /**
     * Check if service code exists
     */
    boolean existsByServiceCode(String serviceCode);
}
