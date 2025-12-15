package com.dental.clinic.management.patient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageCommentResponse {

    private Long commentId;
    private Long imageId;
    private String commentText;
    
    // Creator info
    private Integer createdById;
    private String createdByName;
    private String createdByCode;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
}
