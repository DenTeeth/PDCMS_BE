package com.dental.clinic.management.booking_appointment.service;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.domain.AppointmentAuditLog;
import com.dental.clinic.management.booking_appointment.domain.AppointmentPlanItemBridge;

import com.dental.clinic.management.booking_appointment.dto.CreateAppointmentRequest;
import com.dental.clinic.management.booking_appointment.dto.request.RescheduleAppointmentRequest;
import com.dental.clinic.management.booking_appointment.dto.response.RescheduleAppointmentResponse;
import com.dental.clinic.management.booking_appointment.enums.AppointmentActionType;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.booking_appointment.repository.AppointmentAuditLogRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentPlanItemRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentServiceRepository;
import com.dental.clinic.management.booking_appointment.repository.BookingDentalServiceRepository;
import com.dental.clinic.management.booking_appointment.repository.PatientPlanItemRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.treatment_plans.domain.PatientPlanItem;
import com.dental.clinic.management.treatment_plans.enums.PlanItemStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for rescheduling appointments (Cancel old + Create new).
 *
 * Business Flow:
 * 1. Lock old appointment (SELECT FOR UPDATE)
 * 2. Validate old appointment can be rescheduled (SCHEDULED/CHECKED_IN only)
 * 3. Reuse patient_id and service_ids from old appointment
 * 4. Validate new appointment (reuse logic from AppointmentCreationService)
 * 5. Create new appointment
 * 6. Cancel old appointment with link to new appointment
 * 7. Create audit logs for both
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentRescheduleService {

        private final AppointmentRepository appointmentRepository;
        private final AppointmentServiceRepository appointmentServiceRepository;
        private final BookingDentalServiceRepository dentalServiceRepository;
        private final com.dental.clinic.management.patient.repository.PatientRepository patientRepository;
        private final AppointmentAuditLogRepository auditLogRepository;
        private final EmployeeRepository employeeRepository;
        private final AppointmentCreationService creationService;
        private final AppointmentDetailService detailService;
        private final AppointmentPlanItemRepository appointmentPlanItemRepository;
        private final PatientPlanItemRepository itemRepository;
        private final EntityManager entityManager;

        // ISSUE #53: Holiday Validation
        private final com.dental.clinic.management.utils.validation.HolidayValidator holidayValidator;

        /**
         * Reschedule appointment: Cancel old and create new in one transaction.
         *
         * @param oldAppointmentCode Code of appointment to reschedule
         * @param request            New appointment details + cancellation reason
         * @return Both old (cancelled) and new (scheduled) appointments
         */
        @Transactional
        public RescheduleAppointmentResponse rescheduleAppointment(
                        String oldAppointmentCode,
                        RescheduleAppointmentRequest request) {

                log.info("Đang dời lịch hẹn {} sang thời gian mới {}",
                                oldAppointmentCode, request.getNewStartTime());

                // STEP 0: ISSUE #53 - Validate new date is NOT a holiday (early check)
                java.time.LocalDate newDate = request.getNewStartTime().toLocalDate();
                holidayValidator.validateNotHoliday(newDate, "lịch hẹn (reschedule)");

                // STEP 1: Lock old appointment
                Appointment oldAppointment = appointmentRepository.findByCodeForUpdate(oldAppointmentCode)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Không tìm thấy lịch hẹn: " + oldAppointmentCode));

                // STEP 2: Validate old appointment can be rescheduled
                validateOldAppointment(oldAppointment);

                // STEP 3: Get service codes from old appointment
                List<String> serviceCodes = getServiceCodes(oldAppointment, request);

                // STEP 3.5: FIX Issue #39 - Get plan item IDs from old appointment
                List<Long> planItemIds = getPlanItemIdsFromOldAppointment(oldAppointment);

                // STEP 3.6: FIX Issue #42 - Reset plan items status from SCHEDULED to
                // READY_FOR_BOOKING
                // This is necessary because old appointment will be cancelled, allowing
                // re-booking
                if (planItemIds != null && !planItemIds.isEmpty()) {
                        resetPlanItemsStatusForReschedule(planItemIds);
                        log.info("Đã đặt lại {} hạng mục điều trị từ trạng thái SCHEDULED sang READY_FOR_BOOKING để dời lịch",
                                        planItemIds.size());
                }

                // STEP 4: Get patient code from old appointment
                String patientCode = getPatientCode(oldAppointment);

                // STEP 5: Create new appointment with plan items linked
                CreateAppointmentRequest createRequest = buildCreateRequest(request, patientCode, serviceCodes,
                                planItemIds);
                Appointment newAppointment = creationService.createAppointmentInternal(createRequest);

                // STEP 5.5: Rule #9 - Inherit and increment reschedule counter
                Integer oldRescheduleCount = oldAppointment.getRescheduleCount();
                if (oldRescheduleCount == null) {
                        oldRescheduleCount = 0; // Handle null for legacy data
                }
                newAppointment.setRescheduleCount(oldRescheduleCount + 1);
                appointmentRepository.save(newAppointment);
                log.info("Tăng số lần dời lịch: {} -> {} cho lịch hẹn mới {}",
                                oldRescheduleCount, oldRescheduleCount + 1, newAppointment.getAppointmentCode());

                // STEP 6: Cancel old appointment and link to new one
                cancelOldAppointment(oldAppointment, newAppointment, request);

                // STEP 7: Create audit logs
                createAuditLogs(oldAppointment, newAppointment, request);

                log.info("Dời lịch thành công từ {} -> {}",
                                oldAppointmentCode, newAppointment.getAppointmentCode());

                // STEP 8: Return both appointments
                return RescheduleAppointmentResponse.builder()
                                .cancelledAppointment(detailService.getAppointmentDetail(oldAppointmentCode))
                                .newAppointment(detailService.getAppointmentDetail(newAppointment.getAppointmentCode()))
                                .build();
        }

        /**
         * Validate old appointment can be rescheduled.
         * Only SCHEDULED or CHECKED_IN can be rescheduled.
         * Rule #9: Maximum 2 reschedules allowed per appointment.
         */
        private void validateOldAppointment(Appointment oldAppointment) {
                AppointmentStatus status = oldAppointment.getStatus();

                // Cannot reschedule terminal states
                if (status == AppointmentStatus.COMPLETED) {
                        throw new IllegalStateException(
                                        "Không thể dời lịch hẹn đã hoàn thành. Mã lỗi: APPOINTMENT_NOT_RESCHEDULABLE");
                }

                if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.CANCELLED_LATE) {
                        throw new IllegalStateException(
                                        "Không thể dời lịch hẹn đã hủy. Mã lỗi: APPOINTMENT_NOT_RESCHEDULABLE");
                }

                // ALLOW NO_SHOW appointments to be rescheduled (patient can return)
                // Removed restriction: NO_SHOW appointments can now be rescheduled

                // Allow SCHEDULED, CHECKED_IN, and NO_SHOW
                if (status != AppointmentStatus.SCHEDULED
                                && status != AppointmentStatus.CHECKED_IN
                                && status != AppointmentStatus.NO_SHOW) {
                        throw new IllegalStateException(
                                        String.format("Không thể dời lịch hẹn ở trạng thái %s. Chỉ cho phép dời lịch với các trạng thái SCHEDULED, CHECKED_IN hoặc NO_SHOW.",
                                                        status));
                }

                // Rule #9: Check reschedule limit (max 2 reschedules)
                Integer rescheduleCount = oldAppointment.getRescheduleCount();
                if (rescheduleCount == null) {
                        rescheduleCount = 0; // Handle null for legacy data
                }
                if (rescheduleCount >= 2) {
                        throw new IllegalStateException(
                                        String.format("Lịch hẹn đã đạt giới hạn dời tối đa (2 lần). " +
                                                        "Số lần dời hiện tại: %d. " +
                                                        "Vui lòng liên hệ nhân viên phòng khám để được hỗ trợ.",
                                                        rescheduleCount));
                }

                log.debug("Lịch hẹn cũ {} hợp lệ để dời (số lần dời: {})",
                                oldAppointment.getAppointmentCode(), rescheduleCount);
        }

        /**
         * Get patient code for new appointment from old appointment.
         */
        private String getPatientCode(Appointment oldAppointment) {
                return patientRepository.findById(oldAppointment.getPatientId())
                                .map(patient -> patient.getPatientCode())
                                .orElseThrow(
                                                () -> new IllegalStateException("Không tìm thấy bệnh nhân cho ID: "
                                                                + oldAppointment.getPatientId()));
        }

        /**
         * Get service codes for new appointment.
         * If request.newServiceIds is provided -> convert to codes.
         * Otherwise -> reuse old appointment's service codes.
         */
        private List<String> getServiceCodes(Appointment oldAppointment, RescheduleAppointmentRequest request) {
                if (request.getNewServiceIds() != null && !request.getNewServiceIds().isEmpty()) {
                        log.debug("Sử dụng danh sách dịch vụ mới từ yêu cầu: {}", request.getNewServiceIds());
                        // Convert service IDs to codes
                        return dentalServiceRepository.findAllById(request.getNewServiceIds())
                                        .stream()
                                        .map(service -> service.getServiceCode())
                                        .collect(Collectors.toList());
                }

                // Reuse old services
                log.debug("Tái sử dụng dịch vụ từ lịch hẹn cũ: {}", oldAppointment.getAppointmentCode());
                List<com.dental.clinic.management.booking_appointment.domain.AppointmentService> oldServices = appointmentServiceRepository
                                .findByIdAppointmentId(oldAppointment.getAppointmentId());

                // Get service IDs and convert to codes
                List<Integer> serviceIds = oldServices.stream()
                                .map(asvc -> asvc.getId().getServiceId())
                                .collect(Collectors.toList());

                return dentalServiceRepository.findAllById(serviceIds)
                                .stream()
                                .map(service -> service.getServiceCode())
                                .collect(Collectors.toList());
        }

        /**
         * Build CreateAppointmentRequest from reschedule request.
         * Reuses patient and services from old appointment.
         * FIX Issue #39: Link plan items if old appointment was from treatment plan.
         */
        private CreateAppointmentRequest buildCreateRequest(
                        RescheduleAppointmentRequest request,
                        String patientCode,
                        List<String> serviceCodes,
                        List<Long> planItemIds) {

                CreateAppointmentRequest.CreateAppointmentRequestBuilder builder = CreateAppointmentRequest.builder()
                                .patientCode(patientCode)
                                .employeeCode(request.getNewEmployeeCode())
                                .roomCode(request.getNewRoomCode())
                                .appointmentStartTime(request.getNewStartTime().toString())
                                .serviceCodes(serviceCodes)
                                .participantCodes(request.getNewParticipantCodes())
                                .notes("Dời từ lịch hẹn trước");

                // FIX Issue #39: Link plan items if old appointment was from treatment plan
                if (planItemIds != null && !planItemIds.isEmpty()) {
                        builder.patientPlanItemIds(planItemIds);
                        log.info("Dời lịch hẹn từ phác đồ điều trị: sẽ liên kết {} hạng mục điều trị",
                                        planItemIds.size());
                }

                return builder.build();
        }

        /**
         * FIX Issue #39: Get plan item IDs linked to old appointment.
         * Returns empty list if appointment was not from treatment plan.
         *
         * @param oldAppointment The appointment being rescheduled
         * @return List of plan item IDs or empty list if standalone appointment
         */
        private List<Long> getPlanItemIdsFromOldAppointment(Appointment oldAppointment) {
                List<AppointmentPlanItemBridge> bridges = appointmentPlanItemRepository
                                .findById_AppointmentId(oldAppointment.getAppointmentId());

                if (bridges.isEmpty()) {
                        log.debug("Lịch hẹn cũ {} là lịch hẹn độc lập (không thuộc phác đồ điều trị)",
                                        oldAppointment.getAppointmentCode());
                        return List.of();
                }

                List<Long> planItemIds = bridges.stream()
                                .map(bridge -> bridge.getId().getItemId())
                                .collect(Collectors.toList());

                log.info("Lịch hẹn cũ {} được liên kết với {} hạng mục điều trị: {}",
                                oldAppointment.getAppointmentCode(), planItemIds.size(), planItemIds);

                return planItemIds;
        }

        /**
         * Cancel old appointment and link to new one.
         */
        private void cancelOldAppointment(
                        Appointment oldAppointment,
                        Appointment newAppointment,
                        RescheduleAppointmentRequest request) {

                oldAppointment.setStatus(AppointmentStatus.CANCELLED);
                oldAppointment.setRescheduledToAppointmentId(newAppointment.getAppointmentId());
                appointmentRepository.save(oldAppointment);

                log.info("Đã hủy lịch hẹn cũ {} và liên kết với lịch hẹn mới {}",
                                oldAppointment.getAppointmentCode(), newAppointment.getAppointmentCode());
        }

        /**
         * Create audit logs for both old and new appointments.
         */
        private void createAuditLogs(
                        Appointment oldAppointment,
                        Appointment newAppointment,
                        RescheduleAppointmentRequest request) {

                Integer performedByEmployeeId = getCurrentEmployeeId();

                // Fetch employee entity if ID is not 0 (SYSTEM)
                com.dental.clinic.management.employee.domain.Employee performedByEmployee = null;
                if (performedByEmployeeId != 0) {
                        performedByEmployee = employeeRepository.findById(performedByEmployeeId).orElse(null);
                }

                // Audit log for OLD appointment (RESCHEDULE_SOURCE)
                AppointmentAuditLog oldLog = AppointmentAuditLog.builder()
                                .appointment(oldAppointment)
                                .performedByEmployee(performedByEmployee)
                                .actionType(AppointmentActionType.RESCHEDULE_SOURCE)
                                .oldStatus(AppointmentStatus.SCHEDULED) // or CHECKED_IN
                                .newStatus(AppointmentStatus.CANCELLED)
                                .reasonCode(request.getReasonCode())
                                .notes(request.getCancelNotes())
                                .build();
                auditLogRepository.save(oldLog);

                // Audit log for NEW appointment (RESCHEDULE_TARGET)
                AppointmentAuditLog newLog = AppointmentAuditLog.builder()
                                .appointment(newAppointment)
                                .performedByEmployee(performedByEmployee)
                                .actionType(AppointmentActionType.RESCHEDULE_TARGET)
                                .oldStatus(null) // New appointment has no old status
                                .newStatus(AppointmentStatus.SCHEDULED)
                                .reasonCode(request.getReasonCode())
                                .notes("Dời từ " + oldAppointment.getAppointmentCode())
                                .build();
                auditLogRepository.save(newLog);

                log.info("Đã tạo log kiểm toán cho thao tác dời lịch");
        }

        /**
         * Get current employee ID from JWT token.
         */
        private Integer getCurrentEmployeeId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                        throw new IllegalStateException("Không tìm thấy xác thực JWT hợp lệ");
                }

                Jwt jwt = (Jwt) authentication.getPrincipal();
                String username = jwt.getSubject();

                Employee employee = employeeRepository.findByAccount_Username(username)
                                .orElseThrow(() -> new IllegalStateException(
                                                "Người dùng hiện tại không phải là nhân viên: " + username));

                return employee.getEmployeeId();
        }

        /**
         * FIX Issue #42: Reset plan items status from SCHEDULED to READY_FOR_BOOKING
         * for reschedule.
         * Only resets items that are currently SCHEDULED (from old appointment).
         *
         * Why this is needed:
         * - When rescheduling, we need to create a new appointment with the same plan
         * items
         * - AppointmentCreationService.validatePlanItems() requires all items to be
         * READY_FOR_BOOKING
         * - But items from old appointment are still in SCHEDULED status
         * - We reset them here before validation to allow the new appointment to be
         * created
         * - The old appointment will be cancelled afterwards, which would normally
         * trigger this reset
         *
         * @param planItemIds List of plan item IDs to reset
         */
        private void resetPlanItemsStatusForReschedule(List<Long> planItemIds) {
                List<PatientPlanItem> items = itemRepository.findAllById(planItemIds);

                for (PatientPlanItem item : items) {
                        if (item.getStatus() == PlanItemStatus.SCHEDULED) {
                                item.setStatus(PlanItemStatus.READY_FOR_BOOKING);
                                itemRepository.save(item);
                                log.debug("Đặt lại hạng mục điều trị {} từ SCHEDULED về READY_FOR_BOOKING để dời lịch",
                                                item.getItemId());
                        }
                }

                // Ensure changes are persisted before validation
                entityManager.flush();
                log.info("Đặt lại thành công {} hạng mục điều trị để dời lịch", items.size());
        }
}
