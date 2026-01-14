package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.employee.EmployeeNotFoundException;
import com.dental.clinic.management.exception.validation.InvalidRequestException;
import com.dental.clinic.management.exception.time_off.TimeOffTypeNotFoundException;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.working_schedule.domain.EmployeeLeaveBalance;
import com.dental.clinic.management.working_schedule.domain.LeaveBalanceHistory;
import com.dental.clinic.management.working_schedule.domain.TimeOffType;
import com.dental.clinic.management.working_schedule.dto.request.AdjustLeaveBalanceRequest;
import com.dental.clinic.management.working_schedule.dto.request.AnnualResetRequest;
import com.dental.clinic.management.working_schedule.dto.response.AllEmployeesLeaveBalanceResponse;
import com.dental.clinic.management.working_schedule.dto.response.EmployeeLeaveBalanceResponse;
import com.dental.clinic.management.working_schedule.dto.response.LeaveBalanceDetailResponse;
import com.dental.clinic.management.working_schedule.dto.response.TimeOffTypeInfoResponse;
import com.dental.clinic.management.working_schedule.enums.BalanceChangeReason;
import com.dental.clinic.management.working_schedule.repository.EmployeeLeaveBalanceRepository;
import com.dental.clinic.management.working_schedule.repository.LeaveBalanceHistoryRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Admin Leave Balance Management (P5.2)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminLeaveBalanceService {

        private final EmployeeLeaveBalanceRepository balanceRepository;
        private final LeaveBalanceHistoryRepository historyRepository;
        private final EmployeeRepository employeeRepository;
        private final TimeOffTypeRepository timeOffTypeRepository;
        private final AccountRepository accountRepository;

        /**
         * Get leave balances for an employee in a specific year
         */
        public EmployeeLeaveBalanceResponse getEmployeeLeaveBalances(Integer employeeId, Integer cycleYear) {
                log.debug("Getting leave balances for employee {} in year {}", employeeId, cycleYear);

                // Kiểm tra nhân viên có tồn tại không
                employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

                // Lấy tất cả số dư phép cho nhân viên này trong năm
                List<EmployeeLeaveBalance> balances = balanceRepository.findByEmployeeIdAndYear(employeeId, cycleYear);

                // Chuyển đổi sang response DTOs
                List<LeaveBalanceDetailResponse> balanceDetails = balances.stream()
                                .map(this::toBalanceDetailResponse)
                                .collect(Collectors.toList());

                return EmployeeLeaveBalanceResponse.builder()
                                .employeeId(employeeId)
                                .cycleYear(cycleYear)
                                .balances(balanceDetails)
                                .build();
        }

        /**
         * Get leave balances for ALL active employees (Admin Dashboard)
         * Uses optimized JOIN query to avoid N+1 problem
         */
        public AllEmployeesLeaveBalanceResponse getAllEmployeesLeaveBalances(Integer cycleYear, String timeOffTypeId) {
                log.debug("Getting leave balances for all employees in year {} for type {}", cycleYear, timeOffTypeId);

                // 1. Lấy tất cả nhân viên đang làm việc
                List<Integer> activeEmployeeIds = employeeRepository.findAllActiveEmployeeIds();

                // 2. Lấy số dư phép với bộ lọc tùy chọn
                List<EmployeeLeaveBalance> allBalances;
                if (timeOffTypeId != null) {
                        // Lọc theo loại nghỉ phép cụ thể
                        allBalances = balanceRepository.findByYearAndTimeOffTypeId(cycleYear, timeOffTypeId);
                } else {
                        // Lấy tất cả loại
                        allBalances = balanceRepository.findByYear(cycleYear);
                }

                // 3. Nhóm số dư phép theo employee_id
                Map<Integer, List<EmployeeLeaveBalance>> balancesByEmployee = allBalances.stream()
                                .collect(Collectors.groupingBy(EmployeeLeaveBalance::getEmployeeId));

                // 4. Tạo response cho từng nhân viên
                List<AllEmployeesLeaveBalanceResponse.EmployeeBalanceSummary> summaries = activeEmployeeIds.stream()
                                .map(empId -> {
                                        // Lấy thông tin nhân viên
                                        String employeeName = employeeRepository.findById(empId)
                                                        .map(emp -> emp.getFirstName() + " " + emp.getLastName())
                                                        .orElse("Unknown");

                                        // Lấy số dư phép của nhân viên này
                                        List<EmployeeLeaveBalance> empBalances = balancesByEmployee.getOrDefault(empId,
                                                        List.of());

                                        // Chuyển đổi sang balance info
                                        List<AllEmployeesLeaveBalanceResponse.BalanceInfo> balanceInfos = empBalances
                                                        .stream()
                                                        .map(balance -> {
                                                                String typeName = timeOffTypeRepository
                                                                                .findById(balance.getTimeOffTypeId())
                                                                                .map(TimeOffType::getTypeName)
                                                                                .orElse("Unknown Type");

                                                                return AllEmployeesLeaveBalanceResponse.BalanceInfo
                                                                                .builder()
                                                                                .timeOffTypeName(typeName)
                                                                                .totalDaysAllowed(balance
                                                                                                .getTotalAllotted())
                                                                                .daysTaken(balance.getUsed())
                                                                                .daysRemaining(balance.getRemaining())
                                                                                .build();
                                                        })
                                                        .collect(Collectors.toList());

                                        return AllEmployeesLeaveBalanceResponse.EmployeeBalanceSummary.builder()
                                                        .employeeId(empId)
                                                        .employeeName(employeeName)
                                                        .balances(balanceInfos)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // 5. Tạo response cuối cùng
                AllEmployeesLeaveBalanceResponse.FilterInfo filterInfo = AllEmployeesLeaveBalanceResponse.FilterInfo
                                .builder()
                                .cycleYear(cycleYear)
                                .timeOffTypeId(timeOffTypeId)
                                .build();

                return AllEmployeesLeaveBalanceResponse.builder()
                                .filter(filterInfo)
                                .data(summaries)
                                .build();
        }

        /**
         * Manually adjust leave balance for an employee
         */
        @Transactional
        public void adjustLeaveBalance(AdjustLeaveBalanceRequest request) {
                log.debug("Adjusting leave balance for employee {} type {} year {}: {} days",
                                request.getEmployeeId(), request.getTimeOffTypeId(),
                                request.getCycleYear(), request.getChangeAmount());

                // 1. Kiểm tra nhân viên có tồn tại không
                employeeRepository.findById(request.getEmployeeId())
                                .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

                // 2. Kiểm tra loại nghỉ phép có tồn tại không
                timeOffTypeRepository.findById(request.getTimeOffTypeId())
                                .orElseThrow(() -> new TimeOffTypeNotFoundException(request.getTimeOffTypeId()));

                // 3. Tìm hoặc tạo bản ghi số dư phép
                EmployeeLeaveBalance balance;
                try {
                        balance = balanceRepository
                                        .findByEmployeeIdAndTimeOffTypeIdAndYear(
                                                        request.getEmployeeId(),
                                                        request.getTimeOffTypeId(),
                                                        request.getCycleYear())
                                        .orElseGet(() -> {
                                                log.info("Balance not found, creating new balance record for employee {} type {} year {}",
                                                                request.getEmployeeId(), request.getTimeOffTypeId(),
                                                                request.getCycleYear());
                                                return createNewBalance(request.getEmployeeId(),
                                                                request.getTimeOffTypeId(), request.getCycleYear());
                                        });
                } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
                        throw new InvalidRequestException(
                                        "DUPLICATE_BALANCE_RECORDS",
                                        String.format("Phát hiện dữ liệu bị trùng lặp cho nhân viên %d loại %s năm %d. Vui lòng liên hệ quản trị viên để xử lý.",
                                                        request.getEmployeeId(), request.getTimeOffTypeId(),
                                                        request.getCycleYear()));
                }

                // 4. Áp dụng điều chỉnh
                Double oldTotalAllotted = balance.getTotalAllotted();
                Double oldUsed = balance.getUsed();

                if (request.getChangeAmount() > 0) {
                        // Dương: Thêm vào tổng số ngày được phép
                        balance.setTotalAllotted(balance.getTotalAllotted() + request.getChangeAmount());
                        log.info("Adding {} days to total_allotted: {} -> {}",
                                        request.getChangeAmount(), oldTotalAllotted, balance.getTotalAllotted());
                } else {
                        // Âm: Thêm vào số ngày đã dùng (trừ khỏi số còn lại)
                        balance.setUsed(balance.getUsed() + Math.abs(request.getChangeAmount()));
                        log.info("Adding {} days to used: {} -> {}",
                                        Math.abs(request.getChangeAmount()), oldUsed, balance.getUsed());
                }

                // 5. Kiểm tra: số còn lại không được âm
                Double remaining = balance.getTotalAllotted() - balance.getUsed();
                if (remaining < 0) {
                        throw new InvalidRequestException(
                                        "INVALID_BALANCE",
                                        String.format("Số dư phép không thể âm sau khi điều chỉnh. " +
                                                        "Total allowed: %.1f, Used: %.1f, Remaining: %.1f",
                                                        balance.getTotalAllotted(), balance.getUsed(), remaining));
                }

                // 6. Lưu số dư phép
                balanceRepository.save(balance);

                // 7. Lấy người dùng hiện tại (admin thực hiện thay đổi)
                String username = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new RuntimeException("Người dùng chưa xác thực"));

                Integer changedBy = accountRepository.findOneByUsername(username)
                                .map(account -> account.getEmployee().getEmployeeId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với người dùng: " + username));

                // 8. Tạo bản ghi lịch sử
                LeaveBalanceHistory history = LeaveBalanceHistory.builder()
                                .balance(balance) // Set the relationship, not just the ID
                                .changedBy(changedBy)
                                .changeAmount(request.getChangeAmount())
                                .reason(BalanceChangeReason.MANUAL_ADJUSTMENT)
                                .notes(request.getNotes() != null ? request.getNotes() : "Điều chỉnh thủ công")
                                .build();

                historyRepository.save(history);

                log.info("Adjustment completed. Balance ID: {}, New total: {}, New used: {}, Remaining: {}",
                                balance.getBalanceId(), balance.getTotalAllotted(), balance.getUsed(), remaining);
        }

        /**
         * Annual leave balance reset for all active employees
         */
        @Transactional
        public Map<String, Object> annualReset(AnnualResetRequest request) {
                log.info("Starting annual reset for year {} type {} with {} days",
                                request.getCycleYear(), request.getApplyToTypeId(), request.getDefaultAllowance());

                // 1. Kiểm tra năm có hợp lý không (cho phép năm hiện tại và 2 năm tiếp theo)
                int currentYear = Year.now().getValue();
                if (request.getCycleYear() < currentYear - 1 || request.getCycleYear() > currentYear + 2) {
                        throw new InvalidRequestException(
                                        "INVALID_YEAR",
                                        String.format("Năm reset không hợp lệ: %d. Chỉ cho phép từ %d đến %d",
                                                        request.getCycleYear(), currentYear - 1, currentYear + 2));
                }

                // 2. Kiểm tra loại nghỉ phép có tồn tại không
                TimeOffType timeOffType = timeOffTypeRepository.findById(request.getApplyToTypeId())
                                .orElseThrow(() -> new TimeOffTypeNotFoundException(request.getApplyToTypeId()));

                // 3. Lấy tất cả nhân viên đang làm việc
                List<Integer> activeEmployeeIds = employeeRepository.findAllActiveEmployeeIds();
                log.info("Found {} active employees", activeEmployeeIds.size());

                int createdCount = 0;
                int updatedCount = 0;
                int skippedCount = 0;

                // 4. Với mỗi nhân viên, tạo hoặc reset số dư phép
                for (Integer employeeId : activeEmployeeIds) {
                        try {
                                Optional<EmployeeLeaveBalance> existingBalanceOpt;
                                try {
                                        existingBalanceOpt = balanceRepository
                                                        .findByEmployeeIdAndTimeOffTypeIdAndYear(
                                                                        employeeId,
                                                                        request.getApplyToTypeId(),
                                                                        request.getCycleYear());
                                } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
                                        log.error("Duplicate balance records found for employee {} type {} year {}, skipping",
                                                        employeeId, request.getApplyToTypeId(), request.getCycleYear());
                                        skippedCount++;
                                        continue;
                                }

                                EmployeeLeaveBalance balance;
                                boolean isUpdate = false;

                                if (existingBalanceOpt.isPresent()) {
                                        // Balance exists - RESET it
                                        balance = existingBalanceOpt.get();
                                        double oldAllowed = balance.getTotalAllotted();
                                        double oldUsed = balance.getUsed();

                                        balance.setTotalAllotted(request.getDefaultAllowance());
                                        balance.setUsed(0.0);
                                        balance.setUpdatedAt(LocalDateTime.now());

                                        balance = balanceRepository.save(balance);
                                        isUpdate = true;

                                        log.debug("Reset balance for employee {}: {} days (was {}/{} used/allowed)",
                                                        employeeId, request.getDefaultAllowance(), oldUsed, oldAllowed);
                                } else {
                                        // Balance doesn't exist - CREATE new
                                        // Load Employee entity (required because employeeId is insertable=false)
                                        var employee = employeeRepository.findById(employeeId)
                                                        .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

                                        balance = EmployeeLeaveBalance.builder()
                                                        .employee(employee) // Set Employee entity, not employeeId
                                                        .timeOffType(timeOffType) // Set TimeOffType entity, not
                                                                                  // timeOffTypeId
                                                        .year(request.getCycleYear())
                                                        .totalAllotted(request.getDefaultAllowance())
                                                        .used(0.0)
                                                        .build();

                                        balance = balanceRepository.save(balance);

                                        log.debug("Created new balance for employee {} with {} days",
                                                        employeeId, request.getDefaultAllowance());
                                }

                                // Create history record
                                LeaveBalanceHistory history = LeaveBalanceHistory.builder()
                                                .balance(balance) // Set the relationship, not just the ID
                                                .changedBy(1) // System action (admin user ID)
                                                .changeAmount(request.getDefaultAllowance())
                                                .reason(BalanceChangeReason.ANNUAL_RESET)
                                                .notes(String.format("%s %.1f ngày nghỉ phép %s cho năm %d",
                                                                isUpdate ? "Reset về" : "Cấp",
                                                                request.getDefaultAllowance(),
                                                                timeOffType.getTypeName(),
                                                                request.getCycleYear()))
                                                .build();

                                historyRepository.save(history);

                                if (isUpdate) {
                                        updatedCount++;
                                } else {
                                        createdCount++;
                                }

                        } catch (Exception e) {
                                log.error("Failed to process balance for employee {}: {} - {}",
                                                employeeId, e.getClass().getSimpleName(), e.getMessage(), e);
                                skippedCount++;
                                // Re-throw to see full error in response (for debugging)
                                throw new RuntimeException("Không thể xử lý nhân viên " + employeeId, e);
                        }
                }

                log.info("Annual reset completed: {} created, {} updated, {} skipped",
                                createdCount, updatedCount, skippedCount);

                Map<String, Object> result = new HashMap<>();
                result.put("message", "Annual reset hoàn tất");
                result.put("cycle_year", request.getCycleYear());
                result.put("time_off_type_id", request.getApplyToTypeId());
                result.put("default_allowance", request.getDefaultAllowance());
                result.put("total_employees", activeEmployeeIds.size());
                result.put("created_count", createdCount);
                result.put("updated_count", updatedCount);
                result.put("skipped_count", skippedCount);

                return result;
        }

        /**
         * Helper: Create a new empty balance record
         */
        private EmployeeLeaveBalance createNewBalance(Integer employeeId, String timeOffTypeId, Integer year) {
                // Load Employee and TimeOffType entities (required because fields are
                // insertable=false)
                var employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
                var timeOffType = timeOffTypeRepository.findById(timeOffTypeId)
                                .orElseThrow(() -> new TimeOffTypeNotFoundException(timeOffTypeId));

                EmployeeLeaveBalance balance = EmployeeLeaveBalance.builder()
                                .employee(employee) // Set Employee entity, not employeeId
                                .timeOffType(timeOffType) // Set TimeOffType entity, not timeOffTypeId
                                .year(year)
                                .totalAllotted(0.0)
                                .used(0.0)
                                .build();

                return balanceRepository.save(balance);
        }

        /**
         * Helper: Convert EmployeeLeaveBalance to LeaveBalanceDetailResponse
         */
        private LeaveBalanceDetailResponse toBalanceDetailResponse(EmployeeLeaveBalance balance) {
                // Get time-off type info
                TimeOffType timeOffType = timeOffTypeRepository.findById(balance.getTimeOffTypeId())
                                .orElse(null);

                TimeOffTypeInfoResponse typeInfo = null;
                if (timeOffType != null) {
                        typeInfo = TimeOffTypeInfoResponse.builder()
                                        .typeId(timeOffType.getTypeId())
                                        .typeName(timeOffType.getTypeName())
                                        .isPaid(timeOffType.getIsPaid())
                                        .build();
                }

                return LeaveBalanceDetailResponse.builder()
                                .balanceId(balance.getBalanceId())
                                .timeOffType(typeInfo)
                                .totalDaysAllowed(balance.getTotalAllotted())
                                .daysTaken(balance.getUsed())
                                .daysRemaining(balance.getRemaining())
                                .build();
        }
}
