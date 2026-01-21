package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.working_schedule.dto.response.EmployeeLeaveBalanceResponse;
import com.dental.clinic.management.working_schedule.service.AdminLeaveBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Year;

/**
 * REST controller for Employee Leave Balance Self-Service
 * Allows employees to view their own leave balances
 */
@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee - Leave Balance Self-Service", description = "APIs for employees to view their own leave balances")
public class EmployeeLeaveBalanceController {

    private final AdminLeaveBalanceService balanceService;
    private final AccountRepository accountRepository;

    /**
     * GET /api/v1/employee/leave-balances
     * Lấy số dư phép của nhân viên hiện tại (chính mình)
     *
     * Authorization: VIEW_LEAVE_OWN
     *
     * Query Params:
     * - cycle_year (integer, optional): Năm muốn xem. Nếu để trống, mặc định là năm hiện tại
     *
     * Security:
     * - Employee ID được tự động trích xuất từ JWT token
     * - Nhân viên chỉ có thể xem số dư phép của chính mình
     * - Không cho phép chỉ định employee_id trong request
     *
     * Response:
     * - 200 OK: Trả về danh sách "ví phép" của nhân viên
     * - 403 FORBIDDEN: Không có quyền VIEW_LEAVE_OWN
     * - 404 NOT_FOUND: Không tìm thấy thông tin nhân viên hoặc không có số dư phép
     *
     * Response Body:
     * {
     *   "employee_id": 123,
     *   "cycle_year": 2026,
     *   "balances": [
     *     {
     *       "balance_id": 101,
     *       "time_off_type": {
     *         "type_id": "ANNUAL_LEAVE",
     *         "type_name": "Nghỉ phép năm",
     *         "is_paid": true
     *       },
     *       "total_days_allowed": 12.0,
     *       "days_taken": 3.5,
     *       "days_remaining": 8.5
     *     }
     *   ]
     * }
     *
     * @param cycleYear the year to query (optional, defaults to current year)
     * @return EmployeeLeaveBalanceResponse
     */
    @Operation(
        summary = "Get own leave balances",
        description = "Retrieve leave balance information for the current employee (auto-extracted from JWT token). Employees can only view their own balances."
    )
    @GetMapping("/leave-balances")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_LEAVE_OWN + "')")
    public ResponseEntity<EmployeeLeaveBalanceResponse> getOwnLeaveBalances(
            @RequestParam(required = false, name = "cycle_year") Integer cycleYear) {

        // Default to current year if not specified
        if (cycleYear == null) {
            cycleYear = Year.now().getValue();
        }

        // Extract employee ID from JWT token
        Integer employeeId = getCurrentEmployeeId();

        log.info("Employee {} REST request to get own leave balances in year {}", employeeId, cycleYear);

        // Reuse existing service method from AdminLeaveBalanceService
        EmployeeLeaveBalanceResponse response = balanceService.getEmployeeLeaveBalances(employeeId, cycleYear);

        return ResponseEntity.ok(response);
    }

    /**
     * Get current employee ID from security context.
     * Extracts the employee ID from the authenticated user's JWT token.
     *
     * @return the current employee ID
     * @throws RuntimeException if user is not authenticated or employee not found
     */
    private Integer getCurrentEmployeeId() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

        return accountRepository.findOneByUsername(username)
                .map(account -> account.getEmployee().getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên cho người dùng: " + username));
    }
}
