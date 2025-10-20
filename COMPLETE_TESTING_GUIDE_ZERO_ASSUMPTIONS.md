# 🔧 Complete Step-by-Step Testing Guide - BE-304 Overtime Request Management

**NO ASSUMPTIONS - Everything from Scratch**

---

## 📋 Part 1: Verify Database Setup

### Step 1.1: Check if Permissions Exist

1. **Open your database client** (pgAdmin, DBeaver, or command line)
2. **Connect to your database**
3. **Run this query**:

```sql
SELECT permission_id, permission_name, module, description 
FROM permissions 
WHERE module = 'OVERTIME'
ORDER BY permission_name;
```

**Expected Result**: Should return 7 rows:
```
permission_id       | permission_name      | module   | description
--------------------|----------------------|----------|----------------------------------
APPROVE_OT          | APPROVE_OT           | OVERTIME | Phê duyệt yêu cầu tăng ca
CANCEL_OT_OWN       | CANCEL_OT_OWN        | OVERTIME | Hủy yêu cầu tăng ca của bản thân
CANCEL_OT_PENDING   | CANCEL_OT_PENDING    | OVERTIME | Hủy yêu cầu tăng ca đang chờ duyệt
CREATE_OT           | CREATE_OT            | OVERTIME | Tạo yêu cầu tăng ca mới
REJECT_OT           | REJECT_OT            | OVERTIME | Từ chối yêu cầu tăng ca
VIEW_OT_ALL         | VIEW_OT_ALL          | OVERTIME | Xem tất cả yêu cầu tăng ca
VIEW_OT_OWN         | VIEW_OT_OWN          | OVERTIME | Xem yêu cầu tăng ca của bản thân
```

**❌ If you get 0 rows (permissions don't exist):**
- Your seed data hasn't been loaded
- **STOP HERE** and do Part 2 first (Database Setup from Scratch)

**✅ If you get 7 rows:**
- Permissions are loaded correctly
- Continue to Step 1.2

---

### Step 1.2: Check if ROLE_MANAGER Exists

**Run this query**:
```sql
SELECT role_id, role_name, description, is_active 
FROM roles 
WHERE role_id = 'ROLE_MANAGER';
```

**Expected Result**: Should return 1 row:
```
role_id      | role_name    | description                        | is_active
-------------|--------------|------------------------------------|-----------
ROLE_MANAGER | ROLE_MANAGER | Quản lý - Phê duyệt và giám sát   | true
```

**❌ If you get 0 rows:**
- ROLE_MANAGER doesn't exist
- **STOP HERE** and do Part 2 first

**✅ If you get 1 row:**
- ROLE_MANAGER exists
- Continue to Step 1.3

---

### Step 1.3: Check Role-Permission Assignments

**Run this query**:
```sql
SELECT rp.role_id, p.permission_name
FROM role_permissions rp
JOIN permissions p ON rp.permission_id = p.permission_id
WHERE p.module = 'OVERTIME'
ORDER BY rp.role_id, p.permission_name;
```

**Expected Result**: Should show permissions assigned to roles:
```
role_id             | permission_name
--------------------|------------------
ROLE_ACCOUNTANT     | CANCEL_OT_OWN
ROLE_ACCOUNTANT     | CREATE_OT
ROLE_ACCOUNTANT     | VIEW_OT_OWN
ROLE_ADMIN          | APPROVE_OT
ROLE_ADMIN          | CANCEL_OT_OWN
ROLE_ADMIN          | CANCEL_OT_PENDING
ROLE_ADMIN          | CREATE_OT
ROLE_ADMIN          | REJECT_OT
ROLE_ADMIN          | VIEW_OT_ALL
ROLE_ADMIN          | VIEW_OT_OWN
ROLE_DOCTOR         | CANCEL_OT_OWN
ROLE_DOCTOR         | CREATE_OT
ROLE_DOCTOR         | VIEW_OT_OWN
... (continues for all roles)
```

**❌ If you get 0 rows or incomplete results:**
- Permissions not assigned to roles
- **STOP HERE** and do Part 2 first

**✅ If you see assignments:**
- Database is properly configured
- Continue to Step 1.4

---

### Step 1.4: Check if overtime_requests Table Exists

**Run this query**:
```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name = 'overtime_requests';
```

**Expected Result**: Should return 1 row:
```
table_name
------------------
overtime_requests
```

**❌ If you get 0 rows:**
- Table doesn't exist
- Application needs to run to create it (JPA auto-creates it)
- **Make sure application has started at least once**
- **Restart application** and come back to this step

**✅ If table exists:**
- Database structure is ready
- Continue to Step 1.5

---

### Step 1.5: Check Available Employees

**Run this query**:
```sql
SELECT employee_id, first_name, last_name, employee_code, is_active
FROM employees
WHERE is_active = true
ORDER BY employee_id
LIMIT 10;
```

**Expected Result**: Should show active employees:
```
employee_id | first_name | last_name | employee_code | is_active
------------|------------|-----------|---------------|----------
1           | Admin      | User      | EMP001        | true
2           | Minh       | Nguyen    | EMP002        | true
5           | John       | Doe       | EMP005        | true
... (more rows)
```

**Write down one employee_id** (e.g., 5) - **you'll need this for testing**

**❌ If you get 0 rows:**
- No employees in database
- **You need to add test employees first** or check seed data

---

### Step 1.6: Check Available Work Shifts

**Run this query**:
```sql
SELECT work_shift_id, shift_name, start_time, end_time, is_active
FROM work_shifts
WHERE is_active = true
ORDER BY work_shift_id;
```

**Expected Result**: Should show work shifts:
```
work_shift_id  | shift_name    | start_time | end_time | is_active
---------------|---------------|------------|----------|----------
WKS_MORNING_01 | Ca sáng       | 08:00:00   | 12:00:00 | true
WKS_NIGHT_01   | Ca tối        | 18:00:00   | 22:00:00 | true
... (more rows)
```

**Write down one work_shift_id** (e.g., WKS_NIGHT_01) - **you'll need this for testing**

**❌ If you get 0 rows:**
- No work shifts in database
- **You need to add work shifts first** or check seed data

---

### Step 1.7: Check Admin User Exists

**Run this query**:
```sql
SELECT account_id, username, email, is_active
FROM accounts
WHERE username = 'admin'
AND is_active = true;
```

**Expected Result**: Should return admin account:
```
account_id | username | email           | is_active
-----------|----------|-----------------|----------
1          | admin    | admin@email.com | true
```

**Write down the username**: `admin`

**❌ If admin doesn't exist:**
- Check what users exist:
```sql
SELECT account_id, username, email, is_active
FROM accounts
WHERE is_active = true
LIMIT 5;
```
- **Write down any valid username** to use for testing

---

## 📋 Part 2: Database Setup from Scratch (If Part 1 Failed)

### Step 2.1: Stop the Application

1. Go to VS Code
2. Find the terminal running the application
3. Press `Ctrl + C` to stop it
4. Wait for it to fully stop

---

### Step 2.2: Backup Database (Safety First)

**Run this in your database client**:
```sql
-- Create a backup schema
CREATE SCHEMA IF NOT EXISTS backup_20251021;

-- Backup permissions table
CREATE TABLE backup_20251021.permissions AS 
SELECT * FROM permissions;

-- Backup roles table
CREATE TABLE backup_20251021.roles AS 
SELECT * FROM roles;

-- Backup role_permissions table
CREATE TABLE backup_20251021.role_permissions AS 
SELECT * FROM role_permissions;
```

**✅ Backup created successfully**

---

### Step 2.3: Check Seed Data File

1. **Open file**: `src/main/resources/db/dental-clinic-seed-data.sql`
2. **Search for** (Ctrl+F): `OVERTIME`
3. **Verify you see**:
   - Lines with `'VIEW_OT_ALL'`
   - Lines with `'VIEW_OT_OWN'`
   - Lines with `'CREATE_OT'`
   - Lines with `'APPROVE_OT'`
   - Lines with `'REJECT_OT'`
   - Lines with `'CANCEL_OT_OWN'`
   - Lines with `'CANCEL_OT_PENDING'`

4. **Search for**: `ROLE_MANAGER`
5. **Verify you see a line** like:
   ```sql
   ('ROLE_MANAGER', 'ROLE_MANAGER', 'Quản lý - Phê duyệt và giám sát', FALSE, TRUE, NOW()),
   ```

**❌ If you DON'T see these:**
- The seed data file wasn't updated correctly
- **Contact me** - the file needs to be fixed

**✅ If you see them:**
- Seed data file is correct
- Continue to Step 2.4

---

### Step 2.4: Check Application Properties

1. **Open file**: `src/main/resources/application.yaml`
2. **Find the line** that says `ddl-auto:`
3. **Check the value**:
   - If it says `validate` → Database won't auto-create tables
   - If it says `update` → Database will auto-create/update tables
   - If it says `create-drop` → Database will be recreated on startup

**For development, you want**: `update`

**If it's not `update`, change it to**:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

4. **Find the line** about SQL initialization:
   ```yaml
   spring:
     sql:
       init:
         mode: always  # or embedded
   ```

**Make sure it says**: `always` or `embedded`

---

### Step 2.5: Manually Insert Permissions (If Needed)

**If seed data isn't loading automatically**, manually insert:

```sql
-- Insert overtime permissions
INSERT INTO permissions (permission_id, permission_name, module, description, created_at)
VALUES
('VIEW_OT_ALL', 'VIEW_OT_ALL', 'OVERTIME', 'Xem tất cả yêu cầu tăng ca', NOW()),
('VIEW_OT_OWN', 'VIEW_OT_OWN', 'OVERTIME', 'Xem yêu cầu tăng ca của bản thân', NOW()),
('CREATE_OT', 'CREATE_OT', 'OVERTIME', 'Tạo yêu cầu tăng ca mới', NOW()),
('APPROVE_OT', 'APPROVE_OT', 'OVERTIME', 'Phê duyệt yêu cầu tăng ca', NOW()),
('REJECT_OT', 'REJECT_OT', 'OVERTIME', 'Từ chối yêu cầu tăng ca', NOW()),
('CANCEL_OT_OWN', 'CANCEL_OT_OWN', 'OVERTIME', 'Hủy yêu cầu tăng ca của bản thân', NOW()),
('CANCEL_OT_PENDING', 'CANCEL_OT_PENDING', 'OVERTIME', 'Hủy yêu cầu tăng ca đang chờ duyệt', NOW())
ON CONFLICT (permission_id) DO NOTHING;
```

**Run the query** and verify:
```sql
SELECT COUNT(*) FROM permissions WHERE module = 'OVERTIME';
```

**Should return**: 7

---

### Step 2.6: Manually Insert ROLE_MANAGER (If Needed)

```sql
-- Insert ROLE_MANAGER
INSERT INTO roles (role_id, role_name, description, requires_specialization, is_active, created_at)
VALUES ('ROLE_MANAGER', 'ROLE_MANAGER', 'Quản lý - Phê duyệt và giám sát', FALSE, TRUE, NOW())
ON CONFLICT (role_id) DO NOTHING;
```

**Verify**:
```sql
SELECT * FROM roles WHERE role_id = 'ROLE_MANAGER';
```

**Should return**: 1 row

---

### Step 2.7: Manually Assign Permissions to Roles (If Needed)

```sql
-- Assign all overtime permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_ADMIN', permission_id 
FROM permissions 
WHERE module = 'OVERTIME'
ON CONFLICT DO NOTHING;

-- Assign all overtime permissions to ROLE_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_MANAGER', permission_id 
FROM permissions 
WHERE module = 'OVERTIME'
ON CONFLICT DO NOTHING;

-- Assign employee permissions to ROLE_DOCTOR
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_OT_OWN'),
('ROLE_DOCTOR', 'CREATE_OT'),
('ROLE_DOCTOR', 'CANCEL_OT_OWN')
ON CONFLICT DO NOTHING;

-- Assign employee permissions to ROLE_NURSE
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_NURSE', 'VIEW_OT_OWN'),
('ROLE_NURSE', 'CREATE_OT'),
('ROLE_NURSE', 'CANCEL_OT_OWN')
ON CONFLICT DO NOTHING;

-- Assign employee permissions to ROLE_RECEPTIONIST
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_RECEPTIONIST', 'VIEW_OT_OWN'),
('ROLE_RECEPTIONIST', 'CREATE_OT'),
('ROLE_RECEPTIONIST', 'CANCEL_OT_OWN')
ON CONFLICT DO NOTHING;

-- Assign employee permissions to ROLE_ACCOUNTANT
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_ACCOUNTANT', 'VIEW_OT_OWN'),
('ROLE_ACCOUNTANT', 'CREATE_OT'),
('ROLE_ACCOUNTANT', 'CANCEL_OT_OWN')
ON CONFLICT DO NOTHING;

-- Assign employee permissions to ROLE_INVENTORY_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_INVENTORY_MANAGER', 'VIEW_OT_OWN'),
('ROLE_INVENTORY_MANAGER', 'CREATE_OT'),
('ROLE_INVENTORY_MANAGER', 'CANCEL_OT_OWN')
ON CONFLICT DO NOTHING;
```

**Verify**:
```sql
SELECT rp.role_id, COUNT(*) as permission_count
FROM role_permissions rp
JOIN permissions p ON rp.permission_id = p.permission_id
WHERE p.module = 'OVERTIME'
GROUP BY rp.role_id
ORDER BY rp.role_id;
```

**Expected**:
```
role_id              | permission_count
---------------------|------------------
ROLE_ACCOUNTANT      | 3
ROLE_ADMIN           | 7
ROLE_DOCTOR          | 3
ROLE_INVENTORY_...   | 3
ROLE_MANAGER         | 7
ROLE_NURSE           | 3
ROLE_RECEPTIONIST    | 3
```

---

### Step 2.8: Restart Application

1. In VS Code, go to the terminal
2. Run:
```powershell
mvn spring-boot:run
```

3. **Wait for this message**:
```
Started DentalClinicManagementApplication in X.XXX seconds
```

4. **Check for errors** in the console
5. **If you see errors**, copy them and let me know

**✅ Application started successfully**

---

## 📋 Part 3: Test Authentication

### Step 3.1: Find Your Application Port

1. **Look at the console** output when application started
2. **Find a line** like:
```
Tomcat started on port(s): 8080 (http)
```

3. **Write down the port number**: `____` (usually 8080)

**Your base URL is**: `http://localhost:8080`

---

### Step 3.2: Open Insomnia/Postman

**If you have Insomnia**:
1. Open Insomnia
2. Click "New Request"
3. Continue to Step 3.3

**If you have Postman**:
1. Open Postman
2. Click "New" → "HTTP Request"
3. Continue to Step 3.3

**If you have NEITHER**:
- Download Insomnia: https://insomnia.rest/download
- OR use Postman: https://www.postman.com/downloads/
- Install and open it
- Then continue to Step 3.3

---

### Step 3.3: Create Login Request

1. **Set method to**: `POST`
2. **Set URL to**: `http://localhost:8080/api/v1/auth/login`
3. **Click on "Body" tab**
4. **Select**: `JSON`
5. **Paste this**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

6. **Click "Send"**

---

### Step 3.4: Check Login Response

**Look at the response**:

**✅ SUCCESS (Status 200)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYzMjE...",
  "tokenExpiresAt": 1729468800,
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["ROLE_ADMIN"],
  "permissions": ["VIEW_OT_ALL", "VIEW_OT_OWN", "CREATE_OT", ...]
}
```

**Action**: 
- **Copy the entire token value** (the long string starting with `eyJ...`)
- **Paste it into a text file** or notepad for later use
- **Check permissions array** - should include overtime permissions like `VIEW_OT_ALL`, `CREATE_OT`, etc.

---

**❌ ERROR (Status 401 Unauthorized)**:
```json
{
  "success": false,
  "message": "Bad credentials"
}
```

**This means**: Username or password is wrong

**Action**:
1. **Go to database**
2. **Run**:
```sql
SELECT username FROM accounts WHERE is_active = true LIMIT 5;
```
3. **Try one of those usernames** with password `123456`
4. **Or ask someone** for correct admin credentials

---

**❌ ERROR (Status 404 Not Found)**:

**This means**: Login endpoint doesn't exist or URL is wrong

**Check**:
1. Is application running?
2. Is the port number correct?
3. Is the URL exactly: `http://localhost:8080/api/v1/auth/login`?

---

**❌ ERROR (Connection refused/timeout)**:

**This means**: Application is not running

**Action**:
1. Go back to VS Code
2. Check if application is still running
3. Restart if needed: `mvn spring-boot:run`

---

## 📋 Part 4: Test Create Overtime Request

### Step 4.1: Create New Request

1. **In Insomnia/Postman**, create a NEW request
2. **Set method to**: `POST`
3. **Set URL to**: `http://localhost:8080/api/v1/overtime-requests`

---

### Step 4.2: Add Authorization Header

1. **Click on "Headers" tab**
2. **Add a new header**:
   - **Key**: `Authorization`
   - **Value**: `Bearer ` + (paste your token here)
   
**Example**:
```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYzMjE...
```

**IMPORTANT**: There MUST be a space after "Bearer"

3. **Add another header**:
   - **Key**: `Content-Type`
   - **Value**: `application/json`

---

### Step 4.3: Prepare Request Body

**You need 3 pieces of information**:
1. ✅ **employeeId** - from Step 1.5 (e.g., 5)
2. ✅ **workShiftId** - from Step 1.6 (e.g., "WKS_NIGHT_01")
3. ✅ **workDate** - a date in the future (e.g., "2025-12-01")

**If you don't have these**, go back to:
- Step 1.5 to get employeeId
- Step 1.6 to get workShiftId

---

### Step 4.4: Create Request Body

1. **Click on "Body" tab**
2. **Select**: `JSON`
3. **Paste this** (replace with YOUR values):
```json
{
  "employeeId": 5,
  "workDate": "2025-12-01",
  "workShiftId": "WKS_NIGHT_01",
  "reason": "Cần tăng ca để hoàn thành dự án khẩn cấp"
}
```

**Replace**:
- `5` with your actual employeeId
- `"WKS_NIGHT_01"` with your actual workShiftId
- `"2025-12-01"` with a future date

4. **Click "Send"**

---

### Step 4.5: Check Create Response

**✅ SUCCESS (Status 201 Created)**:
```json
{
  "success": true,
  "message": "Overtime request created successfully",
  "data": {
    "requestId": "OTR251201001",
    "employeeId": 5,
    "employeeCode": "EMP005",
    "employeeName": "John Doe",
    "workDate": "2025-12-01",
    "workShiftId": "WKS_NIGHT_01",
    "shiftName": "Ca tối",
    "status": "PENDING",
    "reason": "Cần tăng ca để hoàn thành dự án khẩn cấp",
    "requestedById": 1,
    "requestedByName": "Admin User",
    "createdAt": "2025-10-21T10:30:00"
  }
}
```

**Action**:
- ✅ **FEATURE WORKS!** 🎉
- **Copy the requestId** (e.g., "OTR251201001")
- **Save it** - you'll need it for next tests

---

**❌ ERROR (Status 400 Bad Request)**:
```json
{
  "success": false,
  "message": "Work date cannot be in the past"
}
```

**This means**: Your work date is in the past

**Action**: Change `workDate` to a future date like "2025-12-15"

---

**❌ ERROR (Status 404 Not Found)**:
```json
{
  "success": false,
  "message": "Employee with ID 5 not found"
}
```

**This means**: Employee doesn't exist

**Action**:
1. Go to database
2. Run:
```sql
SELECT employee_id FROM employees WHERE is_active = true LIMIT 5;
```
3. Use one of those employee IDs

---

**❌ ERROR (Status 404 Not Found)**:
```json
{
  "success": false,
  "message": "Work shift with ID WKS_NIGHT_01 not found"
}
```

**This means**: Work shift doesn't exist

**Action**:
1. Go to database
2. Run:
```sql
SELECT work_shift_id FROM work_shifts WHERE is_active = true LIMIT 5;
```
3. Use one of those work shift IDs

---

**❌ ERROR (Status 409 Conflict)**:
```json
{
  "success": false,
  "message": "An overtime request already exists for this employee, date, and shift"
}
```

**This means**: You already created this exact request

**Action**: 
- This is CORRECT behavior! ✅
- Change the date, employee, or shift to create a different request

---

**❌ ERROR (Status 401 Unauthorized)**:

**This means**: Token is missing or invalid

**Action**:
1. Check Authorization header exists
2. Check format: `Bearer <token>`
3. Check there's a space after "Bearer"
4. Token might be expired - login again to get a new one

---

**❌ ERROR (Status 403 Forbidden)**:

**This means**: User doesn't have CREATE_OT permission

**Action**:
1. Go to database
2. Check permissions:
```sql
SELECT p.permission_name
FROM role_permissions rp
JOIN permissions p ON rp.permission_id = p.permission_id
JOIN account_roles ar ON ar.role_id = rp.role_id
JOIN accounts a ON a.account_id = ar.account_id
WHERE a.username = 'admin'
AND p.module = 'OVERTIME';
```
3. Should see `CREATE_OT` in the list
4. If not, permissions weren't assigned - go back to Part 2

---

## 📋 Part 5: Test List Overtime Requests

### Step 5.1: Create List Request

1. **Create NEW request**
2. **Set method to**: `GET`
3. **Set URL to**: `http://localhost:8080/api/v1/overtime-requests?page=0&size=10`
4. **Add Authorization header** (same as Step 4.2):
   - Key: `Authorization`
   - Value: `Bearer <your_token>`

5. **Click "Send"**

---

### Step 5.2: Check List Response

**✅ SUCCESS (Status 200 OK)**:
```json
{
  "success": true,
  "message": "Overtime requests retrieved successfully",
  "data": {
    "content": [
      {
        "requestId": "OTR251201001",
        "employeeId": 5,
        "employeeCode": "EMP005",
        "employeeName": "John Doe",
        "workDate": "2025-12-01",
        "workShiftId": "WKS_NIGHT_01",
        "shiftName": "Ca tối",
        "status": "PENDING",
        "requestedByName": "Admin User",
        "createdAt": "2025-10-21T10:30:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

**✅ FEATURE WORKS! You can see your created request in the list!** 🎉

---

**❌ ERROR (Status 401)**:
- Token issue - get a new token

**❌ ERROR (Status 403)**:
- Permission issue - user needs VIEW_OT_ALL or VIEW_OT_OWN

---

## 📋 Part 6: Test Get Request Details

### Step 6.1: Create Get Details Request

1. **Create NEW request**
2. **Set method to**: `GET`
3. **Set URL to**: `http://localhost:8080/api/v1/overtime-requests/OTR251201001`
   - Replace `OTR251201001` with YOUR actual requestId from Step 4.5

4. **Add Authorization header**
5. **Click "Send"**

---

### Step 6.2: Check Get Details Response

**✅ SUCCESS (Status 200 OK)**:
```json
{
  "success": true,
  "message": "Overtime request retrieved successfully",
  "data": {
    "requestId": "OTR251201001",
    "employeeId": 5,
    "employeeCode": "EMP005",
    "employeeName": "John Doe",
    "workDate": "2025-12-01",
    "workShiftId": "WKS_NIGHT_01",
    "shiftName": "Ca tối",
    "shiftStartTime": "18:00:00",
    "shiftEndTime": "22:00:00",
    "status": "PENDING",
    "reason": "Cần tăng ca để hoàn thành dự án khẩn cấp",
    "approvalReason": null,
    "requestedById": 1,
    "requestedByName": "Admin User",
    "approvedById": null,
    "approvedByName": null,
    "approvedAt": null,
    "createdAt": "2025-10-21T10:30:00",
    "updatedAt": "2025-10-21T10:30:00"
  }
}
```

**✅ FEATURE WORKS! You can see full details including shift times!** 🎉

---

## 📋 Part 7: Test Approve Request

### Step 7.1: Create Approve Request

1. **Create NEW request**
2. **Set method to**: `PATCH`
3. **Set URL to**: `http://localhost:8080/api/v1/overtime-requests/OTR251201001`
   - Replace with YOUR requestId

4. **Add headers**:
   - Authorization: `Bearer <token>`
   - Content-Type: `application/json`

5. **Set Body** (JSON):
```json
{
  "action": "APPROVE",
  "reason": "Đã xem xét và phê duyệt yêu cầu tăng ca"
}
```

6. **Click "Send"**

---

### Step 7.2: Check Approve Response

**✅ SUCCESS (Status 200 OK)**:
```json
{
  "success": true,
  "message": "Overtime request approved successfully",
  "data": {
    "requestId": "OTR251201001",
    "status": "APPROVED",
    "approvalReason": "Đã xem xét và phê duyệt yêu cầu tăng ca",
    "approvedById": 1,
    "approvedByName": "Admin User",
    "approvedAt": "2025-10-21T10:35:00"
  }
}
```

**✅ FEATURE WORKS! Request status changed to APPROVED!** 🎉

---

## 📋 Part 8: Verify in Database

### Step 8.1: Check Overtime Requests in Database

```sql
SELECT 
    request_id,
    employee_id,
    work_date,
    work_shift_id,
    status,
    reason,
    created_at
FROM overtime_requests
ORDER BY created_at DESC
LIMIT 5;
```

**You should see**:
- Your created request
- Status: APPROVED
- All fields filled correctly

**✅ DATABASE VERIFICATION PASSED!** 🎉

---

## 🎯 Final Checklist

- [ ] Part 1: Database verified (permissions, roles, tables exist)
- [ ] Part 3: Login works, got JWT token
- [ ] Part 4: Created overtime request successfully
- [ ] Part 5: Listed requests successfully
- [ ] Part 6: Got request details successfully
- [ ] Part 7: Approved request successfully
- [ ] Part 8: Verified data in database

**If all checked** ✅ → **FEATURE IS 100% COMPLETE AND WORKING!** 🎉🎊

---

## 📝 Next Steps

1. **Commit your changes**:
```bash
git add .
git commit -m "feat(BE-304): Complete overtime request management system - tested and verified"
git push origin feat/BE-304-manage-overtime-requests
```

2. **Share API documentation** with frontend team:
   - Send them: `OVERTIME_API_DOCUMENTATION.md`

3. **Mark task as DONE** in your project management tool

---

## 🆘 If You Get Stuck

**At any step**, if something doesn't work:

1. **Copy the EXACT error message**
2. **Note which step you're on**
3. **Share with me**:
   - The step number
   - The error message
   - What you tried

I'll help you fix it! 😊

---

**This guide assumes NOTHING and walks you through EVERYTHING step by step!**
