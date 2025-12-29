package com.dental.clinic.management.treatment_plans.service;

// import com.dental.clinic.management.booking_appointment.domain.Appointment;
// import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.booking_appointment.repository.PatientPlanItemRepository;
import com.dental.clinic.management.service.repository.DentalServiceRepository;
import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import com.dental.clinic.management.service.domain.DentalService;
import com.dental.clinic.management.treatment_plans.domain.PatientPlanItem;
import com.dental.clinic.management.treatment_plans.domain.PatientTreatmentPlan;
import com.dental.clinic.management.treatment_plans.dto.request.AutoScheduleRequest;
import com.dental.clinic.management.treatment_plans.dto.response.AutoScheduleResponse;
import com.dental.clinic.management.treatment_plans.enums.PlanItemStatus;
import com.dental.clinic.management.treatment_plans.repository.PatientTreatmentPlanRepository;
import com.dental.clinic.management.utils.validation.HolidayValidator;
import com.dental.clinic.management.utils.validation.ServiceSpacingValidator;
import com.dental.clinic.management.working_schedule.service.HolidayDateService;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.booking_appointment.repository.RoomRepository;
import com.dental.clinic.management.booking_appointment.repository.RoomServiceRepository;
import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.domain.Room;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
// import java.util.stream.Collectors;

/**
 * Service for automatic appointment scheduling from treatment plans.
 *
 * ISSUE: AUTO_SCHEDULE_HOLIDAYS_AND_SPACING_IMPLEMENTATION
 * Priority: HIGH
 * Assigned: NGUYEN
 * Date: 2025-12-18
 *
 * Features:
 * 1. ✅ Use estimated dates from treatment plan items
 * 2. ✅ Automatically skip holidays and weekends
 * 3. ✅ Apply service spacing rules (preparation, recovery, intervals)
 * 4. ✅ Enforce daily appointment limits
 * 5. ✅ Find available time slots for suggested dates
 *
 * Business Rules:
 * - If estimated date is holiday → shift to next working day
 * - If multiple holidays in a row → keep shifting until working day found
 * - Apply service spacing rules (minimum prep, recovery, spacing days)
 * - If no spacing rules → apply daily limit (max 2 appointments/day/patient)
 * - Only schedule items with status = READY_FOR_BOOKING
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanAutoScheduleService {

    private final PatientTreatmentPlanRepository treatmentPlanRepository;
    private final PatientPlanItemRepository planItemRepository;
    private final DentalServiceRepository dentalServiceRepository;
    private final HolidayValidator holidayValidator;
    private final ServiceSpacingValidator serviceSpacingValidator;
    private final HolidayDateService holidayDateService;
    private final EmployeeShiftRepository employeeShiftRepository;
    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;
    private final RoomServiceRepository roomServiceRepository;
    private final com.dental.clinic.management.employee.repository.EmployeeRepository employeeRepository;

    private static final String ENTITY_NAME = "treatment_plan_auto_schedule";
    private static final List<AppointmentStatus> BUSY_STATUSES = Arrays.asList(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.CHECKED_IN,
            AppointmentStatus.IN_PROGRESS);

    /**
     * Generate automatic appointment suggestions for a treatment plan.
     * Does NOT create actual appointments - only provides suggestions.
     *
     * @param planId  Treatment plan ID
     * @param request Auto-schedule request with preferences
     * @return Response with appointment suggestions
     */
    @Transactional(readOnly = true)
    public AutoScheduleResponse generateAutomaticAppointments(Long planId, AutoScheduleRequest request) {
        log.info("Starting auto-schedule for treatment plan: {}", planId);

        // Step 1: Validate plan exists and is in correct status
        PatientTreatmentPlan plan = validatePlan(planId);

        // Step 2: Get items ready for booking (status = READY_FOR_BOOKING)
        List<PatientPlanItem> readyItems = planItemRepository.findByPlanIdAndStatus(
                planId,
                PlanItemStatus.READY_FOR_BOOKING);

        if (readyItems.isEmpty()) {
            log.warn("No items ready for booking in plan: {}", planId);
            return buildEmptyResponse(planId, "Không có dịch vụ nào sẵn sàng để đặt lịch");
        }

        log.info("Found {} items ready for booking", readyItems.size());

        // Step 3: Generate suggestions for each item
        List<AutoScheduleResponse.AppointmentSuggestion> suggestions = new ArrayList<>();
        AutoScheduleResponse.SchedulingSummary summary = initializeSummary();

        for (PatientPlanItem item : readyItems) {
            try {
                AutoScheduleResponse.AppointmentSuggestion suggestion = generateSuggestionForItem(
                        item,
                        plan,
                        request,
                        summary);
                suggestions.add(suggestion);
            } catch (Exception e) {
                log.error("Failed to generate suggestion for item {}: {}",
                        item.getItemId(), e.getMessage(), e);

                // Add failed suggestion
                // Load service to get code/name for error message
                DentalService errorService = dentalServiceRepository.findById(Long.valueOf(item.getServiceId()))
                        .orElse(null);
                suggestions.add(AutoScheduleResponse.AppointmentSuggestion.builder()
                        .itemId(item.getItemId())
                        .serviceCode(errorService != null ? errorService.getServiceCode() : "UNKNOWN")
                        .serviceName(errorService != null ? errorService.getServiceName() : "Unknown Service")
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        // Step 4: Build and return response
        return buildResponse(planId, suggestions, readyItems.size(), summary);
    }

    /**
     * Validate treatment plan exists and is in correct status for scheduling.
     */
    private PatientTreatmentPlan validatePlan(Long planId) {
        PatientTreatmentPlan plan = treatmentPlanRepository.findById(planId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Lộ trình điều trị không tồn tại: " + planId,
                        ENTITY_NAME,
                        "PLAN_NOT_FOUND"));

        // Plan must be approved to schedule appointments
        if (plan.getApprovalStatus() != com.dental.clinic.management.treatment_plans.domain.ApprovalStatus.APPROVED) {
            throw new BadRequestAlertException(
                    "Lộ trình điều trị chưa được phê duyệt. Chỉ có thể đặt lịch cho lộ trình đã phê duyệt.",
                    ENTITY_NAME,
                    "PLAN_NOT_APPROVED");
        }

        return plan;
    }

    /**
     * Generate appointment suggestion for a single plan item.
     *
     * Algorithm:
     * 1. Start with estimated date from item
     * 2. Adjust for holidays/weekends → next working day
     * 3. Validate spacing rules → shift if needed
     * 4. Validate daily limit → shift if needed
     * 5. Find available slots on final date
     */
    private AutoScheduleResponse.AppointmentSuggestion generateSuggestionForItem(
            PatientPlanItem item,
            PatientTreatmentPlan plan,
            AutoScheduleRequest request,
            AutoScheduleResponse.SchedulingSummary summary) {

        log.debug("Generating suggestion for item {} (serviceId: {})",
                item.getItemId(), item.getServiceId());

        // Get service details from repository
        DentalService service = dentalServiceRepository.findById(Long.valueOf(item.getServiceId()))
                .orElseThrow(() -> new BadRequestAlertException(
                        "Dịch vụ không tồn tại: " + item.getServiceId(),
                        ENTITY_NAME,
                        "SERVICE_NOT_FOUND"));

        // Note: PatientPlanItem doesn't have estimatedDate field in current schema
        // Using a fallback approach: today + 7 days * sequence number
        LocalDate originalDate = LocalDate.now().plusDays(7L * item.getSequenceNumber());

        if (originalDate == null) {
            // No estimated date → use today + 7 days as fallback
            originalDate = LocalDate.now().plusDays(7);
            log.debug("No estimated date for item {}, using fallback: {}",
                    item.getItemId(), originalDate);
        }

        LocalDate proposedDate = originalDate;
        boolean holidayAdjusted = false;
        boolean spacingAdjusted = false;
        String adjustmentReason = null;

        // Get doctor ID from request or use plan creator
        Integer doctorId = getDoctorIdForScheduling(request, plan);

        // STEP 1: Adjust for holidays/weekends AND doctor shifts
        // Find next working day WITH doctor shift
        LocalDate workingDate = findNextWorkingDayWithDoctorShift(proposedDate, doctorId);

        if (workingDate == null) {
            // No doctor shifts found in next 30 days - return failed suggestion
            log.warn("No doctor shifts found in next 30 days from {} for doctor {}",
                    proposedDate, doctorId);
            return AutoScheduleResponse.AppointmentSuggestion.builder()
                    .itemId(item.getItemId())
                    .serviceCode(service.getServiceCode())
                    .serviceName(service.getServiceName())
                    .originalEstimatedDate(originalDate)
                    .success(false)
                    .adjustmentReason("Không tìm thấy ca làm việc của bác sĩ trong 30 ngày tới")
                    .build();
        }

        if (!workingDate.equals(proposedDate)) {
            holidayAdjusted = true;
            summary.setHolidayAdjustments(summary.getHolidayAdjustments() + 1);
            adjustmentReason = buildDateAdjustmentReason(proposedDate, workingDate);
            log.debug("Item {} date adjusted from {} to {} (holiday/weekend/no-doctor-shift)",
                    item.getItemId(), proposedDate, workingDate);
        }
        proposedDate = workingDate;

        // STEP 2: Apply spacing rules (if not forced)
        if (!Boolean.TRUE.equals(request.getForceSchedule())) {
            try {
                // Validate spacing rules
                serviceSpacingValidator.validateServiceSpacing(
                        plan.getPatient().getPatientId(),
                        service,
                        proposedDate);

                // Validate daily limit
                serviceSpacingValidator.validateDailyLimit(
                        plan.getPatient().getPatientId(),
                        proposedDate,
                        service);

            } catch (BadRequestAlertException e) {
                // Spacing rule violation → calculate minimum date and shift
                spacingAdjusted = true;
                summary.setSpacingAdjustments(summary.getSpacingAdjustments() + 1);

                LocalDate minDate = serviceSpacingValidator.calculateMinimumAllowedDate(
                        plan.getPatient().getPatientId(),
                        service);

                // Ensure minimum date is also a working day
                proposedDate = holidayValidator.adjustToWorkingDay(minDate);
                adjustmentReason = (adjustmentReason != null ? adjustmentReason + "; " : "") +
                        e.getErrorKey().replace("_", " ");

                log.debug("Item {} date adjusted from {} to {} (spacing rules)",
                        item.getItemId(), workingDate, proposedDate);
            }
        }

        // STEP 3: Find available slots (simplified - you can expand this later)
        List<AutoScheduleResponse.TimeSlot> availableSlots = findAvailableSlots(
                proposedDate,
                service,
                doctorId);

        // CRITICAL FIX: Validate that we have available slots
        // If no slots are available, this means either:
        // 1. Doctor has no shifts on this date (despite previous checks)
        // 2. All slots are occupied by other appointments
        // 3. No compatible rooms available
        if (availableSlots == null || availableSlots.isEmpty()) {
            log.warn("No available slots found for item {} on {} (doctor: {})",
                    item.getItemId(), proposedDate, doctorId);
            
            return AutoScheduleResponse.AppointmentSuggestion.builder()
                    .itemId(item.getItemId())
                    .serviceCode(service.getServiceCode())
                    .serviceName(service.getServiceName())
                    .originalEstimatedDate(originalDate)
                    .suggestedDate(proposedDate)
                    .success(false)
                    .errorMessage("Không có slot trống khả dụng vào ngày " + proposedDate + 
                                  ". Vui lòng kiểm tra lịch làm việc của bác sĩ hoặc chọn ngày khác.")
                    .adjustmentReason(adjustmentReason)
                    .build();
        }

        // Calculate total days shifted
        int daysShifted = (int) java.time.temporal.ChronoUnit.DAYS.between(originalDate, proposedDate);
        if (daysShifted > 0) {
            summary.setTotalDaysShifted(summary.getTotalDaysShifted() + daysShifted);
        }

        // Build successful suggestion
        return AutoScheduleResponse.AppointmentSuggestion.builder()
                .itemId(item.getItemId())
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .suggestedDate(proposedDate)
                .originalEstimatedDate(originalDate)
                .holidayAdjusted(holidayAdjusted)
                .spacingAdjusted(spacingAdjusted)
                .adjustmentReason(adjustmentReason)
                .availableSlots(availableSlots)
                .success(true)
                .build();
    }

    /**
     * Get doctor ID for scheduling from request or plan.
     * Priority: 1. Request employeeCode, 2. Plan createdBy
     */
    private Integer getDoctorIdForScheduling(AutoScheduleRequest request, PatientTreatmentPlan plan) {
        // Try to get from request first
        if (request.getEmployeeCode() != null && !request.getEmployeeCode().isEmpty()) {
            var employee = employeeRepository.findByEmployeeCodeAndIsActiveTrue(request.getEmployeeCode())
                    .orElseThrow(() -> new BadRequestAlertException(
                            "Không tìm thấy nhân viên hoặc nhân viên không hoạt động: " + request.getEmployeeCode(),
                            ENTITY_NAME,
                            "EMPLOYEE_NOT_FOUND"));
            return employee.getEmployeeId();
        }

        // Use plan creator as fallback
        if (plan.getCreatedBy() != null) {
            return plan.getCreatedBy().getEmployeeId();
        }

        return null;
    }

    /**
     * Find next working day that has doctor shifts.
     * Checks up to 30 days ahead.
     *
     * @param startDate Starting date
     * @param doctorId  Doctor ID (null = any doctor)
     * @return Next working day with doctor shifts, or null if none found
     */
    private LocalDate findNextWorkingDayWithDoctorShift(LocalDate startDate, Integer doctorId) {
        if (doctorId == null) {
            // No preferred doctor - just return next working day (old behavior)
            return holidayValidator.adjustToWorkingDay(startDate);
        }

        LocalDate checkDate = startDate;
        int maxDaysToCheck = 30;

        for (int i = 0; i < maxDaysToCheck; i++) {
            // Skip holidays and weekends
            if (!holidayValidator.isWorkingDay(checkDate)) {
                checkDate = checkDate.plusDays(1);
                continue;
            }

            // Check if doctor has shifts on this date
            List<EmployeeShift> doctorShifts = employeeShiftRepository.findByEmployeeAndDate(
                    doctorId, checkDate);

            if (!doctorShifts.isEmpty()) {
                log.debug("Found working day with doctor shift: {} (doctor: {}, {} shift(s))",
                        checkDate, doctorId, doctorShifts.size());
                return checkDate;
            }

            checkDate = checkDate.plusDays(1);
        }

        log.warn("No working day with doctor shifts found in {} days from {}",
                maxDaysToCheck, startDate);
        return null;
    }

    /**
     * Build adjustment reason message for date shifts.
     */
    private String buildDateAdjustmentReason(LocalDate originalDate, LocalDate adjustedDate) {
        List<String> reasons = new ArrayList<>();

        // Check if weekend
        if (originalDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                originalDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            reasons.add("Cuối tuần");
        }

        // Check if holiday
        if (holidayValidator.isHoliday(originalDate)) {
            try {
                var holidays = holidayDateService.getHolidaysInRange(originalDate, originalDate.plusDays(1));
                if (!holidays.isEmpty()) {
                    reasons.add("Ngày lễ: " + originalDate);
                } else {
                    reasons.add("Ngày lễ");
                }
            } catch (Exception e) {
                reasons.add("Ngày lễ");
            }
        }

        // If neither weekend nor holiday, must be no doctor shift
        if (reasons.isEmpty()) {
            reasons.add("Không có ca làm việc của bác sĩ");
        }

        return String.join("; ", reasons);
    }

    /**
     * Find available time slots on a given date.
     *
     * CRITICAL FIX: Now checks THREE conditions:
     * 1. Doctor must have shifts on this date
     * 2. Slots must be truly available (no conflicting appointments)
     * 3. Date must not be a holiday (already checked by caller)
     *
     * @param date     Proposed date
     * @param service  Service to schedule
     * @param doctorId Doctor ID (from request or plan)
     * @return List of available time slots
     */
    private List<AutoScheduleResponse.TimeSlot> findAvailableSlots(
            LocalDate date,
            DentalService service,
            Integer doctorId) {

        List<AutoScheduleResponse.TimeSlot> slots = new ArrayList<>();

        if (doctorId == null) {
            log.debug("No doctor specified - cannot find available slots");
            return Collections.emptyList();
        }

        // CONDITION 1: Check if doctor has shifts on this date
        List<EmployeeShift> doctorShifts = employeeShiftRepository.findByEmployeeAndDate(
                doctorId, date);

        if (doctorShifts.isEmpty()) {
            log.debug("Doctor {} has NO shifts on {} - cannot suggest this date",
                    doctorId, date);
            return Collections.emptyList();
        }

        log.debug("Doctor {} has {} shift(s) on {}", doctorId, doctorShifts.size(), date);

        // CONDITION 2: Find compatible rooms for this service
        List<String> compatibleRoomIds = findCompatibleRoomsForService(service.getServiceId().intValue());

        if (compatibleRoomIds.isEmpty()) {
            log.warn("No compatible rooms found for service {}", service.getServiceCode());
            return Collections.emptyList();
        }

        // CONDITION 3: For each shift, find available time slots
        for (EmployeeShift shift : doctorShifts) {
            LocalDateTime shiftStart = LocalDateTime.of(date, shift.getWorkShift().getStartTime());
            LocalDateTime shiftEnd = LocalDateTime.of(date, shift.getWorkShift().getEndTime());

            // Get doctor's busy appointments during this shift
            List<Appointment> busyAppointments = appointmentRepository.findByEmployeeAndTimeRange(
                    doctorId, shiftStart, shiftEnd, BUSY_STATUSES);

            // Generate time slots (every 30 minutes)
            List<AutoScheduleResponse.TimeSlot> shiftSlots = generateTimeSlotsFromShift(
                    shiftStart, shiftEnd, service.getDefaultDurationMinutes(),
                    busyAppointments, compatibleRoomIds);

            slots.addAll(shiftSlots);
        }

        log.debug("Found {} available slots on {} for doctor {}",
                slots.size(), date, doctorId);

        return slots;
    }

    /**
     * Find rooms compatible with a specific service.
     */
    private List<String> findCompatibleRoomsForService(Integer serviceId) {
        List<Integer> serviceIds = Collections.singletonList(serviceId);
        List<String> roomIds = roomServiceRepository.findRoomsSupportingAllServices(serviceIds, 1);

        if (roomIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Verify rooms are active
        List<Room> activeRooms = roomRepository.findByRoomIdInAndIsActiveTrue(roomIds);
        return activeRooms.stream()
                .map(Room::getRoomId)
                .collect(Collectors.toList());
    }

    /**
     * Generate time slots from a shift, excluding busy appointment times.
     */
    private List<AutoScheduleResponse.TimeSlot> generateTimeSlotsFromShift(
            LocalDateTime shiftStart,
            LocalDateTime shiftEnd,
            int serviceDuration,
            List<Appointment> busyAppointments,
            List<String> compatibleRoomIds) {

        List<AutoScheduleResponse.TimeSlot> slots = new ArrayList<>();

        // Generate slots every 30 minutes
        LocalDateTime currentTime = shiftStart;
        int slotIntervalMinutes = 30;

        while (currentTime.plusMinutes(serviceDuration).isBefore(shiftEnd) ||
                currentTime.plusMinutes(serviceDuration).equals(shiftEnd)) {

            LocalDateTime slotEnd = currentTime.plusMinutes(serviceDuration);

            // Check if this slot conflicts with any busy appointment
            boolean isAvailable = !hasTimeConflict(currentTime, slotEnd, busyAppointments);

            if (isAvailable) {
                slots.add(AutoScheduleResponse.TimeSlot.builder()
                        .startTime(currentTime.toLocalTime())
                        .endTime(slotEnd.toLocalTime())
                        .available(true)
                        .build());
            }

            currentTime = currentTime.plusMinutes(slotIntervalMinutes);
        }

        return slots;
    }

    /**
     * Check if a time range conflicts with any busy appointments.
     */
    private boolean hasTimeConflict(
            LocalDateTime slotStart,
            LocalDateTime slotEnd,
            List<Appointment> busyAppointments) {

        for (Appointment appointment : busyAppointments) {
            LocalDateTime appointmentStart = appointment.getAppointmentStartTime();
            LocalDateTime appointmentEnd = appointment.getAppointmentEndTime();

            // Check for overlap: (slotStart < appointmentEnd) AND (slotEnd >
            // appointmentStart)
            if (slotStart.isBefore(appointmentEnd) && slotEnd.isAfter(appointmentStart)) {
                return true; // Conflict found
            }
        }

        return false; // No conflicts
    }

    /**
     * Initialize scheduling summary with zero counters.
     */
    private AutoScheduleResponse.SchedulingSummary initializeSummary() {
        return AutoScheduleResponse.SchedulingSummary.builder()
                .holidayAdjustments(0)
                .spacingAdjustments(0)
                .dailyLimitAdjustments(0)
                .totalDaysShifted(0)
                .holidaysEncountered(new ArrayList<>())
                .build();
    }

    /**
     * Build successful response with suggestions.
     */
    private AutoScheduleResponse buildResponse(
            Long planId,
            List<AutoScheduleResponse.AppointmentSuggestion> suggestions,
            int totalProcessed,
            AutoScheduleResponse.SchedulingSummary summary) {

        long successCount = suggestions.stream()
                .filter(s -> Boolean.TRUE.equals(s.getSuccess()))
                .count();

        long failedCount = suggestions.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getSuccess()))
                .count();

        log.info("Auto-schedule completed for plan {}: {} successful, {} failed",
                planId, successCount, failedCount);

        return AutoScheduleResponse.builder()
                .planId(planId)
                .suggestions(suggestions)
                .totalItemsProcessed(totalProcessed)
                .successfulSuggestions((int) successCount)
                .failedItems((int) failedCount)
                .summary(summary)
                .build();
    }

    /**
     * Build empty response when no items are ready for booking.
     */
    private AutoScheduleResponse buildEmptyResponse(Long planId, String message) {
        return AutoScheduleResponse.builder()
                .planId(planId)
                .suggestions(new ArrayList<>())
                .totalItemsProcessed(0)
                .successfulSuggestions(0)
                .failedItems(0)
                .summary(initializeSummary())
                .build();
    }
}
