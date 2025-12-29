package com.dental.clinic.management.patient.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import com.dental.clinic.management.patient.domain.PatientImage;
import com.dental.clinic.management.patient.domain.PatientImageComment;
import com.dental.clinic.management.patient.dto.request.CreateImageCommentRequest;
import com.dental.clinic.management.patient.dto.request.UpdateImageCommentRequest;
import com.dental.clinic.management.patient.dto.response.ImageCommentResponse;
import com.dental.clinic.management.patient.repository.PatientImageCommentRepository;
import com.dental.clinic.management.patient.repository.PatientImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing comments on patient images.
 * 
 * Features:
 * - Create comments on images (doctors, assistants can annotate)
 * - Update own comments
 * - Soft delete comments
 * - List all comments for an image
 * - Permission-based access control
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientImageCommentService {

    private final PatientImageCommentRepository commentRepository;
    private final PatientImageRepository imageRepository;
    private final EmployeeRepository employeeRepository;

    private static final String ENTITY_NAME = "PatientImageComment";

    /**
     * Create a new comment on an image
     * 
     * @param request Comment creation request
     * @return Created comment response
     */
    @Transactional
    public ImageCommentResponse createComment(CreateImageCommentRequest request) {
        log.info("Creating comment on image: {}", request.getImageId());

        // Validate image exists
        PatientImage image = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Image not found with ID: " + request.getImageId(),
                        ENTITY_NAME,
                        "IMAGE_NOT_FOUND"));

        // Get current user
        Employee currentEmployee = getCurrentEmployee();

        // Create comment
        PatientImageComment comment = PatientImageComment.builder()
                .image(image)
                .commentText(request.getCommentText())
                .createdBy(currentEmployee)
                .isDeleted(false)
                .build();

        PatientImageComment savedComment = commentRepository.save(comment);
        log.info("Created comment ID: {} on image: {}", savedComment.getCommentId(), image.getImageId());

        return mapToResponse(savedComment);
    }

    /**
     * Get all comments for a specific image
     * 
     * @param imageId Image ID
     * @return List of comments (newest first)
     */
    @Transactional(readOnly = true)
    public List<ImageCommentResponse> getCommentsForImage(Long imageId) {
        log.debug("Fetching comments for image: {}", imageId);

        // Validate image exists
        if (!imageRepository.existsById(imageId)) {
            throw new BadRequestAlertException(
                    "Image not found with ID: " + imageId,
                    ENTITY_NAME,
                    "IMAGE_NOT_FOUND");
        }

        List<PatientImageComment> comments = commentRepository.findByImageIdOrderByCreatedAtDesc(imageId);
        log.debug("Found {} comments for image: {}", comments.size(), imageId);

        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing comment (only by creator)
     * 
     * @param commentId Comment ID
     * @param request Update request
     * @return Updated comment response
     */
    @Transactional
    public ImageCommentResponse updateComment(Long commentId, UpdateImageCommentRequest request) {
        log.info("Updating comment: {}", commentId);

        PatientImageComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Comment not found with ID: " + commentId,
                        ENTITY_NAME,
                        "COMMENT_NOT_FOUND"));

        // Check if current user is the creator
        Employee currentEmployee = getCurrentEmployee();
        if (!comment.getCreatedBy().getEmployeeId().equals(currentEmployee.getEmployeeId())) {
            throw new BadRequestAlertException(
                        "You can only update your own comments",
                        ENTITY_NAME,
                        "UNAUTHORIZED_UPDATE");
        }

        // Check if comment is deleted
        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new BadRequestAlertException(
                        "Cannot update deleted comment",
                        ENTITY_NAME,
                        "COMMENT_DELETED");
        }

        comment.setCommentText(request.getCommentText());
        PatientImageComment updatedComment = commentRepository.save(comment);

        log.info("Updated comment: {}", commentId);
        return mapToResponse(updatedComment);
    }

    /**
     * Soft delete a comment (only by creator or admin)
     * 
     * @param commentId Comment ID
     */
    @Transactional
    public void deleteComment(Long commentId) {
        log.info("Deleting comment: {}", commentId);

        PatientImageComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Comment not found with ID: " + commentId,
                        ENTITY_NAME,
                        "COMMENT_NOT_FOUND"));

        // Check if current user is the creator
        Employee currentEmployee = getCurrentEmployee();
        if (!comment.getCreatedBy().getEmployeeId().equals(currentEmployee.getEmployeeId())) {
            throw new BadRequestAlertException(
                        "You can only delete your own comments",
                        ENTITY_NAME,
                        "UNAUTHORIZED_DELETE");
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);

        log.info("Soft deleted comment: {}", commentId);
    }

    /**
     * Get comment count for an image
     * 
     * @param imageId Image ID
     * @return Number of non-deleted comments
     */
    @Transactional(readOnly = true)
    public Long getCommentCount(Long imageId) {
        return commentRepository.countByImageId(imageId);
    }

    // ===== Helper Methods =====

    /**
     * Get current authenticated employee
     */
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadRequestAlertException(
                    "Người dùng chưa được xác thực",
                    ENTITY_NAME,
                    "UNAUTHORIZED");
        }

        String username;
        if (auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            username = jwt.getSubject();
        } else if (auth.getPrincipal() instanceof String) {
            username = (String) auth.getPrincipal();
        } else {
            throw new BadRequestAlertException(
                    "Could not extract username from authentication",
                    ENTITY_NAME,
                    "UNAUTHORIZED");
        }

        return employeeRepository.findByAccount_Username(username)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy nhân viên cho người dùng: " + username,
                        ENTITY_NAME,
                        "EMPLOYEE_NOT_FOUND"));
    }

    /**
     * Map entity to response DTO
     */
    private ImageCommentResponse mapToResponse(PatientImageComment comment) {
        Employee creator = comment.getCreatedBy();
        
        return ImageCommentResponse.builder()
                .commentId(comment.getCommentId())
                .imageId(comment.getImage().getImageId())
                .commentText(comment.getCommentText())
                .createdById(creator.getEmployeeId())
                .createdByName(creator.getFirstName() + " " + creator.getLastName())
                .createdByCode(creator.getEmployeeCode())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getIsDeleted())
                .build();
    }
}
