#  DENTAL CLINIC MANAGEMENT SYSTEM - API DOCUMENTATION

**Version:** 1.0.0
**Base URL:** `http://localhost:8080/api/v1`
**Authentication:** JWT Bearer Token (except public endpoints)

---

##  Table of Contents

1. [Authentication APIs](#authentication-apis)
2. [Account Management APIs](#account-management-apis)
3. [Employee Management APIs](#employee-management-apis)
4. [Patient Management APIs](#patient-management-apis)
5. [Appointment Management APIs](#appointment-management-apis)
6. [Treatment Management APIs](#treatment-management-apis)
7. [Work Shift Management APIs](#work-shift-management-apis)
8. [Shift Registration & Renewal APIs](#shift-registration--renewal-apis)
9. [Time Off & Overtime APIs](#time-off--overtime-apis)
10. [Role & Permission Management APIs](#role--permission-management-apis)
11. [Error Handling](#error-handling)
12. [Security & Rate Limiting](#security--rate-limiting)
13. [Status Codes](#status-codes)

---

##  Authentication APIs

Base path: `/api/v1/auth`

### 1. Login

**POST** `/auth/login`

Authenticate user with username/password and issue JWT tokens.

**Request Body:**

```json
{
  "username": "admin",
  "password": "123456"
}
```

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Đăng nhập thành công",
  "error": null,
  "data": {
    "token": "eyJhbGciOiJSUzI1NiJ9...",
    "tokenExpiresAt": 1729758000,
    "username": "admin",
    "email": "admin@dentalclinic.com",
    "roles": ["ADMIN"],
    "permissions": ["CREATE_EMPLOYEE", "VIEW_EMPLOYEE", ...],
    "groupedPermissions": {
      "ACCOUNT": ["VIEW_ACCOUNT", "CREATE_ACCOUNT", ...],
      "EMPLOYEE": ["VIEW_EMPLOYEE", "CREATE_EMPLOYEE", ...],
      "PATIENT": ["VIEW_PATIENT", "CREATE_PATIENT", ...]
    },
    "employmentType": "FULL_TIME",
    "mustChangePassword": false
  }
}
```

**Note:** Refresh token is automatically set in HTTP-only cookie.

**Errors:**

- `401 Unauthorized` - Invalid credentials
- `403 Forbidden` - Account not verified (PENDING_VERIFICATION status)

---

### 2. Refresh Token

**POST** `/auth/refresh-token`

Issue new access token using refresh token from HTTP-only cookie.

**Headers:**

- `Cookie: refreshToken=<refresh_token>`

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Làm mới access token",
  "error": null,
  "data": {
    "token": "eyJhbGciOiJSUzI1NiJ9...",
    "tokenExpiresAt": 1729758000
  }
}
```

**Errors:**

- `401 Unauthorized` - Invalid or expired refresh token

---

### 3. Logout

**POST** `/auth/logout`

Invalidate access and refresh tokens, clear refresh token cookie.

**Headers:**

- `Authorization: Bearer <access_token>`
- `Cookie: refreshToken=<refresh_token>`

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Đăng xuất thành công",
  "error": null,
  "data": null
}
```

---

### 4. Verify Email  NEW

**GET** `/auth/verify-email?token={token}`

Verify email address using token from email link. This changes account status from `PENDING_VERIFICATION` to `ACTIVE`.

**Query Parameters:**

- `token` (required) - Verification token from email

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Xác thực email thành công",
  "error": null,
  "data": null
}
```

**Errors:**

- `400 Bad Request` - Invalid token or token already used
- `400 Bad Request` - Token expired (24h expiry)

---

### 5. Resend Verification Email  NEW

**POST** `/auth/resend-verification`

Resend verification email to user if they didn't receive it or token expired.

**Request Body:**

```json
{
  "email": "patient@example.com"
}
```

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Đã gửi lại email xác thực",
  "error": null,
  "data": null
}
```

**Errors:**

- `404 Not Found` - Email not found
- `400 Bad Request` - Account already verified

---

### 6. Forgot Password  NEW  🔒 RATE LIMITED

**POST** `/auth/forgot-password`

Initiate password reset process. Sends password reset email to user if account exists.

**Security Features:**
- ✅ **Email Enumeration Prevention:** Always returns 200 OK regardless of whether email exists
- ✅ **Rate Limiting:** 3 requests per 15 minutes per IP address
- ✅ **Logging:** All requests are logged with IP address for security monitoring

**Request Body:**

```json
{
  "email": "user@example.com"
}
```

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Đã gửi email đặt lại mật khẩu",
  "error": null,
  "data": null
}
```

**Email Contains:**
- Reset link: `https://yourapp.com/reset-password?token=<uuid>`
- Token format: UUID (e.g., `550e8400-e29b-41d4-a716-446655440000`)
- Token validity: 24 hours

**Errors:**

- `400 Bad Request` - Invalid email format
- `429 Too Many Requests` - Rate limit exceeded (retry after X seconds)

**Rate Limit Response:**

```json
{
  "statusCode": 429,
  "message": "Bạn đã vượt quá số lần yêu cầu cho phép. Vui lòng thử lại sau 15 phút.",
  "error": "error.rate.limit.exceeded",
  "data": {
    "retryAfterSeconds": 900
  }
}
```

---

### 7. Reset Password  NEW  🔒 RATE LIMITED

**POST** `/auth/reset-password`

Reset password using token from email. Token expires in 24 hours and can only be used once.

**Security Features:**
- ✅ **Single-Use Tokens:** Token cannot be reused after successful reset
- ✅ **Token Expiration:** 24-hour validity period
- ✅ **Rate Limiting:** 5 attempts per 10 minutes per IP address (prevents brute force)
- ✅ **Strong Password Enforcement:** Requires uppercase, lowercase, number, and special character

**Request Body:**

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewPass123!",
  "confirmPassword": "NewPass123!"
}
```

**Password Validation Rules:**

- **Minimum length:** 8 characters
- **Maximum length:** 50 characters
- **Required characters:**
  - At least 1 uppercase letter (A-Z)
  - At least 1 lowercase letter (a-z)
  - At least 1 number (0-9)
  - At least 1 special character (@$!%*?&)
- **Confirmation:** newPassword and confirmPassword must match

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Đặt lại mật khẩu thành công",
  "error": null,
  "data": null
}
```

**Errors:**

- `400 Bad Request` - Invalid token format
- `400 Bad Request` - Token expired: `"Token đặt lại mật khẩu đã hết hạn. Vui lòng yêu cầu đặt lại mật khẩu mới."`
- `400 Bad Request` - Token already used: `"Token này đã được sử dụng"`
- `400 Bad Request` - Passwords don't match: `"Mật khẩu xác nhận không khớp"`
- `400 Bad Request` - Password validation failed: `"Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"`
- `429 Too Many Requests` - Rate limit exceeded (retry after X seconds)

**Token Expired Response:**

```json
{
  "statusCode": 400,
  "message": "Token đặt lại mật khẩu đã hết hạn. Vui lòng yêu cầu đặt lại mật khẩu mới.",
  "error": "error.token.expired",
  "data": null
}
```

**Token Already Used Response:**

```json
{
  "statusCode": 400,
  "message": "Token này đã được sử dụng",
  "error": "error.token.invalid",
  "data": null
}
```

**Password Validation Failed Response:**

```json
{
  "statusCode": 400,
  "message": "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt",
  "error": "VALIDATION_ERROR",
  "data": {
    "missingFields": ["newPassword"]
  }
}
```

---

### 8. Get My Permissions

**GET** `/auth/my-permissions`

Get all permissions of the currently authenticated user, grouped by module.

**Headers:**

- `Authorization: Bearer <access_token>`

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách quyền thành công",
  "error": null,
  "data": {
    "ACCOUNT": ["VIEW_ACCOUNT", "CREATE_ACCOUNT", "UPDATE_ACCOUNT"],
    "EMPLOYEE": ["VIEW_EMPLOYEE", "CREATE_EMPLOYEE"],
    "PATIENT": ["VIEW_PATIENT", "CREATE_PATIENT", "UPDATE_PATIENT"]
  }
}
```

---

##  Account Management APIs

Base path: `/api/v1/accounts`

### 1. Get Account by ID

**GET** `/accounts/{accountId}`

**Required Permission:** `VIEW_ACCOUNT` or `ADMIN` role

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Lấy thông tin tài khoản thành công",
  "error": null,
  "data": {
    "accountId": 1,
    "accountCode": "ACC001",
    "username": "admin",
    "email": "admin@dentalclinic.com",
    "status": "ACTIVE",
    "role": {
      "roleId": 1,
      "roleName": "Admin",
      "baseRole": "ADMIN"
    },
    "mustChangePassword": false,
    "passwordChangedAt": "2025-10-23T10:30:00",
    "createdAt": "2025-01-01T00:00:00"
  }
}
```

**Errors:**

- `404 Not Found` - Account not found
- `403 Forbidden` - Insufficient permissions

---

### 2. Get My Profile

**GET** `/accounts/me`

Get profile information of currently authenticated user.

**Headers:**

- `Authorization: Bearer <access_token>`

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Lấy thông tin profile thành công",
  "error": null,
  "data": {
    "accountId": 1,
    "username": "admin",
    "email": "admin@dentalclinic.com",
    "role": {
      "roleId": 1,
      "roleName": "Admin"
    },
    "groupedPermissions": {
      "ACCOUNT": ["VIEW_ACCOUNT", "CREATE_ACCOUNT"],
      "EMPLOYEE": ["VIEW_EMPLOYEE", "CREATE_EMPLOYEE"]
    }
  }
}
```

---

### 3. Change Password

**POST** `/accounts/change-password`

Change password for currently authenticated user.

**Headers:**

- `Authorization: Bearer <access_token>`

**Request Body:**

```json
{
  "oldPassword": "123456",
  "newPassword": "NewPass123",
  "confirmPassword": "NewPass123"
}
```

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Đổi mật khẩu thành công",
  "error": null,
  "data": null
}
```

**Errors:**

- `400 Bad Request` - Old password incorrect
- `400 Bad Request` - Passwords don't match
- `400 Bad Request` - Password validation failed

---

##  Employee Management APIs

Base path: `/api/v1/employees`

### 1. Get All Employees

**GET** `/employees?page=0&size=10&sortBy=employeeId&sortDirection=ASC`

**Required Permission:** `VIEW_EMPLOYEE` or `ADMIN` role

**Query Parameters:**

- `page` (default: 0) - Page number (zero-based)
- `size` (default: 10) - Number of items per page (max: 100)
- `sortBy` (default: employeeId) - Field to sort by
- `sortDirection` (default: ASC) - ASC or DESC

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách nhân viên thành công",
  "error": null,
  "data": {
    "content": [
      {
        "employeeId": 1,
        "employeeCode": "EMP001",
        "fullName": "Nguyễn Văn A",
        "dateOfBirth": "1990-01-15",
        "gender": "MALE",
        "phoneNumber": "0901234567",
        "email": "nva@dentalclinic.com",
        "address": "123 Nguyễn Huệ, Q.1, TP.HCM",
        "employmentType": "FULL_TIME",
        "hireDate": "2024-01-01",
        "jobPosition": "Bác sĩ Nha khoa",
        "department": "Khám Tổng Quát",
        "salary": 25000000,
        "isActive": true,
        "specializations": ["Nha Khoa Tổng Quát", "Răng Sứ Thẩm Mỹ"]
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 25,
    "totalPages": 3,
    "last": false,
    "first": true,
    "number": 0,
    "size": 10
  }
}
```

---

### 2. Create Employee

**POST** `/employees`

**Required Permission:** `CREATE_EMPLOYEE` or `ADMIN` role

**Request Body:**

```json
{
  "fullName": "Nguyễn Văn B",
  "dateOfBirth": "1992-05-20",
  "gender": "MALE",
  "phoneNumber": "0912345678",
  "email": "nvb@dentalclinic.com",
  "address": "456 Lê Lợi, Q.1, TP.HCM",
  "employmentType": "FULL_TIME",
  "hireDate": "2025-01-15",
  "jobPosition": "Bác sĩ Nha khoa",
  "department": "Khám Tổng Quát",
  "salary": 20000000,
  "username": "nvb_employee",
  "password": "123456",
  "roleId": 2,
  "specializationIds": [1, 2]
}
```

**Response:** `201 Created`

```json
{
  "statusCode": 201,
  "message": "Tạo nhân viên thành công",
  "error": null,
  "data": {
    "employeeId": 26,
    "employeeCode": "EMP026",
    "fullName": "Nguyễn Văn B",
    ...
  }
}
```

**Errors:**

- `400 Bad Request` - Validation errors (missing fields, invalid format)
- `409 Conflict` - Username or email already exists

---

### 3. Update Employee

**PUT** `/employees/{employeeId}`

**Required Permission:** `UPDATE_EMPLOYEE` or `ADMIN` role

**Request Body:** (Same as Create Employee)

**Response:** `200 OK`

---

### 4. Delete Employee (Soft Delete)

**DELETE** `/employees/{employeeId}`

**Required Permission:** `DELETE_EMPLOYEE` or `ADMIN` role

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Xóa nhân viên thành công",
  "error": null,
  "data": null
}
```

---

##  Patient Management APIs

Base path: `/api/v1/patients`

### 1. Get All Patients

**GET** `/patients?page=0&size=10&sortBy=patientId&sortDirection=ASC`

**Required Permission:** `VIEW_PATIENT` or `ADMIN` role

**Response:** Similar structure to Employee list

---

### 2. Create Patient  UPDATED

**POST** `/patients`

**Required Permission:** `CREATE_PATIENT` or `ADMIN` role

**Request Body:**

```json
{
  "fullName": "Trần Thị C",
  "dateOfBirth": "1995-08-10",
  "gender": "FEMALE",
  "phoneNumber": "0923456789",
  "email": "ttc@example.com",
  "address": "789 Võ Văn Tần, Q.3, TP.HCM",
  "emergencyContact": "0987654321",
  "medicalHistory": "Không có tiền sử bệnh lý",
  "allergies": "Không",
  "insuranceInfo": "Bảo hiểm Y tế",
  "username": "ttc_patient",
  "password": "123456"
}
```

**Important Changes:**

- If `username` and `password` are provided → Account is created with `PENDING_VERIFICATION` status
- Verification email is automatically sent to the provided email
- Patient must verify email before they can login
- `mustChangePassword` flag is set to `true` for first login

**Response:** `201 Created`

```json
{
  "statusCode": 201,
  "message": "Tạo bệnh nhân thành công. Email xác thực đã được gửi.",
  "error": null,
  "data": {
    "patientId": 101,
    "patientCode": "PAT101",
    "fullName": "Trần Thị C",
    "email": "ttc@example.com",
    "accountStatus": "PENDING_VERIFICATION",
    ...
  }
}
```

**Errors:**

- `400 Bad Request` - Email required when creating account
- `409 Conflict` - Username or email already exists

---

### 3. Search Patients

**GET** `/patients/search?keyword=Trần&page=0&size=10`

**Required Permission:** `VIEW_PATIENT` or `ADMIN` role

**Query Parameters:**

- `keyword` - Search in fullName, phoneNumber, email, patientCode
- `page`, `size`, `sortBy`, `sortDirection`

**Response:** `200 OK` - Paginated patient list

---

##  Appointment Management APIs

Base path: `/api/v1/appointments`

### 1. Get All Appointments

**GET** `/appointments?page=0&size=10`

**Required Permission:** `VIEW_APPOINTMENT` or `ADMIN` role

### 2. Create Appointment (API 3.2)

**POST** `/appointments`

**Required Permission:** `CREATE_APPOINTMENT` or `ADMIN` role

**Important:** This API supports **TWO booking modes** with **XOR validation** (must choose exactly one):

1. **Mode 1: Standalone Booking** - Use `serviceCodes` for walk-in patients
2. **Mode 2: Treatment Plan Booking** - Use `patientPlanItemIds` for patients with treatment plans

####  XOR Validation Rule (CRITICAL)

You **MUST** provide **EITHER** `serviceCodes` **OR** `patientPlanItemIds`, **NOT BOTH** and **NOT NEITHER**.

**Valid Options:**
-  Provide only `serviceCodes` → Standalone booking
-  Provide only `patientPlanItemIds` → Treatment plan booking
-  Provide both → 400 Bad Request
-  Provide neither → 400 Bad Request

#### Request Body (Mode 1: Standalone Booking)

```json
{
  "patientCode": "BN-1001",
  "serviceCodes": ["SCALING_L1", "FILLING_L2"],
  "employeeCode": "EMP001",
  "roomCode": "P-01",
  "appointmentStartTime": "2025-11-15T10:00:00",
  "reason": "Khám tổng quát",
  "notes": "Bệnh nhân có tiền sử đau răng",
  "participantCodes": ["EMP005"]
}
```

#### Request Body (Mode 2: Treatment Plan Booking)

```json
{
  "patientCode": "BN-1003",
  "patientPlanItemIds": [307, 308],
  "employeeCode": "EMP001",
  "roomCode": "P-01",
  "appointmentStartTime": "2025-12-08T14:00:00",
  "reason": "Thực hiện lộ trình điều trị",
  "notes": "Lần 3 và 4 trong kế hoạch niềng răng",
  "participantCodes": ["EMP006"]
}
```

**Response:** `201 Created`

```json
{
  "statusCode": 201,
  "message": "Tạo lịch hẹn thành công",
  "error": null,
  "data": {
    "appointmentCode": "APT-20251115-001",
    "patientCode": "BN-1001",
    "patientName": "Nguyễn Văn A",
    "doctorCode": "EMP001",
    "doctorName": "Dr. Trần Thị B",
    "roomCode": "P-01",
    "appointmentStartTime": "2025-11-15T10:00:00",
    "appointmentEndTime": "2025-11-15T11:00:00",
    "status": "SCHEDULED",
    "services": [
      {
        "serviceCode": "SCALING_L1",
        "serviceName": "Lấy cao răng Level 1",
        "estimatedTime": 30,
        "price": 300000
      },
      {
        "serviceCode": "FILLING_L2",
        "serviceName": "Trám răng Level 2",
        "estimatedTime": 30,
        "price": 500000
      }
    ],
    "linkedPlanItems": null,
    "totalEstimatedTime": 60,
    "totalPrice": 800000
  }
}
```

**Common Errors:**

| Error Code | HTTP Status | Description | Solution |
|------------|-------------|-------------|----------|
| `INVALID_BOOKING_TYPE` | 400 | Violated XOR rule (both or neither provided) | Provide **EITHER** `serviceCodes` OR `patientPlanItemIds`, not both and not neither |
| `PLAN_ITEMS_NOT_FOUND` | 400 | Plan item IDs don't exist | Verify item IDs from treatment plan API |
| `PLAN_ITEMS_WRONG_PATIENT` | 400 | Plan items don't belong to patient | Check patientCode matches plan owner |
| `PLAN_ITEMS_NOT_READY` | 400 | Items not in READY_FOR_BOOKING status | Items may be SCHEDULED/IN_PROGRESS/COMPLETED |
| `INVALID_TIME_SLOT` | 400 | Time slot unavailable | Check doctor's schedule and room availability |

**For detailed documentation, see:** `docs/api-guides/booking/appointment/Appointment.md`

---

##  Treatment Management APIs

Base path: `/api/v1/treatments`

### 1. Get All Treatments

**GET** `/treatments?page=0&size=10`

**Required Permission:** `VIEW_TREATMENT` or `ADMIN` role

### 2. Create Treatment

**POST** `/treatments`

**Required Permission:** `CREATE_TREATMENT` or `ADMIN` role

---

## ⏰ Work Shift Management APIs

Base path: `/api/v1/work-shifts`

### 1. Get All Work Shifts

**GET** `/work-shifts?page=0&size=10`

**Required Permission:** `VIEW_WORK_SHIFTS` or `ADMIN` role

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách ca làm việc thành công",
  "error": null,
  "data": {
    "content": [
      {
        "workShiftId": 1,
        "workShiftCode": "SHIFT001",
        "shiftName": "Ca Sáng",
        "startTime": "08:00:00",
        "endTime": "12:00:00",
        "isActive": true
      }
    ]
  }
}
```

### 2. Create Work Shift

**POST** `/work-shifts`

**Required Permission:** `CREATE_WORK_SHIFTS` or `ADMIN` role

---

##  Shift Registration & Renewal APIs

Base path: `/api/v1/shift-registrations`

### 1. Register for Shift

**POST** `/shift-registrations`

**Required Permission:** `CREATE_REGISTRATION` or `ADMIN` role

### 2. Renew Shift Registration

**POST** `/shift-registrations/{registrationId}/renew`

**Required Permission:** `CREATE_SHIFT_RENEWAL` or `ADMIN` role

---

##  Time Off & Overtime APIs

Base path: `/api/v1/time-off` and `/api/v1/overtime`

### 1. Request Time Off

**POST** `/time-off/request`

**Required Permission:** `CREATE_TIME_OFF` or `ADMIN` role

### 2. Approve/Reject Time Off

**POST** `/time-off/{timeOffId}/approve`

**Required Permission:** `APPROVE_TIME_OFF` or `ADMIN` role

### 3. Request Overtime

**POST** `/overtime/request`

**Required Permission:** `CREATE_OVERTIME` or `ADMIN` role

---

##  Role & Permission Management APIs

Base path: `/api/v1/roles` and `/api/v1/permissions`

### 1. Get All Roles

**GET** `/roles?page=0&size=10`

**Required Permission:** `VIEW_ROLE` or `ADMIN` role

### 2. Create Role

**POST** `/roles`

**Required Permission:** `CREATE_ROLE` or `ADMIN` role

### 3. Get All Permissions (Grouped by Module)

**GET** `/permissions/grouped`

**Required Permission:** `VIEW_PERMISSION` or `ADMIN` role

**Response:** `200 OK`

```json
{
  "statusCode": 200,
  "message": "Lấy danh sách quyền thành công",
  "error": null,
  "data": {
    "ACCOUNT": [
      { "permissionId": "VIEW_ACCOUNT", "permissionName": "Xem tài khoản" },
      { "permissionId": "CREATE_ACCOUNT", "permissionName": "Tạo tài khoản" }
    ],
    "EMPLOYEE": [...],
    "PATIENT": [...]
  }
}
```

---

## ️ Error Handling

All error responses follow this format:

```json
{
  "statusCode": 400,
  "message": "Validation error message",
  "error": "error.validation",
  "data": null
}
```

### Common Error Codes:

| Error Code                    | Description                         |
| ----------------------------- | ----------------------------------- |
| `error.authentication.failed` | Invalid username or password        |
| `error.access.denied`         | Insufficient permissions            |
| `error.account.not.verified`  | Account email not verified          |
| `error.token.expired`         | Verification or reset token expired |
| `error.token.invalid`         | Invalid token                       |
| `error.rate.limit.exceeded`   | Too many requests (rate limited)    |
| `error.validation`            | Request validation failed           |
| `error.bad.request`           | Malformed request                   |
| `error.not.found`             | Resource not found                  |
| `error.internal`              | Internal server error               |

---

## 🔒 Security & Rate Limiting

### Rate Limiting

To prevent abuse and brute force attacks, the following endpoints are rate limited:

| Endpoint              | Limit                      | Window | Purpose                  |
| --------------------- | -------------------------- | ------ | ------------------------ |
| `/auth/forgot-password` | 3 requests per IP          | 15 min | Prevent email spam       |
| `/auth/reset-password`  | 5 attempts per IP          | 10 min | Prevent brute force      |

**Rate Limit Response (429 Too Many Requests):**

```json
{
  "statusCode": 429,
  "message": "Bạn đã vượt quá số lần yêu cầu cho phép. Vui lòng thử lại sau X phút.",
  "error": "error.rate.limit.exceeded",
  "data": {
    "retryAfterSeconds": 600
  }
}
```

**Headers:**
- `Retry-After: 600` (seconds until rate limit resets)

### Security Best Practices

1. **Email Enumeration Prevention:**
   - `/auth/forgot-password` always returns 200 OK, even if email doesn't exist
   - Prevents attackers from discovering valid email addresses

2. **Token Security:**
   - Password reset tokens are UUID format (128-bit random)
   - Single-use tokens (cannot be reused)
   - 24-hour expiration
   - Tokens are invalidated after use

3. **Password Requirements:**
   - Minimum 8 characters
   - Must contain uppercase, lowercase, number, and special character
   - Same requirements across all password endpoints (reset, create, change)

4. **Request Logging:**
   - All authentication attempts are logged with IP address
   - Failed attempts are logged for security monitoring
   - Rate limit violations are logged

---

##  Status Codes

| Status Code                 | Description                           |
| --------------------------- | ------------------------------------- |
| `200 OK`                    | Request successful                    |
| `201 Created`               | Resource created successfully         |
| `400 Bad Request`           | Validation error or malformed request |
| `401 Unauthorized`          | Authentication required or failed     |
| `403 Forbidden`             | Insufficient permissions              |
| `404 Not Found`             | Resource not found                    |
| `409 Conflict`              | Resource already exists (duplicate)   |
| `429 Too Many Requests`     | Rate limit exceeded                   |
| `500 Internal Server Error` | Server error                          |

---

##  Email Verification Flow

### For NEW Accounts (Created via API):

1. **Patient Registration:**

   - POST `/api/v1/patients` with username, password, email
   - Account status: `PENDING_VERIFICATION`
   - Verification email sent automatically

2. **Email Verification:**

   - User clicks link in email → GET `/api/v1/auth/verify-email?token={token}`
   - Account status changes to `ACTIVE`

3. **Login:**

   - POST `/api/v1/auth/login`
   - If status = `PENDING_VERIFICATION` → `403 Forbidden` with message "Tài khoản chưa được xác thực"
   - If status = `ACTIVE` → Login successful

4. **First Login:**
   - Response includes `mustChangePassword: true`
   - User should change password via POST `/api/v1/accounts/change-password`

### For SEEDED Accounts (Demo Data):

- Status: `ACTIVE` (skip verification)
- Can login immediately without email verification
- Default password: "123456"

---

##  Password Reset Flow

1. **Forgot Password:**

   - POST `/api/v1/auth/forgot-password` with email
   - Reset email sent with token (1-hour expiry)

2. **Reset Password:**

   - User clicks link in email
   - POST `/api/v1/auth/reset-password` with token, newPassword, confirmPassword
   - Password updated, `mustChangePassword` set to `false`

3. **Login with New Password:**
   - POST `/api/v1/auth/login` with new credentials

---

##  Notes

1. **Authentication:** Most endpoints require `Authorization: Bearer <token>` header
2. **Refresh Token:** Automatically stored in HTTP-only cookie
3. **Pagination:** Default page size is 10, maximum is 100
4. **Date Format:** ISO 8601 format (YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss)
5. **Permission System:** 9 modules with merged permissions (see seed data)
6. **Email Service:** Requires SMTP configuration (Gmail recommended)

---

##  Getting Started

### 1. Environment Variables:

```bash
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
FRONTEND_URL=http://localhost:3000
```

### 2. Default Admin Account:

```
Username: admin
Password: 123456
Status: ACTIVE (no verification needed)
```

### 3. Test New Patient Flow:

```bash
# 1. Create patient with account
POST /api/v1/patients
{
  "fullName": "Test User",
  "email": "test@example.com",
  "username": "testuser",
  "password": "Test123"
}

# 2. Check email for verification link
# Click link: GET /api/v1/auth/verify-email?token={token}

# 3. Login
POST /api/v1/auth/login
{
  "username": "testuser",
  "password": "Test123"
}
```

---

**Last Updated:** October 23, 2025
**Swagger UI:** http://localhost:8080/swagger-ui.html
