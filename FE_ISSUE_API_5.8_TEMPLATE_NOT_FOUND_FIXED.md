# FE Issue Fixed: API 5.8 - Template Not Found (404)

**Date**: November 16, 2025
**Issue Reporter**: FE Team
**Fixed By**: Backend Team
**Severity**: HIGH (Blocking FE development)

---

## 1. Problem Description

### FE Error Report

```
GET /api/v1/treatment-plan-templates/TPL_ORTHO_METAL
Response: 404 Not Found
```

**Expected**: API trả về chi tiết template
**Actual**: 404 Not Found

---

## 2. Root Cause Analysis

### Investigation Steps

#### Step 1: Check if API implementation exists ✅

```bash
# API đã được implement đầy đủ
- TreatmentPlanController.java (Line 747): getTemplateDetail()
- TreatmentPlanTemplateService.java: getTemplateDetail()
- GetTemplateDetailResponse.java: Response DTO
```

#### Step 2: Check database connection ✅

```bash
docker ps | grep postgres
# ✅ PostgreSQL container running
```

#### Step 3: Check if table exists ✅

```sql
SELECT * FROM treatment_plan_templates;
-- ✅ Table exists but 0 rows
```

#### Step 4: Check seed data file ✅

```bash
grep "TPL_ORTHO_METAL" src/main/resources/db/dental-clinic-seed-data.sql
-- ✅ INSERT statements exist
```

#### Step 5: Root cause found! ❌

```sql
-- Lines 1800-2331 in seed data:
/*
INSERT INTO treatment_plan_templates (...)
VALUES ('TPL_ORTHO_METAL', ...);
...
*/
```

**ROOT CAUSE**: Toàn bộ phần INSERT templates bị COMMENT `/* ... */`

**Why?** Comment có message:

```sql
-- TEMPORARILY COMMENTED OUT - TreatmentPlanTemplate entity not yet created
-- Uncomment when the following entities are implemented
```

Entity đã được implement nhưng seed data vẫn bị comment!

---

## 3. Solution Applied

### Fix #1: Add Missing `specialization_id` Column

**Problem**: INSERT statement thiếu `specialization_id` (Foreign Key constraint)

**Fix**: Thêm `specialization_id` vào INSERT

```sql
-- BEFORE (Missing specialization_id)
INSERT INTO treatment_plan_templates (template_code, template_name, description, ...)
VALUES ('TPL_ORTHO_METAL', 'Niềng răng...', ..., true, NOW());

-- AFTER (Added specialization_id)
INSERT INTO treatment_plan_templates (template_code, template_name, description, ..., specialization_id, ...)
VALUES ('TPL_ORTHO_METAL', 'Niềng răng...', ..., 1, true, NOW());
```

**Mapping**:

- `TPL_ORTHO_METAL` → specialization_id = 1 (Chỉnh nha)
- `TPL_IMPLANT_OSSTEM` → specialization_id = 5 (Phẫu thuật hàm mặt)
- `TPL_CROWN_CERCON` → specialization_id = 4 (Phục hồi răng)

---

### Fix #2: Uncomment Template Section

**File**: `src/main/resources/db/dental-clinic-seed-data.sql`

**Changes**:

1. **Removed opening comment** (Line 1806):

```sql
-- BEFORE
/*
-- Treatment Plan Templates...

-- AFTER
-- Treatment Plan Templates...
```

2. **Removed closing comment** (Line 2331):

```sql
-- BEFORE
*/
-- END OF COMMENTED TEMPLATE SECTION

-- AFTER
-- (Empty line)
```

**Result**: Templates section now executes during seed data load

---

## 4. Verification

### Database Check

```sql
-- Check templates
SELECT template_code, template_name, specialization_id
FROM treatment_plan_templates;

-- Result:
   template_code    |                 template_name                  | specialization_id
--------------------+-----------------------------------------------+-------------------
 TPL_ORTHO_METAL    | Niềng răng mắc cài kim loại trọn gói 2 năm   |                 1
 TPL_IMPLANT_OSSTEM | Cấy ghép Implant Hàn Quốc (Osstem) - Trọn gói |                 5
 TPL_CROWN_CERCON   | Bọc răng sứ Cercon HT - 1 răng                |                 4
(3 rows)
```

✅ **3 templates successfully loaded**

---

### Check Template Phases

```sql
SELECT t.template_code, tp.phase_number, tp.phase_name
FROM template_phases tp
JOIN treatment_plan_templates t ON t.template_id = tp.template_id
ORDER BY t.template_code, tp.phase_number;
```

**Expected**: Each template should have multiple phases

---

### Check Template Services

```sql
SELECT COUNT(*) as total_services
FROM template_phase_services;
```

**Expected**: Services linked to phases

---

## 5. API Test

### Test Case: Get TPL_ORTHO_METAL Template

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/treatment-plan-templates/TPL_ORTHO_METAL" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK)**:

```json
{
  "templateId": 1,
  "templateCode": "TPL_ORTHO_METAL",
  "templateName": "Niềng răng mắc cài kim loại trọn gói 2 năm",
  "description": "Gói điều trị chỉnh nha toàn diện...",
  "specialization": {
    "id": 1,
    "name": "Chỉnh nha"
  },
  "estimatedTotalCost": 30000000,
  "estimatedDurationDays": 730,
  "isActive": true,
  "summary": {
    "totalPhases": 4,
    "totalItemsInTemplate": 7
  },
  "phases": [
    {
      "phaseTemplateId": 1,
      "phaseName": "Giai đoạn 1: Khám & Chuẩn bị",
      "stepOrder": 1,
      "itemsInPhase": [
        {
          "serviceCode": "ORTHO_CONSULT",
          "serviceName": "Khám & Tư vấn Chỉnh nha",
          "price": 0,
          "quantity": 1,
          "sequenceNumber": 1
        }
        // ... more services
      ]
    }
    // ... more phases
  ]
}
```

---

## 6. How to Deploy Fix

### Step 1: Reload Seed Data (Development)

```bash
# Navigate to project root
cd d:/Code/PDCMS_BE

# Load seed data into PostgreSQL
docker exec -i postgres-dental psql -U root -d dental_clinic_db < src/main/resources/db/dental-clinic-seed-data.sql
```

---

### Step 2: Start/Restart Server

**Option A: Using Maven Wrapper**

```bash
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

**Option B: Using Docker**

```bash
docker-compose up --build
```

**Option C: Using IntelliJ IDEA**

- Right-click on `Application.java`
- Select "Run 'Application'"

---

### Step 3: Verify Server Started

```bash
# Wait for server to start (30-60 seconds)
curl http://localhost:8080/actuator/health

# Expected:
{"status":"UP"}
```

---

## 7. Production Deployment

### For Production Database

If production database already exists with empty `treatment_plan_templates` table:

```bash
# Connect to production database
psql -U <prod_user> -d <prod_db>

# Run only the template section
\i src/main/resources/db/dental-clinic-seed-data.sql
```

**⚠️ Warning**: If production already has templates, this will cause duplicate key errors. In that case:

```sql
-- Check existing templates
SELECT template_code FROM treatment_plan_templates;

-- If empty, proceed with INSERT
-- If has data, skip or use UPDATE statements
```

---

## 8. Files Modified

| File                                                | Line      | Change                                              |
| --------------------------------------------------- | --------- | --------------------------------------------------- |
| `src/main/resources/db/dental-clinic-seed-data.sql` | 1814      | Added `specialization_id` = 1 to TPL_ORTHO_METAL    |
| `src/main/resources/db/dental-clinic-seed-data.sql` | 1820      | Added `specialization_id` = 5 to TPL_IMPLANT_OSSTEM |
| `src/main/resources/db/dental-clinic-seed-data.sql` | 1826      | Added `specialization_id` = 4 to TPL_CROWN_CERCON   |
| `src/main/resources/db/dental-clinic-seed-data.sql` | 1800-1807 | Removed opening `/*` comment                        |
| `src/main/resources/db/dental-clinic-seed-data.sql` | 2331      | Removed closing `*/` comment                        |

---

## 9. Summary

### Problem

- API 5.8 trả về 404 Not Found
- Templates không có trong database

### Root Causes

1. Template INSERT statements bị comment `/* ... */`
2. INSERT statements thiếu `specialization_id` column

### Fixes Applied

1. ✅ Uncommented template section (lines 1800-2331)
2. ✅ Added `specialization_id` to all 3 template INSERT statements
3. ✅ Reloaded seed data

### Result

- ✅ 3 templates successfully loaded: TPL_ORTHO_METAL, TPL_IMPLANT_OSSTEM, TPL_CROWN_CERCON
- ✅ API 5.8 now ready for testing
- ✅ FE can proceed with template detail feature

---

## 10. Next Steps for FE

1. **Start Server** (Backend will start server and provide endpoint)
2. **Get JWT Token** (Login as ADMIN or DENTIST)
3. **Test API 5.8**:
   ```bash
   GET /api/v1/treatment-plan-templates/TPL_ORTHO_METAL
   GET /api/v1/treatment-plan-templates/TPL_IMPLANT_OSSTEM
   GET /api/v1/treatment-plan-templates/TPL_CROWN_CERCON
   ```
4. **Implement UI** to display template structure
5. **Implement Customize** feature (edit phases/services)
6. **Call API 5.4** to create custom treatment plan

---

## Contact

If FE encounters any issues:

- Check server logs: `logs/application.log`
- Verify JWT token is valid (not expired)
- Verify user has `CREATE_TREATMENT_PLAN` permission
- Contact Backend team with error details

**Status**: ✅ FIXED - Ready for FE testing
