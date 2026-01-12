package com.dental.clinic.management.feedback.repository;

import com.dental.clinic.management.feedback.domain.AppointmentFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface AppointmentFeedbackRepository extends JpaRepository<AppointmentFeedback, Long> {

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
     * @param rating Filter theo số sao (nullable)
     * @param employeeCode Filter theo mã bác sĩ (nullable)
     * @param patientCode Filter theo mã bệnh nhân (nullable)
     * @param fromDate Filter từ ngày (nullable)
     * @param toDate Filter đến ngày (nullable)
     * @param pageable Pagination
     * @return Page of feedbacks
     */
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
     */
    @Query("SELECT COUNT(f) FROM AppointmentFeedback f " +
           "WHERE (:fromDate IS NULL OR CAST(f.createdAt AS LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(f.createdAt AS LocalDate) <= :toDate)")
    Long countWithDateRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * Tính trung bình rating
     */
    @Query("SELECT AVG(CAST(f.rating AS double)) FROM AppointmentFeedback f " +
           "WHERE (:fromDate IS NULL OR CAST(f.createdAt AS LocalDate) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(f.createdAt AS LocalDate) <= :toDate)")
    Double calculateAverageRating(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    /**
     * Đếm số lượng feedback theo từng rating
     */
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
     */
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
     */
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
     */
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
}
