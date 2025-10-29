package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.DentalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DentalService entity
 */
@Repository
public interface DentalServiceRepository extends JpaRepository<DentalService, Integer> {

    /**
     * Find service by service code
     */
    Optional<DentalService> findByServiceCode(String serviceCode);

    /**
     * Check if service code exists
     */
    boolean existsByServiceCode(String serviceCode);

    /**
     * Check if service code exists (excluding given service ID)
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM DentalService s WHERE s.serviceCode = :serviceCode AND s.serviceId <> :serviceId")
    boolean existsByServiceCodeAndServiceIdNot(@Param("serviceCode") String serviceCode, 
                                                 @Param("serviceId") Integer serviceId);

    /**
     * Find all active services
     */
    List<DentalService> findByIsActiveTrue();

    /**
     * Find services by specialization ID
     */
    List<DentalService> findBySpecializationSpecializationId(Integer specializationId);

    /**
     * Find active services by specialization ID
     */
    List<DentalService> findBySpecializationSpecializationIdAndIsActiveTrue(Integer specializationId);

    /**
     * Search services by keyword (service code or name)
     */
    @Query("SELECT s FROM DentalService s WHERE " +
           "LOWER(s.serviceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DentalService> searchByCodeOrName(@Param("keyword") String keyword);

    /**
     * Search active services by keyword
     */
    @Query("SELECT s FROM DentalService s WHERE s.isActive = true AND (" +
           "LOWER(s.serviceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DentalService> searchActiveByCodeOrName(@Param("keyword") String keyword);

    /**
     * Find services with filters (paginated)
     */
    @Query("SELECT s FROM DentalService s WHERE " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(:specializationId IS NULL OR s.specialization.specializationId = :specializationId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.serviceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<DentalService> findWithFilters(@Param("isActive") Boolean isActive,
                                          @Param("specializationId") Integer specializationId,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);
}
