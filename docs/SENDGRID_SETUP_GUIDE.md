# SendGrid Setup Guide for DigitalOcean

## 🎯 Tại sao cần SendGrid?

**Vấn đề**: DigitalOcean **CHẶN** các SMTP ports (25, 465, 587) mặc định để chống spam.

**Giải pháp**: Dùng SendGrid - dịch vụ email chuyên nghiệp với **100 emails/day MIỄN PHÍ VĨNH VIỄN**.

---

## 📋 Setup SendGrid (5 phút)

### Bước 1: Đăng ký SendGrid

1. Truy cập: https://sendgrid.com/pricing/
2. Chọn **Free Plan** (100 emails/day forever)
3. Click **"Try for Free"**
4. Điền thông tin:
   - Email (dùng email công ty tốt nhất)
   - Password
   - Company name: "DenTeeth Dental Clinic"
   - Website: "https://pdcms.vercel.app" (hoặc domain của bạn)

### Bước 2: Verify Email

1. Check email inbox (có thể trong spam)
2. Click link "Verify Your Account"
3. Login vào SendGrid dashboard

### Bước 3: Tạo API Key

1. Trong SendGrid dashboard, vào: **Settings** → **API Keys**
2. Click **"Create API Key"**
3. Điền thông tin:
   - **Name**: `PDCMS Production`
   - **API Key Permissions**: Chọn **"Full Access"** (hoặc "Mail Send" nếu chỉ cần gửi email)
4. Click **"Create & View"**
5. **COPY API KEY NGAY** (chỉ hiện 1 lần!)
   - Format: `SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### Bước 4: Configure Sender Identity (Quan trọng!)

SendGrid yêu cầu verify sender trước khi gửi email.

#### Option A: Single Sender Verification (Nhanh - Recommended)

1. Vào: **Settings** → **Sender Authentication** → **Single Sender Verification**
2. Click **"Create New Sender"**
3. Điền thông tin:
   - **From Name**: `Phòng khám nha khoa DenTeeth`
   - **From Email**: `hellodenteeth@gmail.com` (hoặc email công ty)
   - **Reply To**: `hellodenteeth@gmail.com`
   - **Company Address**: Địa chỉ phòng khám
   - **City**: TP.HCM
   - **Country**: Vietnam
4. Click **"Create"**
5. Check email `hellodenteeth@gmail.com` và **verify** sender

#### Option B: Domain Authentication (Chuyên nghiệp - Optional)

Nếu có domain riêng (vd: `denteeth.com`):

1. Vào: **Settings** → **Sender Authentication** → **Domain Authentication**
2. Follow wizard để add DNS records (CNAME, TXT)
3. Sau khi verify, có thể gửi từ `no-reply@denteeth.com`

---

## 🔧 Cấu hình Backend

### File: `.env` (Production Server)

```bash
# SSH vào DigitalOcean Droplet
ssh root@your-droplet-ip

# Edit .env
cd ~/PDCMS_BE
nano .env

# Thêm/cập nhật dòng này:
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Save: Ctrl+O, Enter, Ctrl+X
```

### Verify Config

```bash
# Check config
docker-compose config | grep SENDGRID

# Should show:
# SENDGRID_API_KEY: SG.xxx...
```

### Restart Application

```bash
# Restart để load env mới
docker-compose restart app

# Check logs
docker-compose logs -f app | grep -i "mail\|email"
```

---

## ✅ Test Email

### Option 1: Create Patient Test

```bash
# Frontend: Create new patient with valid email
# Example:
{
  "firstName": "Test",
  "lastName": "Patient",
  "email": "your-email@gmail.com",
  "phone": "0123456789"
}

# Check logs:
docker-compose logs app | grep -i "email" | tail -20

# Expected:
# ✅ "Welcome email with password setup sent to: your-email@gmail.com"
# ❌ "Failed to send welcome email: ..." (nếu có lỗi)
```

### Option 2: API Test (Manual)

```bash
# Call password reset endpoint
curl -X POST "https://your-domain.com/api/v1/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{"email": "your-email@gmail.com"}'

# Check email inbox for reset link
```

---

## 📊 SendGrid Dashboard

Sau khi gửi email, check SendGrid dashboard:

1. Vào: **Activity** → **Email Activity**
2. Xem status:
   - ✅ **Delivered**: Email đã gửi thành công
   - ⏳ **Processed**: Đang xử lý
   - ❌ **Bounced**: Email không tồn tại
   - ❌ **Dropped**: Sender không verified

---

## 🐛 Troubleshooting

### Error 1: "Bad username / password"

**Nguyên nhân**: API Key sai hoặc không có quyền

**Giải pháp**:

```bash
# Tạo API Key mới với "Full Access"
# Copy lại đúng format: SG.xxx...
# Update .env và restart
```

### Error 2: "Sender not verified"

**Nguyên nhân**: Chưa verify sender identity

**Giải pháp**:

1. Vào SendGrid → **Sender Authentication**
2. Verify sender email
3. Check email inbox và click verify link

### Error 3: "Daily send limit exceeded"

**Nguyên nhân**: Vượt quá 100 emails/day (free tier)

**Giải pháp**:

- Chờ đến ngày mai (reset lúc 00:00 UTC)
- Hoặc upgrade plan:
  - Essentials: $19.95/month (50,000 emails)
  - Pro: $89.95/month (100,000 emails)

### Error 4: Email vào Spam

**Nguyên nhân**: Chưa authenticate domain

**Giải pháp**:

1. Setup Domain Authentication (SPF, DKIM)
2. Add unsubscribe link trong email
3. Avoid spam keywords trong subject

---

## 📈 Monitoring

### Check SendGrid Stats

1. Login SendGrid dashboard
2. Vào **Statistics** → **Overview**
3. Xem:
   - Requests: Số email gửi
   - Delivered: Số email thành công
   - Bounces: Email bounce
   - Spam Reports: Bị report spam

### Check Application Logs

```bash
# On server
cd ~/PDCMS_BE

# Today's email logs
docker-compose logs app | grep -i "email" | grep "$(date +%Y-%m-%d)"

# Count emails sent today
docker-compose logs app | grep "Welcome email with password setup sent" | grep "$(date +%Y-%m-%d)" | wc -l
```

---

## 💰 Pricing Comparison

### Free Tier (Current)

- **Cost**: $0
- **Limit**: 100 emails/day
- **Features**: Full API access, email validation
- **Good for**: Small clinics, testing

### Essentials ($19.95/month)

- **Cost**: $19.95
- **Limit**: 50,000 emails/month
- **Features**: + Email support, dedicated IP
- **Good for**: Growing clinics

### Pro ($89.95/month)

- **Cost**: $89.95
- **Limit**: 100,000 emails/month
- **Features**: + Chat support, advanced insights
- **Good for**: Large clinics, multiple branches

---

## 🔐 Security Best Practices

### 1. API Key Security

```bash
# NEVER commit API key to Git
echo "SENDGRID_API_KEY" >> .gitignore

# Use environment variables
export SENDGRID_API_KEY=SG.xxx...

# Rotate API keys every 90 days
```

### 2. Rate Limiting

```java
// Add rate limiting to prevent abuse
@RateLimiter(name = "email", fallbackMethod = "emailFallback")
public void sendEmail(...) {
    // Send email
}
```

### 3. Email Validation

```java
// Validate email before sending
if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
    throw new InvalidEmailException();
}
```

---

## 🎯 Migration Checklist

Khi chuyển từ Gmail sang SendGrid:

- [ ] Đăng ký SendGrid account
- [ ] Tạo API Key với Full Access
- [ ] Verify Single Sender (hellodenteeth@gmail.com)
- [ ] Update `.env` với `SENDGRID_API_KEY`
- [ ] Verify `application-prod.yaml` dùng SendGrid config
- [ ] Restart application: `docker-compose restart app`
- [ ] Test gửi email qua create patient
- [ ] Check SendGrid Activity dashboard
- [ ] Monitor logs: `docker-compose logs -f app`
- [ ] Setup domain authentication (optional)
- [ ] Add monitoring alerts (optional)

---

## 📚 Useful Links

- **SendGrid Documentation**: https://docs.sendgrid.com/
- **API Key Management**: https://app.sendgrid.com/settings/api_keys
- **Email Activity**: https://app.sendgrid.com/email_activity
- **Sender Authentication**: https://app.sendgrid.com/settings/sender_auth
- **Pricing**: https://sendgrid.com/pricing/
- **Support**: https://support.sendgrid.com/

---

## ✅ Success Indicators

Khi setup thành công:

1. **Logs hiển thị**:

   ```
   Welcome email with password setup sent to: patient@example.com
   ```

2. **SendGrid Activity hiển thị**:

   - Status: **Delivered**
   - Opens: (nếu patient mở email)
   - Clicks: (nếu patient click link)

3. **Email inbox hiển thị**:
   - From: **Phòng khám nha khoa DenTeeth** <hellodenteeth@gmail.com>
   - Subject: **Chào mừng đến với Phòng khám nha khoa - Thiết lập mật khẩu**
   - Content: Beautiful HTML email with password setup button

---

**Date**: 2025-12-30
**Status**: ✅ READY TO DEPLOY
**Free Tier**: 100 emails/day forever
**Setup Time**: ~5 minutes
