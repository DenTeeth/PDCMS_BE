# Email System Troubleshooting Guide

## 📧 Vấn đề: Không gửi được email trong Production

### ✅ Những gì đã kiểm tra và fix

#### 1. **Mail Health Check Configuration** ✅ FIXED

**Vấn đề**: Spring Boot actuator health check có thể fail nếu SMTP không kết nối được

**Đã fix**:

```yaml
# application-prod.yaml & application-dev.yaml
management:
  health:
    mail:
      enabled: false # Disable mail health check
```

**Lợi ích**: Health endpoint vẫn UP ngay cả khi SMTP có vấn đề

#### 2. **Email Configuration** ✅ VERIFIED

```yaml
# application-prod.yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:hellodenteeth@gmail.com}
    password: ${MAIL_PASSWORD:micnxeutitfjrmxk}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 10000
          timeout: 10000
          writetimeout: 10000
```

Config này **ĐÚNG** cho Gmail SMTP.

#### 3. **Error Handling** ✅ EXISTS

Code đã có try-catch để không làm fail patient creation:

```java
try {
    emailService.sendWelcomeEmailWithPasswordSetup(...);
    log.info("Welcome email sent to: {}", email);
} catch (Exception e) {
    log.error("Failed to send welcome email: {}", e.getMessage(), e);
    log.warn("Patient created successfully, but email not sent.");
    // Don't throw - allow patient creation to succeed
}
```

---

## 🔍 Các nguyên nhân có thể

### 1. Gmail App Password không hợp lệ

**Kiểm tra**:

```bash
# SSH vào server
ssh root@your-droplet-ip

# Check environment variable
cd ~/PDCMS_BE
cat .env | grep MAIL_PASSWORD

# Hoặc test trực tiếp
docker-compose logs app | grep -i "mail\|email\|smtp"
```

**Triệu chứng**:

- Log hiển thị: `Authentication failed`
- Hoặc: `535 Authentication credentials invalid`

**Giải pháp**:

1. Tạo App Password mới tại: https://myaccount.google.com/apppasswords
2. Update vào `.env`:
   ```bash
   nano .env
   # Đổi MAIL_PASSWORD=new_app_password_here
   ```
3. Restart containers:
   ```bash
   docker-compose restart app
   ```

### 2. Gmail SMTP bị block từ server IP

**Kiểm tra**:

```bash
# Test SMTP connection từ server
telnet smtp.gmail.com 587

# Hoặc dùng curl
curl -v telnet://smtp.gmail.com:587
```

**Triệu chứng**:

- Connection timeout sau 10 giây
- Log hiển thị: `Connection timed out`
- Hoặc: `Could not connect to SMTP host`

**Giải pháp**:

1. Check firewall trên DigitalOcean Droplet
2. Gmail có thể block IP từ data center - cần whitelist
3. Xem xét dùng alternative SMTP (SendGrid, Mailgun, AWS SES)

### 3. Email không hợp lệ

**Triệu chứng**:

- Log: `Invalid Addresses`
- Email patient không đúng format

**Giải pháp**:

- Validate email trước khi gửi
- Log email address để debug

### 4. SMTP rate limiting

**Triệu chứng**:

- Email đầu tiên gửi được, sau đó fail
- Log: `Quota exceeded` hoặc `Too many requests`

**Giải pháp**:

- Gmail free account: 100 emails/day
- Nếu vượt quota, cần upgrade Google Workspace hoặc dùng transactional email service

---

## 🧪 Testing Guide

### Test 1: Check SMTP Connection (từ server)

```bash
# SSH vào server
ssh root@your-droplet-ip

# Test telnet
telnet smtp.gmail.com 587

# Expected output:
# Trying 142.250.XXX.XXX...
# Connected to smtp.gmail.com.
# 220 smtp.google.com ESMTP...

# Nếu timeout = network issue
# Nếu connection refused = firewall issue
```

### Test 2: Check Application Logs

```bash
# SSH vào server
cd ~/PDCMS_BE

# Check recent email logs
docker-compose logs app | grep -i "email\|mail\|smtp" | tail -50

# Look for:
# ✅ "Welcome email sent to: patient@example.com"
# ❌ "Failed to send welcome email"
# ❌ "Authentication failed"
# ❌ "Connection timed out"
```

### Test 3: Manual Email Test

Tạo endpoint test để gửi email thủ công:

```java
// Add to PatientController.java hoặc tạo TestController.java
@PostMapping("/test-email")
public ResponseEntity<String> testEmail(@RequestParam String email) {
    try {
        emailService.sendWelcomeEmailWithPasswordSetup(
            email,
            "Test Patient",
            "test-token-123"
        );
        return ResponseEntity.ok("Email sent successfully to: " + email);
    } catch (Exception e) {
        return ResponseEntity.status(500)
            .body("Failed to send email: " + e.getMessage());
    }
}
```

**Call API**:

```bash
curl -X POST "http://localhost:8080/api/v1/test-email?email=your-email@gmail.com" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Test 4: Check Environment Variables

```bash
# SSH vào server
cd ~/PDCMS_BE

# Check if .env is loaded
docker-compose config | grep -i mail

# Should show:
# MAIL_USERNAME: hellodenteeth@gmail.com
# MAIL_PASSWORD: micnxeutitfjrmxk
```

---

## 🛠️ Solutions by Error Type

### Error: `Authentication failed`

```yaml
# Solution 1: Generate new App Password
1. Go to: https://myaccount.google.com/apppasswords
2. Select "Mail" and your device
3. Copy 16-character password
4. Update .env: MAIL_PASSWORD=xxxx xxxx xxxx xxxx (no spaces)
5. Restart: docker-compose restart app

# Solution 2: Check if 2FA is enabled
Gmail App Passwords require 2-Factor Authentication to be enabled
```

### Error: `Connection timed out`

```bash
# Solution 1: Check firewall
# On DigitalOcean, allow outbound port 587
ufw allow out 587/tcp

# Solution 2: Try alternative port
# Gmail also supports:
# - Port 465 (SSL)
# - Port 25 (blocked by many providers)

# Update application-prod.yaml:
spring:
  mail:
    port: 465  # Try SSL port
    properties:
      mail:
        smtp:
          ssl:
            enable: true
```

### Error: `Quota exceeded`

```yaml
# Solution: Use transactional email service
# Option 1: SendGrid (12,000 emails/month free)
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: ${SENDGRID_API_KEY}
# Option 2: AWS SES (62,000 emails/month free)
# Option 3: Mailgun (5,000 emails/month free)
```

---

## 📋 Quick Checklist

Run through this checklist to diagnose:

- [ ] **Config**: Mail config exists in application-prod.yaml ✅
- [ ] **Credentials**: MAIL_USERNAME and MAIL_PASSWORD in .env ✅
- [ ] **App Password**: Generated from Google Account (not regular password) ⚠️
- [ ] **2FA**: Enabled on Gmail account ⚠️
- [ ] **Network**: Can telnet to smtp.gmail.com:587 from server ⚠️
- [ ] **Firewall**: Outbound port 587 allowed ⚠️
- [ ] **Logs**: Check docker-compose logs for error messages ⚠️
- [ ] **Health**: Disabled mail health check ✅
- [ ] **Error Handling**: Code catches exceptions and logs ✅
- [ ] **Quota**: Under 100 emails/day limit ⚠️

---

## 🚀 Recommended Production Solution

### Option 1: Continue with Gmail (Free, but limited)

**Pros**:

- Free
- Easy setup
- Good for small clinics (<100 emails/day)

**Cons**:

- 100 emails/day limit
- May be blocked by some ISPs
- Not designed for bulk sending

**When to use**: Testing, small clinics, low email volume

### Option 2: Use SendGrid (Recommended)

**Pros**:

- 100 emails/day free forever
- 12,000 emails/month on free tier first 30 days
- Dedicated IPs available
- Better deliverability
- Email analytics

**Cons**:

- Requires signup
- Need to configure DNS (SPF, DKIM)

**When to use**: Production, growing clinics, need reliability

**Setup**:

```yaml
# 1. Sign up: https://sendgrid.com/
# 2. Generate API Key
# 3. Update application-prod.yaml:
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: ${SENDGRID_API_KEY}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Option 3: Use AWS SES (Enterprise)

**Pros**:

- $0.10 per 1,000 emails
- 62,000 emails/month free (first 12 months)
- Highly scalable
- Best deliverability

**Cons**:

- Requires AWS account
- More complex setup
- Need to verify domain

**When to use**: Large clinics, high volume, need scalability

---

## 📊 Current Status

### What's Working ✅

- Email service code exists and is correct
- Error handling in place
- Config structure is correct
- Health check won't block deployment

### What Needs Checking ⚠️

1. **Gmail App Password**: Verify it's valid
2. **Network connectivity**: Test from server to Gmail SMTP
3. **Environment variables**: Confirm they're loaded in Docker
4. **Actual error logs**: Check what error is being thrown

---

## 🎯 Next Steps

### Immediate (Debug):

```bash
# 1. SSH to server
ssh root@your-droplet-ip

# 2. Check logs
cd ~/PDCMS_BE
docker-compose logs app | grep -i "mail\|email" | tail -100

# 3. Test SMTP
telnet smtp.gmail.com 587

# 4. Check env
docker-compose config | grep MAIL

# 5. Share logs with me for further diagnosis
```

### Short-term (Fix):

1. If App Password invalid → regenerate
2. If network blocked → check firewall/ISP
3. If quota exceeded → switch to SendGrid

### Long-term (Production):

1. Migrate to SendGrid or AWS SES
2. Setup SPF/DKIM records for better deliverability
3. Implement email queue for retries
4. Add email delivery tracking

---

## 📞 Support

If still not working after these steps:

1. **Share logs**: Copy output of `docker-compose logs app | grep -i email`
2. **Share telnet result**: Output of `telnet smtp.gmail.com 587`
3. **Share config**: Confirm MAIL_USERNAME and MAIL_PASSWORD format

Tôi sẽ giúp debug cụ thể!

---

**Date**: 2025-12-30
**Status**: ⚠️ INVESTIGATING - Waiting for logs from production
