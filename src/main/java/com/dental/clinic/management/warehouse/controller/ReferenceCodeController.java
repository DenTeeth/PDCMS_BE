package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import com.dental.clinic.management.warehouse.dto.response.AppointmentReferenceDto;
import com.dental.clinic.management.warehouse.dto.response.ReferenceCodeSuggestion;
import com.dental.clinic.management.warehouse.dto.response.ReferenceCodeValidation;
import com.dental.clinic.management.warehouse.service.ReferenceCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.ADMIN;

/**
 * Reference Code Controller
 * Provides APIs for reference code auto-complete, validation, and suggestions
 */
@RestController
@RequestMapping("/api/v1/warehouse/reference-codes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Warehouse Reference Codes", description = "APIs for reference code management and suggestions")
public class ReferenceCodeController {

    private final ReferenceCodeService referenceCodeService;

    /**
     * Get recent reference codes for auto-complete
     * 
     * @param limit Maximum number of suggestions (default: 10)
     * @return List of reference code suggestions
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('VIEW_WAREHOUSE')")
    @Operation(summary = "Lấy danh sách mã tham chiếu gần đây", description = """
            Trả về danh sách mã tham chiếu đã được sử dụng gần đây để hỗ trợ auto-complete.
            
            **Features:**
            - Hiển thị tên bệnh nhân nếu liên kết với appointment
            - Sắp xếp theo ngày sử dụng gần nhất
            - Hỗ trợ giới hạn số lượng kết quả
            
            **Use Cases:**
            - Dropdown suggestions trong form xuất kho
            - Auto-complete khi user nhập mã tham chiếu
            
            **Permissions:**
            - VIEW_WAREHOUSE: Required
            """)
    @ApiMessage("Lấy danh sách mã tham chiếu thành công")
    public ResponseEntity<List<ReferenceCodeSuggestion>> getRecentReferenceCodes(
            @Parameter(description = "Số lượng kết quả tối đa (1-50)") 
            @RequestParam(defaultValue = "10") Integer limit) {
        
        log.info("GET /api/v1/warehouse/reference-codes/recent - limit: {}", limit);

        // Validate limit
        if (limit < 1 || limit > 50) {
            limit = 10;
        }

        List<ReferenceCodeSuggestion> suggestions = referenceCodeService.getRecentReferenceCodes(limit);
        
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Validate reference code and get related entity info
     * 
     * @param code Reference code to validate
     * @return Validation result with details
     */
    @GetMapping("/validate")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('VIEW_WAREHOUSE')")
    @Operation(summary = "Kiểm tra tính hợp lệ của mã tham chiếu", description = """
            Kiểm tra xem mã tham chiếu có tồn tại trong hệ thống không và trả về thông tin liên quan.
            
            **Validation Logic:**
            - APT-xxx: Kiểm tra trong bảng appointments
            - REQ-xxx: Mã yêu cầu (custom)
            - Other: Mã tùy chỉnh (luôn valid)
            
            **Response:**
            - exists: true/false
            - valid: true/false
            - type: APPOINTMENT, REQUEST, CUSTOM
            - relatedEntity: Thông tin appointment nếu tìm thấy
            
            **Permissions:**
            - VIEW_WAREHOUSE: Required
            """)
    @ApiMessage("Kiểm tra mã tham chiếu thành công")
    public ResponseEntity<ReferenceCodeValidation> validateReferenceCode(
            @Parameter(description = "Mã tham chiếu cần kiểm tra", required = true) 
            @RequestParam String code) {
        
        log.info("GET /api/v1/warehouse/reference-codes/validate - code: {}", code);

        ReferenceCodeValidation validation = referenceCodeService.validateReferenceCode(code);
        
        return ResponseEntity.ok(validation);
    }

    /**
     * Search appointments for reference code selection
     * 
     * @param search Search term (appointment code or patient name)
     * @param status Filter by appointment status
     * @param limit  Maximum results
     * @return List of appointment references
     */
    @GetMapping("/appointments")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('VIEW_APPOINTMENT_ALL') or hasAuthority('VIEW_WAREHOUSE')")
    @Operation(summary = "Tìm kiếm ca điều trị để làm mã tham chiếu", description = """
            Tìm kiếm appointments để chọn làm reference code cho phiếu xuất kho.
            
            **Search Fields:**
            - Appointment code (APT-xxx)
            - Patient name
            
            **Filters:**
            - status: Lọc theo trạng thái appointment
            
            **Response:**
            - appointmentId: ID của appointment
            - appointmentCode: Mã appointment
            - patientName: Tên bệnh nhân
            - appointmentDate: Ngày giờ hẹn
            - services: Danh sách dịch vụ
            - displayLabel: Nhãn hiển thị cho dropdown
            
            **Permissions:**
            - VIEW_WAREHOUSE or VIEW_APPOINTMENT_ALL: Required
            """)
    @ApiMessage("Tìm kiếm ca điều trị thành công")
    public ResponseEntity<List<AppointmentReferenceDto>> searchAppointments(
            @Parameter(description = "Tìm kiếm theo mã appointment hoặc tên bệnh nhân") 
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Lọc theo trạng thái appointment") 
            @RequestParam(required = false) AppointmentStatus status,
            
            @Parameter(description = "Số lượng kết quả tối đa (1-50)") 
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("GET /api/v1/warehouse/reference-codes/appointments - search: {}, status: {}, limit: {}", 
                search, status, limit);

        // Validate limit
        if (limit < 1 || limit > 50) {
            limit = 20;
        }

        List<AppointmentReferenceDto> results = referenceCodeService.searchAppointments(search, status, limit);
        
        return ResponseEntity.ok(results);
    }
}
