# Email Configuration Complete - Gmail App Password Required

## [YES] What's Been Done

### 1. Secure Configuration Setup

- [YES] Created `.env` file with your credentials (hellodenteeth@gmail.com)
- [YES] Created `.env.example` template for teammates
- [YES] Updated `.gitignore` to exclude `.env`, `.env.local`, `application-local.yaml`
- [YES] Added `spring-dotenv` dependency to `pom.xml` for automatic .env loading

### 2. Security Configuration Update

- [YES] Added public access to email endpoints in `SecurityConfig.java`:
  - `/api/v1/auth/verify-email`
  - `/api/v1/auth/resend-verification`
  - `/api/v1/auth/forgot-password`
  - `/api/v1/auth/reset-password`

### 3. Documentation

- [YES] Updated `README.md` with detailed Gmail SMTP setup instructions
- [YES] Added step-by-step guide for generating Gmail App Password
- [YES] Documented environment variable configuration process

## [WARN] Action Required: Gmail App Password

### Why Your Current Password Won't Work

Gmail tested with password `Anhkhoa04` resulted in this error:

```
jakarta.mail.AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

**Gmail requires an App Password for SMTP authentication** (not your regular Gmail password). This is a security measure Google implemented for third-party applications.

### How to Generate Gmail App Password

1. **Enable 2-Step Verification** (if not already):

   - Go to https://myaccount.google.com/security
   - Find "2-Step Verification" and turn it on

2. **Generate App Password**:

   - Go to https://myaccount.google.com/apppasswords
   - Select "Mail" as the app
   - Select your device
   - Click "Generate"
   - **Copy the 16-character password** (example: `abcd efgh ijkl mnop`)

3. **Update `.env` file**:

   ```env
   MAIL_USERNAME=hellodenteeth@gmail.com
   MAIL_PASSWORD=abcdefghijklmnop  # Remove spaces from generated password
   ```

4. **Restart the server**:
   ```bash
   ./mvnw spring-boot:run -DskipTests
   ```

### Testing After Setup

Once you have the App Password configured, you can test:

**1. Forgot Password API:**

```bash
curl -X POST "http://localhost:8080/api/v1/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{"email":"khoa.la@dentalclinic.com"}'
```

Expected: Email sent to hellodenteeth@gmail.com with password reset link

**2. Resend Verification API:**

```bash
curl -X POST "http://localhost:8080/api/v1/auth/resend-verification" \
  -H "Content-Type: application/json" \
  -d '{"email":"khoa.la@dentalclinic.com"}'
```

Expected: Email sent with verification link

**3. Verify Email API:**

```bash
curl "http://localhost:8080/api/v1/auth/verify-email?token=<token_from_email>"
```

**4. Reset Password API:**

```bash
curl -X POST "http://localhost:8080/api/v1/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d '{
    "token":"<token_from_email>",
    "newPassword":"NewPassword123",
    "confirmPassword":"NewPassword123"
  }'
```

## 📝 Files Modified

### New Files:

- `.env` - Your actual credentials ([WARN] NOT committed to Git)
- `.env.example` - Template for teammates

### Modified Files:

- `pom.xml` - Added spring-dotenv dependency
- `.gitignore` - Added .env exclusions
- `SecurityConfig.java` - Added public access to email endpoints
- `README.md` - Added environment setup guide

## 🔒 Security Notes

- [YES] `.env` file is in `.gitignore` - your credentials are safe
- [YES] Teammates will create their own `.env` from `.env.example`
- [YES] Public GitHub push is safe - no credentials will be committed
- [YES] Email endpoints are public (no authentication required)

## 🎯 Next Steps

1. Generate Gmail App Password (5 minutes)
2. Update `.env` with the App Password
3. Restart server
4. Test email APIs
5. Push to GitHub (credentials safe!)

## 📧 Email Flow Architecture

### Password Reset Flow:

1. User requests password reset → POST `/auth/forgot-password`
2. System sends email with reset token → `hellodenteeth@gmail.com`
3. User clicks link with token → GET `/auth/reset-password?token=...`
4. User submits new password → POST `/auth/reset-password`

### Email Verification Flow:

1. New user registration (or manual trigger)
2. System sends verification email → `hellodenteeth@gmail.com`
3. User clicks verification link → GET `/auth/verify-email?token=...`
4. Account verified

## 🛡️ Spring Dotenv Library

The `spring-dotenv` library automatically loads `.env` file on application startup:

- Reads key-value pairs from `.env`
- Makes them available as Spring environment properties
- Works seamlessly with `${MAIL_USERNAME}` placeholders in `application.yaml`

## [YES] Safe to Push to GitHub

All sensitive data is protected:

- [YES] `.env` in `.gitignore`
- [YES] Only `.env.example` template will be committed
- [YES] Teammates can easily configure their own environment
- [YES] No hardcoded credentials in any committed files

---

**Ready to proceed once you generate the Gmail App Password!** 🚀
