package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.request.CreateWorkShiftRequest;
import com.dental.clinic.management.dto.request.UpdateWorkShiftRequest;
import com.dental.clinic.management.dto.response.WorkShiftResponse;
import com.dental.clinic.management.service.WorkShiftService;
import com.dental.clinic.management.utils.ResponseMessage;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing work shifts (predefined shift templates).
 * Admin-only access for all operations.
 */
@RestController
@RequestMapping("/api/v1/work-shifts")
@Tag(name = "Work Shifts", description = "APIs for managing predefined work shift templates")
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    public WorkShiftController(WorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }

    /**
     * Create new work shift template.
     *
     * @param request Create work shift request
     * @return Created work shift response
     */
    @PostMapping
    @ApiMessage("Ca làm việc đã được tạo thành công")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create work shift", description = "Create a new work shift template (Admin only)")
    public ResponseEntity<WorkShiftResponse> createWorkShift(@Valid @RequestBody CreateWorkShiftRequest request) {
        WorkShiftResponse response = workShiftService.createWorkShift(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update existing work shift.
     *
     * @param shiftId Work shift ID
     * @param request Update work shift request
     * @return Updated work shift response
     */
    @PutMapping("/{shiftId}")
    @ApiMessage("Ca làm việc đã được cập nhật thành công")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update work shift", description = "Update an existing work shift template (Admin only)")
    public ResponseEntity<WorkShiftResponse> updateWorkShift(
            @PathVariable String shiftId,
            @Valid @RequestBody UpdateWorkShiftRequest request) {
        WorkShiftResponse response = workShiftService.updateWorkShift(shiftId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get work shift by ID.
     *
     * @param shiftId Work shift ID
     * @return Work shift response
     */
    @GetMapping("/{shiftId}")
    @ApiMessage("Lấy thông tin ca làm việc thành công")
    @Operation(summary = "Get work shift", description = "Get work shift by ID (All roles)")
    public ResponseEntity<WorkShiftResponse> getWorkShiftById(@PathVariable String shiftId) {
        WorkShiftResponse response = workShiftService.getWorkShiftById(shiftId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all work shifts with pagination.
     *
     * @param page            Page number (default: 0)
     * @param size            Page size (default: 10, max: 100)
     * @param includeInactive Include inactive shifts (default: false)
     * @return Page of work shifts
     */
    @GetMapping
    @ApiMessage("Lấy danh sách ca làm việc thành công")
    @Operation(summary = "Get all work shifts", description = "Get all work shifts with pagination (All roles)")
    public ResponseEntity<Page<WorkShiftResponse>> getAllWorkShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        Page<WorkShiftResponse> response = workShiftService.getAllWorkShifts(includeInactive, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete work shift (soft delete).
     * Cannot delete if shift is in use.
     *
     * @param shiftId Work shift ID
     * @return Success message
     */
    @DeleteMapping("/{shiftId}")
    @ApiMessage("Ca làm việc đã được xóa thành công")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete work shift", description = "Soft delete work shift (Admin only)")
    public ResponseEntity<ResponseMessage> deleteWorkShift(@PathVariable String shiftId) {
        workShiftService.deleteWorkShift(shiftId);
        return ResponseEntity.ok(new ResponseMessage("Ca làm việc đã được xóa"));
    }
}
