package com.dental.clinic.management.treatment_plans.dto.request;

import com.dental.clinic.management.treatment_plans.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new patient treatment plan from a template.
 * Used by API 5.3: POST /api/v1/patients/{patientCode}/treatment-plans
 *
 * Design Philosophy:
 * - Frontend only needs to provide 5 simple fields
 * - Backend handles all the complex snapshot logic (phases, items, financials)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a patient treatment plan from a template package")
public class CreateTreatmentPlanRequest {

    /**
     * Template code to copy from
     * Example: "TPL_ORTHO_METAL", "TPL_IMPLANT_OSSTEM"
     */
    @NotBlank(message = "Mã mẫu là bắt buộc")
    @Size(max = 50, message = "Mã mẫu không được vượt quá 50 ký tự")
    @Schema(description = "Code of the template package to use", example = "TPL_ORTHO_METAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceTemplateCode;

    /**
     * Employee code of the doctor who will be responsible for this plan
     * Example: "DR_AN_KHOA"
     */
    @NotBlank(message = "Mã nhân viên bác sĩ là bắt buộc")
    @Size(max = 20, message = "Mã nhân viên không được vượt quá 20 ký tự")
    @Schema(description = "Employee code of the doctor responsible for this treatment plan", example = "DR_AN_KHOA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String doctorEmployeeCode;

    /**
     * Custom name for this patient's plan (optional)
     * If null, system will use the template's name
     * Example: "Lộ trình niềng răng 2 năm cho BN Phong (Gói khuyến mãi)"
     */
    @Size(max = 255, message = "Tên kế hoạch không được vượt quá 255 ký tự")
    @Schema(description = "Custom name for this treatment plan. If not provided, template name will be used.", example = "Lộ trình niềng răng 2 năm cho BN Phong (Gói khuyến mãi)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String planNameOverride;

    /**
     * Discount amount to apply to the total cost
     * Must be >= 0 and <= total cost (validated by business logic)
     * Example: 5000000 (5 triệu VND giảm giá)
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Số tiền giảm giá phải >= 0")
    @Digits(integer = 10, fraction = 2, message = "Số tiền giảm giá phải có tối đa 10 chữ số nguyên và 2 chữ số thập phân")
    @Schema(description = "Discount amount in VND (must be <= total cost, validated by business logic)", example = "5000000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * Payment type for this treatment plan
     * FULL: Pay all at once
     * PHASED: Pay by phases (when completing each phase)
     * INSTALLMENT: Pay in installments (monthly/custom schedule)
     */
    @NotNull(message = "Loại thanh toán là bắt buộc")
    @Schema(description = "Payment method for this treatment plan", example = "INSTALLMENT", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {
            "FULL", "PHASED", "INSTALLMENT" })
    private PaymentType paymentType;
}
