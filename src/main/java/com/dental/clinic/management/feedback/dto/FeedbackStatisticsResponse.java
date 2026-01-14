package com.dental.clinic.management.feedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO cho thống kê feedback
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackStatisticsResponse {

    @JsonProperty("totalFeedbacks")
    private Long totalFeedbacks;

    @JsonProperty("averageRating")
    private Double averageRating;

    /**
     * Phân bố rating
     * Key: rating (1-5), Value: count
     * Example: {"1": 5, "2": 10, "3": 20, "4": 45, "5": 70}
     */
    @JsonProperty("ratingDistribution")
    private Map<String, Long> ratingDistribution;

    /**
     * Top tags phổ biến nhất
     */
    @JsonProperty("topTags")
    private List<TagCount> topTags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagCount {
        @JsonProperty("tag")
        private String tag;

        @JsonProperty("count")
        private Long count;
    }
}
