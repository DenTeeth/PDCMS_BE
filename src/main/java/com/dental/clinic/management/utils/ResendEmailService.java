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

/**
 * Email service using Resend API
 * Replaces SendGrid with Resend for email sending
 */
@Service
public class ResendEmailService {

    private static final Logger logger = LoggerFactory.getLogger(ResendEmailService.class);

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
        logger.info("🔧 [Resend] Initializing Resend client with API key: {}...",
                resendApiKey != null ? resendApiKey.substring(0, Math.min(10, resendApiKey.length())) + "..." : "NULL");

        if (resendApiKey == null || resendApiKey.isEmpty()) {
            logger.error("❌ [Resend] API key is missing! Check RESEND_API_KEY environment variable.");
            throw new IllegalStateException("Resend API key is not configured");
        }

        this.resend = new Resend(resendApiKey);
        logger.info("✅ [Resend] Client initialized successfully");
    }

    /**
     * Send welcome email to new patient with password setup link
     * NOTE: @Async REMOVED temporarily to allow exception to be caught
     */
    public void sendWelcomeEmailWithPasswordSetup(String toEmail, String patientName, String token) {
        try {
            logger.info("📧 [Resend] Preparing welcome email to: {}", toEmail);

            String setupPasswordUrl = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = String.format(
                    """
                            <html>
                            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                                <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;">
                                    <div style="background-color: #fff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                                        <h2 style="color: #2196F3; margin-bottom: 20px;">Chào mừng đến với Phòng khám nha khoa DenTeeth!</h2>
                                        <p>Xin chào <strong>%s</strong>,</p>
                                        <p>Hồ sơ bệnh nhân của bạn đã được tạo thành công tại phòng khám của chúng tôi.</p>
                                        <p>Để hoàn tất quá trình đăng ký và có thể truy cập hệ thống, vui lòng nhấn vào nút bên dưới để thiết lập mật khẩu cho tài khoản của bạn:</p>

                                        <div style="text-align: center; margin: 30px 0;">
                                            <a href="%s" style="background-color: #4CAF50; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                                                Thiết lập mật khẩu
                                            </a>
                                        </div>

                                        <p style="color: #666; font-size: 14px;">Hoặc copy link sau vào trình duyệt:</p>
                                        <p style="background-color: #f5f5f5; padding: 10px; border-left: 4px solid #2196F3; word-break: break-all; font-size: 12px;">%s</p>

                                        <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;">
                                            <p style="margin: 0; color: #856404;"><strong>Lưu ý quan trọng:</strong></p>
                                            <ul style="margin: 10px 0; padding-left: 20px; color: #856404;">
                                                <li>Link này sẽ hết hạn sau <strong>24 giờ</strong></li>
                                                <li>Mật khẩu của bạn cần có ít nhất 8 ký tự</li>
                                                <li>Nên sử dụng kết hợp chữ hoa, chữ thường, số và ký tự đặc biệt</li>
                                            </ul>
                                        </div>

                                        <p>Sau khi thiết lập mật khẩu, bạn có thể:</p>
                                        <ul style="color: #666;">
                                            <li>Xem lịch sử khám bệnh</li>
                                            <li>Đặt lịch hẹn online</li>
                                            <li>Xem kế hoạch điều trị</li>
                                            <li>Cập nhật thông tin cá nhân</li>
                                        </ul>

                                        <p style="margin-top: 30px;">Nếu bạn không yêu cầu đăng ký tài khoản này, vui lòng liên hệ với chúng tôi ngay.</p>

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
                    patientName, setupPasswordUrl, setupPasswordUrl);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromName + " <" + fromEmail + ">")
                    .to(toEmail)
                    .replyTo(replyToEmail)
                    .subject("Chào mừng đến với Phòng khám nha khoa - Thiết lập mật khẩu")
                    .html(htmlContent)
                    .build();

            logger.info("📧 [Resend] Sending email to: {}", toEmail);
            CreateEmailResponse data = resend.emails().send(params);
            logger.info("✅ [Resend] Email sent successfully! ID: {}", data.getId());

        } catch (ResendException e) {
            logger.error("❌ [Resend] Failed to send welcome email to {}: {}", toEmail, e.getMessage());
            logger.error("❌ [Resend] Error details:", e);
            throw new RuntimeException("Đã xảy ra lỗi khi gửi email chào mừng qua Resend", e);
        } catch (Exception e) {
            logger.error("❌ [Resend] Unexpected error sending email to {}: {}", toEmail, e.getMessage());
            logger.error("❌ [Resend] Error details:", e);
            throw new RuntimeException("Lỗi không xác định khi gửi email", e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        try {
            logger.info("📧 [Resend] Preparing password reset email to: {}", toEmail);

            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = String.format(
                    """
                            <html>
                            <body style="font-family: Arial, sans-serif;">
                                <h2>Xin chào %s,</h2>
                                <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                                <p>Vui lòng nhấn vào link bên dưới để đặt lại mật khẩu:</p>
                                <p><a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Đặt lại mật khẩu</a></p>
                                <p>Hoặc copy link sau vào trình duyệt:</p>
                                <p>%s</p>
                                <p><strong>Lưu ý:</strong> Link này sẽ hết hạn sau 24 giờ.</p>
                                <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                                <br>
                                <p>Trân trọng,</p>
                                <p>Đội ngũ Phòng khám nha khoa</p>
                            </body>
                            </html>
                            """,
                    username, resetUrl, resetUrl);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromName + " <" + fromEmail + ">")
                    .to(toEmail)
                    .replyTo(replyToEmail)
                    .subject("Đặt lại mật khẩu - Phòng khám nha khoa")
                    .html(htmlContent)
                    .build();

            logger.info("📧 [Resend] Sending password reset email to: {}", toEmail);
            CreateEmailResponse data = resend.emails().send(params);
            logger.info("✅ [Resend] Password reset email sent successfully! ID: {}", data.getId());

        } catch (ResendException e) {
            logger.error("❌ [Resend] Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Đã xảy ra lỗi khi gửi email đặt lại mật khẩu", e);
        }
    }

    /**
     * Send email verification
     */
    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;

            String htmlContent = String.format(
                    """
                            <html>
                            <body style="font-family: Arial, sans-serif;">
                                <h2>Xin chào %s,</h2>
                                <p>Cảm ơn bạn đã đăng ký tài khoản tại Phòng khám nha khoa của chúng tôi.</p>
                                <p>Vui lòng nhấn vào link bên dưới để xác thực email của bạn:</p>
                                <p><a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Xác thực email</a></p>
                                <p>Hoặc copy link sau vào trình duyệt:</p>
                                <p>%s</p>
                                <p><strong>Lưu ý:</strong> Link này sẽ hết hạn sau 24 giờ.</p>
                                <p>Nếu bạn không yêu cầu đăng ký tài khoản này, vui lòng bỏ qua email này.</p>
                                <br>
                                <p>Trân trọng,</p>
                                <p>Đội ngũ Phòng khám nha khoa</p>
                            </body>
                            </html>
                            """,
                    username, verificationUrl, verificationUrl);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromName + " <" + fromEmail + ">")
                    .to(toEmail)
                    .replyTo(replyToEmail)
                    .subject("Xác thực tài khoản - Phòng khám nha khoa")
                    .html(htmlContent)
                    .build();

            @SuppressWarnings("unused")
            CreateEmailResponse data = resend.emails().send(params);
            logger.info("✅ [Resend] Verification email sent to: {}", toEmail);

        } catch (ResendException e) {
            logger.error("❌ [Resend] Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }
}
