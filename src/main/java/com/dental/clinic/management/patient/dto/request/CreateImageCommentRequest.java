package com.dental.clinic.management.patient.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImageCommentRequest {

    @NotNull(message = "Image ID is required")
    private Long imageId;

    @NotBlank(message = "Comment text is required")
    private String commentText;
}
