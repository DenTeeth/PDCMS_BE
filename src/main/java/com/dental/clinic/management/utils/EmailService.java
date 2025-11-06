package com.dental.clinic.management.utils;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send email verification link to new user
     */
    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("XÃƒÂ¡c thÃ¡Â»Â±c tÃƒÂ i khoÃ¡ÂºÂ£n - PhÃƒÂ²ng khÃƒÂ¡m nha khoa");

            String htmlContent = String.format(
                    """
                            <html>
                            <body style="font-family: Arial, sans-serif;">
                                <h2>Xin chÃƒÂ o %s,</h2>
                                <p>CÃ¡ÂºÂ£m Ã†Â¡n bÃ¡ÂºÂ¡n Ã„â€˜ÃƒÂ£ Ã„â€˜Ã„Æ’ng kÃƒÂ½ tÃƒÂ i khoÃ¡ÂºÂ£n tÃ¡ÂºÂ¡i PhÃƒÂ²ng khÃƒÂ¡m nha khoa cÃ¡Â»Â§a chÃƒÂºng tÃƒÂ´i.</p>
                                <p>Vui lÃƒÂ²ng nhÃ¡ÂºÂ¥n vÃƒÂ o link bÃƒÂªn dÃ†Â°Ã¡Â»â€ºi Ã„â€˜Ã¡Â»Æ’ xÃƒÂ¡c thÃ¡Â»Â±c email cÃ¡Â»Â§a bÃ¡ÂºÂ¡n:</p>
                                <p><a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">XÃƒÂ¡c thÃ¡Â»Â±c email</a></p>
                                <p>HoÃ¡ÂºÂ·c copy link sau vÃƒÂ o trÃƒÂ¬nh duyÃ¡Â»â€¡t:</p>
                                <p>%s</p>
                                <p><strong>LÃ†Â°u ÃƒÂ½:</strong> Link nÃƒÂ y sÃ¡ÂºÂ½ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n sau 24 giÃ¡Â»Â.</p>
                                <p>NÃ¡ÂºÂ¿u bÃ¡ÂºÂ¡n khÃƒÂ´ng yÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜Ã„Æ’ng kÃƒÂ½ tÃƒÂ i khoÃ¡ÂºÂ£n nÃƒÂ y, vui lÃƒÂ²ng bÃ¡Â»Â qua email nÃƒÂ y.</p>
                                <br>
                                <p>TrÃƒÂ¢n trÃ¡Â»Âng,</p>
                                <p>Ã„ÂÃ¡Â»â„¢i ngÃ…Â© PhÃƒÂ²ng khÃƒÂ¡m nha khoa</p>
                            </body>
                            </html>
                            """,
                    username, verificationUrl, verificationUrl);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Ã¢Å“â€¦ Verification email sent to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Ã¢ÂÅ’ Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send password reset link to user
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Ã„ÂÃ¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u - PhÃƒÂ²ng khÃƒÂ¡m nha khoa");

            String htmlContent = String.format(
                    """
                            <html>
                            <body style="font-family: Arial, sans-serif;">
                                <h2>Xin chÃƒÂ o %s,</h2>
                                <p>ChÃƒÂºng tÃƒÂ´i nhÃ¡ÂºÂ­n Ã„â€˜Ã†Â°Ã¡Â»Â£c yÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜Ã¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u cho tÃƒÂ i khoÃ¡ÂºÂ£n cÃ¡Â»Â§a bÃ¡ÂºÂ¡n.</p>
                                <p>Vui lÃƒÂ²ng nhÃ¡ÂºÂ¥n vÃƒÂ o link bÃƒÂªn dÃ†Â°Ã¡Â»â€ºi Ã„â€˜Ã¡Â»Æ’ Ã„â€˜Ã¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u:</p>
                                <p><a href="%s" style="background-color: #2196F3; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Ã„ÂÃ¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u</a></p>
                                <p>HoÃ¡ÂºÂ·c copy link sau vÃƒÂ o trÃƒÂ¬nh duyÃ¡Â»â€¡t:</p>
                                <p>%s</p>
                                <p><strong>LÃ†Â°u ÃƒÂ½:</strong> Link nÃƒÂ y sÃ¡ÂºÂ½ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n sau 1 giÃ¡Â»Â vÃƒÂ  chÃ¡Â»â€° sÃ¡Â»Â­ dÃ¡Â»Â¥ng Ã„â€˜Ã†Â°Ã¡Â»Â£c 1 lÃ¡ÂºÂ§n.</p>
                                <p>NÃ¡ÂºÂ¿u bÃ¡ÂºÂ¡n khÃƒÂ´ng yÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜Ã¡ÂºÂ·t lÃ¡ÂºÂ¡i mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u, vui lÃƒÂ²ng bÃ¡Â»Â qua email nÃƒÂ y. TÃƒÂ i khoÃ¡ÂºÂ£n cÃ¡Â»Â§a bÃ¡ÂºÂ¡n vÃ¡ÂºÂ«n an toÃƒÂ n.</p>
                                <br>
                                <p>TrÃƒÂ¢n trÃ¡Â»Âng,</p>
                                <p>Ã„ÂÃ¡Â»â„¢i ngÃ…Â© PhÃƒÂ²ng khÃƒÂ¡m nha khoa</p>
                            </body>
                            </html>
                            """,
                    username, resetUrl, resetUrl);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Ã¢Å“â€¦ Password reset email sent to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Ã¢ÂÅ’ Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send simple text email (fallback method)
     */
    @Async
    public void sendSimpleEmail(String toEmail, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            logger.info("Ã¢Å“â€¦ Simple email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Ã¢ÂÅ’ Failed to send simple email to {}: {}", toEmail, e.getMessage());
        }
    }
}
