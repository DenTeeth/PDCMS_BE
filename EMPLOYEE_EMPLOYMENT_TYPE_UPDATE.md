# Thêm EmploymentType vào Module Employee - Summary

## 🎯 Mục Đích

Thêm field `employmentType` vào **TẤT CẢ** các DTO request/response trong module Employee để:

- ✅ **Xem** employmentType khi get employee (FULL_TIME hay PART_TIME)
- ✅ **Thêm** employmentType khi tạo employee mới
- ✅ **Sửa** employmentType khi update employee
- ✅ **Hiển thị** employmentType trong danh sách employees

## 📋 Files Đã Thay Đổi

### 1. DTO Response (Đã có sẵn ✅)

**File:** `EmployeeInfoResponse.java`

- ✅ Đã có field `employmentType` từ trước (line 32)
- ✅ Không cần thay đổi gì

```java
private EmploymentType employeeType;
```

---

### 2. DTO Request - CreateEmployeeRequest

**File:** `CreateEmployeeRequest.java`

**Thay đổi:**

- ✅ Import `EmploymentType` enum
- ✅ Import `@NotNull` validation
- ✅ Thêm field `employmentType` với validation `@NotNull`
- ✅ Thêm getter/setter
- ✅ Update `toString()`

**Code thêm vào:**

```java
@NotNull(message = "Employment type is required")
private EmploymentType employmentType;

public EmploymentType getEmploymentType() {
    return employmentType;
}

public void setEmploymentType(EmploymentType employmentType) {
    this.employmentType = employmentType;
}
```

**Request example:**

```json
{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "password123",
  "roleId": "ROLE_DENTIST",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "0123456789",
  "dateOfBirth": "1990-01-01",
  "address": "123 Street",
  "employmentType": "FULL_TIME", // ← REQUIRED
  "specializationIds": [1, 2]
}
```

---

### 3. DTO Request - UpdateEmployeeRequest

**File:** `UpdateEmployeeRequest.java`

**Thay đổi:**

- ✅ Import `EmploymentType` enum
- ✅ Thêm field `employmentType` (optional - vì là partial update)
- ✅ Thêm getter/setter
- ✅ Update `toString()`

**Code thêm vào:**

```java
private EmploymentType employmentType;

public EmploymentType getEmploymentType() {
    return employmentType;
}

public void setEmploymentType(EmploymentType employmentType) {
    this.employmentType = employmentType;
}
```

**Request example:**

```json
{
  "employmentType": "PART_TIME" // ← Optional, chỉ update field này
}
```

---

### 4. DTO Request - ReplaceEmployeeRequest

**File:** `ReplaceEmployeeRequest.java`

**Thay đổi:**

- ✅ Import `EmploymentType` enum
- ✅ Thêm field `employmentType` với validation `@NotNull`
- ✅ Update constructor
- ✅ Thêm getter/setter
- ✅ Update `toString()`

**Code thêm vào:**

```java
@NotNull(message = "Employment type is required")
private EmploymentType employmentType;

public EmploymentType getEmploymentType() {
    return employmentType;
}

public void setEmploymentType(EmploymentType employmentType) {
    this.employmentType = employmentType;
}
```

**Request example (PUT - phải có tất cả fields):**

```json
{
  "roleId": "ROLE_DENTIST",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "0123456789",
  "dateOfBirth": "1990-01-01",
  "address": "123 Street",
  "employmentType": "FULL_TIME", // ← REQUIRED
  "isActive": true,
  "specializationIds": [1, 2]
}
```

---

### 5. Service Layer - EmployeeService

**File:** `EmployeeService.java`

#### 5.1. Method `createEmployee()`

**Thay đổi:**

```java
// Create new employee
Employee employee = new Employee();
employee.setAccount(account);
employee.setFirstName(request.getFirstName());
employee.setLastName(request.getLastName());
employee.setPhone(request.getPhone());
employee.setDateOfBirth(request.getDateOfBirth());
employee.setAddress(request.getAddress());
employee.setEmploymentType(request.getEmploymentType()); // ← THÊM
employee.setIsActive(true);
```

#### 5.2. Method `updateEmployee()`

**Thay đổi:**

```java
if (request.getEmploymentType() != null) {
    employee.setEmploymentType(request.getEmploymentType()); // ← THÊM
}
```

#### 5.3. Method `replaceEmployee()`

**Thay đổi:**

```java
employee.setFirstName(request.getFirstName());
employee.setLastName(request.getLastName());
employee.setPhone(request.getPhone());
employee.setDateOfBirth(request.getDateOfBirth());
employee.setAddress(request.getAddress());
employee.setEmploymentType(request.getEmploymentType()); // ← THÊM
employee.setIsActive(request.getIsActive());
```

---

## 🧪 Test Cases

### Test 1: Create Employee với employmentType

```bash
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "username": "john.doe",
    "email": "john@example.com",
    "password": "password123",
    "roleId": "ROLE_DENTIST",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "0123456789",
    "dateOfBirth": "1990-01-01",
    "employmentType": "FULL_TIME",
    "specializationIds": [1]
  }'
```

**Expected response:**

```json
{
  "employeeId": 1,
  "employeeCode": "EMP001",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "employeeType": "FULL_TIME",  // ← Kiểm tra field này
  "phone": "0123456789",
  ...
}
```

### Test 2: Update employmentType (PATCH)

```bash
curl -X PATCH http://localhost:8080/api/v1/employees/EMP001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "employmentType": "PART_TIME"
  }'
```

**Expected:** Employee chuyển từ FULL_TIME → PART_TIME

### Test 3: Replace Employee (PUT) với employmentType

```bash
curl -X PUT http://localhost:8080/api/v1/employees/EMP001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "roleId": "ROLE_DENTIST",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "0123456789",
    "dateOfBirth": "1990-01-01",
    "address": "123 Street",
    "employmentType": "FULL_TIME",
    "isActive": true,
    "specializationIds": [1]
  }'
```

### Test 4: Get Employee - Kiểm tra employeeType trong response

```bash
curl -X GET http://localhost:8080/api/v1/employees/EMP001 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected response có:**

```json
{
  "employeeType": "FULL_TIME",
  ...
}
```

### Test 5: Get All Employees - Kiểm tra employeeType trong list

```bash
curl -X GET http://localhost:8080/api/v1/employees \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected:** Tất cả employees đều có field `employeeType`

---

## 📊 API Endpoints Summary

### POST `/api/v1/employees` (Create)

- ✅ Request có `employmentType` (REQUIRED)
- ✅ Response có `employeeType`

### GET `/api/v1/employees` (Get All)

- ✅ Response có `employeeType` cho mỗi employee

### GET `/api/v1/employees/{code}` (Get By Code)

- ✅ Response có `employeeType`

### PATCH `/api/v1/employees/{code}` (Partial Update)

- ✅ Request có `employmentType` (OPTIONAL)
- ✅ Response có `employeeType`

### PUT `/api/v1/employees/{code}` (Full Replace)

- ✅ Request có `employmentType` (REQUIRED)
- ✅ Response có `employeeType`

### DELETE `/api/v1/employees/{code}` (Soft Delete)

- No body (chỉ soft delete isActive = false)

---

## 🎨 Frontend Integration

### Hiển thị trong danh sách employees

```typescript
interface Employee {
  employeeId: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  fullName: string;
  employeeType: "FULL_TIME" | "PART_TIME"; // ← Sử dụng field này
  phone: string;
  // ...
}

// Display
const employees: Employee[] = await fetchEmployees();

employees.forEach((emp) => {
  console.log(`${emp.fullName} - ${emp.employeeType}`);
  // Output: "John Doe - FULL_TIME"
});
```

### Form tạo/sửa employee

```tsx
const CreateEmployeeForm = () => {
  const [employmentType, setEmploymentType] = useState<
    "FULL_TIME" | "PART_TIME"
  >("FULL_TIME");

  return (
    <form>
      <label>Employment Type</label>
      <select
        value={employmentType}
        onChange={(e) => setEmploymentType(e.target.value)}
      >
        <option value="FULL_TIME">Full Time</option>
        <option value="PART_TIME">Part Time</option>
      </select>

      {/* Other fields... */}
    </form>
  );
};
```

### Badge hiển thị employment type

```tsx
const EmployeeTypeBadge = ({ type }: { type: "FULL_TIME" | "PART_TIME" }) => {
  const color = type === "FULL_TIME" ? "green" : "blue";
  const label = type === "FULL_TIME" ? "Full Time" : "Part Time";

  return <span className={`badge badge-${color}`}>{label}</span>;
};
```

---

## ✅ Validation Rules

### CreateEmployeeRequest (POST)

- `employmentType`: **REQUIRED** (@NotNull)
- Allowed values: `FULL_TIME`, `PART_TIME`

### UpdateEmployeeRequest (PATCH)

- `employmentType`: **OPTIONAL** (có thể null)
- Nếu null → không update field này
- Nếu có giá trị → update

### ReplaceEmployeeRequest (PUT)

- `employmentType`: **REQUIRED** (@NotNull)
- Vì PUT replace toàn bộ resource

---

## 🔍 Database

### Employee Table

```sql
CREATE TABLE employees (
  employee_id SERIAL PRIMARY KEY,
  employee_code VARCHAR(20),
  account_id INTEGER REFERENCES accounts(account_id),
  first_name VARCHAR(50),
  last_name VARCHAR(50),
  phone VARCHAR(15),
  date_of_birth DATE,
  address VARCHAR(500),
  employment_type VARCHAR(20) NOT NULL,  -- ← Đã có sẵn
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  ...
);
```

**Note:** Database đã có column `employment_type` rồi, nên không cần migration.

---

## 📝 Checklist

### DTOs

- [x] EmployeeInfoResponse - Đã có sẵn
- [x] CreateEmployeeRequest - Thêm employmentType (REQUIRED)
- [x] UpdateEmployeeRequest - Thêm employmentType (OPTIONAL)
- [x] ReplaceEmployeeRequest - Thêm employmentType (REQUIRED)

### Service Layer

- [x] EmployeeService.createEmployee() - Set employmentType
- [x] EmployeeService.updateEmployee() - Update employmentType nếu có
- [x] EmployeeService.replaceEmployee() - Set employmentType

### Validation

- [x] CreateEmployeeRequest - @NotNull employmentType
- [x] ReplaceEmployeeRequest - @NotNull employmentType
- [x] UpdateEmployeeRequest - Optional employmentType

### Testing

- [ ] Test POST /api/v1/employees với employmentType
- [ ] Test PATCH /api/v1/employees/{code} update employmentType
- [ ] Test PUT /api/v1/employees/{code} với employmentType
- [ ] Test GET responses có employeeType field
- [ ] Test validation: POST/PUT without employmentType → 400 error

---

## 🎉 Tóm Tắt

### Trước khi fix:

- ❌ CreateEmployeeRequest không có `employmentType` → Không thể set khi tạo
- ❌ UpdateEmployeeRequest không có `employmentType` → Không thể update
- ❌ ReplaceEmployeeRequest không có `employmentType` → Không thể replace
- ❌ Service không set employmentType vào entity

### Sau khi fix:

- ✅ Tất cả request DTOs đều có `employmentType`
- ✅ Service đúng cách set/update employmentType
- ✅ Response đã có `employeeType` từ trước (EmployeeInfoResponse)
- ✅ Frontend có thể CRUD đầy đủ với employmentType

---

**Build status:** ✅ SUCCESS
**Ready for testing:** ✅ YES
**Breaking changes:** ⚠️ YES - API contracts changed (employmentType now required for POST/PUT)

**Next steps:**

1. Restart application
2. Test tất cả endpoints Employee
3. Update Frontend để sử dụng employmentType field
4. Update API documentation (Swagger)
