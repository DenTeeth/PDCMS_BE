package com.dental.clinic.management.feedback.repository;

import com.dental.clinic.management.feedback.domain.AppointmentFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AppointmentFeedback Entity
 */
@Repository
public interface AppointmentFeedbackRepository extends JpaRepository<AppointmentFeedback, Long>, JpaSpecificationExecutor<AppointmentFeedback> {

    /**
     * Tìm feedback theo appointment code
     */
    Optional<AppointmentFeedback> findByAppointmentCode(String appointmentCode);

    /**
     * Kiểm tra xem appointment đã có feedback chưa
     */
    boolean existsByAppointmentCode(String appointmentCode);

    /**
     * Lấy danh sách feedbacks với filter động
     * 
     * @deprecated This method causes PostgreSQL type inference errors with NULL date parameters.
     *             Use AppointmentFeedbackSpecification.withFilters() with findAll(spec, pageable) instead.
     * @see com.dental.clinic.management.feedback.specification.AppointmentFeedbackSpecification#withFilters
     * 
     * @param rating Filter theo số sao (nullable)
     * @param employeeCode Filter theo mã bác sĩ (nullable)
     * @param patientCode Filter theo mã bệnh nhân (nullable)
     * @param fromDate Filter từ ngày (nullable)
     * @param toDate Filter đến ngày (nullable)
     * @param pageable Pagination
     * @return Page of feedbacks
     */
    @Deprecated(since = "2026-01-13", forRemoval = true)
    @Query("SELECT f FROM AppointmentFeedback f " +
           "WHERE (:rating IS NULL OR f.rating = :rating) " +
           "AND (:patientId IS NULL OR f.patientId = :patientId) " +
           "AND (:fromDate IS NULL OR CAST(f.createdAt AS LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(f.createdAt AS LocalDate) <= :toDate)")
    Page<AppointmentFeedback> findWithFilters(
        @Param("rating") Integer rating,
        @Param("patientId") Integer patientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    /**
     * Đếm tổng số feedback
     * 
     * @deprecated Use count(AppointmentFeedbackSpecification.withDateRange()) instead
     */
    @Deprecated(since = "2026-01-13", forRemoval = true)
    @Query("SELECT COUNT(f) FROM AppointmentFeedback f " +
           "WHERE (:fromDate IS NULL OR CAST(f.createdAt AS LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(f.createdAt AS LocalDate) <= :toDate)")
    Long countWithDateRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * Tính trung bình rating
     * 
     * @deprecated Will be replaced with Specification-based calculation
     */
    @Deprecated(since = "2026-01-13", forRemoval = true)
    @Query("SELECT AVG(CAST(f.rating AS double)) FROM AppointmentFeedback f " +
           "WHERE (:fromDate IS NULL OR CAST(f.createdAt AS LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(f.createdAt AS LocalDate) <= :toDate)")
    Double calculateAverageRating(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * Đếm số lượng feedback theo từng rating
     * 
     * @deprecated Will be replaced with Specification-based grouping
     */
    @Deprecated(since = "2026-01-13", forRemoval = true)
    @Query("SELECT f.rating, COUNT(f) FROM AppointmentFeedback f " +
           "WHERE (:fromDate IS NULL OR CAST(f.createdAt AS LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(f.createdAt AS LocalDate) <= :toDate) " +
           "GROUP BY f.rating " +
           "ORDER BY f.rating")
    Object[][] getRatingDistribution(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * Lấy thống kê feedback theo bác sĩ
     * Trả về: employeeId, averageRating, totalFeedbacks
     * 
     * @deprecated This method causes PostgreSQL type inference errors with NULL date parameters.
     *             Will be replaced with Specification-based grouping query.
     */
    @Deprecated(since = "2026-01-13", forRemoval = true)
    @Query("SELECT a.employeeId, AVG(CAST(f.rating AS double)), COUNT(f) " +
           "FROM AppointmentFeedback f " +
           "JOIN Appointment a ON f.appointmentCode = a.appointmentCode " +
           "WHERE (:fromDate IS NULL OR CAST(f.createdAt AS LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(f.createdAt AS LocalDate) <= :toDate) " +
           "GROUP BY a.employeeId " +
           "ORDER BY AVG(CAST(f.rating AS double)) DESC")
    Object[][] getDoctorStatisticsGrouped(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * Lấy feedbacks của một bác sĩ cụ thể
     * 
     * @deprecated Use Specification with JOIN instead
     */
    @Deprecated(since = "2026-01-13", forRemoval = true)
    @Query("SELECT f FROM AppointmentFeedback f " +
           "JOIN Appointment a ON f.appointmentCode = a.appointmentCode " +
           "WHERE a.employeeId = :employeeId " +
           "AND (:fromDate IS NULL OR CAST(f.createdAt AS LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(f.createdAt AS LocalDate) <= :toDate)")
    List<AppointmentFeedback> findByEmployeeIdAndDateRange(
        @Param("employeeId") Integer employeeId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * Lấy rating distribution của một bác sĩ
     * 
     * @deprecated Use Specification with JOIN and grouping instead
     */
    @Deprecated(since = "2026-01-13", forRemoval = true)
    @Query("SELECT f.rating, COUNT(f) FROM AppointmentFeedback f " +
           "JOIN Appointment a ON f.appointmentCode = a.appointmentCode " +
           "WHERE a.employeeId = :employeeId " +
           "AND (:fromDate IS NULL OR CAST(f.createdAt AS LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(f.createdAt AS LocalDate) <= :toDate) " +
           "GROUP BY f.rating " +
           "ORDER BY f.rating")
    Object[][] getDoctorRatingDistribution(
        @Param("employeeId") Integer employeeId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    // ==================== NEW FIXED METHODS (No NULL check pattern) ====================

    /**
     * Get doctor statistics WITHOUT date filter
     * Returns: employeeId, averageRating, totalFeedbacks
     */
    @Query("SELECT a.employeeId, AVG(CAST(f.rating AS double)), COUNT(f) " +
           "FROM AppointmentFeedback f " +
           "JOIN Appointment a ON f.appointmentCode = a.appointmentCode " +
           "GROUP BY a.employeeId " +
           "ORDER BY AVG(CAST(f.rating AS double)) DESC")
    List<Object[]> getDoctorStatisticsAll();

    /**
     * Get doctor statistics WITH date range filter (BETWEEN - no NULL check)
     * Returns: employeeId, averageRating, totalFeedbacks
     */
    @Query("SELECT a.employeeId, AVG(CAST(f.rating AS double)), COUNT(f) " +
           "FROM AppointmentFeedback f " +
           "JOIN Appointment a ON f.appointmentCode = a.appointmentCode " +
           "WHERE f.createdAt BETWEEN :fromDateTime AND :toDateTime " +
           "GROUP BY a.employeeId " +
           "ORDER BY AVG(CAST(f.rating AS double)) DESC")
    List<Object[]> getDoctorStatisticsByDateRange(
        @Param("fromDateTime") java.time.LocalDateTime fromDateTime,
        @Param("toDateTime") java.time.LocalDateTime toDateTime
    );

    /**
     * Count feedbacks WITHOUT date filter
     */
    @Query("SELECT COUNT(f) FROM AppointmentFeedback f")
    Long countAllFeedbacks();

    /**
     * Count feedbacks WITH date range filter (BETWEEN - no NULL check)
     */
    @Query("SELECT COUNT(f) FROM AppointmentFeedback f WHERE f.createdAt BETWEEN :fromDateTime AND :toDateTime")
    Long countFeedbacksByDateRange(
        @Param("fromDateTime") java.time.LocalDateTime fromDateTime,
        @Param("toDateTime") java.time.LocalDateTime toDateTime
    );

    /**
     * Calculate average rating WITHOUT date filter
     */
    @Query("SELECT AVG(CAST(f.rating AS double)) FROM AppointmentFeedback f")
    Double calculateAverageRatingAll();

    /**
     * Calculate average rating WITH date range filter (BETWEEN - no NULL check)
     */
    @Query("SELECT AVG(CAST(f.rating AS double)) FROM AppointmentFeedback f WHERE f.createdAt BETWEEN :fromDateTime AND :toDateTime")
    Double calculateAverageRatingByDateRange(
        @Param("fromDateTime") java.time.LocalDateTime fromDateTime,
        @Param("toDateTime") java.time.LocalDateTime toDateTime
    );

    /**
     * Get rating distribution WITHOUT date filter
     */
    @Query("SELECT f.rating, COUNT(f) FROM AppointmentFeedback f GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> getRatingDistributionAll();

    /**
     * Get rating distribution WITH date range filter (BETWEEN - no NULL check)
     */
    @Query("SELECT f.rating, COUNT(f) FROM AppointmentFeedback f " +
           "WHERE f.createdAt BETWEEN :fromDateTime AND :toDateTime " +
           "GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> getRatingDistributionByDateRange(
        @Param("fromDateTime") java.time.LocalDateTime fromDateTime,
        @Param("toDateTime") java.time.LocalDateTime toDateTime
    );

    /**
     * Get doctor rating distribution WITHOUT date filter
     */
    @Query("SELECT f.rating, COUNT(f) FROM AppointmentFeedback f " +
           "JOIN Appointment a ON f.appointmentCode = a.appointmentCode " +
           "WHERE a.employeeId = :employeeId " +
           "GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> getDoctorRatingDistributionAll(@Param("employeeId") Integer employeeId);

    /**
     * Get doctor rating distribution WITH date range filter (BETWEEN - no NULL check)
     */
    @Query("SELECT f.rating, COUNT(f) FROM AppointmentFeedback f " +
           "JOIN Appointment a ON f.appointmentCode = a.appointmentCode " +
           "WHERE a.employeeId = :employeeId " +
           "AND f.createdAt BETWEEN :fromDateTime AND :toDateTime " +
           "GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> getDoctorRatingDistributionByDateRange(
        @Param("employeeId") Integer employeeId,
        @Param("fromDateTime") java.time.LocalDateTime fromDateTime,
        @Param("toDateTime") java.time.LocalDateTime toDateTime
    );

    /**
     * Get feedbacks by employee WITHOUT date filter
     */
    @Query("SELECT f FROM AppointmentFeedback f " +
           "JOIN Appointment a ON f.appointmentCode = a.appointmentCode " +
           "WHERE a.employeeId = :employeeId")
    List<AppointmentFeedback> findByEmployeeIdAll(@Param("employeeId") Integer employeeId);

    /**
     * Get feedbacks by employee WITH date range filter (BETWEEN - no NULL check)
     */
    @Query("SELECT f FROM AppointmentFeedback f " +
           "JOIN Appointment a ON f.appointmentCode = a.appointmentCode " +
           "WHERE a.employeeId = :employeeId " +
           "AND f.createdAt BETWEEN :fromDateTime AND :toDateTime")
    List<AppointmentFeedback> findByEmployeeIdByDateRange(
        @Param("employeeId") Integer employeeId,
        @Param("fromDateTime") java.time.LocalDateTime fromDateTime,
        @Param("toDateTime") java.time.LocalDateTime toDateTime
    );
}
