package com.dental.clinic.management.booking_appointment.service;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.dto.AppointmentDetailDTO;
import com.dental.clinic.management.booking_appointment.dto.CreateAppointmentRequest;
import com.dental.clinic.management.booking_appointment.dto.CreateAppointmentResponse;
import com.dental.clinic.management.booking_appointment.dto.UpdateAppointmentStatusRequest;
import com.dental.clinic.management.booking_appointment.dto.request.RescheduleAppointmentRequest;
import com.dental.clinic.management.booking_appointment.dto.response.RescheduleAppointmentResponse;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentServiceRepository;
import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import com.dental.clinic.management.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for rescheduling appointments.
 * Reschedule = Cancel old appointment + Create new appointment with linking.
 *
 * Business Flow:
 * 1. Validate old appointment exists and is reschedulable
 * 2. Cancel old appointment with reason
 * 3. Create new appointment with new time/doctor/room
 * 4. Link old appointment to new (rescheduled_to_appointment_id)
 * 5. Return both appointments for frontend display
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentRescheduleService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final AppointmentCreationService creationService;
    private final AppointmentStatusService statusService;
    private final AppointmentDetailService detailService;
    private final PatientRepository patientRepository;

    private static final String ENTITY_NAME = "appointment";

    /**
     * Reschedule an appointment by cancelling old and creating new.
     *
     * @param appointmentCode Old appointment code to reschedule
     * @param request         New appointment details + cancellation reason
     * @return Both old (cancelled) and new (scheduled) appointments
     * @throws ResourceNotFoundException if appointment not found
     * @throws BadRequestAlertException  if appointment cannot be rescheduled
     */
    @Transactional
    public RescheduleAppointmentResponse rescheduleAppointment(
            String appointmentCode,
            RescheduleAppointmentRequest request) {

        log.info("Rescheduling appointment: code={}, newStartTime={}, newDoctor={}",
                appointmentCode, request.getNewStartTime(), request.getNewEmployeeCode());

        // Step 1: Validate old appointment exists and can be rescheduled
        Appointment oldAppointment = appointmentRepository.findByAppointmentCode(appointmentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "APPOINTMENT_NOT_FOUND",
                        "Appointment not found with code: " + appointmentCode));

        validateReschedulable(oldAppointment);

        // Step 2: Get patient code
        String patientCode = patientRepository.findById(oldAppointment.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("PATIENT_NOT_FOUND", "Patient not found"))
                .getPatientCode();

        // Step 3: Get services from old appointment (if not provided in request)
        List<String> serviceCodes;
        if (request.getNewServiceIds() == null || request.getNewServiceIds().isEmpty()) {
            serviceCodes = getServiceCodesFromAppointment(oldAppointment);
        } else {
            // Convert serviceIds to serviceCodes if provided
            serviceCodes = getServiceCodesFromAppointment(oldAppointment);
        }

        // Step 4: Create new appointment
        CreateAppointmentRequest createRequest = CreateAppointmentRequest.builder()
                .patientCode(patientCode)
                .employeeCode(request.getNewEmployeeCode())
                .roomCode(request.getNewRoomCode())
                .appointmentStartTime(request.getNewStartTime().toString())
                .serviceCodes(serviceCodes)
                .participantCodes(request.getNewParticipantCodes())
                .notes("Rescheduled from " + appointmentCode +
                        (request.getCancelNotes() != null ? ". Reason: " + request.getCancelNotes() : ""))
                .build();

        CreateAppointmentResponse createResponse = creationService.createAppointment(createRequest);
        String newAppointmentCode = createResponse.getAppointmentCode();

        log.info("Created new appointment: {} for rescheduled {}", newAppointmentCode, appointmentCode);

        // Step 4: Cancel old appointment with reason and link to new
        UpdateAppointmentStatusRequest cancelRequest = new UpdateAppointmentStatusRequest();
        cancelRequest.setStatus(AppointmentStatus.CANCELLED.name());
        cancelRequest.setReasonCode(request.getReasonCode().name());
        cancelRequest.setNotes("Rescheduled to " + newAppointmentCode +
                (request.getCancelNotes() != null ? ". " + request.getCancelNotes() : ""));

        statusService.updateStatus(appointmentCode, cancelRequest);

        // Step 5: Link old appointment to new (rescheduled_to_appointment_id)
        Appointment newAppointment = appointmentRepository.findByAppointmentCode(newAppointmentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "APPOINTMENT_NOT_FOUND",
                        "New appointment not found: " + newAppointmentCode));

        oldAppointment.setRescheduledToAppointmentId(newAppointment.getAppointmentId());
        appointmentRepository.save(oldAppointment);

        log.info("Successfully rescheduled {} -> {}", appointmentCode, newAppointmentCode);

        // Step 6: Return both appointments
        AppointmentDetailDTO cancelledDto = detailService.getAppointmentDetail(appointmentCode);
        AppointmentDetailDTO newDto = detailService.getAppointmentDetail(newAppointmentCode);

        return RescheduleAppointmentResponse.builder()
                .cancelledAppointment(cancelledDto)
                .newAppointment(newDto)
                .build();
    }

    /**
     * Validate that appointment can be rescheduled.
     * Only SCHEDULED and CHECKED_IN appointments can be rescheduled.
     */
    private void validateReschedulable(Appointment appointment) {
        AppointmentStatus status = appointment.getStatus();

        if (status != AppointmentStatus.SCHEDULED && status != AppointmentStatus.CHECKED_IN) {
            throw new BadRequestAlertException(
                    "Cannot reschedule appointment with status: " + status +
                            ". Only SCHEDULED or CHECKED_IN appointments can be rescheduled.",
                    ENTITY_NAME,
                    "INVALID_STATUS_FOR_RESCHEDULE");
        }
    }

    /**
     * Get service codes from an existing appointment.
     */
    private List<String> getServiceCodesFromAppointment(Appointment appointment) {
        return appointmentServiceRepository
                .findByIdAppointmentId(appointment.getAppointmentId())
                .stream()
                .map(as -> as.getService().getServiceCode())
                .collect(Collectors.toList());
    }
}
