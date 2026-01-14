package com.dental.clinic.management.feedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO để tạo feedback mới
 * 
 * Validation:
 * - appointmentCode: Required, must exist, status = COMPLETED
 * - rating: Required, integer 1-5
 * - comment: Optional, max 1000 characters
 * - tags: Optional, array of strings, max 10 tags
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeedbackRequest {

    @NotBlank(message = "Mã lịch hẹn là bắt buộc")
    @JsonProperty("appointmentCode")
    private String appointmentCode;

    @NotNull(message = "Rating là bắt buộc")
    @Min(value = 1, message = "Rating phải từ 1 đến 5")
    @Max(value = 5, message = "Rating phải từ 1 đến 5")
    @JsonProperty("rating")
    private Integer rating;

    @Size(max = 1000, message = "Comment không được vượt quá 1000 ký tự")
    @JsonProperty("comment")
    private String comment;

    @Size(max = 10, message = "Tối đa 10 tags")
    @JsonProperty("tags")
    private List<String> tags;
}
