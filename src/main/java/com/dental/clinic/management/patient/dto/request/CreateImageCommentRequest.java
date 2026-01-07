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

    @NotNull(message = "Mã hình ảnh là bắt buộc")
    private Long imageId;

    @NotBlank(message = "Nội dung bình luận là bắt buộc")
    private String commentText;
}
