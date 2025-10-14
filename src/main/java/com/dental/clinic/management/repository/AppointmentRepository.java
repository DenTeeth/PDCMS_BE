package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDate = :date " +
            "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    List<Appointment> findOverlappingByDoctorAndDate(@Param("doctorId") String doctorId,
                                                     @Param("date") LocalDate date,
                                                     @Param("startTime") LocalTime startTime,
                                                     @Param("endTime") LocalTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDate = :date " +
            "AND (a.startTime < :endTime AND a.endTime > :startTime) AND a.appointmentId <> :excludeId")
    List<Appointment> findOverlappingByDoctorAndDateExcluding(@Param("doctorId") String doctorId,
                                                              @Param("date") LocalDate date,
                                                              @Param("startTime") LocalTime startTime,
                                                              @Param("endTime") LocalTime endTime,
                                                              @Param("excludeId") String excludeId);

        /**
         * Find all appointments for a doctor on a specific date.
         */
        List<Appointment> findByDoctorIdAndAppointmentDate(String doctorId, LocalDate appointmentDate);

        // count existing appointments for code generation (per doctor per day)
        long countByDoctorIdAndAppointmentDate(String doctorId, java.time.LocalDate appointmentDate);

            @Query("SELECT a FROM Appointment a WHERE " +
                    "(:date IS NULL OR a.appointmentDate = :date) AND " +
                    "(:doctorId IS NULL OR a.doctorId = :doctorId) AND " +
                    "(:status IS NULL OR a.status = :status) AND " +
                    "(:type IS NULL OR a.type = :type)")
            Page<Appointment> findByOptionalFilters(@Param("date") LocalDate date,
                                                    @Param("doctorId") String doctorId,
                                                    @Param("status") com.dental.clinic.management.domain.enums.AppointmentStatus status,
                                                    @Param("type") com.dental.clinic.management.domain.enums.AppointmentType type,
                                                    Pageable pageable);

}
