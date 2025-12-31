package com.dental.clinic.management.warehouse.dto.request;

import com.dental.clinic.management.warehouse.enums.ExportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * API 6.5: Export Transaction Request DTO
 *
 * Enhanced with:
 * - Audit fields (departmentName, requestedBy)
 * - Force flags (allowExpired)
 * - Financial tracking support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTransactionRequest {

    @NotNull(message = "Ngày giao dịch là bắt buộc")
    private LocalDate transactionDate;

    @NotNull(message = "Loại xuất kho là bắt buộc")
    private ExportType exportType; // USAGE, DISPOSAL, RETURN

    @Size(max = 100, message = "Mã tham chiếu không được vượt quá 100 ký tự")
    private String referenceCode; // Mã phiếu yêu cầu hoặc mã ca điều trị

    @Schema(description = "Link to appointment (auto-fills referenceCode and relatedAppointment)")
    private Long appointmentId; // When provided, auto-set referenceCode from appointment.appointmentCode

    // Audit Fields (Enhanced from review)
    @Size(max = 200, message = "Tên bộ phận không được vượt quá 200 ký tự")
    private String departmentName;

    @Size(max = 200, message = "Người yêu cầu không được vượt quá 200 ký tự")
    private String requestedBy;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String notes;

    // Force Flags
    @Builder.Default
    private Boolean allowExpired = false; // Cho phép xuất hàng hết hạn (true nếu DISPOSAL)

    @NotEmpty(message = "Danh sách vật tư không được để trống")
    @Valid
    private List<ExportItemRequest> items;

    /**
     * Nested class: Export Item Request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportItemRequest {

        @NotNull(message = "Mã vật tư chính là bắt buộc")
        @Positive(message = "Mã vật tư chính phải là số dương")
        private Long itemMasterId;

        @NotNull(message = "Số lượng là bắt buộc")
        @Min(value = 1, message = "Số lượng phải ít nhất là 1")
        @Max(value = 1000000, message = "Số lượng không được vượt quá 1.000.000")
        private Integer quantity;

        @NotNull(message = "Mã đơn vị là bắt buộc")
        @Positive(message = "Mã đơn vị phải là số dương")
        private Long unitId;

        @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
        private String notes;
    }
}
