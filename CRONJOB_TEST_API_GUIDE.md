# Cron Job Test API Guide for Frontend Developers

## Overview
This guide provides instructions for testing the automated schedule sync cron jobs through manual trigger endpoints.

**⚠️ IMPORTANT**: These endpoints are for **TESTING/DEVELOPMENT ONLY** and require **ADMIN role**.

---

## Authentication

### Step 1: Login as Admin
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "roles": ["ROLE_ADMIN"],
  "employmentType": "FULL_TIME"
}
```

**Save the token** from response for subsequent API calls.

---

## Available Test Endpoints

### Base URL
```
http://localhost:8080/api/v1/admin/test/scheduled-jobs
```

### Authentication Header
All requests require the Bearer token:
```
Authorization: Bearer {token}
```

---

## 1. List Available Endpoints

**Purpose**: Get documentation of all available test endpoints

```http
GET http://localhost:8080/api/v1/admin/test/scheduled-jobs/list
Authorization: Bearer {token}
```

**Response Example:**
```json
{
  "title": "Scheduled Jobs Test Endpoints",
  "warning": "⚠️ These endpoints are for TESTING/DEVELOPMENT only",
  "security": "Only accessible by ADMIN role",
  "endpoints": {
    "GET /api/v1/admin/test/scheduled-jobs/trigger-sync": {
      "job": "Job P8: UnifiedScheduleSyncJob",
      "schedule": "Daily at 00:01 AM",
      "description": "Sync Fixed & Flex registrations to employee_shifts for next 14 days"
    },
    "GET /api/v1/admin/test/scheduled-jobs/trigger-cleanup-flex": {
      "job": "Job P11: CleanupExpiredFlexRegistrationsJob",
      "schedule": "Daily at 00:15 AM",
      "description": "Deactivate expired part-time flex registrations"
    },
    "GET /api/v1/admin/test/scheduled-jobs/trigger-cleanup-inactive": {
      "job": "Job P3: CleanupInactiveEmployeeRegistrationsJob",
      "schedule": "Daily at 00:20 AM",
      "description": "Cleanup registrations for inactive employees"
    },
    "GET /api/v1/admin/test/scheduled-jobs/trigger-all": {
      "job": "ALL Main Jobs (P8 → P11 → P3)",
      "schedule": "Sequential execution",
      "description": "Run all critical jobs in correct order"
    }
  }
}
```

---

## 2. Trigger Schedule Sync (Job P8) ⭐ MOST IMPORTANT

**Purpose**: Sync employee schedules for the next 14 days from Fixed & Flex registrations

**When to use**: 
- Test if shifts are created correctly after adding/updating registrations
- Verify schedule sync logic after changes

```http
GET http://localhost:8080/api/v1/admin/test/scheduled-jobs/trigger-sync
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Job P8 (UnifiedScheduleSyncJob) executed successfully",
  "jobName": "UnifiedScheduleSyncJob",
  "normalSchedule": "Daily at 00:01 AM",
  "executionTimeMs": 662,
  "action": "Synced schedules for next 14 days from Fixed & Flex registrations"
}
```

**Error Response (500):**
```json
{
  "success": false,
  "message": "Job execution failed: {error details}",
  "jobName": "UnifiedScheduleSyncJob",
  "error": "{detailed error message}"
}
```

**What this job does:**
- ✅ Creates employee shifts for next 14 days based on Fixed registrations (Mon-Sun pattern)
- ✅ Creates employee shifts for Part-Time Flex registrations (claimed slots)
- ✅ Only creates shifts for ACTIVE employees (is_active = true)
- ✅ Skips if shift already exists (idempotent)
- ✅ Sets source = 'BATCH_JOB', status = 'SCHEDULED'

**Verification SQL:**
```sql
-- Check total shifts created
SELECT COUNT(*) as total_shifts, 
       MIN(work_date) as from_date, 
       MAX(work_date) as to_date 
FROM employee_shifts 
WHERE work_date >= CURRENT_DATE 
  AND work_date <= CURRENT_DATE + 14 
  AND source = 'BATCH_JOB';

-- Check today's shifts
SELECT es.employee_id, e.employee_code, e.first_name, e.last_name, 
       es.work_date, es.work_shift_id, es.source
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
WHERE es.work_date = CURRENT_DATE 
  AND es.source = 'BATCH_JOB'
ORDER BY e.employee_code;
```

---

## 3. Trigger Cleanup Expired Flex (Job P11)

**Purpose**: Deactivate expired part-time flex registrations (effective_to < today)

**When to use**:
- Test if expired flex registrations are properly deactivated
- Verify slots become available after registration expires

```http
GET http://localhost:8080/api/v1/admin/test/scheduled-jobs/trigger-cleanup-flex
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Job P11 (CleanupExpiredFlexRegistrationsJob) executed successfully",
  "jobName": "CleanupExpiredFlexRegistrationsJob",
  "normalSchedule": "Daily at 00:15 AM",
  "executionTimeMs": 45,
  "action": "Deactivated expired part-time flex registrations"
}
```

**What this job does:**
- ✅ Finds all part_time_registrations with effective_to < CURRENT_DATE
- ✅ Sets is_active = false for expired registrations
- ✅ Logs count of deactivated registrations

**Verification SQL:**
```sql
-- Check for expired but still active registrations (should be 0 after job runs)
SELECT COUNT(*) as expired_but_active
FROM part_time_registrations
WHERE effective_to < CURRENT_DATE 
  AND is_active = true;
```

---

## 4. Trigger Cleanup Inactive Employees (Job P3)

**Purpose**: Cleanup registrations for inactive employees (is_active = false)

**When to use**:
- Test if deactivating an employee properly cleans up their registrations
- Verify inactive employees don't appear in schedules

```http
GET http://localhost:8080/api/v1/admin/test/scheduled-jobs/trigger-cleanup-inactive
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Job P3 (CleanupInactiveEmployeeRegistrationsJob) executed successfully",
  "jobName": "CleanupInactiveEmployeeRegistrationsJob",
  "normalSchedule": "Daily at 00:20 AM",
  "executionTimeMs": 82,
  "action": "Cleaned up registrations for inactive employees"
}
```

**What this job does:**
- ✅ Finds all employees with is_active = false
- ✅ Deactivates their Fixed registrations (is_active = false)
- ✅ Deactivates their Part-Time Flex registrations (is_active = false)
- ✅ Deletes their future scheduled shifts (work_date >= today)

**Verification SQL:**
```sql
-- Check for inactive employees with active registrations (should be 0 after job runs)
SELECT e.employee_code, e.first_name, e.last_name,
       COUNT(DISTINCT fsr.registration_id) as active_fixed_regs,
       COUNT(DISTINCT ptr.id) as active_flex_regs
FROM employees e
LEFT JOIN fixed_shift_registrations fsr ON e.employee_id = fsr.employee_id AND fsr.is_active = true
LEFT JOIN part_time_registrations ptr ON e.employee_id = ptr.employee_id AND ptr.is_active = true
WHERE e.is_active = false
  AND (fsr.registration_id IS NOT NULL OR ptr.id IS NOT NULL)
GROUP BY e.employee_code, e.first_name, e.last_name;
```

---

## 5. Trigger All Jobs (Sequential Execution) ⚡

**Purpose**: Run all 3 main jobs in the correct order (P8 → P11 → P3)

**When to use**:
- Comprehensive test after major changes
- End-to-end verification of schedule sync + cleanup logic

```http
GET http://localhost:8080/api/v1/admin/test/scheduled-jobs/trigger-all
Authorization: Bearer {token}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "All main scheduled jobs executed successfully",
  "executionOrder": "P8 → P11 → P3",
  "totalExecutionTimeMs": 789,
  "results": {
    "job_P8_UnifiedScheduleSync": {
      "status": "success",
      "executionTimeMs": 662
    },
    "job_P11_CleanupExpiredFlex": {
      "status": "success",
      "executionTimeMs": 45
    },
    "job_P3_CleanupInactiveEmployees": {
      "status": "success",
      "executionTimeMs": 82
    }
  }
}
```

**Execution Order:**
1. **Job P8** (00:01 AM in production): Sync schedules
2. **Job P11** (00:15 AM in production): Cleanup expired flex
3. **Job P3** (00:20 AM in production): Cleanup inactive employees

---

## Testing Scenarios

### Scenario 1: Test Schedule Sync for New Registration
```bash
# Step 1: Create a new Fixed registration for an employee
POST /api/v1/fixed-registrations
# (with days: Mon-Fri, shift_id: MORNING)

# Step 2: Trigger schedule sync
GET /api/v1/admin/test/scheduled-jobs/trigger-sync

# Step 3: Verify shifts created
# Check employee_shifts table for next 14 days
```

### Scenario 2: Test Expired Flex Registration Cleanup
```bash
# Step 1: Check current date
GET /api/v1/system/date  # Let's say today is 2025-11-11

# Step 2: Create a part-time registration with effective_to = 2025-11-10 (yesterday)
POST /api/v1/part-time-registrations
# (with effective_to: "2025-11-10")

# Step 3: Trigger cleanup
GET /api/v1/admin/test/scheduled-jobs/trigger-cleanup-flex

# Step 4: Verify registration is now is_active = false
GET /api/v1/part-time-registrations/{id}
```

### Scenario 3: Test Inactive Employee Cleanup
```bash
# Step 1: Deactivate an employee
PATCH /api/v1/employees/{employeeId}
# Body: { "isActive": false }

# Step 2: Trigger cleanup job
GET /api/v1/admin/test/scheduled-jobs/trigger-cleanup-inactive

# Step 3: Verify:
# - Fixed registrations are is_active = false
# - Part-time registrations are is_active = false
# - Future employee_shifts are deleted
```

### Scenario 4: End-to-End Test
```bash
# Run all jobs in sequence
GET /api/v1/admin/test/scheduled-jobs/trigger-all

# Verify:
# - All schedules synced correctly
# - Expired flex registrations deactivated
# - Inactive employee registrations cleaned up
```

---

## Error Handling

### 401 Unauthorized
**Cause**: Token is missing or invalid
**Solution**: Login again to get a new token

### 403 Forbidden
**Cause**: User does not have ADMIN role
**Solution**: Use an account with ROLE_ADMIN

### 500 Internal Server Error
**Cause**: Job execution failed (database error, logic error, etc.)
**Solution**: 
1. Check application logs for detailed error
2. Verify database connection
3. Check data integrity (foreign keys, constraints)

---

## Postman Collection Example

```json
{
  "info": {
    "name": "Cron Job Test APIs",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Login as Admin",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"admin\",\n  \"password\": \"123456\"\n}"
        },
        "url": "{{baseUrl}}/api/v1/auth/login"
      }
    },
    {
      "name": "2. List Available Jobs",
      "request": {
        "method": "GET",
        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
        "url": "{{baseUrl}}/api/v1/admin/test/scheduled-jobs/list"
      }
    },
    {
      "name": "3. Trigger Schedule Sync (Job P8)",
      "request": {
        "method": "GET",
        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
        "url": "{{baseUrl}}/api/v1/admin/test/scheduled-jobs/trigger-sync"
      }
    },
    {
      "name": "4. Trigger Cleanup Expired Flex (Job P11)",
      "request": {
        "method": "GET",
        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
        "url": "{{baseUrl}}/api/v1/admin/test/scheduled-jobs/trigger-cleanup-flex"
      }
    },
    {
      "name": "5. Trigger Cleanup Inactive (Job P3)",
      "request": {
        "method": "GET",
        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
        "url": "{{baseUrl}}/api/v1/admin/test/scheduled-jobs/trigger-cleanup-inactive"
      }
    },
    {
      "name": "6. Trigger All Jobs",
      "request": {
        "method": "GET",
        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
        "url": "{{baseUrl}}/api/v1/admin/test/scheduled-jobs/trigger-all"
      }
    }
  ],
  "variable": [
    {"key": "baseUrl", "value": "http://localhost:8080"},
    {"key": "token", "value": ""}
  ]
}
```

---

## Production Behavior

**⚠️ IMPORTANT**: In production, these jobs run automatically:

| Job | Time | Cron Expression | Purpose |
|-----|------|-----------------|---------|
| Job P8: UnifiedScheduleSyncJob | 00:01 AM | `0 1 0 * * ?` | Sync schedules for next 14 days |
| Job P11: CleanupExpiredFlexRegistrationsJob | 00:15 AM | `0 15 0 * * ?` | Deactivate expired flex registrations |
| Job P3: CleanupInactiveEmployeeRegistrationsJob | 00:20 AM | `0 20 0 * * ?` | Cleanup inactive employee registrations |

**Timezone**: Asia/Ho_Chi_Minh (UTC+7)

**Self-Healing**: Job P8 runs daily, so any manual schedule changes by admins are automatically corrected within 24 hours.

---

## FAQs

### Q: Can I call these endpoints from the frontend?
**A**: Yes, but only for testing/development. Use the admin token in the Authorization header.

### Q: Will calling these endpoints affect production data?
**A**: Yes! These endpoints execute the actual cron job logic. Only use in development/staging environments.

### Q: How often should I test these APIs?
**A**: 
- After creating/updating Fixed registrations
- After creating/updating Part-Time Flex registrations
- After deactivating employees
- Before deploying changes to schedule-related features

### Q: What if the job takes longer than expected?
**A**: Check `executionTimeMs` in the response. Normal execution:
- Job P8: 500-1000ms (depends on number of employees)
- Job P11: 20-100ms
- Job P3: 50-150ms

If execution time is > 5 seconds, check database performance.

### Q: Can I test these on the staging environment?
**A**: Yes, but use a staging database. Do not test on production!

---

## Support

For issues or questions, contact:
- Backend Team: [Your contact info]
- Documentation: See `CRON_JOB_P8_ARCHITECTURE.md` for detailed logic

---

**Last Updated**: November 11, 2025
**API Version**: v1
**Spring Boot Version**: 3.2.10
