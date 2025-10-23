# DEFAULT PASSWORD & ACCOUNT ACTIVATION PLAN

## 📋 Requirement Overview

**Yêu cầu từ user:**

> "Sau khi tạo được tài khoản customer (có thể là do khách walk-in hoặc để lại thông tin trên web) sẽ gửi về tk mk cho họ thông qua sđt hoặc gmail - sau đó họ vào reset password, password nên là default 123456 - để khách hàng hoặc nhân viên khi vừa tạo tài khoản có thể đổi mật khẩu, cơ chế active thông qua mail"

---

## 🎯 Goals

1. **Default Password**: Tất cả tài khoản mới (Customer/Patient/Employee) có password mặc định `123456`
2. **Email Notification**: Gửi email thông báo tk/mk cho customer
3. **SMS Notification**: Gửi SMS thông báo tk/mk (optional)
4. **Account Activation**: Verify email trước khi active tài khoản
5. **Force Password Change**: Bắt buộc đổi password lần đầu login

---

## 🔐 Account Status Flow

### Current Status Enum:

```java
public enum AccountStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
```

### NEW Status Enum (Add PENDING_VERIFICATION):

```java
public enum AccountStatus {
    PENDING_VERIFICATION,  // ← NEW: Chưa verify email
    ACTIVE,                // Đã verify và active
    INACTIVE,              // Bị vô hiệu hóa
    SUSPENDED              // Bị đình chỉ
}
```

### Status Transition Flow:

```
Customer/Patient Registration
    ↓
[PENDING_VERIFICATION] → Email sent with verification link
    ↓
User clicks verification link
    ↓
[ACTIVE] → Can login
    ↓
First login → Force change password
    ↓
[ACTIVE] → Normal usage
```

---

## 📧 Email Verification Flow

### Step 1: Create Verification Token

```java
// New Entity: AccountVerificationToken
@Entity
@Table(name = "account_verification_tokens")
public class AccountVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;  // UUID token

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;  // Token valid for 24 hours

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### Step 2: Generate Token When Creating Account

```java
// In PatientService.createPatient()
public PatientInfoResponse createPatient(CreatePatientRequest request) {
    // 1. Create account with PENDING_VERIFICATION status
    Account account = new Account();
    account.setUsername(request.getUsername());
    account.setEmail(request.getEmail());
    account.setPassword(passwordEncoder.encode("123456"));  // ← DEFAULT PASSWORD
    account.setRole(patientRole);
    account.setStatus(AccountStatus.PENDING_VERIFICATION);  // ← NEW STATUS
    accountRepository.save(account);

    // 2. Create patient
    Patient patient = new Patient();
    // ... set patient fields
    patientRepository.save(patient);

    // 3. Generate verification token
    String token = UUID.randomUUID().toString();
    AccountVerificationToken verificationToken = new AccountVerificationToken();
    verificationToken.setToken(token);
    verificationToken.setAccount(account);
    verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
    verificationToken.setCreatedAt(LocalDateTime.now());
    verificationTokenRepository.save(verificationToken);

    // 4. Send verification email
    emailService.sendVerificationEmail(
        account.getEmail(),
        account.getUsername(),
        token
    );

    // 5. Send SMS notification (optional)
    if (patient.getPhone() != null) {
        smsService.sendAccountCreatedSMS(
            patient.getPhone(),
            account.getUsername(),
            "123456"  // Default password
        );
    }

    return mapToResponse(patient);
}
```

### Step 3: Verify Email Endpoint

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        AccountVerificationToken verificationToken =
            verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        // Check if token expired
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification token has expired");
        }

        // Check if already verified
        if (verificationToken.getVerifiedAt() != null) {
            throw new AlreadyVerifiedException("Account already verified");
        }

        // Activate account
        Account account = verificationToken.getAccount();
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        // Mark token as verified
        verificationToken.setVerifiedAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationToken);

        return ResponseEntity.ok(new MessageResponse("Email verified successfully. You can now login."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerificationEmail(@RequestBody ResendVerificationRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new AlreadyVerifiedException("Account already verified");
        }

        // Invalidate old tokens
        verificationTokenRepository.deleteByAccount(account);

        // Generate new token
        String token = UUID.randomUUID().toString();
        AccountVerificationToken verificationToken = new AccountVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setAccount(account);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationToken.setCreatedAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationToken);

        // Resend email
        emailService.sendVerificationEmail(
            account.getEmail(),
            account.getUsername(),
            token
        );

        return ResponseEntity.ok(new MessageResponse("Verification email resent successfully"));
    }
}
```

---

## 🔄 Force Password Change Flow

### Step 1: Add Field to Account Entity

```java
@Entity
@Table(name = "accounts")
public class Account {
    // ... existing fields

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = true;  // ← NEW: Default true for new accounts

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
}
```

### Step 2: Update Login Response

```java
public class LoginResponse {
    // ... existing fields

    private Boolean mustChangePassword;  // ← NEW: FE cần biết để redirect
}
```

### Step 3: Check on Login

```java
// In AuthenticationService.login()
public LoginResponse login(LoginRequest request) {
    // Authenticate
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

    Account account = accountRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new AccountNotFoundException("Account not found"));

    // Check if account is verified
    if (account.getStatus() == AccountStatus.PENDING_VERIFICATION) {
        throw new AccountNotVerifiedException("Please verify your email first");
    }

    // Generate token
    String token = jwtUtils.generateToken(authentication);

    // Build response
    LoginResponse response = new LoginResponse();
    response.setToken(token);
    response.setUsername(account.getUsername());
    response.setEmail(account.getEmail());
    response.setMustChangePassword(account.getMustChangePassword());  // ← INCLUDE THIS
    // ... other fields

    return response;
}
```

### Step 4: Change Password Endpoint

```java
@PostMapping("/change-password")
public ResponseEntity<MessageResponse> changePassword(
    @Valid @RequestBody ChangePasswordRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    Account account = accountRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new AccountNotFoundException("Account not found"));

    // Verify old password
    if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
        throw new InvalidPasswordException("Current password is incorrect");
    }

    // Validate new password
    if (request.getNewPassword().equals("123456")) {
        throw new WeakPasswordException("Cannot use default password");
    }

    // Update password
    account.setPassword(passwordEncoder.encode(request.getNewPassword()));
    account.setMustChangePassword(false);  // ← CLEAR FLAG
    account.setPasswordChangedAt(LocalDateTime.now());
    accountRepository.save(account);

    return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
}
```

---

## 📨 Email Templates

### 1. Verification Email Template

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Verify Your Email</title>
  </head>
  <body>
    <h2>Welcome to Dental Clinic Management System!</h2>

    <p>Hello {{username}},</p>

    <p>
      Thank you for registering. Please verify your email address by clicking
      the link below:
    </p>

    <a href="{{verificationUrl}}">Verify Email Address</a>

    <p>Or copy this link to your browser:</p>
    <p>{{verificationUrl}}</p>

    <p><strong>Your account credentials:</strong></p>
    <ul>
      <li>Username: {{username}}</li>
      <li>Temporary Password: 123456</li>
    </ul>

    <p>
      <strong>⚠️ Important:</strong> You will be required to change your
      password on first login.
    </p>

    <p>This verification link will expire in 24 hours.</p>

    <p>If you did not create this account, please ignore this email.</p>

    <p>Best regards,<br />Dental Clinic Team</p>
  </body>
</html>
```

### 2. Account Created Email (After Verification)

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Account Activated</title>
  </head>
  <body>
    <h2>Your Account is Now Active!</h2>

    <p>Hello {{username}},</p>

    <p>
      Your email has been successfully verified and your account is now active.
    </p>

    <p>You can now login to the system using:</p>
    <ul>
      <li>Username: {{username}}</li>
      <li>Password: (your chosen password)</li>
    </ul>

    <p>Login URL: {{loginUrl}}</p>

    <p>Best regards,<br />Dental Clinic Team</p>
  </body>
</html>
```

### 3. Password Change Required Email

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Password Change Required</title>
  </head>
  <body>
    <h2>Action Required: Change Your Password</h2>

    <p>Hello {{username}},</p>

    <p>For security reasons, you are required to change your password.</p>

    <p>Please login and follow the prompts to set a new password.</p>

    <p><strong>Security Tips:</strong></p>
    <ul>
      <li>Use at least 8 characters</li>
      <li>Include uppercase and lowercase letters</li>
      <li>Include numbers and special characters</li>
      <li>Do not use default password (123456)</li>
    </ul>

    <p>Login URL: {{loginUrl}}</p>

    <p>Best regards,<br />Dental Clinic Team</p>
  </body>
</html>
```

---

## 📱 SMS Templates

### 1. Account Created SMS

```
[Dental Clinic]
Your account has been created.
Username: {{username}}
Password: 123456
Please verify your email to activate.
```

### 2. Verification Reminder SMS

```
[Dental Clinic]
Please verify your email to activate your account.
Check your inbox for verification link.
```

---

## 🛠️ Implementation Files

### New Files to Create:

1. **Entity:**

   - `AccountVerificationToken.java`

2. **Repository:**

   - `AccountVerificationTokenRepository.java`

3. **Service:**

   - `EmailService.java`
   - `SMSService.java` (optional)

4. **DTO:**

   - `ResendVerificationRequest.java`
   - `ChangePasswordRequest.java`
   - `MessageResponse.java`

5. **Exception:**

   - `InvalidTokenException.java`
   - `TokenExpiredException.java`
   - `AlreadyVerifiedException.java`
   - `AccountNotVerifiedException.java`
   - `WeakPasswordException.java`

6. **Email Templates:**
   - `verification-email.html`
   - `account-activated-email.html`
   - `password-change-required-email.html`

### Files to Update:

1. **Account.java** - Add `mustChangePassword`, `passwordChangedAt`
2. **AccountStatus.java** - Add `PENDING_VERIFICATION`
3. **LoginResponse.java** - Add `mustChangePassword`
4. **AuthenticationController.java** - Add verify/resend endpoints
5. **AuthenticationService.java** - Check verification status on login
6. **PatientService.java** - Set default password, send emails
7. **CustomerContactService.java** - Same as PatientService

---

## 🔐 Security Considerations

### Password Policy:

```java
@Service
public class PasswordValidator {

    public void validate(String password) {
        if (password.equals("123456")) {
            throw new WeakPasswordException("Cannot use default password");
        }

        if (password.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new WeakPasswordException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new WeakPasswordException("Password must contain at least one number");
        }
    }
}
```

### Token Security:

- ✅ Use UUID for verification tokens (unpredictable)
- ✅ Tokens expire after 24 hours
- ✅ One-time use (mark as verified after use)
- ✅ Invalidate old tokens when resending

### Email Security:

- ✅ Use HTTPS for verification URLs
- ✅ Include token in URL parameter (GET request)
- ✅ Verify token in database before activating

---

## 📊 Database Migration

```sql
-- Add new column to accounts table
ALTER TABLE accounts
ADD COLUMN must_change_password BOOLEAN DEFAULT TRUE,
ADD COLUMN password_changed_at TIMESTAMP;

-- Update enum to include PENDING_VERIFICATION
ALTER TYPE account_status ADD VALUE 'PENDING_VERIFICATION';

-- Create verification tokens table
CREATE TABLE account_verification_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    account_id INTEGER NOT NULL REFERENCES accounts(account_id),
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_token (token),
    INDEX idx_account_id (account_id),
    INDEX idx_expires_at (expires_at)
);
```

---

## 🧪 Testing Checklist

### Registration Flow:

- [ ] Create patient with default password `123456`
- [ ] Account status is `PENDING_VERIFICATION`
- [ ] Verification email sent successfully
- [ ] SMS sent successfully (if phone provided)

### Email Verification:

- [ ] Click verification link → Account becomes `ACTIVE`
- [ ] Expired token → Show error message
- [ ] Already verified → Show appropriate message
- [ ] Resend verification email works

### Login Flow:

- [ ] Unverified account cannot login
- [ ] Verified account can login
- [ ] Response includes `mustChangePassword: true`
- [ ] FE redirects to change password page

### Password Change:

- [ ] Cannot use `123456` as new password
- [ ] Password validation works (8 chars, uppercase, lowercase, number)
- [ ] After change: `mustChangePassword` becomes `false`
- [ ] Second login: No forced password change

---

## 🚀 Deployment Steps

1. ✅ Create database migration
2. ✅ Create new entities and repositories
3. ✅ Implement EmailService with SMTP config
4. ✅ Implement SMSService (optional)
5. ✅ Update AuthenticationService
6. ✅ Update PatientService/CustomerContactService
7. ✅ Add verification endpoints
8. ✅ Configure email templates
9. ✅ Test all flows
10. ✅ Deploy to production

---

## 📧 Email Configuration (application.yaml)

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

app:
  mail:
    from: noreply@dentalclinic.com
    verification-url: ${FRONTEND_URL}/verify-email?token={token}
    login-url: ${FRONTEND_URL}/login
```

---

**Ready to implement?**

Priority order:

1. Run permission restructure migration first
2. Then implement default password + email verification
3. Update FE to handle new flow
