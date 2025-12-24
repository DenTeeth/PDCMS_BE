package com.dental.clinic.management.notification.service;

import com.dental.clinic.management.notification.dto.CreateNotificationRequest;
import com.dental.clinic.management.notification.dto.NotificationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Tạo thông báo mới và lưu vào database
     *
     * @param request thông tin thông báo
     * @return NotificationDTO
     */
    NotificationDTO createNotification(CreateNotificationRequest request);

    /**
     * Đánh dấu một thông báo là đã đọc
     *
     * @param notificationId ID thông báo
     * @param userId         ID người dùng
     */
    void markAsRead(Long notificationId, Integer userId);

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     *
     * @param userId ID người dùng
     */
    void markAllAsRead(Integer userId);

    /**
     * Lấy số lượng thông báo chưa đọc của user
     *
     * @param userId ID người dùng
     * @return số lượng thông báo chưa đọc
     */
    Long getUnreadCount(Integer userId);

    /**
     * Lấy danh sách thông báo của user (có phân trang)
     *
     * @param userId   ID người dùng
     * @param pageable thông tin phân trang
     * @return Page<NotificationDTO>
     */
    Page<NotificationDTO> getUserNotifications(Integer userId, Pageable pageable);

    /**
     * Xóa thông báo
     *
     * @param notificationId ID thông báo
     * @param userId         ID người dùng
     */
    void deleteNotification(Long notificationId, Integer userId);

    /**
     * Tạo thông báo cho yêu cầu nghỉ phép mới
     *
     * @param employeeName Tên nhân viên gửi yêu cầu
     * @param requestId    ID của time-off request
     * @param startDate    Ngày bắt đầu nghỉ
     * @param endDate      Ngày kết thúc nghỉ
     */
    void createTimeOffRequestNotification(String employeeName, String requestId, String startDate, String endDate);

    /**
     * Tạo thông báo cho yêu cầu tăng ca mới
     *
     * @param employeeName Tên nhân viên gửi yêu cầu
     * @param requestId    ID của overtime request
     * @param workDate     Ngày tăng ca
     * @param shiftName    Tên ca làm việc
     */
    void createOvertimeRequestNotification(String employeeName, String requestId, String workDate, String shiftName);

    /**
     * Tạo thông báo cho đăng ký part-time mới
     *
     * @param employeeName   Tên nhân viên gửi yêu cầu
     * @param registrationId ID của part-time registration
     * @param effectiveFrom  Ngày bắt đầu hiệu lực
     * @param effectiveTo    Ngày kết thúc hiệu lực
     */
    void createPartTimeRequestNotification(String employeeName, Integer registrationId, String effectiveFrom,
            String effectiveTo);
}
