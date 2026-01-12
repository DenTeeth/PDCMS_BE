package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.notification.dto.CreateNotificationRequest;
import com.dental.clinic.management.notification.enums.NotificationEntityType;
import com.dental.clinic.management.notification.enums.NotificationType;
import com.dental.clinic.management.notification.service.NotificationService;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.utils.AppointmentEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BR-17: Scheduled job to send appointment reminders 24 hours before appointment time
 * Runs daily at 08:00 AM (Vietnam time)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderJob {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final AppointmentEmailService appointmentEmailService;

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Send reminders for appointments happening in the next 24 hours
     * Runs daily at 08:00 AM Vietnam time
     * 
     * Cron: "0 0 8 * * ?" = At 08:00 every day
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendAppointmentReminders() {
        try {
            log.info("========================================");
            log.info("🔔 [AppointmentReminderJob] Starting 24-hour reminder job at {}", LocalDateTime.now());
            log.info("========================================");

            // Get appointments for tomorrow (24 hours window)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderStart = now.plusHours(23); // 23 hours from now
            LocalDateTime reminderEnd = now.plusHours(25);   // 25 hours from now (2-hour window)

            log.info("Searching for appointments between {} and {}", reminderStart, reminderEnd);

            // Only send reminders for SCHEDULED appointments (not cancelled, completed, etc.)
            List<Appointment> upcomingAppointments = appointmentRepository.findByStatusAndStartTimeBetween(
                AppointmentStatus.SCHEDULED,
                reminderStart,
                reminderEnd
            );

            log.info("Found {} appointments requiring 24h reminder", upcomingAppointments.size());

            if (upcomingAppointments.isEmpty()) {
                log.info("No appointments to remind. Job finished.");
                return;
            }

            int successCount = 0;
            int failureCount = 0;

            for (Appointment appointment : upcomingAppointments) {
                try {
                    sendReminderForAppointment(appointment);
                    successCount++;
                } catch (Exception e) {
                    log.error("❌ Failed to send reminder for appointment {}: {}", 
                        appointment.getAppointmentCode(), e.getMessage());
                    failureCount++;
                }
            }

            log.info("========================================");
            log.info("✅ [AppointmentReminderJob] Completed: {} success, {} failures", 
                successCount, failureCount);
            log.info("========================================");

        } catch (Exception e) {
            log.error("❌ [AppointmentReminderJob] CRITICAL ERROR: {}", e.getMessage(), e);
        }
    }

    /**
     * Send reminder notification and email for a single appointment
     */
    private void sendReminderForAppointment(Appointment appointment) {
        log.info("📧 Processing reminder for appointment: {}", appointment.getAppointmentCode());

        // Load patient with account
        Patient patient = patientRepository.findById(appointment.getPatientId())
            .orElse(null);
        
        if (patient == null) {
            log.warn("Patient not found for appointment {}", appointment.getAppointmentCode());
            return;
        }

        // Load doctor
        Employee doctor = employeeRepository.findById(appointment.getEmployeeId())
            .orElse(null);

        String formattedTime = appointment.getAppointmentStartTime().format(DISPLAY_FORMATTER);

        // 1. Send in-app notification to patient
        if (patient.getAccount() != null) {
            Integer patientUserId = patient.getAccount().getAccountId();
            
            CreateNotificationRequest notification = CreateNotificationRequest.builder()
                .userId(patientUserId)
                .type(NotificationType.APPOINTMENT_REMINDER)
                .title("🔔 Nhắc nhở: Lịch hẹn sắp tới")
                .message(String.format("Bạn có lịch hẹn %s vào ngày mai (%s). Vui lòng đến trước 10 phút!",
                    appointment.getAppointmentCode(), formattedTime))
                .relatedEntityType(NotificationEntityType.APPOINTMENT)
                .relatedEntityId(appointment.getAppointmentCode())
                .build();

            notificationService.createNotification(notification);
            log.info("✓ In-app reminder sent to patient userId={}", patientUserId);
        } else {
            log.warn("Patient {} has no account, skipping in-app notification", patient.getPatientId());
        }

        // 2. Send email reminder to patient
        if (patient.getEmail() != null && !patient.getEmail().isEmpty()) {
            try {
                // Get appointment details for email
                String doctorName = (doctor != null) ? doctor.getFullName() : "Bác sĩ";
                String roomName = "Phòng " + appointment.getRoomId(); // Can enhance later to get room name
                String serviceNames = "Dịch vụ nha khoa"; // Can enhance later to get actual service names

                appointmentEmailService.sendAppointmentReminder(
                    patient.getEmail(),
                    patient.getFullName(),
                    appointment.getAppointmentCode(),
                    appointment.getAppointmentStartTime(),
                    doctorName,
                    roomName,
                    serviceNames
                );
                
                log.info("✓ Email reminder sent to {}", patient.getEmail());
            } catch (Exception e) {
                log.error("Failed to send email reminder to {}: {}", patient.getEmail(), e.getMessage());
                // Continue processing - email failure shouldn't stop notifications
            }
        } else {
            log.warn("Patient {} has no email, skipping email reminder", patient.getPatientCode());
        }

        log.info("✅ Reminder completed for appointment {}", appointment.getAppointmentCode());
    }
}
