package com.dental.clinic.management.feedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO cho thống kê feedback theo bác sĩ
 * Được tạo để đáp ứng yêu cầu từ FE Team - Dashboard Statistics Tab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorFeedbackStatisticsResponse {

    @JsonProperty("doctors")
    private List<DoctorStatistics> doctors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorStatistics {
        
        @JsonProperty("employeeId")
        private Integer employeeId;
        
        @JsonProperty("employeeCode")
        private String employeeCode;
        
        @JsonProperty("employeeName")
        private String employeeName;
        
        @JsonProperty("specialization")
        private String specialization;
        
        @JsonProperty("avatar")
        private String avatar;
        
        @JsonProperty("statistics")
        private Statistics statistics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        
        @JsonProperty("averageRating")
        private Double averageRating;
        
        @JsonProperty("totalFeedbacks")
        private Long totalFeedbacks;
        
        /**
         * Phân bố rating
         * Key: rating (1-5), Value: count
         * Example: {"1": 0, "2": 1, "3": 5, "4": 15, "5": 30}
         */
        @JsonProperty("ratingDistribution")
        private java.util.Map<String, Long> ratingDistribution;
        
        /**
         * Top tags của bác sĩ này
         */
        @JsonProperty("topTags")
        private List<FeedbackStatisticsResponse.TagCount> topTags;
        
        /**
         * Các comment gần đây
         */
        @JsonProperty("recentComments")
        private List<RecentComment> recentComments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentComment {
        
        @JsonProperty("feedbackId")
        private Long feedbackId;
        
        @JsonProperty("patientName")
        private String patientName;
        
        @JsonProperty("rating")
        private Integer rating;
        
        @JsonProperty("comment")
        private String comment;
        
        @JsonProperty("tags")
        private List<String> tags;
        
        @JsonProperty("createdAt")
        private java.time.LocalDateTime createdAt;
    }
}
