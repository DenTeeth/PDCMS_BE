package com.dental.clinic.management.utils;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Email service specifically for appointment-related emails
 * Uses Resend API for sending confirmation and reminder emails
 */
@Service
public class AppointmentEmailService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentEmailService.class);
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'lúc' HH:mm");

    @Value("${app.resend.api-key}")
    private String resendApiKey;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.mail.from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.mail.from-name:Phòng khám nha khoa DenTeeth}")
    private String fromName;

    @Value("${app.mail.reply-to:hellodenteeth@gmail.com}")
    private String replyToEmail;

    private Resend resend;

    @PostConstruct
    public void init() {
        logger.info("🔧 [AppointmentEmail] Initializing Resend client");
        if (resendApiKey == null || resendApiKey.isEmpty()) {
            logger.error("❌ [AppointmentEmail] API key is missing!");
            throw new IllegalStateException("Resend API key is not configured");
        }
        this.resend = new Resend(resendApiKey);
        logger.info("✅ [AppointmentEmail] Client initialized successfully");
    }

    /**
     * Send confirmation email immediately after appointment is booked
     * BR-17: Email xác nhận ngay khi đặt lịch
     */
    @Async
    public void sendAppointmentConfirmation(
            String toEmail,
            String patientName,
            String appointmentCode,
            LocalDateTime appointmentStartTime,
            String doctorName,
            String roomName,
            String serviceNames) {
        
        try {
            logger.info("📧 [AppointmentEmail] Sending CONFIRMATION to: {} for appointment: {}", 
                toEmail, appointmentCode);

            String formattedTime = appointmentStartTime.format(DISPLAY_FORMATTER);
            String appointmentUrl = frontendUrl + "/appointments/" + appointmentCode;

            String htmlContent = String.format(
                """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;">
                        <div style="background-color: #fff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                            <h2 style="color: #4CAF50; margin-bottom: 20px;">✅ Đặt lịch hẹn thành công!</h2>
                            
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Lịch hẹn của bạn đã được xác nhận thành công tại Phòng khám nha khoa DenTeeth.</p>
                            
                            <div style="background-color: #f0f8ff; border-left: 4px solid #2196F3; padding: 15px; margin: 20px 0;">
                                <h3 style="margin-top: 0; color: #2196F3;">Thông tin lịch hẹn</h3>
                                <table style="width: 100%%; border-collapse: collapse;">
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Mã lịch hẹn:</strong></td>
                                        <td style="padding: 8px 0;"><strong style="color: #2196F3;">%s</strong></td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Thời gian:</strong></td>
                                        <td style="padding: 8px 0;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Bác sĩ:</strong></td>
                                        <td style="padding: 8px 0;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Phòng khám:</strong></td>
                                        <td style="padding: 8px 0;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Dịch vụ:</strong></td>
                                        <td style="padding: 8px 0;">%s</td>
                                    </tr>
                                </table>
                            </div>

                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #2196F3; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                                    Xem chi tiết lịch hẹn
                                </a>
                            </div>

                            <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;">
                                <p style="margin: 0; color: #856404;"><strong>Lưu ý quan trọng:</strong></p>
                                <ul style="margin: 10px 0; padding-left: 20px; color: #856404;">
                                    <li>Vui lòng đến <strong>trước 10 phút</strong> để làm thủ tục</li>
                                    <li>Mang theo <strong>CMND/CCCD</strong> và các xét nghiệm liên quan (nếu có)</li>
                                    <li>Nếu không thể đến, vui lòng <strong>hủy lịch trước 24 giờ</strong></li>
                                    <li>Bạn sẽ nhận được email nhắc nhở trước 24 giờ</li>
                                </ul>
                            </div>

                            <p style="margin-top: 30px;">Cần hỗ trợ? Liên hệ với chúng tôi:</p>
                            <p style="color: #666;">
                                Hotline: <strong>028-1234-5678</strong><br>
                                Email: <strong>%s</strong>
                            </p>

                            <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">

                            <p style="color: #666; font-size: 14px; margin-bottom: 0;">Trân trọng,</p>
                            <p style="color: #2196F3; font-weight: bold; margin-top: 5px;">Đội ngũ Phòng khám nha khoa DenTeeth</p>
                        </div>
                        <p style="text-align: center; color: #999; font-size: 12px; margin-top: 20px;">
                            © 2026 Phòng khám nha khoa DenTeeth. All rights reserved.
                        </p>
                    </div>
                </body>
                </html>
                """,
                patientName, appointmentCode, formattedTime, doctorName, roomName, serviceNames,
                appointmentUrl, replyToEmail
            );

            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromName + " <" + fromEmail + ">")
                .to(toEmail)
                .replyTo(replyToEmail)
                .subject("Xác nhận lịch hẹn " + appointmentCode + " - " + formattedTime)
                .html(htmlContent)
                .build();

            CreateEmailResponse data = resend.emails().send(params);
            logger.info("✅ [AppointmentEmail] CONFIRMATION email sent! ID: {}", data.getId());

        } catch (ResendException e) {
            logger.error("❌ [AppointmentEmail] Failed to send confirmation email: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("❌ [AppointmentEmail] Unexpected error: {}", e.getMessage(), e);
        }
    }

    /**
     * Send reminder email 24 hours before appointment
     * BR-17: Email nhắc nhở trước 24h
     */
    @Async
    public void sendAppointmentReminder(
            String toEmail,
            String patientName,
            String appointmentCode,
            LocalDateTime appointmentStartTime,
            String doctorName,
            String roomName,
            String serviceNames) {
        
        try {
            logger.info("📧 [AppointmentEmail] Sending 24H REMINDER to: {} for appointment: {}", 
                toEmail, appointmentCode);

            String formattedTime = appointmentStartTime.format(DISPLAY_FORMATTER);
            String appointmentUrl = frontendUrl + "/appointments/" + appointmentCode;

            String htmlContent = String.format(
                """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;">
                        <div style="background-color: #fff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                            <h2 style="color: #ff9800; margin-bottom: 20px;">🔔 Nhắc nhở: Lịch hẹn sắp tới!</h2>
                            
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Đây là email nhắc nhở về lịch hẹn của bạn tại Phòng khám nha khoa DenTeeth.</p>
                            
                            <div style="background-color: #fff3e0; border-left: 4px solid #ff9800; padding: 15px; margin: 20px 0;">
                                <h3 style="margin-top: 0; color: #ff9800;">⏰ Lịch hẹn của bạn</h3>
                                <table style="width: 100%%; border-collapse: collapse;">
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Mã lịch hẹn:</strong></td>
                                        <td style="padding: 8px 0;"><strong style="color: #ff9800;">%s</strong></td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Thời gian:</strong></td>
                                        <td style="padding: 8px 0;"><strong style="color: #d84315;">%s</strong></td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Bác sĩ:</strong></td>
                                        <td style="padding: 8px 0;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Phòng khám:</strong></td>
                                        <td style="padding: 8px 0;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #666;"><strong>Dịch vụ:</strong></td>
                                        <td style="padding: 8px 0;">%s</td>
                                    </tr>
                                </table>
                            </div>

                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #ff9800; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                                    Xem chi tiết lịch hẹn
                                </a>
                            </div>

                            <div style="background-color: #ffebee; border-left: 4px solid #f44336; padding: 15px; margin: 20px 0;">
                                <p style="margin: 0; color: #c62828;"><strong>Nhắc nhở quan trọng:</strong></p>
                                <ul style="margin: 10px 0; padding-left: 20px; color: #c62828;">
                                    <li><strong>Đến trước 10 phút</strong> để làm thủ tục</li>
                                    <li>Mang theo <strong>CMND/CCCD</strong> và các xét nghiệm liên quan (nếu có)</li>
                                    <li>Nếu không thể đến, vui lòng <strong>hủy lịch ngay</strong> để người khác có cơ hội</li>
                                </ul>
                            </div>

                            <p style="margin-top: 30px;">Cần đổi lịch hoặc hủy? Liên hệ ngay:</p>
                            <p style="color: #666;">
                                Hotline: <strong>028-1234-5678</strong><br>
                                Email: <strong>%s</strong>
                            </p>

                            <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">

                            <p style="color: #666; font-size: 14px; margin-bottom: 0;">Chúng tôi rất mong được phục vụ bạn!</p>
                            <p style="color: #2196F3; font-weight: bold; margin-top: 5px;">Đội ngũ Phòng khám nha khoa DenTeeth</p>
                        </div>
                        <p style="text-align: center; color: #999; font-size: 12px; margin-top: 20px;">
                            © 2026 Phòng khám nha khoa DenTeeth. All rights reserved.
                        </p>
                    </div>
                </body>
                </html>
                """,
                patientName, appointmentCode, formattedTime, doctorName, roomName, serviceNames,
                appointmentUrl, replyToEmail
            );

            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromName + " <" + fromEmail + ">")
                .to(toEmail)
                .replyTo(replyToEmail)
                .subject("🔔 Nhắc nhở: Lịch hẹn " + appointmentCode + " sắp tới - " + formattedTime)
                .html(htmlContent)
                .build();

            CreateEmailResponse data = resend.emails().send(params);
            logger.info("✅ [AppointmentEmail] 24H REMINDER email sent! ID: {}", data.getId());

        } catch (ResendException e) {
            logger.error("❌ [AppointmentEmail] Failed to send reminder email: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("❌ [AppointmentEmail] Unexpected error: {}", e.getMessage(), e);
        }
    }
}
