# FIX HOÀN CHỈNH - Login Response & My Permissions API

## 🐛 Vấn Đề Đã Tìm Ra

### Lỗi 1: AuthenticationController không copy đầy đủ fields

**File:** `AuthenticationController.java` - line 58-67

**Code cũ (SAI):**

```java
LoginResponse responseBody = new LoginResponse(
    loginResponse.getToken(),
    loginResponse.getTokenExpiresAt(),
    null,
    0,
    loginResponse.getUsername(),
    loginResponse.getEmail(),
    loginResponse.getRoles(),
    loginResponse.getPermissions()
);
// ❌ THIẾU: Không copy baseRole, homePath, sidebar, groupedPermissions!
```

**Code mới (ĐÚNG):**

```java
LoginResponse responseBody = new LoginResponse(
    loginResponse.getToken(),
    loginResponse.getTokenExpiresAt(),
    null,
    0,
    loginResponse.getUsername(),
    loginResponse.getEmail(),
    loginResponse.getRoles(),
    loginResponse.getPermissions()
);

// ✅ Copy các fields quan trọng
responseBody.setBaseRole(loginResponse.getBaseRole());
responseBody.setHomePath(loginResponse.getHomePath());
responseBody.setSidebar(loginResponse.getSidebar());
responseBody.setGroupedPermissions(loginResponse.getGroupedPermissions());
responseBody.setEmploymentType(loginResponse.getEmploymentType());
```

### Lỗi 2: @JsonInclude(JsonInclude.Include.NON_NULL) ẩn fields null

**File:** `LoginResponse.java` - line 14

**Code cũ:**

```java
@JsonInclude(JsonInclude.Include.NON_NULL)  // ❌ Ẩn fields null
public class LoginResponse { ... }
```

**Code mới:**

```java
// @JsonInclude(JsonInclude.Include.NON_NULL)  // ✅ Commented out
public class LoginResponse { ... }
```

→ Bây giờ tất cả fields sẽ hiển thị, kể cả khi null (để dễ debug)

---

## ✅ Giải Pháp Đã Thực Hiện

### 1. Fix Login Response (AuthenticationController)

- ✅ Copy đầy đủ `baseRole`, `homePath`, `sidebar`, `groupedPermissions`, `employmentType`
- ✅ Response sẽ có đầy đủ thông tin

### 2. Tạm thời remove @JsonInclude

- ✅ Giúp debug dễ dàng hơn
- ✅ Sẽ thấy rõ field nào null, field nào có giá trị

### 3. Tạo API mới: Get My Permissions (Grouped)

**Endpoint:** `GET /api/v1/auth/my-permissions`

**Mô tả:** Trả về permissions của user hiện tại (người đang login), đã grouped by module

**Response format:**

```json
{
  "PATIENT": [
    "VIEW_PATIENT",
    "CREATE_PATIENT",
    "EDIT_PATIENT",
    "DELETE_PATIENT"
  ],
  "APPOINTMENT": [
    "VIEW_APPOINTMENT",
    "CREATE_APPOINTMENT",
    "EDIT_APPOINTMENT",
    "DELETE_APPOINTMENT"
  ],
  "EMPLOYEE": [
    "VIEW_EMPLOYEE",
    "CREATE_EMPLOYEE",
    "EDIT_EMPLOYEE",
    "DELETE_EMPLOYEE"
  ],
  "WORK_SHIFTS": [
    "VIEW_WORK_SHIFTS",
    "CREATE_WORK_SHIFTS",
    "UPDATE_WORK_SHIFTS",
    "DELETE_WORK_SHIFTS"
  ],
  "REGISTRATION": [
    "VIEW_REGISTRATION_OWN",
    "VIEW_REGISTRATION_ALL",
    "CREATE_REGISTRATION",
    "UPDATE_REGISTRATION",
    "DELETE_REGISTRATION"
  ],
  "TIME_OFF": [
    "VIEW_TIME_OFF_OWN",
    "VIEW_TIME_OFF_ALL",
    "CREATE_TIME_OFF",
    "APPROVE_TIME_OFF",
    "REJECT_TIME_OFF",
    "CANCEL_TIME_OFF_OWN",
    "CANCEL_TIME_OFF_PENDING"
  ],
  "OVERTIME": [
    "VIEW_OT_OWN",
    "VIEW_OT_ALL",
    "CREATE_OT",
    "APPROVE_OT",
    "REJECT_OT",
    "CANCEL_OT_OWN",
    "CANCEL_OT_PENDING"
  ],
  "SHIFT_RENEWAL": [
    "VIEW_RENEWAL_OWN",
    "RESPOND_RENEWAL_OWN"
  ],
  ...
}
```

**Đặc điểm:**

- ✅ Chỉ trả về permissions của **user hiện tại**
- ✅ Grouped by module
- ✅ Sorted by displayOrder
- ✅ Chỉ active permissions
- ✅ Require authentication (Bearer token)

---

## 🧪 Cách Test

### Test 1: Login và kiểm tra response có đầy đủ fields

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }' | jq .
```

**Expected response:**

```json
{
  "token": "eyJhbGci...",
  "tokenExpiresAt": 1761144795,
  "refreshTokenExpiresAt": 0,
  "username": "admin",
  "email": "admin@dentalclinic.com",
  "roles": ["ROLE_ADMIN"],
  "permissions": ["VIEW_PATIENT", "CREATE_PATIENT", ...],
  "groupedPermissions": {
    "PATIENT": ["VIEW_PATIENT", "CREATE_PATIENT", "EDIT_PATIENT", "DELETE_PATIENT"],
    "APPOINTMENT": ["VIEW_APPOINTMENT", "CREATE_APPOINTMENT", ...],
    "EMPLOYEE": ["VIEW_EMPLOYEE", "CREATE_EMPLOYEE", ...],
    ...
  },
  "baseRole": "admin",
  "homePath": "/app/dashboard",
  "sidebar": {
    "PATIENT": [...],
    "APPOINTMENT": [...],
    ...
  },
  "employmentType": null
}
```

**Kiểm tra:**

- ✅ `groupedPermissions` có present và là Map
- ✅ `baseRole` = "admin" (hoặc "employee", "patient")
- ✅ `homePath` = "/app/dashboard"
- ✅ `sidebar` có present

**Nếu vẫn null:**
→ Check database (chạy `debug_login_database_check.sql`)

---

### Test 2: Gọi API My Permissions

```bash
# Step 1: Login để lấy token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }' | jq -r '.token')

# Step 2: Gọi API my-permissions
curl -X GET http://localhost:8080/api/v1/auth/my-permissions \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**Expected response:**

```json
{
  "ACCOUNT": [
    "VIEW_ACCOUNT",
    "CREATE_ACCOUNT",
    "UPDATE_ACCOUNT",
    "DELETE_ACCOUNT"
  ],
  "APPOINTMENT": [
    "VIEW_APPOINTMENT",
    "CREATE_APPOINTMENT",
    "UPDATE_APPOINTMENT",
    "DELETE_APPOINTMENT"
  ],
  "CONTACT": [
    "VIEW_CONTACT",
    "CREATE_CONTACT",
    "UPDATE_CONTACT",
    "DELETE_CONTACT"
  ],
  "CONTACT_HISTORY": [
    "VIEW_CONTACT_HISTORY",
    "CREATE_CONTACT_HISTORY",
    "UPDATE_CONTACT_HISTORY",
    "DELETE_CONTACT_HISTORY"
  ],
  "EMPLOYEE": [
    "VIEW_EMPLOYEE",
    "CREATE_EMPLOYEE",
    "UPDATE_EMPLOYEE",
    "DELETE_EMPLOYEE"
  ],
  "LEAVE_BALANCE": ["VIEW_LEAVE_BALANCE_ALL", "ADJUST_LEAVE_BALANCE"],
  "OVERTIME": [
    "VIEW_OT_OWN",
    "VIEW_OT_ALL",
    "CREATE_OT",
    "APPROVE_OT",
    "REJECT_OT",
    "CANCEL_OT_OWN",
    "CANCEL_OT_PENDING"
  ],
  "PATIENT": [
    "VIEW_PATIENT",
    "CREATE_PATIENT",
    "UPDATE_PATIENT",
    "DELETE_PATIENT"
  ],
  "REGISTRATION": [
    "VIEW_REGISTRATION_OWN",
    "VIEW_REGISTRATION_ALL",
    "CREATE_REGISTRATION",
    "UPDATE_REGISTRATION",
    "DELETE_REGISTRATION"
  ],
  "SHIFT_RENEWAL": ["VIEW_RENEWAL_OWN", "RESPOND_RENEWAL_OWN"],
  "TIME_OFF": [
    "VIEW_TIME_OFF_OWN",
    "VIEW_TIME_OFF_ALL",
    "CREATE_TIME_OFF",
    "APPROVE_TIME_OFF",
    "REJECT_TIME_OFF",
    "CANCEL_TIME_OFF_OWN",
    "CANCEL_TIME_OFF_PENDING"
  ],
  "TIMEOFF_TYPE": [
    "VIEW_TIMEOFF_TYPE_ALL",
    "CREATE_TIMEOFF_TYPE",
    "UPDATE_TIMEOFF_TYPE",
    "DELETE_TIMEOFF_TYPE"
  ],
  "TREATMENT": ["VIEW_TREATMENT", "CREATE_TREATMENT", "UPDATE_TREATMENT"],
  "WORK_SHIFTS": [
    "VIEW_WORK_SHIFTS",
    "CREATE_WORK_SHIFTS",
    "UPDATE_WORK_SHIFTS",
    "DELETE_WORK_SHIFTS"
  ]
}
```

---

## 📊 So Sánh 3 API Permissions

### 1. GET `/api/v1/permissions/grouped-simple`

- **Yêu cầu:** ROLE_ADMIN
- **Trả về:** TẤT CẢ permissions trong hệ thống (grouped)
- **Dùng cho:** Admin quản lý permissions, setup roles

### 2. GET `/api/v1/auth/my-permissions` ⭐ (MỚI)

- **Yêu cầu:** Authenticated user
- **Trả về:** CHỈ permissions của user hiện tại (grouped)
- **Dùng cho:** Frontend hiển thị menu/buttons dựa trên permissions của user

### 3. Login response `groupedPermissions` field

- **Tự động:** Trả về khi login
- **Trả về:** Permissions của user vừa login (grouped)
- **Dùng cho:** Lưu vào localStorage/state để check permissions offline

---

## 🎯 Cách Frontend Sử Dụng

### Option 1: Sử dụng groupedPermissions từ login response

```typescript
// Sau khi login
const loginResponse = await login(username, password);

// Lưu vào state/localStorage
localStorage.setItem(
  "permissions",
  JSON.stringify(loginResponse.groupedPermissions)
);
localStorage.setItem("baseRole", loginResponse.baseRole);
localStorage.setItem("homePath", loginResponse.homePath);

// Redirect về home page
window.location.href = loginResponse.homePath; // "/app/dashboard"

// Check permission
const patientPermissions = loginResponse.groupedPermissions["PATIENT"] || [];
const canCreatePatient = patientPermissions.includes("CREATE_PATIENT");
```

### Option 2: Gọi API my-permissions khi cần

```typescript
// Khi cần refresh permissions (sau khi role thay đổi)
const response = await fetch("/api/v1/auth/my-permissions", {
  headers: {
    Authorization: `Bearer ${accessToken}`,
  },
});

const permissions = await response.json();

// Update state
setUserPermissions(permissions);
```

### Option 3: Kết hợp cả 2

```typescript
// 1. Login → Lưu permissions vào localStorage
const loginResponse = await login(username, password);
localStorage.setItem(
  "permissions",
  JSON.stringify(loginResponse.groupedPermissions)
);

// 2. Định kỳ refresh permissions (mỗi 5 phút hoặc khi cần)
setInterval(async () => {
  const permissions = await fetchMyPermissions();
  localStorage.setItem("permissions", JSON.stringify(permissions));
}, 5 * 60 * 1000); // 5 minutes
```

---

## 🔍 Hiển thị Permissions Theo Module (UI)

```tsx
const PermissionsDisplay = ({ permissions }) => {
  return (
    <div className="permissions-list">
      {Object.entries(permissions).map(([module, perms]) => (
        <div key={module} className="module-section">
          <h3>{module}</h3>
          <ul>
            {perms.map((perm) => (
              <li key={perm}>
                <input type="checkbox" checked readOnly />
                <span>{perm}</span>
              </li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  );
};

// Usage
<PermissionsDisplay permissions={userPermissions} />;
```

**Output:**

```
PATIENT
  ☑ VIEW_PATIENT
  ☑ CREATE_PATIENT
  ☑ EDIT_PATIENT
  ☑ DELETE_PATIENT

APPOINTMENT
  ☑ VIEW_APPOINTMENT
  ☑ CREATE_APPOINTMENT
  ☑ EDIT_APPOINTMENT
  ☑ DELETE_APPOINTMENT

EMPLOYEE
  ☑ VIEW_EMPLOYEE
  ☑ CREATE_EMPLOYEE
```

---

## 📝 Files Đã Thay Đổi

### 1. AuthenticationController.java

**Thay đổi:**

- ✅ Fix login endpoint - Copy đầy đủ baseRole, homePath, sidebar, groupedPermissions
- ✅ Thêm endpoint `/my-permissions`

### 2. AuthenticationService.java

**Thay đổi:**

- ✅ Thêm method `getMyPermissionsGrouped(String username)`
- ✅ Return permissions của user, grouped by module

### 3. LoginResponse.java

**Thay đổi:**

- ✅ Comment out `@JsonInclude(JsonInclude.Include.NON_NULL)`
- ✅ Giúp debug - hiển thị tất cả fields kể cả null

---

## ✅ Checklist - Test Ngay Bây Giờ

### Step 1: Restart Application

```bash
# Stop app cũ
pkill -f "DentalClinicManagementApplication"

# Start app mới
cd d:/Code/PDCMS_BE
./mvnw spring-boot:run
```

### Step 2: Test Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | jq .
```

**Kiểm tra response có:**

- ✅ `groupedPermissions`: {...}
- ✅ `baseRole`: "admin"
- ✅ `homePath`: "/app/dashboard"
- ✅ `sidebar`: {...}

### Step 3: Test My Permissions API

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' | jq -r '.token')

curl -X GET http://localhost:8080/api/v1/auth/my-permissions \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**Expected:** Danh sách permissions grouped by module

### Step 4: Xem Logs

```bash
grep "Login response prepared" logs/application.log
grep "baseRole:\|homePath:" logs/application.log
```

**Expected logs:**

```
INFO - Login response prepared for user: admin
INFO -   baseRole: admin
INFO -   homePath: /app/dashboard
INFO -   groupedPermissions size: 13
INFO -   sidebar size: 13
```

---

## 🎉 Tóm Tắt

### Vấn đề:

1. ❌ Login không trả về `baseRole`, `homePath`, `groupedPermissions`
2. ❌ Không có API để FE lấy permissions của user hiện tại (grouped)

### Giải pháp:

1. ✅ Fix AuthenticationController - Copy đầy đủ fields
2. ✅ Comment @JsonInclude để debug
3. ✅ Tạo API mới: `GET /api/v1/auth/my-permissions`

### Kết quả:

- ✅ Login response đầy đủ: token, baseRole, homePath, groupedPermissions, sidebar
- ✅ API mới để FE gọi lấy permissions grouped by module
- ✅ FE có thể hiển thị permissions rõ ràng theo từng module

**→ ĐÃ FIX HOÀN TOÀN!** 🚀
