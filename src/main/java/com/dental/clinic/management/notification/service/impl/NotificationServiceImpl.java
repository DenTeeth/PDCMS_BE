package com.dental.clinic.management.notification.service.impl;

import com.dental.clinic.management.account.domain.Account;
import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.notification.domain.Notification;
import com.dental.clinic.management.notification.dto.CreateNotificationRequest;
import com.dental.clinic.management.notification.dto.NotificationDTO;
import com.dental.clinic.management.notification.enums.NotificationType;
import com.dental.clinic.management.notification.repository.NotificationRepository;
import com.dental.clinic.management.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AccountRepository accountRepository;

    @Override
    public NotificationDTO createNotification(CreateNotificationRequest request) {
        log.info("NotificationService.createNotification() CALLED for user: {}, type: {}", request.getUserId(),
                request.getType());

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .relatedEntityType(request.getRelatedEntityType())
                .relatedEntityId(request.getRelatedEntityId())
                .isRead(false)
                .build();

        log.info("Saving notification to database...");
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification SAVED to DB with ID: {}", savedNotification.getNotificationId());

        NotificationDTO notificationDTO = convertToDTO(savedNotification);

        // Push real-time notification qua WebSocket
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + request.getUserId(),
                    notificationDTO);
            log.info("WebSocket notification sent to user: {}", request.getUserId());
        } catch (Exception e) {
            log.warn("Failed to send WebSocket notification to user {}: {}", request.getUserId(), e.getMessage());
            // Không throw exception vì thông báo đã được lưu vào DB
        }

        return notificationDTO;
    }

    @Override
    public void markAsRead(Long notificationId, Integer userId) {
        log.info("Marking notification {} as read for user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("NOTIFICATION_NOT_FOUND",
                        "Notification not found with ID: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User không có quyền đánh dấu thông báo này");
        }

        if (!notification.getIsRead()) {
            notificationRepository.markAsRead(notificationId, userId);
            log.info("Notification {} marked as read", notificationId);
        }
    }

    @Override
    public void markAllAsRead(Integer userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Integer userId) {
        Long count = notificationRepository.countUnreadByUserId(userId);
        log.debug("Unread count for user {}: {}", userId, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Integer userId, Pageable pageable) {
        log.debug("Getting notifications for user: {}, page: {}", userId, pageable.getPageNumber());
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDTO);
    }

    @Override
    public void deleteNotification(Long notificationId, Integer userId) {
        log.info("Deleting notification {} for user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("NOTIFICATION_NOT_FOUND",
                        "Notification not found with ID: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User không có quyền xóa thông báo này");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted successfully", notificationId);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    @Override
    public void createTimeOffRequestNotification(String employeeName, String requestId, String startDate,
            String endDate) {
        log.info("Creating TIME_OFF_REQUEST notifications for ADMIN and MANAGER users, employee: {}", employeeName);

        String title = "Yeu cau nghi phep tu " + employeeName;
        String message = employeeName + " da gui yeu cau nghi phep tu " + startDate + " den " + endDate;

        // Find all ADMIN and MANAGER accounts (both can approve time-off requests)
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        List<Account> managerAccounts = accountRepository.findByRole_RoleName("MANAGER");
        
        // Combine both lists
        List<Account> approverAccounts = new java.util.ArrayList<>(adminAccounts);
        approverAccounts.addAll(managerAccounts);
        
        for (Account account : approverAccounts) {
            if (account.getEmployee() != null) {
                CreateNotificationRequest request = CreateNotificationRequest.builder()
                        .userId(account.getEmployee().getEmployeeId())
                        .type(NotificationType.REQUEST_TIME_OFF_PENDING)
                        .title(title)
                        .message(message)
                        .relatedEntityType(
                                com.dental.clinic.management.notification.enums.NotificationEntityType.TIME_OFF_REQUEST)
                        .relatedEntityId(requestId)
                        .build();

                createNotification(request);
                log.debug("Sent TIME_OFF notification to {} (role: {})", 
                        account.getEmployee().getEmployeeCode(), account.getRole().getRoleName());
            }
        }
        log.info("Sent {} TIME_OFF_REQUEST notifications to approvers", approverAccounts.size());
    }

    @Override
    public void createOvertimeRequestNotification(String employeeName, String requestId, String workDate,
            String shiftName) {
        log.info("Creating OVERTIME_REQUEST notifications for ADMIN and MANAGER users, employee: {}", employeeName);

        String title = "Yeu cau tang ca tu " + employeeName;
        String message = employeeName + " da gui yeu cau tang ca ngay " + workDate + " ca " + shiftName;

        // Find all ADMIN and MANAGER accounts (both can approve overtime requests)
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        List<Account> managerAccounts = accountRepository.findByRole_RoleName("MANAGER");
        
        // Combine both lists
        List<Account> approverAccounts = new java.util.ArrayList<>(adminAccounts);
        approverAccounts.addAll(managerAccounts);
        
        for (Account account : approverAccounts) {
            if (account.getEmployee() != null) {
                CreateNotificationRequest request = CreateNotificationRequest.builder()
                        .userId(account.getEmployee().getEmployeeId())
                        .type(NotificationType.REQUEST_OVERTIME_PENDING)
                        .title(title)
                        .message(message)
                        .relatedEntityType(
                                com.dental.clinic.management.notification.enums.NotificationEntityType.OVERTIME_REQUEST)
                        .relatedEntityId(requestId)
                        .build();

                createNotification(request);
                log.debug("Sent OVERTIME notification to {} (role: {})", 
                        account.getEmployee().getEmployeeCode(), account.getRole().getRoleName());
            }
        }
        log.info("Sent {} OVERTIME_REQUEST notifications to approvers", approverAccounts.size());
    }

    @Override
    public void createPartTimeRequestNotification(String employeeName, Integer registrationId, String effectiveFrom,
            String effectiveTo) {
        log.info("Creating PART_TIME_REGISTRATION notifications for ADMIN and MANAGER users, employee: {}", employeeName);

        String title = "Yeu cau dang ky part-time tu " + employeeName;
        String message = employeeName + " da gui yeu cau dang ky part-time tu " + effectiveFrom + " den " + effectiveTo;

        // Find all ADMIN and MANAGER accounts (both can approve part-time registrations)
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        List<Account> managerAccounts = accountRepository.findByRole_RoleName("MANAGER");
        
        // Combine both lists
        List<Account> approverAccounts = new java.util.ArrayList<>(adminAccounts);
        approverAccounts.addAll(managerAccounts);
        
        for (Account account : approverAccounts) {
            if (account.getEmployee() != null) {
                CreateNotificationRequest request = CreateNotificationRequest.builder()
                        .userId(account.getEmployee().getEmployeeId())
                        .type(NotificationType.REQUEST_PART_TIME_PENDING)
                        .title(title)
                        .message(message)
                        .relatedEntityType(
                                com.dental.clinic.management.notification.enums.NotificationEntityType.PART_TIME_REGISTRATION)
                        .relatedEntityId(String.valueOf(registrationId))
                        .build();

                createNotification(request);
                log.debug("Sent PART_TIME notification to {} (role: {})", 
                        account.getEmployee().getEmployeeCode(), account.getRole().getRoleName());
            }
        }
        log.info("Sent {} PART_TIME_REGISTRATION notifications to approvers", approverAccounts.size());
    }
}
