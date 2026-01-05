package com.dental.clinic.management.scheduled;

import com.dental.clinic.management.account.domain.Account;
import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.notification.dto.CreateNotificationRequest;
import com.dental.clinic.management.notification.enums.NotificationEntityType;
import com.dental.clinic.management.notification.enums.NotificationType;
import com.dental.clinic.management.notification.service.NotificationService;
import com.dental.clinic.management.working_schedule.domain.OvertimeRequest;
import com.dental.clinic.management.working_schedule.domain.PartTimeRegistration;
import com.dental.clinic.management.working_schedule.domain.TimeOffRequest;
import com.dental.clinic.management.working_schedule.enums.RequestStatus;
import com.dental.clinic.management.working_schedule.enums.RegistrationStatus;
import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import com.dental.clinic.management.working_schedule.repository.OvertimeRequestRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Scheduled job to send reminder notifications to managers for pending requests.
 * 
 * Business rules:
 * - Send reminder at 16:00 (4 PM) the day before the request deadline
 * - For Monday deadlines: Remind at 16:00 Saturday
 * - Overtime requests: Remind 1 day before work_date
 * - Time-off requests: Remind 1 day before start_date
 * - Registration requests: Remind 1 day before effective_from
 * 
 * Runs daily at 16:00 (4 PM) Vietnam time
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestReminderNotificationJob {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final TimeOffRequestRepository timeOffRequestRepository;
    private final PartTimeRegistrationRepository partTimeRegistrationRepository;
    private final NotificationService notificationService;
    private final AccountRepository accountRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Send reminder notifications for pending requests
     * Runs at 16:00 (4 PM) every day (Vietnam time)
     * - For normal days: Remind about tomorrow's deadlines
     * - For Saturdays: Remind about Monday's deadlines
     */
    @Scheduled(cron = "0 0 16 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void sendReminderNotifications() {
        log.info("==== Starting reminder notification job for pending requests at 16:00 ====");
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate targetDate;
            
            // If today is Saturday, remind about Monday deadlines
            // Otherwise, remind about tomorrow's deadlines
            if (today.getDayOfWeek() == DayOfWeek.SATURDAY) {
                targetDate = today.plusDays(2); // Monday
                log.info("Saturday reminder: Checking for Monday ({}) deadlines", targetDate);
            } else {
                targetDate = today.plusDays(1); // Tomorrow
                log.info("Regular reminder: Checking for tomorrow ({}) deadlines", targetDate);
            }

            // 1. Send reminders for overtime requests
            int overtimeReminders = sendOvertimeReminders(targetDate);

            // 2. Send reminders for time-off requests
            int timeOffReminders = sendTimeOffReminders(targetDate);

            // 3. Send reminders for registration requests
            int registrationReminders = sendRegistrationReminders(targetDate);

            log.info("==== Reminder notifications completed at 16:00 ====");
            log.info("Summary: {} overtime, {} time-off, {} registration reminders sent for date: {}",
                    overtimeReminders, timeOffReminders, registrationReminders, targetDate);

        } catch (Exception e) {
            log.error("Error during reminder notification job: {}", e.getMessage(), e);
        }
    }

    /**
     * Send reminders for overtime requests due tomorrow
     */
    private int sendOvertimeReminders(LocalDate targetDate) {
        List<OvertimeRequest> requests = overtimeRequestRepository
                .findByStatusAndWorkDate(RequestStatus.PENDING, targetDate);

        int count = 0;
        for (OvertimeRequest request : requests) {
            try {
                String employeeName = request.getEmployee().getFirstName() + " " + 
                                    request.getEmployee().getLastName();
                String shiftName = request.getWorkShift().getShiftName();
                String formattedDate = targetDate.format(DATE_FORMATTER);

                String title = "Nhắc nhở: Phê duyệt yêu cầu tăng ca";
                String message = String.format(
                        "Yêu cầu tăng ca của %s cho ngày %s ca %s cần được xử lý trước ngày mai",
                        employeeName, formattedDate, shiftName);

                sendNotificationToManagers(
                        NotificationType.REQUEST_OVERTIME_PENDING,
                        title,
                        message,
                        NotificationEntityType.OVERTIME_REQUEST,
                        request.getRequestId());

                count++;
                log.info("Sent reminder for overtime request {} (employee: {}, date: {})",
                        request.getRequestId(), employeeName, formattedDate);

            } catch (Exception e) {
                log.error("Failed to send reminder for overtime request {}: {}",
                        request.getRequestId(), e.getMessage());
            }
        }

        if (count > 0) {
            log.info("Sent {} overtime request reminders", count);
        }
        
        return count;
    }

    /**
     * Send reminders for time-off requests due tomorrow
     */
    private int sendTimeOffReminders(LocalDate targetDate) {
        List<TimeOffRequest> requests = timeOffRequestRepository
                .findByStatusAndStartDate(TimeOffStatus.PENDING, targetDate);

        int count = 0;
        for (TimeOffRequest request : requests) {
            try {
                String employeeName = request.getEmployee().getFirstName() + " " + 
                                    request.getEmployee().getLastName();
                String formattedStartDate = targetDate.format(DATE_FORMATTER);
                String formattedEndDate = request.getEndDate().format(DATE_FORMATTER);

                String title = "Nhắc nhở: Phê duyệt yêu cầu nghỉ phép";
                String message = String.format(
                        "Yêu cầu nghỉ phép của %s từ %s đến %s cần được xử lý trước ngày mai",
                        employeeName, formattedStartDate, formattedEndDate);

                sendNotificationToManagers(
                        NotificationType.REQUEST_TIME_OFF_PENDING,
                        title,
                        message,
                        NotificationEntityType.TIME_OFF_REQUEST,
                        request.getRequestId());

                count++;
                log.info("Sent reminder for time-off request {} (employee: {}, dates: {} - {})",
                        request.getRequestId(), employeeName, formattedStartDate, formattedEndDate);

            } catch (Exception e) {
                log.error("Failed to send reminder for time-off request {}: {}",
                        request.getRequestId(), e.getMessage());
            }
        }

        if (count > 0) {
            log.info("Sent {} time-off request reminders", count);
        }
        
        return count;
    }

    /**
     * Send reminders for registration requests due tomorrow
     */
    private int sendRegistrationReminders(LocalDate targetDate) {
        List<PartTimeRegistration> requests = partTimeRegistrationRepository
                .findByStatusAndEffectiveFrom(RegistrationStatus.PENDING, targetDate);

        int count = 0;
        for (PartTimeRegistration request : requests) {
            try {
                String formattedDate = targetDate.format(DATE_FORMATTER);
                String formattedEndDate = request.getEffectiveTo().format(DATE_FORMATTER);

                String title = "Nhắc nhở: Phê duyệt yêu cầu đăng ký ca";
                String message = String.format(
                        "Yêu cầu đăng ký ca của nhân viên ID %d từ %s đến %s cần được xử lý trước ngày mai",
                        request.getEmployeeId(), formattedDate, formattedEndDate);

                sendNotificationToManagers(
                        NotificationType.REQUEST_PART_TIME_PENDING,
                        title,
                        message,
                        NotificationEntityType.PART_TIME_REGISTRATION,
                        request.getRegistrationId().toString());

                count++;
                log.info("Sent reminder for registration {} (employee: {}, date: {})",
                        request.getRegistrationId(), request.getEmployeeId(), formattedDate);

            } catch (Exception e) {
                log.error("Failed to send reminder for registration {}: {}",
                        request.getRegistrationId(), e.getMessage());
            }
        }

        if (count > 0) {
            log.info("Sent {} registration request reminders", count);
        }
        
        return count;
    }

    /**
     * Send notification to all ADMIN users
     */
    private void sendNotificationToManagers(
            NotificationType type,
            String title,
            String message,
            NotificationEntityType entityType,
            String entityId) {
        
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        
        for (Account admin : adminAccounts) {
            if (admin.getEmployee() != null) {
                try {
                    CreateNotificationRequest notification = CreateNotificationRequest.builder()
                            .userId(admin.getEmployee().getEmployeeId())
                            .type(type)
                            .title(title)
                            .message(message)
                            .relatedEntityType(entityType)
                            .relatedEntityId(entityId)
                            .build();

                    notificationService.createNotification(notification);
                } catch (Exception e) {
                    log.error("Failed to send notification to admin {}: {}", 
                            admin.getEmployee().getEmployeeId(), e.getMessage());
                }
            }
        }
    }
}
