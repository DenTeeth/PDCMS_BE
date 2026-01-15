package com.dental.clinic.management.feedback.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AppointmentFeedback Entity - Đánh giá lịch hẹn
 * 
 * Business Rules:
 * - BR-20: Đánh giá đã gửi KHÔNG thể chỉnh sửa hoặc xóa
 * - BR-21: Người được phép đánh giá: Bệnh nhân của lịch hẹn, Admin, Manager
 * - BR-22: Chỉ đánh giá được lịch hẹn có status = COMPLETED
 * - BR-23: Mỗi lịch hẹn chỉ được đánh giá 1 lần (UNIQUE constraint)
 * - BR-24: Rating bắt buộc (1-5 sao), comment và tags là tùy chọn
 */
@Entity
@Table(
    name = "appointment_feedbacks",
    indexes = {
        @Index(name = "idx_feedback_appointment", columnList = "appointment_code"),
        @Index(name = "idx_feedback_patient", columnList = "patient_id"),
        @Index(name = "idx_feedback_rating", columnList = "rating")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    /**
     * Mã lịch hẹn - Unique constraint đảm bảo 1 appointment chỉ có 1 feedback
     */
    @NotNull(message = "Mã lịch hẹn là bắt buộc")
    @Column(name = "appointment_code", nullable = false, unique = true, length = 50)
    private String appointmentCode;

    /**
     * ID bệnh nhân
     */
    @NotNull(message = "ID bệnh nhân là bắt buộc")
    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    /**
     * Số sao đánh giá (1-5)
     */
    @NotNull(message = "Rating là bắt buộc")
    @Min(value = 1, message = "Rating phải từ 1 đến 5")
    @Max(value = 5, message = "Rating phải từ 1 đến 5")
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * Nội dung đánh giá
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /**
     * Danh sách tags (JSON array)
     * Example: ["Thân thiện", "Chuyên nghiệp", "Tư vấn kỹ"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "json")
    private List<String> tags;

    /**
     * Người tạo (patient_id hoặc employee_id)
     * Thường là patient_id, nhưng admin cũng có thể tạo thay
     */
    @NotNull(message = "Người tạo là bắt buộc")
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * Thời gian tạo
     */
    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
