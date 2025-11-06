package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.utils.IdGenerator;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.working_schedule.domain.PartTimeSlot;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.enums.DayOfWeek;
import com.dental.clinic.management.working_schedule.enums.ShiftSource;
import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.working_schedule.repository.HolidayDateRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeSlotRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job 2: Auto-create weekly schedule for part-time employees.
 *
 * ⚠️ DEPRECATED: Replaced by UnifiedScheduleSyncJob (P8)
 *
 * This job has been superseded by the new unified sync job that handles
 * BOTH Fixed and Flex schedules in a single daily run.
 *
 * Kept for reference only. Do NOT enable this job.
 *
 * @deprecated Use {@link UnifiedScheduleSyncJob} instead
 */
@Deprecated
// @Component // DISABLED - replaced by UnifiedScheduleSyncJob
@Slf4j
@RequiredArgsConstructor
public class WeeklyPartTimeScheduleJob {

    private final EmployeeShiftRegistrationRepository registrationRepository;
    private final EmployeeShiftRepository shiftRepository;
    private final WorkShiftRepository workShiftRepository;
    private final PartTimeSlotRepository partTimeSlotRepository;
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

                    // V2: Get work shift from part_time_slot
                    if (registration.getPartTimeSlotId() == null) {
                        log.warn("Registration {} has no part_time_slot_id. Skipping.",
                                registration.getRegistrationId());
                        skippedDueToErrors++;
                        continue;
                    }

                    PartTimeSlot slot = partTimeSlotRepository.findById(registration.getPartTimeSlotId())
                            .orElse(null);

                    if (slot == null) {
                        log.warn("Part-time slot {} not found for registration {}. Skipping.",
                                registration.getPartTimeSlotId(), registration.getRegistrationId());
                        skippedDueToErrors++;
                        continue;
                    }

                    WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId())
                            .orElse(null);

                    if (workShift == null) {
                        log.warn("Work shift {} not found for slot {}. Skipping.",
                                slot.getWorkShiftId(), slot.getSlotId());
                        skippedDueToErrors++;
                        continue;
                    }

                    // V2: Each registration is for ONE slot (one day), not multiple days
                    String slotDayStr = slot.getDayOfWeek();

                    if (slotDayStr == null || slotDayStr.isEmpty()) {
                        log.warn("Slot {} has no day configured. Skipping.",
                                slot.getSlotId());
                        skippedDueToErrors++;
                        continue;
                    }

                    DayOfWeek registeredDay;
                    try {
                        registeredDay = DayOfWeek.valueOf(slotDayStr); // Convert String to enum
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid day value '{}' in slot {}. Skipping.",
                                slotDayStr, slot.getSlotId());
                        skippedDueToErrors++;
                        continue;
                    }

                    log.debug("Registration {} - Employee {} works on: {}",
                            registration.getRegistrationId(), employee.getEmployeeId(), registeredDay);

                    // V2: Calculate exact work date for this day in next week (only 1 occurrence)
                    LocalDate workDate = null;
                    for (int i = 0; i < 7; i++) {
                        LocalDate candidateDate = nextMonday.plusDays(i);
                        DayOfWeek dayOfWeek = mapJavaDayToCustomDay(candidateDate.getDayOfWeek());

                        if (registeredDay.equals(dayOfWeek)) {
                            workDate = candidateDate;
                            break; // Found the matching day
                        }
                    }

                    if (workDate == null) {
                        log.warn("Could not find matching day {} in next week. Skipping registration {}",
                                registeredDay, registration.getRegistrationId());
                        skippedDueToErrors++;
                        continue;
                    }

                    // Skip if it's a holiday
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

                    log.info("Prepared 1 shift for employee {} on {} (Registration: {})",
                            employee.getEmployeeId(), workDate, registration.getRegistrationId());
                    totalShiftsCreated++;

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
