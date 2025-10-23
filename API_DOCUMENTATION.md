# 🏥 DENTAL CLINIC MANAGEMENT SYSTEM - API DOCUMENTATION

**Version:** 1.0.0
**Base URL:** `http://localhost:8080/api/v1`
**Authentication:** JWT Bearer Token (except public endpoints)

---

## 📚 Table of Contents

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
12. [Status Codes](#status-codes)

---

## 🔐 Authentication APIs

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

### 4. Verify Email ✨ NEW

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

### 5. Resend Verification Email ✨ NEW

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

### 6. Forgot Password ✨ NEW

**POST** `/auth/forgot-password`

Initiate password reset process. Sends password reset email to user.

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

**Errors:**

- `404 Not Found` - Email not found

---

### 7. Reset Password ✨ NEW

**POST** `/auth/reset-password`

Reset password using token from email. Token expires in 1 hour and can only be used once.

**Request Body:**

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewPass123",
  "confirmPassword": "NewPass123"
}
```

**Validation Rules:**

- Password: 6-50 characters
- Must contain at least 1 letter and 1 number
- newPassword and confirmPassword must match

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

- `400 Bad Request` - Invalid token, token expired, or token already used
- `400 Bad Request` - Passwords don't match
- `400 Bad Request` - Password validation failed

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

## 👤 Account Management APIs

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

## 👥 Employee Management APIs

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

## 🏥 Patient Management APIs

Base path: `/api/v1/patients`

### 1. Get All Patients

**GET** `/patients?page=0&size=10&sortBy=patientId&sortDirection=ASC`

**Required Permission:** `VIEW_PATIENT` or `ADMIN` role

**Response:** Similar structure to Employee list

---

### 2. Create Patient ✨ UPDATED

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

## 📅 Appointment Management APIs

Base path: `/api/v1/appointments`

### 1. Get All Appointments

**GET** `/appointments?page=0&size=10`

**Required Permission:** `VIEW_APPOINTMENT` or `ADMIN` role

### 2. Create Appointment

**POST** `/appointments`

**Required Permission:** `CREATE_APPOINTMENT` or `ADMIN` role

**Request Body:**

```json
{
  "patientId": 101,
  "employeeId": 5,
  "appointmentDate": "2025-10-25",
  "appointmentTime": "10:00:00",
  "reason": "Khám tổng quát",
  "notes": "Bệnh nhân có tiền sử đau răng"
}
```

---

## 🦷 Treatment Management APIs

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

## 📝 Shift Registration & Renewal APIs

Base path: `/api/v1/shift-registrations`

### 1. Register for Shift

**POST** `/shift-registrations`

**Required Permission:** `CREATE_REGISTRATION` or `ADMIN` role

### 2. Renew Shift Registration

**POST** `/shift-registrations/{registrationId}/renew`

**Required Permission:** `CREATE_SHIFT_RENEWAL` or `ADMIN` role

---

## 🌴 Time Off & Overtime APIs

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

## 🔐 Role & Permission Management APIs

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

## ⚠️ Error Handling

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
| `error.validation`            | Request validation failed           |
| `error.bad.request`           | Malformed request                   |
| `error.not.found`             | Resource not found                  |
| `error.internal`              | Internal server error               |

---

## 📊 Status Codes

| Status Code                 | Description                           |
| --------------------------- | ------------------------------------- |
| `200 OK`                    | Request successful                    |
| `201 Created`               | Resource created successfully         |
| `400 Bad Request`           | Validation error or malformed request |
| `401 Unauthorized`          | Authentication required or failed     |
| `403 Forbidden`             | Insufficient permissions              |
| `404 Not Found`             | Resource not found                    |
| `409 Conflict`              | Resource already exists (duplicate)   |
| `500 Internal Server Error` | Server error                          |

---

## 📧 Email Verification Flow

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

## 🔄 Password Reset Flow

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

## 📝 Notes

1. **Authentication:** Most endpoints require `Authorization: Bearer <token>` header
2. **Refresh Token:** Automatically stored in HTTP-only cookie
3. **Pagination:** Default page size is 10, maximum is 100
4. **Date Format:** ISO 8601 format (YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss)
5. **Permission System:** 9 modules with merged permissions (see seed data)
6. **Email Service:** Requires SMTP configuration (Gmail recommended)

---

## 🚀 Getting Started

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
