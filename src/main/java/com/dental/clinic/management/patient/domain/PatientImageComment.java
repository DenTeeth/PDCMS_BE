package com.dental.clinic.management.patient.domain;

import com.dental.clinic.management.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing comments on patient images.
 * Allows multiple users (employees) to add annotations, observations, or notes on images.
 * 
 * Use cases:
 * - Doctors annotating X-rays or clinical photos
 * - Collaborative review and discussion of images
 * - Tracking observations over time
 * - Patient education notes
 */
@Entity
@Table(name = "patient_image_comments", indexes = {
        @Index(name = "idx_image_id", columnList = "image_id"),
        @Index(name = "idx_created_by", columnList = "created_by"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientImageComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    /**
     * The image this comment belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private PatientImage image;

    /**
     * Comment text content
     */
    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    /**
     * Employee who created this comment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;

    /**
     * When the comment was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the comment was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft delete flag - allows hiding inappropriate comments without losing data
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
