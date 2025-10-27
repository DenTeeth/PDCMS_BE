package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.working_schedule.dto.request.CreateShiftRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateShiftRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.request.ReplaceShiftRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.response.ShiftRegistrationResponse;
import com.dental.clinic.management.working_schedule.service.EmployeeShiftRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Employee Shift Registration Management.
 * Provides endpoints for managing part-time employee shift registrations.
 */
@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Slf4j
public class EmployeeShiftRegistrationController {

    private final EmployeeShiftRegistrationService registrationService;

    /**
     * GET /api/v1/registrations
     * Xem danh sách đăng ký ca làm part-time.
     *
     * Phân quyền:
     * - Admin hoặc VIEW_REGISTRATION_ALL: xem tất cả
     * - VIEW_REGISTRATION_OWN: chỉ xem của chính mình (tự động lọc theo employee_id
     * từ token)
     *
     * @param pageable Pagination information
     * @return Page of ShiftRegistrationResponse
     */
    @GetMapping
    public ResponseEntity<Page<ShiftRegistrationResponse>> getAllRegistrations(Pageable pageable) {
        log.info("REST request to get all Employee Shift Registrations with pagination: {}", pageable);
        Page<ShiftRegistrationResponse> page = registrationService.getAllRegistrations(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * GET /api/v1/registrations/{registration_id}
     * Xem chi tiết một đăng ký ca làm.
     *
     * Phân quyền:
     * - Admin hoặc VIEW_REGISTRATION_ALL: xem bất kỳ registration nào
     * - VIEW_REGISTRATION_OWN: chỉ xem của chính mình
     *
     * Response:
     * - 200 OK: Trả về chi tiết registration
     * - 404 Not Found: Đăng ký không tồn tại hoặc người dùng không có quyền xem
     *
     * @param registrationId The ID of the registration
     * @return ShiftRegistrationResponse with registration details
     */
    @GetMapping("/{registrationId}")
    public ResponseEntity<ShiftRegistrationResponse> getRegistrationById(
            @PathVariable String registrationId) {
        log.info("REST request to get Employee Shift Registration: {}", registrationId);
        ShiftRegistrationResponse response = registrationService.getRegistrationById(registrationId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/registrations
     * Tạo đăng ký ca làm part-time mới.
     *
     * Phân quyền:
     * - CREATE_REGISTRATION: quyền tạo đăng ký ca làm
     *
     * Validation:
     * - Chỉ nhân viên PART_TIME mới được tạo đăng ký
     * - work_shift_id phải tồn tại và is_active = true
     * - effective_from không được là quá khứ
     * - effective_to (nếu có) phải >= effective_from
     * - Không được xung đột với các đăng ký đang hoạt động (cùng employee, work shift,
     * day_of_week)
     *
     * Response:
     * - 201 Created: Đăng ký được tạo thành công
     * - 400 Bad Request: Dữ liệu không hợp lệ
     * - 403 Forbidden: Nhân viên không phải PART_TIME
     * - 404 Not Found: Employee hoặc WorkShift không tồn tại
     * - 409 Conflict: Xung đột với đăng ký đang hoạt động
     *
     * @param request CreateShiftRegistrationRequest with registration details
     * @return Created ShiftRegistrationResponse
     */
    @PostMapping
    public ResponseEntity<ShiftRegistrationResponse> createRegistration(
            @Valid @RequestBody CreateShiftRegistrationRequest request) {
        log.info("REST request to create Employee Shift Registration: {}", request);
        ShiftRegistrationResponse response = registrationService.createRegistration(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/v1/registrations/{registration_id}
     * Cập nhật một phần đăng ký ca làm.
     *
     * Phân quyền:
     * - Admin hoặc UPDATE_REGISTRATION_ALL: cập nhật bất kỳ registration nào
     * - UPDATE_REGISTRATION_OWN: chỉ cập nhật của chính mình
     *
     * Validation:
     * - Nếu workShiftId thay đổi, phải tồn tại và is_active = true
     * - Nếu workShiftId hoặc daysOfWeek thay đổi, phải validate conflict lại
     * - effectiveFrom không được là quá khứ (nếu có)
     * - effectiveTo phải >= effectiveFrom (nếu có)
     *
     * Response:
     * - 200 OK: Cập nhật thành công
     * - 400 Bad Request: Dữ liệu không hợp lệ
     * - 404 Not Found: Đăng ký không tồn tại hoặc không có quyền cập nhật
     * - 409 Conflict: Xung đột với đăng ký đang hoạt động
     *
     * @param registrationId The ID of the registration to update
     * @param request        UpdateShiftRegistrationRequest with fields to update
     *                       (all optional)
     * @return Updated ShiftRegistrationResponse
     */
    @PatchMapping("/{registrationId}")
    public ResponseEntity<ShiftRegistrationResponse> updateRegistration(
            @PathVariable String registrationId,
            @RequestBody UpdateShiftRegistrationRequest request) {
        log.info("REST request to update Employee Shift Registration {}: {}", registrationId, request);
        ShiftRegistrationResponse response = registrationService.updateRegistration(registrationId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/registrations/{registration_id}
     * Thay thế toàn bộ thông tin đăng ký ca làm.
     *
     * Phân quyền:
     * - Admin hoặc UPDATE_REGISTRATION_ALL: cập nhật bất kỳ registration nào
     * - UPDATE_REGISTRATION_OWN: chỉ cập nhật của chính mình
     *
     * Validation:
     * - Yêu cầu tất cả các trường trong body
     * - Thực hiện đầy đủ validation như khi POST
     * - workShiftId phải tồn tại và is_active = true
     * - effectiveFrom không được là quá khứ
     * - effectiveTo phải >= effectiveFrom (nếu có)
     * - Không được xung đột với các đăng ký đang hoạt động khác
     *
     * Response:
     * - 200 OK: Thay thế thành công
     * - 400 Bad Request: Dữ liệu không hợp lệ
     * - 404 Not Found: Đăng ký không tồn tại hoặc không có quyền cập nhật
     * - 409 Conflict: Xung đột với đăng ký đang hoạt động
     *
     * @param registrationId The ID of the registration to replace
     * @param request        ReplaceShiftRegistrationRequest with all required
     *                       fields
     * @return Replaced ShiftRegistrationResponse
     */
    @PutMapping("/{registrationId}")
    public ResponseEntity<ShiftRegistrationResponse> replaceRegistration(
            @PathVariable String registrationId,
            @Valid @RequestBody ReplaceShiftRegistrationRequest request) {
        log.info("REST request to replace Employee Shift Registration {}: {}", registrationId, request);
        ShiftRegistrationResponse response = registrationService.replaceRegistration(registrationId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/registrations/{registration_id}
     * Hủy đăng ký ca làm (xóa mềm - set is_active = false).
     *
     * Phân quyền:
     * - Admin hoặc DELETE_REGISTRATION_ALL: xóa bất kỳ registration nào
     * - DELETE_REGISTRATION_OWN: chỉ xóa của chính mình
     *
     * Business Logic:
     * - KHÔNG xóa record vĩnh viễn
     * - Chỉ cập nhật is_active = false
     *
     * Response:
     * - 204 No Content: Xóa thành công
     * - 404 Not Found: Đăng ký không tồn tại hoặc không có quyền xóa
     *
     * @param registrationId The ID of the registration to delete
     * @return 204 No Content
     */
    @DeleteMapping("/{registrationId}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable String registrationId) {
        log.info("REST request to delete (soft) Employee Shift Registration: {}", registrationId);
        registrationService.deleteRegistration(registrationId);
        return ResponseEntity.noContent().build();
    }
}
