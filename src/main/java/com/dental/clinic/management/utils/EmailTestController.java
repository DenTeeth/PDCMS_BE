package com.dental.clinic.management.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * 🧪 EMAIL TESTING CONTROLLER
 * Use this to test email sending in production with detailed error logging
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test/email")
@RequiredArgsConstructor
public class EmailTestController {

    private final JavaMailSender mailSender;
    private final EmailService emailService;
    private final ResendEmailService resendEmailService;

    @Value("${app.mail.from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.mail.from-name:Phòng khám nha khoa DenTeeth}")
    private String fromName;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String smtpHost;

    @Value("${spring.mail.port:587}")
    private int smtpPort;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    /**
     * GET /api/v1/test/email/config
     * Show current email configuration (without exposing password)
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getEmailConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("smtpHost", smtpHost);
        config.put("smtpPort", smtpPort);
        config.put("smtpUsername", smtpUsername);
        config.put("fromEmail", fromEmail);
        config.put("fromName", fromName);
        config.put("passwordConfigured", smtpUsername != null && !smtpUsername.isEmpty());

        log.info("📧 Email Config Check:");
        log.info("  SMTP Host: {}", smtpHost);
        log.info("  SMTP Port: {}", smtpPort);
        log.info("  SMTP Username: {}", smtpUsername);
        log.info("  From Email: {}", fromEmail);
        log.info("  From Name: {}", fromName);

        return ResponseEntity.ok(config);
    }

    /**
     * POST /api/v1/test/email/send
     * Send a test email directly (SYNCHRONOUS - not @Async)
     * Body: { "to": "recipient@example.com" }
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestBody Map<String, String> request) {
        String toEmail = request.get("to");
        Map<String, Object> result = new HashMap<>();

        if (toEmail == null || toEmail.isEmpty()) {
            result.put("success", false);
            result.put("error", "Recipient email is required");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            log.info("🧪 Attempting to send test email to: {}", toEmail);
            log.info("📧 SMTP Configuration:");
            log.info("  Host: {}", smtpHost);
            log.info("  Port: {}", smtpPort);
            log.info("  Username: {}", smtpUsername);
            log.info("  From: {} <{}>", fromName, fromEmail);

            // Create email message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("🧪 Test Email from PDCMS Backend");

            String htmlContent = String.format("""
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2>✅ Email Test Successful!</h2>
                        <p>This is a test email from PDCMS Backend application.</p>
                        <hr>
                        <p><strong>Configuration:</strong></p>
                        <ul>
                            <li>SMTP Host: %s</li>
                            <li>SMTP Port: %d</li>
                            <li>From: %s &lt;%s&gt;</li>
                            <li>To: %s</li>
                        </ul>
                        <p>If you received this email, your email configuration is working correctly! ✅</p>
                    </body>
                    </html>
                    """, smtpHost, smtpPort, fromName, fromEmail, toEmail);

            helper.setText(htmlContent, true);

            // Send email SYNCHRONOUSLY to catch errors immediately
            log.info("📤 Sending email...");
            mailSender.send(message);
            log.info("✅ Test email sent successfully to: {}", toEmail);

            result.put("success", true);
            result.put("message", "Email sent successfully");
            result.put("to", toEmail);
            result.put("from", fromEmail);
            result.put("smtpHost", smtpHost);
            result.put("smtpPort", smtpPort);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Failed to send test email to {}: {}", toEmail, e.getMessage());
            log.error("❌ Full stack trace:", e);

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("smtpHost", smtpHost);
            result.put("smtpPort", smtpPort);
            result.put("fromEmail", fromEmail);

            // Additional debugging info
            if (e.getCause() != null) {
                result.put("cause", e.getCause().getMessage());
            }

            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * POST /api/v1/test/email/send-async
     * Test async email sending (using EmailService @Async method)
     * Body: { "to": "recipient@example.com" }
     */
    @PostMapping("/send-async")
    public ResponseEntity<Map<String, Object>> sendTestEmailAsync(@RequestBody Map<String, String> request) {
        String toEmail = request.get("to");
        Map<String, Object> result = new HashMap<>();

        if (toEmail == null || toEmail.isEmpty()) {
            result.put("success", false);
            result.put("error", "Recipient email is required");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            log.info("🧪 Sending async test email to: {}", toEmail);

            // Use EmailService sendWelcomeEmailWithPasswordSetup method
            String testToken = "test-token-12345";
            emailService.sendWelcomeEmailWithPasswordSetup(toEmail, "Test User", testToken);

            result.put("success", true);
            result.put("message", "Async email queued successfully");
            result.put("to", toEmail);
            result.put("note", "Check logs for actual sending status (async method)");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Failed to queue async email: {}", e.getMessage(), e);

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * POST /api/v1/test/email/send-welcome
     * Test the exact welcome email flow used in patient creation
     * Body: { "email": "patient@example.com", "name": "John Doe", "token":
     * "test-token" }
     */
    @PostMapping("/send-welcome")
    public ResponseEntity<Map<String, Object>> sendWelcomeEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String name = request.getOrDefault("name", "Test Patient");
        String token = request.getOrDefault("token", "test-token-" + System.currentTimeMillis());

        Map<String, Object> result = new HashMap<>();

        if (email == null || email.isEmpty()) {
            result.put("success", false);
            result.put("error", "Email is required");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            log.info("🧪 Testing welcome email to: {} (name: {}, token: {})", email, name, token);

            emailService.sendWelcomeEmailWithPasswordSetup(email, name, token);

            result.put("success", true);
            result.put("message", "Welcome email sent successfully");
            result.put("email", email);
            result.put("name", name);
            result.put("token", token);
            result.put("note", "Check recipient inbox and logs for confirmation");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Failed to send welcome email: {}", e.getMessage(), e);

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * POST /api/v1/test/email/resend-welcome
     * Test Resend email service - send welcome email
     * 
     * Example: POST /api/v1/test/email/resend-welcome?email=congthaino0@gmail.com&name=Test&token=abc123
     */
    @PostMapping("/resend-welcome")
    public ResponseEntity<Map<String, Object>> testResendWelcomeEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "Test Patient") String name,
            @RequestParam(defaultValue = "test-token-123") String token) {

        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🧪 [TEST] Sending welcome email via Resend to: {}", email);
            log.info("🧪 [TEST] Name: {}, Token: {}", name, token);

            resendEmailService.sendWelcomeEmailWithPasswordSetup(email, name, token);

            result.put("success", true);
            result.put("message", "✅ Welcome email sent successfully via Resend");
            result.put("email", email);
            result.put("name", name);
            result.put("token", token);
            result.put("provider", "Resend");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ [TEST] Failed to send welcome email via Resend: {}", e.getMessage(), e);

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("provider", "Resend");

            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * POST /api/v1/test/email/resend-reset
     * Test Resend email service - send password reset email
     * 
     * Example: POST /api/v1/test/email/resend-reset?email=congthaino0@gmail.com&username=test&token=abc123
     */
    @PostMapping("/resend-reset")
    public ResponseEntity<Map<String, Object>> testResendPasswordReset(
            @RequestParam String email,
            @RequestParam(defaultValue = "test") String username,
            @RequestParam(defaultValue = "test-token-123") String token) {

        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🧪 [TEST] Sending password reset email via Resend to: {}", email);

            resendEmailService.sendPasswordResetEmail(email, username, token);

            result.put("success", true);
            result.put("message", "✅ Password reset email sent successfully via Resend");
            result.put("email", email);
            result.put("provider", "Resend");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ [TEST] Failed to send password reset email via Resend: {}", e.getMessage(), e);

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("provider", "Resend");

            return ResponseEntity.status(500).body(result);
        }
    }
}
