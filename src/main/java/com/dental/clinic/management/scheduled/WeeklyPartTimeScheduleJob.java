package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.working_schedule.domain.RegistrationDays;
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
            // 1. Get all active registrations
            List<EmployeeShiftRegistration> activeRegistrations = registrationRepository
                    .findActiveRegistrations(today);

            log.info("Found {} active part-time registrations", activeRegistrations.size());

            // 2. Get holidays for next week
            Set<LocalDate> holidays = holidayRepository.findHolidayDatesByRange(nextMonday, nextSunday)
                    .stream()
                    .collect(Collectors.toSet());

            log.info("Found {} holidays in next week", holidays.size());

            // 3. Create shifts for each registration
            int totalShiftsCreated = 0;
            List<EmployeeShift> shiftsToSave = new ArrayList<>();

            for (EmployeeShiftRegistration registration : activeRegistrations) {
                // Get the work shift
                WorkShift workShift = workShiftRepository.findById(registration.getSlotId())
                        .orElse(null);

                if (workShift == null) {
                    log.warn("Work shift {} not found for registration {}",
                            registration.getSlotId(), registration.getRegistrationId());
                    continue;
                }

                // Get registered days of week
                Set<DayOfWeek> registeredDays = registration.getRegistrationDays().stream()
                        .map(rd -> rd.getId().getDayOfWeek())
                        .collect(Collectors.toSet());

                log.debug("Registration {} works on: {}",
                        registration.getRegistrationId(), registeredDays);

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
                        log.debug("Skipping holiday: {}", workDate);
                        continue;
                    }

                    // Check if shift already exists
                    if (shiftRepository.existsByEmployeeAndDateAndShift(
                            registration.getEmployeeId(), workDate, workShift.getWorkShiftId())) {
                        continue;
                    }

                    // Create shift
                    EmployeeShift shift = new EmployeeShift();
                    shift.setEmployee(registration.getEmployeeId() != null
                            ? new com.dental.clinic.management.employee.domain.Employee()
                            : null);
                    if (shift.getEmployee() != null) {
                        shift.getEmployee().setEmployeeId(registration.getEmployeeId());
                    }
                    shift.setWorkDate(workDate);
                    shift.setWorkShift(workShift);
                    shift.setSource(ShiftSource.REGISTRATION_JOB);
                    shift.setRegistration(registration);
                    shift.setStatus(ShiftStatus.SCHEDULED);

                    shiftsToSave.add(shift);
                    registrationShifts++;
                }

                if (registrationShifts > 0) {
                    log.info("Created {} shifts for registration {} (Employee ID: {})",
                            registrationShifts, registration.getRegistrationId(), registration.getEmployeeId());
                    totalShiftsCreated += registrationShifts;
                }
            }

            // 4. Batch save all shifts
            if (!shiftsToSave.isEmpty()) {
                shiftRepository.saveAll(shiftsToSave);
                log.info("Successfully saved {} shifts to database", shiftsToSave.size());
            }

            log.info("=== Weekly Part-Time Schedule Job Completed ===");
            log.info("Total shifts created: {}", totalShiftsCreated);

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
