package com.dental.clinic.management.feedback.controller;

import com.dental.clinic.management.feedback.dto.CreateFeedbackRequest;
import com.dental.clinic.management.feedback.dto.FeedbackResponse;
import com.dental.clinic.management.feedback.dto.FeedbackStatisticsResponse;
import com.dental.clinic.management.feedback.service.AppointmentFeedbackService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

/**
 * REST Controller for Appointment Feedback Management
 * 
 * Endpoints:
 * - POST /api/v1/feedbacks - Create feedback
 * - GET /api/v1/feedbacks/appointment/{appointmentCode} - Get feedback by appointment
 * - GET /api/v1/feedbacks - Get feedbacks list (Admin/Employee)
 * - GET /api/v1/feedbacks/statistics - Get feedback statistics (Admin/Employee)
 */
@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appointment Feedback", description = "APIs for managing appointment feedbacks")
public class AppointmentFeedbackController {

    private final AppointmentFeedbackService feedbackService;

    /**
     * POST /api/v1/feedbacks
     * Create new feedback for completed appointment
     * 
     * Authorization:
     * - Patient: Can create feedback for their own appointments
     * - Admin/Manager: Can create feedback for any appointment
     */
    @PostMapping("")
    @Operation(
        summary = "Create feedback",
        description = "Tạo đánh giá cho lịch hẹn đã hoàn thành. " +
                "Bệnh nhân chỉ có thể đánh giá lịch hẹn của mình. " +
                "Admin/Manager có thể đánh giá cho bất kỳ lịch hẹn nào."
    )
    @ApiMessage("Đánh giá đã được gửi thành công")
    public ResponseEntity<FeedbackResponse> createFeedback(
            @Valid @RequestBody CreateFeedbackRequest request) {
        log.info("REST request to create feedback for appointment: {}", request.getAppointmentCode());
        
        FeedbackResponse response = feedbackService.createFeedback(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/feedbacks/appointment/{appointmentCode}
     * Get feedback by appointment code
     * 
     * Authorization:
     * - Patient: Can view feedback for their own appointments
     * - Employee/Admin: Can view any feedback
     */
    @GetMapping("/appointment/{appointmentCode}")
    @Operation(
        summary = "Get feedback by appointment",
        description = "Lấy đánh giá của một lịch hẹn theo mã lịch hẹn"
    )
    @ApiMessage("Lấy đánh giá thành công")
    public ResponseEntity<FeedbackResponse> getFeedbackByAppointmentCode(
            @Parameter(description = "Appointment code", example = "APT-20260107-001")
            @PathVariable String appointmentCode) {
        log.info("REST request to get feedback for appointment: {}", appointmentCode);
        
        FeedbackResponse response = feedbackService.getFeedbackByAppointmentCode(appointmentCode);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/feedbacks
     * Get feedbacks list with filters (Admin/Employee only)
     * 
     * Authorization: Admin, Manager, Employee with VIEW_FEEDBACK permission
     */
    @GetMapping("")
    @Operation(
        summary = "Get feedbacks list",
        description = "Lấy danh sách đánh giá với filter và pagination (Admin/Employee only)"
    )
    @ApiMessage("Lấy danh sách đánh giá thành công")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + MANAGER + "') or hasAuthority('VIEW_FEEDBACK')")
    public ResponseEntity<Page<FeedbackResponse>> getFeedbacksList(
            @Parameter(description = "Filter theo số sao (1-5)")
            @RequestParam(required = false) Integer rating,
            
            @Parameter(description = "Filter theo mã bác sĩ")
            @RequestParam(required = false) String employeeCode,
            
            @Parameter(description = "Filter theo mã bệnh nhân")
            @RequestParam(required = false) String patientCode,
            
            @Parameter(description = "Filter từ ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "Filter đến ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            
            @Parameter(description = "Trang (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Số item/trang")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sắp xếp (format: field,direction)", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        log.info("REST request to get feedbacks list with filters - rating: {}, employeeCode: {}, patientCode: {}",
                rating, employeeCode, patientCode);
        
        Page<FeedbackResponse> response = feedbackService.getFeedbacksList(
                rating, employeeCode, patientCode, fromDate, toDate, page, size, sort);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/feedbacks/statistics
     * Get feedback statistics (Admin/Employee only)
     * 
     * Authorization: Admin, Manager, Employee with VIEW_FEEDBACK permission
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Get feedback statistics",
        description = "Lấy thống kê đánh giá (tổng số, rating trung bình, phân bố rating, top tags)"
    )
    @ApiMessage("Lấy thống kê đánh giá thành công")
    @PreAuthorize("hasRole('" + ADMIN + "') or hasRole('" + MANAGER + "') or hasAuthority('VIEW_FEEDBACK')")
    public ResponseEntity<FeedbackStatisticsResponse> getFeedbackStatistics(
            @Parameter(description = "Filter theo mã bác sĩ (optional)")
            @RequestParam(required = false) String employeeCode,
            
            @Parameter(description = "Filter từ ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "Filter đến ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        log.info("REST request to get feedback statistics");
        
        FeedbackStatisticsResponse response = feedbackService.getFeedbackStatistics(
                employeeCode, fromDate, toDate);
        
        return ResponseEntity.ok(response);
    }
}
