package com.dental.clinic.management.patient.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateImageCommentRequest {

    @NotBlank(message = "Nội dung bình luận là bắt buộc")
    private String commentText;
}
