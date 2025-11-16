# Trả Lời FE Team: VIEW_SERVICE Permission cho ROLE_DENTIST

**Date:** 2025-11-16
**Question From:** FE Team
**Answered By:** BE Team

---

## ❓ Câu Hỏi

> Employee (doctor) có cần VIEW_SERVICE permission để thêm items vào treatment plan không?
>
> - Nếu CÓ → Cần assign VIEW_SERVICE cho ROLE_DENTIST trong seed data
> - Nếu KHÔNG → Có thể tạo endpoint riêng không yêu cầu permission

---

## ✅ Câu Trả Lời: **CÓ - Đã được fix**

### 🎯 Kết Luận

**ROLE_DENTIST CẦN permission `VIEW_SERVICE`** để:

1. Load danh sách services khi thêm items vào treatment plan (API 5.7)
2. Load danh sách services khi đặt lịch hẹn (appointment booking)

### ✅ Fix Đã Áp Dụng

**File:** `src/main/resources/db/dental-clinic-seed-data.sql`

**Change:**

```sql
-- Treatment Plan permissions
('ROLE_DENTIST', 'VIEW_TREATMENT_PLAN_OWN'),
('ROLE_DENTIST', 'CREATE_TREATMENT_PLAN'),
('ROLE_DENTIST', 'UPDATE_TREATMENT_PLAN'),
('ROLE_DENTIST', 'DELETE_TREATMENT_PLAN'),
-- ✅ NEW: Service Management permission
('ROLE_DENTIST', 'VIEW_SERVICE')  -- Load service list when adding items to treatment plan
```

---

## 📋 Chi Tiết Technical

### Workflow Frontend

```
1. Doctor mở modal "Thêm hạng mục" trong treatment plan
   ↓
2. Frontend cần load service dropdown:
   GET /api/v1/services/grouped  ← Requires VIEW_SERVICE permission
   hoặc
   GET /api/v1/services?isActive=true
   ↓
3. Dropdown hiển thị:
   - Name: "Trám răng composite"
   - Code: "FILLING_COMP"
   - Price: 500,000 VND
   ↓
4. Doctor chọn service → auto-fill price
   ↓
5. Submit items:
   POST /api/v1/patient-plan-phases/{phaseId}/items  ← Requires UPDATE_TREATMENT_PLAN
   Body: [{ "serviceCode": "FILLING_COMP", "quantity": 1, "price": 500000 }]
```

### API Endpoints Affected

| API | Method | Endpoint                                      | Permission Required     | Purpose                          |
| --- | ------ | --------------------------------------------- | ----------------------- | -------------------------------- |
| 6.2 | GET    | `/api/v1/services/grouped`                    | `VIEW_SERVICE`          | Load service dropdown (internal) |
| 6.3 | GET    | `/api/v1/services`                            | `VIEW_SERVICE`          | Load service list with filters   |
| 5.7 | POST   | `/api/v1/patient-plan-phases/{phaseId}/items` | `UPDATE_TREATMENT_PLAN` | Add items to plan                |

**Before Fix:**

- ❌ Doctor có `UPDATE_TREATMENT_PLAN` nhưng KHÔNG có `VIEW_SERVICE`
- ❌ Frontend không load được service dropdown → 403 Forbidden
- ❌ Feature bị block

**After Fix:**

- ✅ Doctor có cả `UPDATE_TREATMENT_PLAN` VÀ `VIEW_SERVICE`
- ✅ Frontend load được service dropdown
- ✅ Feature hoạt động đầy đủ

---

## 🧪 Testing Instructions (After Deployment)

### Test 1: Verify Permission Assigned

```bash
# Login as doctor
POST /api/v1/auth/login
{
  "username": "bacsi1",
  "password": "123456"
}

# Decode JWT token at https://jwt.io
# ✅ Check: "permissions" array should contain "VIEW_SERVICE"
```

### Test 2: Load Service List

```bash
TOKEN="<doctor_token>"

# Test internal grouped services endpoint
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/services/grouped"

# ✅ Expected: 200 OK
# Response: [{ categoryCode: "...", services: [...] }]
```

### Test 3: Add Items to Treatment Plan

```bash
TOKEN="<doctor_token>"

# Step 1: Get service list (should work now)
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/services?isActive=true&page=0&size=50"

# ✅ Expected: 200 OK with service list

# Step 2: Add items using serviceCode from step 1
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  "http://localhost:8080/api/v1/patient-plan-phases/10/items" \
  -d '[{
    "serviceCode": "FILLING_COMP",
    "quantity": 2,
    "price": 500000,
    "notes": "Trám 2 răng sâu 46, 47"
  }]'

# ✅ Expected: 201 Created
```

---

## 📱 Frontend Changes Required

### ❌ Before (Broken)

```typescript
// Service dropdown component
const loadServices = async () => {
  const response = await fetch("/api/v1/services/grouped", {
    headers: { Authorization: `Bearer ${token}` },
  });

  if (!response.ok) {
    // ❌ Gets 403 Forbidden - ROLE_DENTIST lacks VIEW_SERVICE
    console.error("Cannot load services");
    return [];
  }

  return response.json();
};
```

### ✅ After (Working)

```typescript
// Service dropdown component
const loadServices = async () => {
  const response = await fetch("/api/v1/services/grouped", {
    headers: { Authorization: `Bearer ${token}` },
  });

  if (!response.ok) {
    throw new Error("Failed to load services");
  }

  // ✅ Now returns 200 OK - ROLE_DENTIST has VIEW_SERVICE
  return response.json();
};

// Usage in treatment plan modal
<ServiceDropdown
  onSelect={(service) => {
    setSelectedService(service);
    setPrice(service.defaultPrice); // Auto-fill price
  }}
/>;
```

**No Code Changes Required** - Dropdown sẽ tự động work sau khi deploy seed data mới ✅

---

## 🔐 Security Analysis

### Q: Có security risk không khi cho DENTIST permission VIEW_SERVICE?

**A: KHÔNG** - Lý do:

1. **Read-only permission:**

   - `VIEW_SERVICE` chỉ cho phép xem danh sách services
   - KHÔNG cho phép create/update/delete services

2. **Business logic hợp lý:**

   - Doctor cần biết services có sẵn để add vào treatment plan
   - Giống như doctor cần xem danh sách thuốc để kê đơn

3. **Consistent với other modules:**

   - Appointment booking cũng cần VIEW_SERVICE để load service dropdown
   - Receptionist cũng có VIEW_SERVICE để tư vấn dịch vụ cho bệnh nhân

4. **Data không sensitive:**
   - Service list (name, code, price) không phải thông tin nhạy cảm
   - Giá dịch vụ public trên website marketing

### Permission Matrix After Fix

| Role         | VIEW_SERVICE | CREATE_SERVICE | UPDATE_SERVICE | DELETE_SERVICE |
| ------------ | ------------ | -------------- | -------------- | -------------- |
| ADMIN        | ✅           | ✅             | ✅             | ✅             |
| MANAGER      | ✅           | ✅             | ✅             | ✅             |
| DENTIST      | ✅ **NEW**   | ❌             | ❌             | ❌             |
| NURSE        | ❌           | ❌             | ❌             | ❌             |
| RECEPTIONIST | ❌           | ❌             | ❌             | ❌             |
| PATIENT      | ❌           | ❌             | ❌             | ❌             |

**Separation of Duties:**

- ✅ DENTIST: Can VIEW services (để add vào treatment plan)
- ✅ MANAGER: Can CRUD services (quản lý danh mục)
- ✅ Clear separation maintained

---

## 🚀 Deployment Plan

### Step 1: Backend Deployment

**File Changed:**

- `src/main/resources/db/dental-clinic-seed-data.sql` (1 line added)

**Deployment:**

```bash
# Option A: Fresh database setup
psql -U root -d dental_clinic_db < src/main/resources/db/dental-clinic-seed-data.sql

# Option B: Add permission to existing database
psql -U root -d dental_clinic_db -c "
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_DENTIST', permission_id
FROM permissions
WHERE permission_name = 'VIEW_SERVICE'
ON CONFLICT (role_id, permission_id) DO NOTHING;
"
```

### Step 2: Verify Deployment

```bash
# Check permission assigned
psql -U root -d dental_clinic_db -c "
SELECT r.role_name, p.permission_name
FROM role_permissions rp
JOIN roles r ON r.role_id = rp.role_id
JOIN permissions p ON p.permission_id = rp.permission_id
WHERE r.role_name = 'ROLE_DENTIST'
  AND p.permission_name = 'VIEW_SERVICE';
"

# ✅ Expected output:
#  role_name   | permission_name
# -------------+-----------------
#  ROLE_DENTIST | VIEW_SERVICE
```

### Step 3: Test with Real Account

```bash
# 1. Doctor login (any dentist account)
# 2. Navigate to Treatment Plan → Add Items modal
# 3. Service dropdown should load successfully
# ✅ If dropdown shows services → Fix successful
# ❌ If 403 Forbidden → Check permission in database
```

---

## 📝 Summary for FE Team

### ✅ Short Answer

**Có, ROLE_DENTIST cần VIEW_SERVICE permission.**

Lý do: Frontend cần load service dropdown khi doctor thêm items vào treatment plan.

**Backend đã fix:** Added `VIEW_SERVICE` permission cho `ROLE_DENTIST` trong seed data.

**Frontend changes required:** NONE - Dropdown sẽ tự động work sau deploy.

---

### 📊 Impact

| Aspect            | Before                     | After                |
| ----------------- | -------------------------- | -------------------- |
| Service dropdown  | ❌ 403 Forbidden           | ✅ 200 OK            |
| Add items feature | ❌ Blocked                 | ✅ Working           |
| Doctor workflow   | ❌ Cannot customize plans  | ✅ Full control      |
| Frontend code     | ⚠️ Error handling required | ✅ Works as designed |

---

### 🔗 Related APIs

**Service APIs (now accessible to DENTIST):**

- `GET /api/v1/services/grouped` - Load services by category (for dropdown)
- `GET /api/v1/services` - Load all services with filters & search

**Treatment Plan APIs (already accessible):**

- `POST /api/v1/patient-plan-phases/{phaseId}/items` - Add items to plan

---

### ❓ Questions?

**Contact:** Backend Team
**Slack:** #treatment-plans
**Status:** ✅ FIXED - Ready for testing after next deployment

---

**Last Updated:** 2025-11-16 06:45 GMT+7
**Version:** 1.0.0
**Status:** ✅ RESOLVED
