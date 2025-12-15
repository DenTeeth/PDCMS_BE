package com.dental.clinic.management.patient.controller;

import com.dental.clinic.management.patient.dto.request.CreateImageCommentRequest;
import com.dental.clinic.management.patient.dto.request.UpdateImageCommentRequest;
import com.dental.clinic.management.patient.dto.response.ImageCommentResponse;
import com.dental.clinic.management.patient.service.PatientImageCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing comments on patient images.
 * 
 * Endpoints:
 * - POST /api/patient-images/comments - Create comment
 * - GET /api/patient-images/{imageId}/comments - Get all comments for image
 * - PUT /api/patient-images/comments/{commentId} - Update comment
 * - DELETE /api/patient-images/comments/{commentId} - Delete comment
 * - GET /api/patient-images/{imageId}/comments/count - Get comment count
 */
@RestController
@RequestMapping("/api/patient-images")
@RequiredArgsConstructor
@Slf4j
public class PatientImageCommentController {

    private final PatientImageCommentService commentService;

    /**
     * Create a new comment on an image
     * 
     * @param request Comment creation request
     * @return Created comment
     */
    @PostMapping("/comments")
    public ResponseEntity<ImageCommentResponse> createComment(
            @Valid @RequestBody CreateImageCommentRequest request) {
        log.info("POST /api/patient-images/comments - imageId: {}", request.getImageId());
        ImageCommentResponse response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all comments for a specific image
     * 
     * @param imageId Image ID
     * @return List of comments (newest first)
     */
    @GetMapping("/{imageId}/comments")
    public ResponseEntity<List<ImageCommentResponse>> getCommentsForImage(
            @PathVariable Long imageId) {
        log.info("GET /api/patient-images/{}/comments", imageId);
        List<ImageCommentResponse> comments = commentService.getCommentsForImage(imageId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Update an existing comment
     * 
     * @param commentId Comment ID
     * @param request Update request
     * @return Updated comment
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ImageCommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateImageCommentRequest request) {
        log.info("PUT /api/patient-images/comments/{}", commentId);
        ImageCommentResponse response = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft delete a comment
     * 
     * @param commentId Comment ID
     * @return No content
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        log.info("DELETE /api/patient-images/comments/{}", commentId);
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get comment count for an image
     * 
     * @param imageId Image ID
     * @return Comment count
     */
    @GetMapping("/{imageId}/comments/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long imageId) {
        log.debug("GET /api/patient-images/{}/comments/count", imageId);
        Long count = commentService.getCommentCount(imageId);
        return ResponseEntity.ok(count);
    }
}
