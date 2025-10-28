package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.utils.IdGenerator;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.enums.DayOfWeek;
import com.dental.clinic.management.working_schedule.enums.ShiftSource;
import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.working_schedule.repository.HolidayDateRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job 2: Auto-create weekly schedule for part-time employees.
 *
 * Runs every Sunday at 01:00 AM.
 * Creates shifts for the next week based on active registrations.
 * Skips holidays.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WeeklyPartTimeScheduleJob {

    private final EmployeeShiftRegistrationRepository registrationRepository;
    private final EmployeeShiftRepository shiftRepository;
    private final WorkShiftRepository workShiftRepository;
    private final HolidayDateRepository holidayRepository;
    private final EmployeeRepository employeeRepository;
    private final IdGenerator idGenerator;

    /**
     * Cron: 0 0 1 ? * SUN
     * - Runs at 01:00 AM every Sunday
     * - Format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 1 ? * SUN", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void createNextWeekSchedule() {
        log.info("=== Starting Weekly Part-Time Schedule Job ===");

        LocalDate today = LocalDate.now();
        LocalDate nextMonday = today.plusDays(1); // Tomorrow (Monday)
        LocalDate nextSunday = nextMonday.plusDays(6); // End of next week

        log.info("Creating schedule for: {} to {}", nextMonday, nextSunday);

        try {
            // VALIDATION: Check if work shifts exist
            long workShiftCount = workShiftRepository.count();
            if (workShiftCount == 0) {
                log.error("No work shifts found in database. Cannot create schedule.");
                return;
            }
            log.info("Validation passed: {} work shifts available", workShiftCount);

            // 1. Get all active registrations
            List<EmployeeShiftRegistration> activeRegistrations = registrationRepository
                    .findActiveRegistrations(today);

            log.info("Found {} active part-time registrations", activeRegistrations.size());

            if (activeRegistrations.isEmpty()) {
                log.info("No active registrations found. Job completed with no actions.");
                return;
            }

            // 2. Get holidays for next week
            Set<LocalDate> holidays = holidayRepository.findHolidayDatesByRange(nextMonday, nextSunday)
                    .stream()
                    .collect(Collectors.toSet());

            log.info("Found {} holidays in next week", holidays.size());

            // 3. Create shifts for each registration
            int totalShiftsCreated = 0;
            int skippedDueToErrors = 0;
            List<EmployeeShift> shiftsToSave = new ArrayList<>();

            for (EmployeeShiftRegistration registration : activeRegistrations) {
                try {
                    // VALIDATION: Check if employee exists
                    Employee employee = employeeRepository.findById(registration.getEmployeeId())
                            .orElse(null);

                    if (employee == null) {
                        log.warn("Employee {} not found for registration {}. Skipping.",
                                registration.getEmployeeId(), registration.getRegistrationId());
                        skippedDueToErrors++;
                        continue;
                    }

                    // VALIDATION: Get and validate work shift
                    WorkShift workShift = workShiftRepository.findById(registration.getWorkShiftId())
                            .orElse(null);

                    if (workShift == null) {
                        log.warn("Work shift {} not found for registration {}. Skipping.",
                                registration.getWorkShiftId(), registration.getRegistrationId());
                        skippedDueToErrors++;
                        continue;
                    }

                    // VALIDATION: Check if registration has days configured
                    Set<DayOfWeek> registeredDays = registration.getRegistrationDays().stream()
                            .map(rd -> rd.getId().getDayOfWeek())
                            .collect(Collectors.toSet());

                    if (registeredDays.isEmpty()) {
                        log.warn("Registration {} has no days configured. Skipping.",
                                registration.getRegistrationId());
                        skippedDueToErrors++;
                        continue;
                    }

                    log.debug("Registration {} - Employee {} works on: {}",
                            registration.getRegistrationId(), employee.getEmployeeId(), registeredDays);

                    // Create shifts for each registered day in next week
                    int registrationShifts = 0;
                    for (int i = 0; i < 7; i++) {
                        LocalDate workDate = nextMonday.plusDays(i);
                        DayOfWeek dayOfWeek = mapJavaDayToCustomDay(workDate.getDayOfWeek());

                        // Check if employee works on this day
                        if (!registeredDays.contains(dayOfWeek)) {
                            continue;
                        }

                        // Skip holidays
                        if (holidays.contains(workDate)) {
                            log.debug("Skipping holiday: {} for employee {}", workDate, employee.getEmployeeId());
                            continue;
                        }

                        // VALIDATION: Check if shift already exists (avoid duplicates)
                        if (shiftRepository.existsByEmployeeAndDateAndShift(
                                employee.getEmployeeId(), workDate, workShift.getWorkShiftId())) {
                            log.debug("Shift already exists for employee {} on {} shift {}. Skipping.",
                                    employee.getEmployeeId(), workDate, workShift.getWorkShiftId());
                            continue;
                        }

                        // Generate unique ID with format EMSyyMMddSEQ (e.g., EMS251029001)
                        String employeeShiftId = idGenerator.generateId("EMS");

                        // Create shift
                        EmployeeShift shift = new EmployeeShift();
                        shift.setEmployeeShiftId(employeeShiftId); // Set generated ID
                        shift.setEmployee(employee);
                        shift.setWorkDate(workDate);
                        shift.setWorkShift(workShift);
                        shift.setSource(ShiftSource.REGISTRATION_JOB);
                        shift.setStatus(ShiftStatus.SCHEDULED);
                        shift.setIsOvertime(false); // Regular shift, not overtime
                        // Note: sourceOffRequestId would be set here if this was from time-off renewal
                        shift.setNotes(String.format("Tạo tự động từ đăng ký %s", registration.getRegistrationId()));

                        shiftsToSave.add(shift);
                        registrationShifts++;
                    }

                    if (registrationShifts > 0) {
                        log.info("Prepared {} shifts for employee {} (Registration: {})",
                                registrationShifts, employee.getEmployeeId(), registration.getRegistrationId());
                        totalShiftsCreated += registrationShifts;
                    }

                } catch (Exception e) {
                    log.error("Error processing registration {}: {}",
                            registration.getRegistrationId(), e.getMessage(), e);
                    skippedDueToErrors++;
                }
            }

            // 4. Batch save all shifts
            if (!shiftsToSave.isEmpty()) {
                shiftRepository.saveAll(shiftsToSave);
                log.info("Successfully saved {} shifts to database", shiftsToSave.size());
            }

            log.info("=== Weekly Part-Time Schedule Job Completed ===");
            log.info("Total shifts created: {}", totalShiftsCreated);
            log.info("Registrations processed: {}", activeRegistrations.size());
            log.info("Registrations skipped due to errors: {}", skippedDueToErrors);

        } catch (Exception e) {
            log.error("Error in Weekly Part-Time Schedule Job", e);
            throw new RuntimeException("Failed to create weekly schedule", e);
        }
    }

    /**
     * Map Java DayOfWeek to custom DayOfWeek enum.
     */
    private DayOfWeek mapJavaDayToCustomDay(java.time.DayOfWeek javaDay) {
        switch (javaDay) {
            case MONDAY:
                return DayOfWeek.MONDAY;
            case TUESDAY:
                return DayOfWeek.TUESDAY;
            case WEDNESDAY:
                return DayOfWeek.WEDNESDAY;
            case THURSDAY:
                return DayOfWeek.THURSDAY;
            case FRIDAY:
                return DayOfWeek.FRIDAY;
            case SATURDAY:
                return DayOfWeek.SATURDAY;
            case SUNDAY:
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException("Unknown day: " + javaDay);
        }
    }
}
