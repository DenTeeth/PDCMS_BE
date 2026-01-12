package com.dental.clinic.management.feedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho feedback
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    @JsonProperty("feedbackId")
    private Long feedbackId;

    @JsonProperty("appointmentCode")
    private String appointmentCode;

    @JsonProperty("patientName")
    private String patientName;

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("rating")
    private Integer rating;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}
