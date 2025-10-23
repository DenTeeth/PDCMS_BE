package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.enums.EmploymentType;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.enums.ShiftSource;
import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.working_schedule.repository.HolidayDateRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job 1: Auto-create monthly schedule for full-time employees.
 *
 * Runs on the 20th of every month.
 * Creates shifts for the next month for all full-time employees.
 * Skips weekends (Saturday, Sunday) and holidays.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MonthlyFullTimeScheduleJob {

    private final EmployeeRepository employeeRepository;
    private final EmployeeShiftRepository shiftRepository;
    private final WorkShiftRepository workShiftRepository;
    private final HolidayDateRepository holidayRepository;

    /**
     * Cron: 0 0 2 20 * ?
     * - Runs at 02:00 AM on the 20th day of every month
     * - Format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 2 20 * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void createNextMonthSchedule() {
        log.info("=== Starting Monthly Full-Time Schedule Job ===");

        LocalDate today = LocalDate.now();
        YearMonth nextMonth = YearMonth.from(today).plusMonths(1);

        log.info("Creating schedule for: {}", nextMonth);

        try {
            // VALIDATION: Check if work shifts exist
            long workShiftCount = workShiftRepository.count();
            if (workShiftCount == 0) {
                log.error("No work shifts found in database. Cannot create schedule.");
                return;
            }
            log.info("Validation passed: {} work shifts available", workShiftCount);

            // 1. Get all active full-time employees
            List<Employee> fullTimeEmployees = employeeRepository
                    .findByEmploymentTypeAndIsActive(EmploymentType.FULL_TIME, true);

            log.info("Found {} active full-time employees", fullTimeEmployees.size());

            if (fullTimeEmployees.isEmpty()) {
                log.info("No full-time employees found. Job completed with no actions.");
                return;
            }

            // VALIDATION: Get and validate required shifts
            WorkShift morningShift = workShiftRepository.findById("SLOT_MORNING")
                    .orElse(null);
            WorkShift afternoonShift = workShiftRepository.findById("SLOT_AFTERNOON")
                    .orElse(null);

            if (morningShift == null || afternoonShift == null) {
                log.error("Required shifts not found. Morning: {}, Afternoon: {}",
                        morningShift != null, afternoonShift != null);
                throw new RuntimeException("SLOT_MORNING or SLOT_AFTERNOON not found in database");
            }

            log.info("Validation passed: Morning and Afternoon shifts found");

            // 3. Get holidays for next month
            LocalDate monthStart = nextMonth.atDay(1);
            LocalDate monthEnd = nextMonth.atEndOfMonth();
            Set<LocalDate> holidays = holidayRepository.findHolidayDatesByRange(monthStart, monthEnd)
                    .stream()
                    .collect(Collectors.toSet());

            log.info("Found {} holidays in {}", holidays.size(), nextMonth);

            // 4. Create shifts for each employee
            int totalShiftsCreated = 0;
            int skippedDueToErrors = 0;
            List<EmployeeShift> shiftsToSave = new ArrayList<>();

            for (Employee employee : fullTimeEmployees) {
                try {
                    // VALIDATION: Check if employee has valid ID
                    if (employee.getEmployeeId() == null) {
                        log.warn("Employee has null ID. Skipping.");
                        skippedDueToErrors++;
                        continue;
                    }

                    int employeeShifts = 0;

                    // Loop through all days of next month
                    for (int day = 1; day <= nextMonth.lengthOfMonth(); day++) {
                        LocalDate workDate = nextMonth.atDay(day);

                        // Skip weekends
                        DayOfWeek dayOfWeek = workDate.getDayOfWeek();
                        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                            continue;
                        }

                        // Skip holidays
                        if (holidays.contains(workDate)) {
                            continue;
                        }

                        // VALIDATION: Create morning shift (check for duplicates)
                        if (!shiftRepository.existsByEmployeeAndDateAndShift(
                                employee.getEmployeeId(), workDate, morningShift.getWorkShiftId())) {

                            EmployeeShift morningShiftEntity = new EmployeeShift();
                            morningShiftEntity.setEmployee(employee);
                            morningShiftEntity.setWorkDate(workDate);
                            morningShiftEntity.setWorkShift(morningShift);
                            morningShiftEntity.setSource(ShiftSource.BATCH_JOB);
                            morningShiftEntity.setStatus(ShiftStatus.SCHEDULED);

                            shiftsToSave.add(morningShiftEntity);
                            employeeShifts++;
                        }

                        // VALIDATION: Create afternoon shift (check for duplicates)
                        if (!shiftRepository.existsByEmployeeAndDateAndShift(
                                employee.getEmployeeId(), workDate, afternoonShift.getWorkShiftId())) {

                            EmployeeShift afternoonShiftEntity = new EmployeeShift();
                            afternoonShiftEntity.setEmployee(employee);
                            afternoonShiftEntity.setWorkDate(workDate);
                            afternoonShiftEntity.setWorkShift(afternoonShift);
                            afternoonShiftEntity.setSource(ShiftSource.BATCH_JOB);
                            afternoonShiftEntity.setStatus(ShiftStatus.SCHEDULED);

                            shiftsToSave.add(afternoonShiftEntity);
                            employeeShifts++;
                        }
                    }

                    if (employeeShifts > 0) {
                        log.info("Prepared {} shifts for employee {} ({})",
                                employeeShifts, employee.getEmployeeId(), employee.getFullName());
                        totalShiftsCreated += employeeShifts;
                    }

                } catch (Exception e) {
                    log.error("Error processing employee {}: {}",
                            employee.getEmployeeId(), e.getMessage(), e);
                    skippedDueToErrors++;
                }
            }

            // 5. Batch save all shifts
            if (!shiftsToSave.isEmpty()) {
                shiftRepository.saveAll(shiftsToSave);
                log.info("Successfully saved {} shifts to database", shiftsToSave.size());
            }

            log.info("=== Monthly Full-Time Schedule Job Completed ===");
            log.info("Total shifts created: {}", totalShiftsCreated);
            log.info("Employees processed: {}", fullTimeEmployees.size());
            log.info("Employees skipped due to errors: {}", skippedDueToErrors);

        } catch (Exception e) {
            log.error("Error in Monthly Full-Time Schedule Job", e);
            throw new RuntimeException("Failed to create monthly schedule", e);
        }
    }
}
