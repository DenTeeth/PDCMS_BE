package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.working_schedule.dto.request.CreateTimeOffRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateTimeOffStatusRequest;
import com.dental.clinic.management.working_schedule.dto.response.TimeOffRequestResponse;
import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import com.dental.clinic.management.working_schedule.service.TimeOffRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for Time-Off Request Management
 */
@RestController
@RequestMapping("/api/v1/time-off-requests")
@RequiredArgsConstructor
@Slf4j
public class TimeOffRequestController {

    private final TimeOffRequestService requestService;

    /**
     * GET /api/v1/time-off-requests
     * LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch cÃƒÂ¡c yÃƒÂªu cÃ¡ÂºÂ§u nghÃ¡Â»â€° phÃƒÂ©p vÃ¡Â»â€ºi phÃƒÂ¢n trang vÃƒÂ  bÃ¡Â»â„¢ lÃ¡Â»Âc
     *
     * PhÃƒÂ¢n quyÃ¡Â»Ân:
     * - Admin hoÃ¡ÂºÂ·c VIEW_TIMEOFF_ALL: xem tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£
     * - VIEW_TIMEOFF_OWN: chÃ¡Â»â€° xem cÃ¡Â»Â§a chÃƒÂ­nh mÃƒÂ¬nh
     *
     * @param employeeId Filter by employee_id (optional, ignored for
     *                   VIEW_TIMEOFF_OWN)
     * @param status     Filter by status (optional)
     * @param startDate  Filter by start_date >= (optional)
     * @param endDate    Filter by end_date <= (optional)
     * @param pageable   Pagination information
     * @return Page of TimeOffRequestResponse
     */
    @GetMapping
    public ResponseEntity<Page<TimeOffRequestResponse>> getAllRequests(
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) TimeOffStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        log.info(
                "REST request to get all time-off requests with filters: employeeId={}, status={}, startDate={}, endDate={}",
                employeeId, status, startDate, endDate);

        Page<TimeOffRequestResponse> page = requestService.getAllRequests(
                employeeId, status, startDate, endDate, pageable);

        return ResponseEntity.ok(page);
    }

    /**
     * GET /api/v1/time-off-requests/{request_id}
     * Xem chi tiÃ¡ÂºÂ¿t mÃ¡Â»â„¢t yÃƒÂªu cÃ¡ÂºÂ§u nghÃ¡Â»â€° phÃƒÂ©p
     *
     * PhÃƒÂ¢n quyÃ¡Â»Ân:
     * - Admin hoÃ¡ÂºÂ·c VIEW_TIMEOFF_ALL: xem bÃ¡ÂºÂ¥t kÃ¡Â»Â³ yÃƒÂªu cÃ¡ÂºÂ§u nÃƒÂ o
     * - VIEW_TIMEOFF_OWN: chÃ¡Â»â€° xem cÃ¡Â»Â§a chÃƒÂ­nh mÃƒÂ¬nh
     *
     * Response:
     * - 200 OK: TrÃ¡ÂºÂ£ vÃ¡Â»Â chi tiÃ¡ÂºÂ¿t yÃƒÂªu cÃ¡ÂºÂ§u
     * - 404 Not Found: YÃƒÂªu cÃ¡ÂºÂ§u khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i hoÃ¡ÂºÂ·c khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân xem
     *
     * @param requestId The ID of the time-off request
     * @return TimeOffRequestResponse with request details
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<TimeOffRequestResponse> getRequestById(@PathVariable String requestId) {
        log.info("REST request to get time-off request: {}", requestId);
        TimeOffRequestResponse response = requestService.getRequestById(requestId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/time-off-requests
     * TÃ¡ÂºÂ¡o yÃƒÂªu cÃ¡ÂºÂ§u nghÃ¡Â»â€° phÃƒÂ©p mÃ¡Â»â€ºi
     *
     * PhÃƒÂ¢n quyÃ¡Â»Ân:
     * - CREATE_TIMEOFF: quyÃ¡Â»Ân tÃ¡ÂºÂ¡o yÃƒÂªu cÃ¡ÂºÂ§u nghÃ¡Â»â€° phÃƒÂ©p
     *
     * Validation:
     * - employee_id vÃƒÂ  time_off_type_id phÃ¡ÂºÂ£i tÃ¡Â»â€œn tÃ¡ÂºÂ¡i
     * - time_off_type_id phÃ¡ÂºÂ£i is_active = true
     * - start_date khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c lÃ¡Â»â€ºn hÃ†Â¡n end_date
     * - NÃ¡ÂºÂ¿u slot_id cÃƒÂ³ giÃƒÂ¡ trÃ¡Â»â€¹ (nghÃ¡Â»â€° nÃ¡Â»Â­a ngÃƒÂ y), start_date phÃ¡ÂºÂ£i bÃ¡ÂºÂ±ng end_date
     * - KhÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c trÃƒÂ¹ng lÃ¡ÂºÂ·p vÃ¡Â»â€ºi yÃƒÂªu cÃ¡ÂºÂ§u nghÃ¡Â»â€° phÃƒÂ©p khÃƒÂ¡c Ã„â€˜ang active
     * - reason lÃƒÂ  bÃ¡ÂºÂ¯t buÃ¡Â»â„¢c
     *
     * Response:
     * - 201 Created: YÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃ¡ÂºÂ¡o thÃƒÂ nh cÃƒÂ´ng
     * - 400 Bad Request: DÃ¡Â»Â¯ liÃ¡Â»â€¡u khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡ (ngÃƒÂ y khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡)
     * - 404 Not Found: Employee hoÃ¡ÂºÂ·c TimeOffType khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i
     * - 409 Conflict: TrÃƒÂ¹ng lÃ¡ÂºÂ·p vÃ¡Â»â€ºi yÃƒÂªu cÃ¡ÂºÂ§u nghÃ¡Â»â€° phÃƒÂ©p khÃƒÂ¡c
     *
     * @param request CreateTimeOffRequest with request details
     * @return Created TimeOffRequestResponse
     */
    @PostMapping
    public ResponseEntity<TimeOffRequestResponse> createRequest(
            @Valid @RequestBody CreateTimeOffRequest request) {
        log.info("REST request to create time-off request: {}", request);
        TimeOffRequestResponse response = requestService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/v1/time-off-requests/{request_id}
     * CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t trÃ¡ÂºÂ¡ng thÃƒÂ¡i yÃƒÂªu cÃ¡ÂºÂ§u nghÃ¡Â»â€° phÃƒÂ©p (DuyÃ¡Â»â€¡t/TÃ¡Â»Â« chÃ¡Â»â€˜i/HÃ¡Â»Â§y)
     *
     * PhÃƒÂ¢n quyÃ¡Â»Ân:
     * - status=APPROVED: APPROVE_TIMEOFF
     * - status=REJECTED: REJECT_TIMEOFF (reason bÃ¡ÂºÂ¯t buÃ¡Â»â„¢c)
     * - status=CANCELLED: CANCEL_TIMEOFF_OWN (nÃ¡ÂºÂ¿u lÃƒÂ  chÃ¡Â»Â§ sÃ¡Â»Å¸ hÃ¡Â»Â¯u) hoÃ¡ÂºÂ·c
     * CANCEL_TIMEOFF_PENDING (nÃ¡ÂºÂ¿u lÃƒÂ  quÃ¡ÂºÂ£n lÃƒÂ½)
     * (reason bÃ¡ÂºÂ¯t buÃ¡Â»â„¢c)
     *
     * Business Logic:
     * - YÃƒÂªu cÃ¡ÂºÂ§u phÃ¡ÂºÂ£i Ã„â€˜ang Ã¡Â»Å¸ trÃ¡ÂºÂ¡ng thÃƒÂ¡i PENDING
     * - NÃ¡ÂºÂ¿u APPROVED, tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t employee_shifts status thÃƒÂ nh ON_LEAVE
     *
     * Response:
     * - 200 OK: CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thÃƒÂ nh cÃƒÂ´ng
     * - 400 Bad Request: ThiÃ¡ÂºÂ¿u reason (cho REJECTED/CANCELLED)
     * - 403 Forbidden: KhÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân thÃ¡Â»Â±c hiÃ¡Â»â€¡n hÃƒÂ nh Ã„â€˜Ã¡Â»â„¢ng
     * - 404 Not Found: YÃƒÂªu cÃ¡ÂºÂ§u khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i
     * - 409 Conflict: YÃƒÂªu cÃ¡ÂºÂ§u khÃƒÂ´ng Ã¡Â»Å¸ trÃ¡ÂºÂ¡ng thÃƒÂ¡i PENDING
     *
     * @param requestId The ID of the time-off request
     * @param request   UpdateTimeOffStatusRequest with new status and optional
     *                  reason
     * @return Updated TimeOffRequestResponse
     */
    @PatchMapping("/{requestId}")
    public ResponseEntity<TimeOffRequestResponse> updateRequestStatus(
            @PathVariable String requestId,
            @Valid @RequestBody UpdateTimeOffStatusRequest request) {
        log.info("REST request to update time-off request {} to status: {}", requestId, request.getStatus());
        TimeOffRequestResponse response = requestService.updateRequestStatus(requestId, request);
        return ResponseEntity.ok(response);
    }
}
