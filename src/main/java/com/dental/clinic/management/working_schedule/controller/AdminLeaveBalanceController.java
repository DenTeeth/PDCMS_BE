package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.working_schedule.dto.request.AdjustLeaveBalanceRequest;
import com.dental.clinic.management.working_schedule.dto.request.AnnualResetRequest;
import com.dental.clinic.management.working_schedule.dto.response.AllEmployeesLeaveBalanceResponse;
import com.dental.clinic.management.working_schedule.dto.response.EmployeeLeaveBalanceResponse;
import com.dental.clinic.management.working_schedule.service.AdminLeaveBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.Map;

/**
 * REST controller for Admin Leave Balance Management (P5.2)
 * Handles leave balance queries, adjustments, and annual reset
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminLeaveBalanceController {

    private final AdminLeaveBalanceService balanceService;

    /**
     * GET /api/v1/admin/leave-balances
     * LÃ¡ÂºÂ¥y sÃ¡Â»â€˜ dÃ†Â° phÃƒÂ©p cÃ¡Â»Â§a TÃ¡ÂºÂ¤T CÃ¡ÂºÂ¢ nhÃƒÂ¢n viÃƒÂªn (Admin Dashboard)
     *
     * Authorization: VIEW_LEAVE_BALANCE_ALL
     *
     * Query Params:
     * - cycle_year (integer, optional): LÃ¡Â»Âc theo nÃ„Æ’m. MÃ¡ÂºÂ·c Ã„â€˜Ã¡Â»â€¹nh lÃƒÂ  nÃ„Æ’m hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i
     * - time_off_type_id (string, optional): LÃ¡Â»Âc theo mÃ¡Â»â„¢t loÃ¡ÂºÂ¡i phÃƒÂ©p cÃ¡Â»Â¥ thÃ¡Â»Æ’ (vÃƒÂ­ dÃ¡Â»Â¥: ANNUAL_LEAVE)
     *
     * Response:
     * - 200 OK: TrÃ¡ÂºÂ£ vÃ¡Â»Â danh sÃƒÂ¡ch "vÃƒÂ­ phÃƒÂ©p" cÃ¡Â»Â§a TÃ¡ÂºÂ¤T CÃ¡ÂºÂ¢ nhÃƒÂ¢n viÃƒÂªn
     *
     * Response Body:
     * {
     *   "filter": {
     *     "cycle_year": 2025,
     *     "time_off_type_id": null
     *   },
     *   "data": [
     *     {
     *       "employee_id": 5,
     *       "employee_name": "HoÃƒÂ ng VÃ„Æ’n TuÃ¡ÂºÂ¥n",
     *       "balances": [
     *         {
     *           "time_off_type_name": "NghÃ¡Â»â€° phÃƒÂ©p nÃ„Æ’m",
     *           "total_days_allowed": 12.0,
     *           "days_taken": 3.5,
     *           "days_remaining": 8.5
     *         }
     *       ]
     *     }
     *   ]
     * }
     *
     * @param cycleYear the year to query (optional, defaults to current year)
     * @param timeOffTypeId filter by specific time-off type (optional)
     * @return AllEmployeesLeaveBalanceResponse
     */
    @GetMapping("/leave-balances")
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
            "hasAuthority('" + AuthoritiesConstants.VIEW_LEAVE_BALANCE_ALL + "')")
    public ResponseEntity<AllEmployeesLeaveBalanceResponse> getAllEmployeesLeaveBalances(
            @RequestParam(required = false, name = "cycle_year") Integer cycleYear,
            @RequestParam(required = false, name = "time_off_type_id") String timeOffTypeId) {

        // Default to current year if not specified
        if (cycleYear == null) {
            cycleYear = Year.now().getValue();
        }

        log.info("Admin REST request to get leave balances for all employees in year {} for type {}",
                cycleYear, timeOffTypeId);

        AllEmployeesLeaveBalanceResponse response = balanceService.getAllEmployeesLeaveBalances(cycleYear, timeOffTypeId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/employees/{employee_id}/leave-balances
     * LÃ¡ÂºÂ¥y sÃ¡Â»â€˜ dÃ†Â° phÃƒÂ©p cÃ¡Â»Â§a mÃ¡Â»â„¢t nhÃƒÂ¢n viÃƒÂªn
     *
     * Authorization: VIEW_LEAVE_BALANCE_ALL
     *
     * Query Params:
     * - cycle_year (integer, optional): NÃ„Æ’m muÃ¡Â»â€˜n xem. NÃ¡ÂºÂ¿u Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng, mÃ¡ÂºÂ·c Ã„â€˜Ã¡Â»â€¹nh lÃƒÂ  nÃ„Æ’m hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i
     *
     * Response:
     * - 200 OK: TrÃ¡ÂºÂ£ vÃ¡Â»Â danh sÃƒÂ¡ch "vÃƒÂ­ phÃƒÂ©p" cÃ¡Â»Â§a nhÃƒÂ¢n viÃƒÂªn
     * - 404 NOT_FOUND: EMPLOYEE_NOT_FOUND
     *
     * Response Body:
     * {
     *   "employee_id": 5,
     *   "cycle_year": 2025,
     *   "balances": [
     *     {
     *       "balance_id": 101,
     *       "time_off_type": {
     *         "type_id": "ANNUAL_LEAVE",
     *         "type_name": "NghÃ¡Â»â€° phÃƒÂ©p nÃ„Æ’m",
     *         "is_paid": true
     *       },
     *       "total_days_allowed": 12.0,
     *       "days_taken": 3.5,
     *       "days_remaining": 8.5
     *     }
     *   ]
     * }
     *
     * @param employeeId the employee ID
     * @param cycleYear the year to query (optional, defaults to current year)
     * @return EmployeeLeaveBalanceResponse
     */
    @GetMapping("/employees/{employee_id}/leave-balances")
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
            "hasAuthority('" + AuthoritiesConstants.VIEW_LEAVE_BALANCE_ALL + "')")
    public ResponseEntity<EmployeeLeaveBalanceResponse> getEmployeeLeaveBalances(
            @PathVariable("employee_id") Integer employeeId,
            @RequestParam(required = false, name = "cycle_year") Integer cycleYear) {

        // Default to current year if not specified
        if (cycleYear == null) {
            cycleYear = Year.now().getValue();
        }

        log.info("Admin REST request to get leave balances for employee {} in year {}", employeeId, cycleYear);
        EmployeeLeaveBalanceResponse response = balanceService.getEmployeeLeaveBalances(employeeId, cycleYear);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/admin/leave-balances/adjust
     * Ã„ÂiÃ¡Â»Âu chÃ¡Â»â€°nh sÃ¡Â»â€˜ dÃ†Â° phÃƒÂ©p (CÃ¡Â»â„¢ng/TrÃ¡Â»Â« thÃ¡Â»Â§ cÃƒÂ´ng)
     *
     * Authorization: ADJUST_LEAVE_BALANCE
     *
     * Request Body:
     * {
     *   "employee_id": 5,
     *   "time_off_type_id": "ANNUAL_LEAVE",
     *   "cycle_year": 2025,
     *   "change_amount": 1.5,    // SÃ¡Â»â€˜ dÃ†Â°Ã†Â¡ng: cÃ¡Â»â„¢ng, SÃ¡Â»â€˜ ÃƒÂ¢m: trÃ¡Â»Â«
     *   "notes": "ThÃ†Â°Ã¡Â»Å¸ng 1.5 ngÃƒÂ y phÃƒÂ©p do hoÃƒÂ n thÃƒÂ nh xuÃ¡ÂºÂ¥t sÃ¡ÂºÂ¯c dÃ¡Â»Â± ÃƒÂ¡n."
     * }
     *
     * Business Logic:
     * - Validate employee_id vÃƒÂ  time_off_type_id phÃ¡ÂºÂ£i tÃ¡Â»â€œn tÃ¡ÂºÂ¡i
     * - TÃƒÂ¬m balance_id tÃ†Â°Ã†Â¡ng Ã¡Â»Â©ng trong employee_leave_balances
     * - NÃ¡ÂºÂ¿u khÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y: TÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng tÃ¡ÂºÂ¡o record mÃ¡Â»â€ºi (vÃ¡Â»â€ºi total_days_allowed = 0, days_taken = 0)
     * - NÃ¡ÂºÂ¿u change_amount > 0: UPDATE total_days_allowed = total_days_allowed + change_amount
     * - NÃ¡ÂºÂ¿u change_amount < 0: UPDATE days_taken = days_taken + abs(change_amount)
     * - KiÃ¡Â»Æ’m tra sÃ¡Â»â€˜ dÃ†Â° mÃ¡Â»â€ºi (total_days_allowed - days_taken) >= 0
     * - INSERT vÃƒÂ o leave_balance_history
     *
     * Response:
     * - 200 OK: Ã„ÂiÃ¡Â»Âu chÃ¡Â»â€°nh thÃƒÂ nh cÃƒÂ´ng
     * - 400 BAD_REQUEST: INVALID_BALANCE (sÃ¡Â»â€˜ dÃ†Â° ÃƒÂ¢m sau Ã„â€˜iÃ¡Â»Âu chÃ¡Â»â€°nh)
     * - 404 NOT_FOUND: RELATED_RESOURCE_NOT_FOUND
     *
     * @param request the adjustment request
     * @return success message
     */
    @PostMapping("/leave-balances/adjust")
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
            "hasAuthority('" + AuthoritiesConstants.ADJUST_LEAVE_BALANCE + "')")
    public ResponseEntity<Map<String, Object>> adjustLeaveBalance(@Valid @RequestBody AdjustLeaveBalanceRequest request) {
        log.info("Admin REST request to adjust leave balance for employee {} type {} year {}: {} days",
                request.getEmployeeId(), request.getTimeOffTypeId(), request.getCycleYear(), request.getChangeAmount());

        balanceService.adjustLeaveBalance(request);

        return ResponseEntity.ok(Map.of(
                "message", "Ã„ÂiÃ¡Â»Âu chÃ¡Â»â€°nh sÃ¡Â»â€˜ dÃ†Â° phÃƒÂ©p thÃƒÂ nh cÃƒÂ´ng",
                "employee_id", request.getEmployeeId(),
                "time_off_type_id", request.getTimeOffTypeId(),
                "cycle_year", request.getCycleYear(),
                "change_amount", request.getChangeAmount()
        ));
    }

    /**
     * POST /api/v1/admin/leave-balances/annual-reset
     * CRON JOB - TÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng reset ngÃƒÂ y nghÃ¡Â»â€° khi sang nÃ„Æ’m mÃ¡Â»â€ºi
     *
     * Authorization: ADMIN only
     *
     * Request Body:
     * {
     *   "cycle_year": 2026,
     *   "apply_to_type_id": "ANNUAL_LEAVE",
     *   "default_allowance": 12.0
     * }
     *
     * Logic cÃ¡Â»Â§a Job (chÃ¡ÂºÂ¡y ngÃ¡ÂºÂ§m):
     * - LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ nhÃƒÂ¢n viÃƒÂªn Ã„â€˜ang is_active = true
     * - LÃ¡ÂºÂ·p qua tÃ¡Â»Â«ng employee_id:
     *   - KiÃ¡Â»Æ’m tra xem Ã„â€˜ÃƒÂ£ cÃƒÂ³ employee_leave_balances cho apply_to_type_id vÃƒÂ  cycle_year chÃ†Â°a
     *   - NÃ¡ÂºÂ¿u CHÃ†Â¯A cÃƒÂ³:
     *     - INSERT record mÃ¡Â»â€ºi (total_days_allowed = default_allowance, days_taken = 0)
     *     - INSERT vÃƒÂ o leave_balance_history (reason: 'ANNUAL_RESET')
     *   - NÃ¡ÂºÂ¿u CÃƒâ€œ RÃ¡Â»â€™I:
     *     - BÃ¡Â»Â qua (log "Ã„ÂÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i") Ã„â€˜Ã¡Â»Æ’ trÃƒÂ¡nh cÃ¡Â»â„¢ng dÃ¡Â»â€œn phÃƒÂ©p
     *
     * Response:
     * - 200 OK: Reset thÃƒÂ nh cÃƒÂ´ng
     * - 400 BAD_REQUEST: INVALID_YEAR
     * - 409 CONFLICT: JOB_ALREADY_RUN (nÃ¡ÂºÂ¿u chÃ¡ÂºÂ¡y lÃ¡ÂºÂ¡i cho cÃƒÂ¹ng nÃ„Æ’m)
     *
     * @param request the annual reset request
     * @return success message with statistics
     */
    @PostMapping("/leave-balances/annual-reset")
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<Map<String, Object>> annualReset(@Valid @RequestBody AnnualResetRequest request) {
        log.info("Admin REST request to run annual leave balance reset for year {} type {} with {} days",
                request.getCycleYear(), request.getApplyToTypeId(), request.getDefaultAllowance());

        Map<String, Object> result = balanceService.annualReset(request);
        return ResponseEntity.ok(result);
    }
}
