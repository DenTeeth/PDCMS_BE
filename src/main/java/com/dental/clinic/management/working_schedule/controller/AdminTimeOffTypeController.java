package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.working_schedule.dto.request.CreateTimeOffTypeRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateTimeOffTypeRequest;
import com.dental.clinic.management.working_schedule.dto.response.TimeOffTypeResponse;
import com.dental.clinic.management.working_schedule.service.TimeOffTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Admin Time-Off Type Management (P6.1)
 * Handles CRUD operations for time-off types
 */
@RestController
@RequestMapping("/api/v1/admin/time-off-types")
@RequiredArgsConstructor
@Slf4j
public class AdminTimeOffTypeController {

    private final TimeOffTypeService typeService;

    /**
     * GET /api/v1/admin/time-off-types
     * LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch LoÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p (Admin View)
     *
     * Authorization: VIEW_TIMEOFF_TYPE_ALL
     *
     * Query Params:
     * - is_active (boolean, optional): LÃ¡Â»Âc theo trÃ¡ÂºÂ¡ng thÃƒÂ¡i
     * - is_paid (boolean, optional): LÃ¡Â»Âc theo loÃ¡ÂºÂ¡i cÃƒÂ³ lÃ†Â°Ã†Â¡ng/khÃƒÂ´ng lÃ†Â°Ã†Â¡ng
     *
     * Response:
     * - 200 OK: TrÃ¡ÂºÂ£ vÃ¡Â»Â danh sÃƒÂ¡ch tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p (kÃ¡Â»Æ’ cÃ¡ÂºÂ£ inactive)
     *
     * @param isActive filter by active status (optional)
     * @param isPaid filter by paid status (optional)
     * @return List of TimeOffTypeResponse
     */
    @GetMapping
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
            "hasAuthority('" + AuthoritiesConstants.VIEW_TIMEOFF_TYPE_ALL + "')")
    public ResponseEntity<List<TimeOffTypeResponse>> getAllTimeOffTypes(
            @RequestParam(required = false, name = "is_active") Boolean isActive,
            @RequestParam(required = false, name = "is_paid") Boolean isPaid) {
        log.info("Admin REST request to get all time-off types, is_active={}, is_paid={}", isActive, isPaid);
        List<TimeOffTypeResponse> types = typeService.getAllTimeOffTypes(isActive, isPaid);
        return ResponseEntity.ok(types);
    }

    /**
     * GET /api/v1/admin/time-off-types/{type_id}
     * LÃ¡ÂºÂ¥y chi tiÃ¡ÂºÂ¿t mÃ¡Â»â„¢t loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p
     *
     * Authorization: VIEW_TIMEOFF_TYPE_ALL
     *
     * Response:
     * - 200 OK: TrÃ¡ÂºÂ£ vÃ¡Â»Â chi tiÃ¡ÂºÂ¿t loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p
     * - 404 NOT_FOUND: KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y loÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p
     *
     * @param typeId the time-off type ID
     * @return TimeOffTypeResponse
     */
    @GetMapping("/{type_id}")
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
            "hasAuthority('" + AuthoritiesConstants.VIEW_TIMEOFF_TYPE_ALL + "')")
    public ResponseEntity<TimeOffTypeResponse> getTimeOffTypeById(@PathVariable("type_id") String typeId) {
        log.info("Admin REST request to get time-off type: {}", typeId);
        TimeOffTypeResponse response = typeService.getTimeOffTypeById(typeId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/admin/time-off-types
     * TÃ¡ÂºÂ¡o LoÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p mÃ¡Â»â€ºi
     *
     * Authorization: CREATE_TIMEOFF_TYPE
     *
     * Request Body:
     * {
     *   "type_code": "UNPAID_LEAVE",
     *   "type_name": "NghÃ¡Â»â€° khÃƒÂ´ng lÃ†Â°Ã†Â¡ng",
     *   "is_paid": false
     * }
     *
     * Business Logic:
     * - type_code phÃ¡ÂºÂ£i unique
     * - type_name lÃƒÂ  bÃ¡ÂºÂ¯t buÃ¡Â»â„¢c
     * - is_paid lÃƒÂ  bÃ¡ÂºÂ¯t buÃ¡Â»â„¢c
     * - TÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng gÃƒÂ¡n is_active = true
     *
     * Response:
     * - 201 CREATED: TÃ¡ÂºÂ¡o thÃƒÂ nh cÃƒÂ´ng
     * - 409 CONFLICT: DUPLICATE_TYPE_CODE
     *
     * @param request the creation request
     * @return TimeOffTypeResponse
     */
    @PostMapping
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
            "hasAuthority('" + AuthoritiesConstants.CREATE_TIMEOFF_TYPE + "')")
    public ResponseEntity<TimeOffTypeResponse> createTimeOffType(@Valid @RequestBody CreateTimeOffTypeRequest request) {
        log.info("Admin REST request to create time-off type: {}", request.getTypeCode());
        TimeOffTypeResponse response = typeService.createTimeOffType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/v1/admin/time-off-types/{type_id}
     * CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t LoÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p
     *
     * Authorization: UPDATE_TIMEOFF_TYPE
     *
     * Request Body (chÃ¡Â»â€° gÃ¡Â»Â­i cÃƒÂ¡c trÃ†Â°Ã¡Â»Âng cÃ¡ÂºÂ§n cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t):
     * {
     *   "type_name": "NghÃ¡Â»â€° khÃƒÂ´ng lÃ†Â°Ã†Â¡ng (ViÃ¡Â»â€¡c riÃƒÂªng)"
     * }
     *
     * Business Logic:
     * - NÃ¡ÂºÂ¿u type_code thay Ã„â€˜Ã¡Â»â€¢i, phÃ¡ÂºÂ£i kiÃ¡Â»Æ’m tra unique
     *
     * Response:
     * - 200 OK: CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thÃƒÂ nh cÃƒÂ´ng
     * - 404 NOT_FOUND: TIMEOFF_TYPE_NOT_FOUND
     * - 409 CONFLICT: DUPLICATE_TYPE_CODE
     *
     * @param typeId the time-off type ID
     * @param request the update request
     * @return TimeOffTypeResponse
     */
    @PatchMapping("/{type_id}")
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
            "hasAuthority('" + AuthoritiesConstants.UPDATE_TIMEOFF_TYPE + "')")
    public ResponseEntity<TimeOffTypeResponse> updateTimeOffType(
            @PathVariable("type_id") String typeId,
            @Valid @RequestBody UpdateTimeOffTypeRequest request) {
        log.info("Admin REST request to update time-off type: {}", typeId);
        TimeOffTypeResponse response = typeService.updateTimeOffType(typeId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/admin/time-off-types/{type_id}
     * VÃƒÂ´ hiÃ¡Â»â€¡u hÃƒÂ³a / KÃƒÂ­ch hoÃ¡ÂºÂ¡t lÃ¡ÂºÂ¡i LoÃ¡ÂºÂ¡i nghÃ¡Â»â€° phÃƒÂ©p (Toggle is_active)
     *
     * Authorization: DELETE_TIMEOFF_TYPE
     *
     * Business Logic:
     * - Soft delete: Ã„ÂÃ¡ÂºÂ£o ngÃ†Â°Ã¡Â»Â£c is_active (true <-> false)
     * - NÃ¡ÂºÂ¿u Ã„â€˜ang vÃƒÂ´ hiÃ¡Â»â€¡u hÃƒÂ³a (true -> false), kiÃ¡Â»Æ’m tra xem cÃƒÂ³ request PENDING nÃƒÂ o
     *   Ã„â€˜ang dÃƒÂ¹ng type_id nÃƒÂ y khÃƒÂ´ng
     * - NÃ¡ÂºÂ¿u cÃƒÂ³, trÃ¡ÂºÂ£ vÃ¡Â»Â lÃ¡Â»â€”i 409 CONFLICT
     *
     * Response:
     * - 200 OK: Toggle thÃƒÂ nh cÃƒÂ´ng
     * - 404 NOT_FOUND: TIMEOFF_TYPE_NOT_FOUND
     * - 409 CONFLICT: TIMEOFF_TYPE_IN_USE
     *
     * @param typeId the time-off type ID
     * @return TimeOffTypeResponse with updated is_active status
     */
    @DeleteMapping("/{type_id}")
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
            "hasAuthority('" + AuthoritiesConstants.DELETE_TIMEOFF_TYPE + "')")
    public ResponseEntity<TimeOffTypeResponse> toggleTimeOffTypeActive(@PathVariable("type_id") String typeId) {
        log.info("Admin REST request to toggle time-off type active status: {}", typeId);
        TimeOffTypeResponse response = typeService.toggleTimeOffTypeActive(typeId);
        return ResponseEntity.ok(response);
    }
}
