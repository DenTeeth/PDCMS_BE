package com.dental.clinic.management.patient.repository;

import com.dental.clinic.management.patient.domain.PatientImageComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientImageCommentRepository extends JpaRepository<PatientImageComment, Long> {

    /**
     * Find all non-deleted comments for a specific image, ordered by creation time (newest first)
     */
    @Query("SELECT c FROM PatientImageComment c " +
           "WHERE c.image.imageId = :imageId " +
           "AND c.isDeleted = false " +
           "ORDER BY c.createdAt DESC")
    List<PatientImageComment> findByImageIdOrderByCreatedAtDesc(@Param("imageId") Long imageId);

    /**
     * Find all comments (including deleted) for a specific image
     */
    List<PatientImageComment> findByImageImageIdOrderByCreatedAtDesc(Long imageId);

    /**
     * Count non-deleted comments for an image
     */
    @Query("SELECT COUNT(c) FROM PatientImageComment c " +
           "WHERE c.image.imageId = :imageId " +
           "AND c.isDeleted = false")
    Long countByImageId(@Param("imageId") Long imageId);
}
