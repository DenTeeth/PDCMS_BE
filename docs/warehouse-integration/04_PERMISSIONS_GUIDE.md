# 🔐 Permissions Guide - Phân Quyền Chi Tiết

## 🎯 Mục Đích
Giải thích **CHI TIẾT** permissions cần thiết cho từng hành động trong warehouse integration.

---

## 📋 Permissions Overview

| Permission | Module | Mục đích |
|------------|--------|----------|
| `VIEW_CLINICAL_RECORD` | Clinical Records | Xem thông tin clinical record & procedures |
| `WRITE_CLINICAL_RECORD` | Clinical Records | Tạo/cập nhật clinical records & procedures |
| `VIEW_WAREHOUSE` | Warehouse | Xem thông tin vật tư, tồn kho |
| `VIEW_WAREHOUSE_COST` | Warehouse | Xem giá vật tư, chi phí |
| `MANAGE_WAREHOUSE` | Warehouse | Quản lý BOM, vật tư, kho |

---

## 👥 Role-Based Permissions

### 🔴 ROLE_ADMIN

**Permissions:**
```
✅ VIEW_CLINICAL_RECORD
✅ WRITE_CLINICAL_RECORD
✅ VIEW_WAREHOUSE
✅ VIEW_WAREHOUSE_COST
✅ MANAGE_WAREHOUSE
```

**Capabilities:**
- ✅ Xem tất cả thông tin clinical record
- ✅ Xem vật tư đã dùng (có giá)
- ✅ Cập nhật số lượng thực tế
- ✅ Quản lý BOM của dịch vụ
- ✅ Xem báo cáo chi phí vật tư

---

### 🔵 ROLE_DOCTOR (Dentist)

**Permissions:**
```
✅ VIEW_CLINICAL_RECORD (own procedures)
✅ WRITE_CLINICAL_RECORD (own procedures)
❌ VIEW_WAREHOUSE_COST
```

**Capabilities:**
- ✅ Xem vật tư đã dùng trong procedures mình làm
- ✅ Cập nhật số lượng thực tế
- ❌ **KHÔNG** xem giá vật tư
- ❌ KHÔNG quản lý BOM

**API Response Example:**
```json
{
  "procedureId": 123,
  "materials": [
    {
      "itemName": "Găng tay y tế",
      "plannedQuantity": 1.00,
      "actualQuantity": 1.00,
      "unitPrice": null,          // ❌ NULL
      "totalPlannedCost": null,   // ❌ NULL
      "stockStatus": "OK",        // ✅ Visible
      "currentStock": 179         // ✅ Visible
    }
  ],
  "totalPlannedCost": null,       // ❌ NULL
  "totalActualCost": null         // ❌ NULL
}
```

---

### 🟢 ROLE_NURSE / ROLE_ASSISTANT

**Permissions:**
```
✅ VIEW_CLINICAL_RECORD
✅ WRITE_CLINICAL_RECORD
❌ VIEW_WAREHOUSE_COST
```

**Capabilities:**
- ✅ Xem vật tư đã dùng trong tất cả procedures
- ✅ Cập nhật số lượng thực tế (chính họ là người hay cập nhật!)
- ❌ **KHÔNG** xem giá vật tư
- ❌ KHÔNG quản lý BOM

**Use Case:**
Sau khi procedure hoàn thành, y tá/phụ tá kiểm tra lại số lượng vật tư thực tế đã dùng và cập nhật nếu khác với planned.

---

### 💰 ROLE_ACCOUNTANT

**Permissions:**
```
✅ VIEW_CLINICAL_RECORD
✅ VIEW_WAREHOUSE
✅ VIEW_WAREHOUSE_COST
❌ WRITE_CLINICAL_RECORD
```

**Capabilities:**
- ✅ Xem tất cả thông tin clinical record
- ✅ Xem vật tư đã dùng (CÓ GIÁ)
- ✅ Xem báo cáo chi phí vật tư
- ❌ **KHÔNG** cập nhật số lượng thực tế
- ❌ KHÔNG quản lý BOM

**API Response Example:**
```json
{
  "procedureId": 123,
  "materials": [
    {
      "itemName": "Găng tay y tế",
      "plannedQuantity": 1.00,
      "actualQuantity": 1.00,
      "unitPrice": 150000.00,      // ✅ Visible
      "totalPlannedCost": 150000.00, // ✅ Visible
      "totalActualCost": 150000.00
    }
  ],
  "totalPlannedCost": 4500000.00,  // ✅ Visible
  "totalActualCost": 4500000.00
}
```

---

### 🟡 ROLE_RECEPTIONIST

**Permissions:**
```
✅ VIEW_CLINICAL_RECORD (limited)
❌ WRITE_CLINICAL_RECORD
❌ VIEW_WAREHOUSE_COST
```

**Capabilities:**
- ✅ Xem thông tin appointment
- ✅ Xem danh sách vật tư đã dùng (không có giá)
- ❌ **KHÔNG** cập nhật số lượng
- ❌ KHÔNG xem giá

---

### 👤 ROLE_PATIENT

**Permissions:**
```
✅ VIEW_CLINICAL_RECORD (own only)
❌ Everything else
```

**Capabilities:**
- ✅ Xem clinical record của chính mình
- ✅ Xem vật tư đã dùng trong điều trị (không có giá)
- ❌ Không cập nhật gì cả

---

## 🔒 Permission Checks in Code

### 1. View Materials (API 8.7)

**Endpoint:**
```
GET /api/v1/clinical-records/procedures/{procedureId}/materials
```

**Permission Check:**
```java
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VIEW_CLINICAL_RECORD')")
public ProcedureMaterialsResponse getProcedureMaterials(Integer procedureId) {
    // ... get materials
    
    // Check if user can view costs
    boolean hasViewCostPermission = SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getAuthorities()
        .stream()
        .anyMatch(auth -> auth.getAuthority().equals("VIEW_WAREHOUSE_COST"));
    
    // If NO permission, set costs to null
    if (!hasViewCostPermission) {
        for (MaterialUsageItem item : materials) {
            item.setUnitPrice(null);
            item.setTotalPlannedCost(null);
            item.setTotalActualCost(null);
        }
        response.setTotalPlannedCost(null);
        response.setTotalActualCost(null);
        response.setCostVariance(null);
    }
    
    return response;
}
```

---

### 2. Update Materials (API 8.8)

**Endpoint:**
```
PUT /api/v1/clinical-records/procedures/{procedureId}/materials
```

**Permission Check:**
```java
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'WRITE_CLINICAL_RECORD')")
public UpdateMaterialsResponse updateProcedureMaterials(
    Integer procedureId, 
    UpdateMaterialsRequest request
) {
    // Only users with WRITE_CLINICAL_RECORD can update
    // ...
}
```

---

### 3. View Service BOM (API 6.17)

**Endpoint:**
```
GET /api/v1/warehouse/service-consumables/{serviceId}
```

**Permission Check:**
```java
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VIEW_SERVICE', 'VIEW_WAREHOUSE')")
public ServiceConsumablesResponse getServiceConsumables(Long serviceId) {
    // ... get consumables
    
    // Check cost permission
    boolean hasViewCostPermission = /* ... */;
    
    if (!hasViewCostPermission) {
        for (ConsumableItem item : consumables) {
            item.setUnitPrice(null);
            item.setTotalCost(null);
        }
        response.setTotalConsumableCost(null);
    }
    
    return response;
}
```

---

### 4. Manage BOM (API 6.18, 6.19)

**Endpoints:**
```
POST /api/v1/warehouse/consumables
PUT /api/v1/warehouse/service-consumables/{serviceId}
```

**Permission Check:**
```java
@PreAuthorize("hasAuthority('MANAGE_WAREHOUSE')")
public int updateServiceConsumables(Long serviceId, List<ConsumableRequest> consumables) {
    // Only ADMIN and WAREHOUSE_MANAGER can update BOM
    // ...
}
```

---

## 🧪 Testing Permissions

### Test 1: Doctor xem materials (no cost)

**Setup:**
```http
POST /auth/login
{
  "username": "dr.nguyen",
  "password": "password123"
}
```

**Test:**
```http
GET /clinical-records/procedures/123/materials
Authorization: Bearer <doctor_token>
```

**Expected:**
```json
{
  "materials": [
    {
      "unitPrice": null,
      "totalPlannedCost": null
    }
  ],
  "totalPlannedCost": null
}
```

**✅ PASS if:** All costs are `null`

---

### Test 2: Accountant xem materials (with cost)

**Setup:**
```http
POST /auth/login
{
  "username": "accountant.minh",
  "password": "password123"
}
```

**Test:**
```http
GET /clinical-records/procedures/123/materials
Authorization: Bearer <accountant_token>
```

**Expected:**
```json
{
  "materials": [
    {
      "unitPrice": 150000.00,
      "totalPlannedCost": 150000.00
    }
  ],
  "totalPlannedCost": 4500000.00
}
```

**✅ PASS if:** All costs are visible (NOT null)

---

### Test 3: Nurse cập nhật materials

**Setup:**
```http
POST /auth/login
{
  "username": "nurse.lan",
  "password": "password123"
}
```

**Test:**
```http
PUT /clinical-records/procedures/123/materials
Authorization: Bearer <nurse_token>

{
  "materials": [
    {
      "usageId": 1001,
      "actualQuantity": 2.0
    }
  ]
}
```

**Expected:**
```
200 OK - Update successful
```

**✅ PASS if:** No 403 Forbidden error

---

### Test 4: Receptionist cập nhật materials (should FAIL)

**Setup:**
```http
POST /auth/login
{
  "username": "receptionist.lan",
  "password": "password123"
}
```

**Test:**
```http
PUT /clinical-records/procedures/123/materials
Authorization: Bearer <receptionist_token>

{
  "materials": [...]
}
```

**Expected:**
```
403 Forbidden
{
  "error": "Access Denied",
  "message": "User does not have WRITE_CLINICAL_RECORD permission"
}
```

**✅ PASS if:** 403 Forbidden error

---

## 📊 Permission Matrix

| Action | Admin | Doctor | Nurse | Accountant | Receptionist | Patient |
|--------|-------|--------|-------|------------|--------------|---------|
| Xem vật tư (no cost) | ✅ | ✅ (own) | ✅ | ✅ | ✅ (limited) | ✅ (own) |
| Xem giá vật tư | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Cập nhật số lượng | ✅ | ✅ (own) | ✅ | ❌ | ❌ | ❌ |
| Xem BOM | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ |
| Cập nhật BOM | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Xem báo cáo chi phí | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |

---

## 🔧 Configuration

### Application Properties
```properties
# Enable method-level security
spring.security.enabled=true
security.enable-csrf=false

# JWT settings
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### Security Config
```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/clinical-records/**")
                    .hasAnyAuthority("ROLE_ADMIN", "VIEW_CLINICAL_RECORD")
                .requestMatchers("/api/v1/warehouse/**")
                    .hasAnyAuthority("ROLE_ADMIN", "VIEW_WAREHOUSE")
                .anyRequest().authenticated()
            )
            // ...
    }
}
```

---

## 🐛 Troubleshooting

### Issue 1: 403 Forbidden khi có permission

**Cause:** JWT token không chứa đúng authorities

**Check:**
```java
// Decode JWT token
String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
// Check claims['authorities']
```

**Solution:**
```http
POST /auth/login
{ "username": "admin", "password": "admin123" }
```

Get new token with correct authorities.

---

### Issue 2: Costs hiện ra cho user không có permission

**Cause:** Backend không check permission đúng cách

**Check Code:**
```java
// MUST have this check
boolean hasViewCostPermission = SecurityContextHolder
    .getContext()
    .getAuthentication()
    .getAuthorities()
    .stream()
    .anyMatch(auth -> auth.getAuthority().equals("VIEW_WAREHOUSE_COST"));

if (!hasViewCostPermission) {
    // Set costs to null
}
```

---

### Issue 3: User có permission nhưng vẫn không xem được

**Check:**
1. Permission có trong database?
```sql
SELECT p.permission_name 
FROM role_permissions rp
JOIN permissions p ON rp.permission_id = p.permission_id
WHERE rp.role_id = (
  SELECT role_id FROM roles WHERE role_name = 'ROLE_DOCTOR'
);
```

2. User có role đó?
```sql
SELECT r.role_name 
FROM user_roles ur
JOIN roles r ON ur.role_id = r.role_id
WHERE ur.user_id = (
  SELECT user_id FROM users WHERE username = 'dr.nguyen'
);
```

3. JWT token có authorities?
```
Decode JWT → Check 'authorities' claim
```

---

## 📚 Security Best Practices

### 1. Always Check Permissions at Controller Level
```java
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VIEW_CLINICAL_RECORD')")
public ResponseEntity<?> getProcedureMaterials(...) {
    // ...
}
```

### 2. Double-Check at Service Level (Defense in Depth)
```java
public ProcedureMaterialsResponse getProcedureMaterials(Integer procedureId) {
    // Check if user has permission to view this specific procedure
    if (!canUserViewProcedure(procedureId)) {
        throw new AccessDeniedException("Cannot view this procedure");
    }
    // ...
}
```

### 3. Filter Sensitive Data Based on Permissions
```java
// ALWAYS check before returning cost data
if (!hasViewCostPermission) {
    response.setUnitPrice(null);
    response.setTotalCost(null);
}
```

### 4. Log Permission Checks for Audit
```java
log.info("User {} attempted to view materials for procedure {} - Permission: {}", 
    username, procedureId, hasPermission);
```

---

## 📚 Next Steps

- ➡️ Đọc `05_SAMPLE_SCENARIOS.md` - Các tình huống thực tế
- ➡️ Đọc `PROCEDURE_MATERIAL_CONSUMPTION_API_GUIDE.md` - Full API spec
