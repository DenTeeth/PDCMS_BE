package com.dental.clinic.management.booking_appointment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new dental service
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new service")
public class CreateServiceRequest {

    @SuppressWarnings("deprecation")
    @NotBlank(message = "Mã dịch vụ là bắt buộc")
    @Schema(description = "Unique service code", example = "SV-CAOVOI", required = true)
    private String serviceCode;

    @SuppressWarnings("deprecation")
    @NotBlank(message = "Tên dịch vụ là bắt buộc")
    @Schema(description = "Service name", example = "Cạo vôi răng và Đánh bóng", required = true)
    private String serviceName;

    @Schema(description = "Service description", example = "Lấy sạch vôi răng và mảng bám")
    private String description;

    @SuppressWarnings("deprecation")
    @NotNull(message = "Thời lượng mặc định là bắt buộc")
    @Min(value = 1, message = "Thời lượng phải ít nhất là 1 phút")
    @Schema(description = "Default duration in minutes", example = "30", required = true)
    private Integer defaultDurationMinutes;

    @SuppressWarnings("deprecation")
    @NotNull(message = "Thời gian đệm mặc định là bắt buộc")
    @Min(value = 0, message = "Thời gian đệm không được âm")
    @Schema(description = "Default buffer time in minutes", example = "10", required = true)
    private Integer defaultBufferMinutes;

    @SuppressWarnings("deprecation")
    @NotNull(message = "Giá là bắt buộc")
    @Min(value = 0, message = "Giá không được âm")
    @Schema(description = "Service price (VND)", example = "300000", required = true)
    private BigDecimal price;

    @Schema(description = "Specialization ID (nullable)", example = "1")
    private Integer specializationId;

    @Min(value = 0, message = "Thứ tự hiển thị không được âm")
    @Schema(description = "Display order for sorting services", example = "1")
    private Integer displayOrder;

    @Min(value = 0, message = "Số ngày chuẩn bị tối thiểu không được âm")
    @Schema(description = "BE_4: Minimum preparation days before this service (days)", example = "0")
    private Integer minimumPreparationDays;

    @Min(value = 0, message = "Số ngày hồi phục không được âm")
    @Schema(description = "BE_4: Recovery days needed after this service (days)", example = "0")
    private Integer recoveryDays;

    @Min(value = 0, message = "Số ngày giãn cách không được âm")
    @Schema(description = "BE_4: Spacing days between consecutive appointments (days)", example = "0")
    private Integer spacingDays;

    @Min(value = 1, message = "Số cuộc hẹn tối đa mỗi ngày phải ít nhất là 1")
    @Schema(description = "BE_4: Maximum appointments allowed per day (null = no limit)", example = "5")
    private Integer maxAppointmentsPerDay;

    @Builder.Default
    @Schema(description = "Active status", example = "true")
    private Boolean isActive = true;
}
