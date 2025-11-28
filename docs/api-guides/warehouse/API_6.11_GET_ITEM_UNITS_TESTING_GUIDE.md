# API 6.11: Get Item Units - Testing Guide

## Overview

This guide provides comprehensive test scenarios for API 6.11 (Get Item Units). All tests use data from the seed file and can be executed via curl, Postman, or Swagger UI.

## Prerequisites

### 1. Start the Application
```bash
cd D:/Code/PDCMS_BE
./mvnw spring-boot:run
```

Wait for: "Started DentalClinicManagementApplication"

### 2. Verify Application Health
```bash
curl http://localhost:8080/actuator/health
```

Expected: `{"status":"UP"}`

### 3. Get Authentication Token

Login as Inventory Manager:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager",
    "password": "manager123"
  }'
```

Save the JWT token from response for subsequent requests.

## Test Data Setup

### Available Test Items (from seed data)

**Item 1: Thuoc giam dau Paracetamol 500mg**
- itemMasterId: Check DB after seed
- itemCode: DP-PARA-500
- Units: 3 (Hop, Vi, Vien)
- Status: Active

**Item 2: Thuoc khang sinh Amoxicillin 500mg**
- itemMasterId: Check DB after seed
- itemCode: DP-AMOX-500
- Units: 3 (Hop, Vi, Vien)
- Status: Active

Query to find item IDs:
```sql
SELECT item_master_id, item_code, item_name 
FROM item_masters 
WHERE is_active = true 
LIMIT 5;
```

## Test Scenarios

### Scenario 1: Get Active Units (Default Behavior)

**Purpose:** Verify default status filter returns only active units

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units" \
  -H "Authorization: Bearer {token}"
```

**Expected Response:**
```json
{
  "statusCode": 200,
  "message": "Item units retrieved successfully",
  "data": {
    "itemMaster": {
      "itemMasterId": 1,
      "itemCode": "DP-PARA-500",
      "itemName": "Thuoc giam dau Paracetamol 500mg",
      "isActive": true
    },
    "baseUnit": {
      "unitId": 3,
      "unitName": "Vien"
    },
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 100,
        "isBaseUnit": false,
        "displayOrder": 1,
        "isActive": true,
        "description": "1 Hop = 100 Vien"
      },
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 10,
        "isBaseUnit": false,
        "displayOrder": 2,
        "isActive": true,
        "description": "1 Vi = 10 Vien"
      },
      {
        "unitId": 3,
        "unitName": "Vien",
        "conversionRate": 1,
        "isBaseUnit": true,
        "displayOrder": 3,
        "isActive": true,
        "description": "Don vi co so"
      }
    ]
  }
}
```

**Validation:**
- [ ] Status code is 200
- [ ] Message is correct
- [ ] itemMaster contains all fields
- [ ] baseUnit matches unit with isBaseUnit=true
- [ ] All units have isActive=true
- [ ] Units sorted by displayOrder (1, 2, 3)
- [ ] Descriptions correctly generated
- [ ] Base unit description is "Don vi co so"

---

### Scenario 2: Get Active Units Explicitly

**Purpose:** Verify explicit status=active parameter works

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units?status=active" \
  -H "Authorization: Bearer {token}"
```

**Expected Result:** Same as Scenario 1

**Validation:**
- [ ] Response identical to default (status=active)
- [ ] Only active units returned

---

### Scenario 3: Get Inactive Units

**Purpose:** Verify status=inactive returns only soft-deleted units

**Setup:**
First, soft-delete a unit using API 6.10:
```bash
curl -X PUT "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "Thuoc giam dau Paracetamol 500mg",
    "categoryId": 1,
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "reorderPoint": 200,
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 100,
        "isBaseUnit": false,
        "isActive": false,
        "displayOrder": 1
      },
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 10,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 2
      },
      {
        "unitId": 3,
        "unitName": "Vien",
        "conversionRate": 1,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 3
      }
    ]
  }'
```

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units?status=inactive" \
  -H "Authorization: Bearer {token}"
```

**Expected Response:**
```json
{
  "statusCode": 200,
  "message": "Item units retrieved successfully",
  "data": {
    "itemMaster": { ... },
    "baseUnit": {
      "unitId": 3,
      "unitName": "Vien"
    },
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 100,
        "isBaseUnit": false,
        "displayOrder": 1,
        "isActive": false,
        "description": "1 Hop = 100 Vien"
      }
    ]
  }
}
```

**Validation:**
- [ ] Only 1 unit returned
- [ ] Unit has isActive=false
- [ ] Description still correctly generated

---

### Scenario 4: Get All Units (Active + Inactive)

**Purpose:** Verify status=all returns all units regardless of status

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units?status=all" \
  -H "Authorization: Bearer {token}"
```

**Expected Response:**
```json
{
  "statusCode": 200,
  "message": "Item units retrieved successfully",
  "data": {
    "itemMaster": { ... },
    "baseUnit": { ... },
    "units": [
      {
        "unitId": 1,
        "isActive": false,
        ...
      },
      {
        "unitId": 2,
        "isActive": true,
        ...
      },
      {
        "unitId": 3,
        "isActive": true,
        ...
      }
    ]
  }
}
```

**Validation:**
- [ ] 3 units returned
- [ ] Mix of isActive=true and isActive=false
- [ ] Still sorted by displayOrder

---

### Scenario 5: Invalid Item Master ID (404)

**Purpose:** Verify proper error for non-existent item

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/999999/units" \
  -H "Authorization: Bearer {token}"
```

**Expected Response:**
```json
{
  "statusCode": 404,
  "error": "ITEM_NOT_FOUND",
  "message": "Item master not found with ID: 999999"
}
```

**Validation:**
- [ ] Status code is 404
- [ ] Error message mentions the ID
- [ ] Response format matches error structure

---

### Scenario 6: Inactive Item Master (410 GONE)

**Purpose:** Verify 410 status for soft-deleted items

**Setup:**
First, soft-delete an item using API 6.10 (set all units to isActive=false, or use direct DB update).

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{inactiveItemId}/units" \
  -H "Authorization: Bearer {token}"
```

**Expected Response:**
```json
{
  "statusCode": 410,
  "error": "ITEM_INACTIVE",
  "message": "Item 'DP-OLD-001' is no longer active"
}
```

**Validation:**
- [ ] Status code is 410 (not 404)
- [ ] Error message mentions item code
- [ ] Distinguishes from non-existent item

---

### Scenario 7: Negative Item Master ID (400)

**Purpose:** Verify input validation rejects negative IDs

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/-1/units" \
  -H "Authorization: Bearer {token}"
```

**Expected Response:**
```json
{
  "statusCode": 400,
  "error": "VALIDATION_ERROR",
  "message": "getItemUnits.itemMasterId: must be greater than or equal to 1"
}
```

**Validation:**
- [ ] Status code is 400
- [ ] Validation error message clear
- [ ] Request rejected before hitting service layer

---

### Scenario 8: Zero Item Master ID (400)

**Purpose:** Verify validation rejects zero

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/0/units" \
  -H "Authorization: Bearer {token}"
```

**Expected Response:** Same as Scenario 7

**Validation:**
- [ ] Status code is 400
- [ ] Validation error for minimum value

---

### Scenario 9: Invalid Status Parameter (Case Insensitive)

**Purpose:** Verify status parameter is case-insensitive

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units?status=ACTIVE" \
  -H "Authorization: Bearer {token}"
```

**Expected Result:** Same as Scenario 1 (active units)

**Validation:**
- [ ] ACTIVE (uppercase) treated as "active"
- [ ] Returns only active units

---

### Scenario 10: Unknown Status Parameter (Defaults to Active)

**Purpose:** Verify unknown status values default to active

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units?status=invalid" \
  -H "Authorization: Bearer {token}"
```

**Expected Result:** Same as Scenario 1 (active units)

**Validation:**
- [ ] Invalid status treated as default (active)
- [ ] No error thrown

---

### Scenario 11: Authorization - No Token (401)

**Purpose:** Verify authentication required

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units"
```

**Expected Response:**
```json
{
  "statusCode": 401,
  "error": "UNAUTHORIZED",
  "message": "Full authentication is required to access this resource"
}
```

**Validation:**
- [ ] Status code is 401
- [ ] Clear authentication error message

---

### Scenario 12: Authorization - Doctor Role (Success)

**Purpose:** Verify doctor can access API for prescription workflow

**Setup:**
Login as doctor:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctor1",
    "password": "doctor123"
  }'
```

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units" \
  -H "Authorization: Bearer {doctorToken}"
```

**Expected Result:** Success (200) with full data

**Validation:**
- [ ] Doctor has VIEW_ITEMS permission
- [ ] Can access units for prescription

---

### Scenario 13: Authorization - Receptionist Role (Success)

**Purpose:** Verify receptionist can access for service booking

**Setup:**
Login as receptionist:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "receptionist1",
    "password": "receptionist123"
  }'
```

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units" \
  -H "Authorization: Bearer {receptionistToken}"
```

**Expected Result:** Success (200)

**Validation:**
- [ ] Receptionist has VIEW_ITEMS permission
- [ ] Can access units

---

### Scenario 14: Description Generation for Base Unit

**Purpose:** Verify base unit has special description

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units" \
  -H "Authorization: Bearer {token}"
```

**Focus:** Find unit where isBaseUnit=true

**Validation:**
- [ ] Base unit description is "Don vi co so"
- [ ] Not formatted as "1 Vien = 1 Vien"

---

### Scenario 15: Sorting by Display Order

**Purpose:** Verify units always sorted by displayOrder ASC

**Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/items/{itemMasterId}/units?status=all" \
  -H "Authorization: Bearer {token}"
```

**Validation:**
- [ ] First unit has displayOrder=1 (Hop - Box)
- [ ] Second unit has displayOrder=2 (Vi - Blister)
- [ ] Third unit has displayOrder=3 (Vien - Tablet/base)
- [ ] Order matches seed data configuration

---

## SQL Queries for Test Verification

### Check Item Units Configuration
```sql
SELECT 
    iu.unit_id,
    iu.unit_name,
    iu.conversion_rate,
    iu.is_base_unit,
    iu.display_order,
    iu.is_active,
    im.item_code,
    im.item_name
FROM item_units iu
JOIN item_masters im ON iu.item_master_id = im.item_master_id
WHERE im.item_code = 'DP-PARA-500'
ORDER BY iu.display_order ASC;
```

### Find Active Items with Units
```sql
SELECT 
    im.item_master_id,
    im.item_code,
    im.item_name,
    COUNT(iu.unit_id) as unit_count
FROM item_masters im
JOIN item_units iu ON im.item_master_id = iu.item_master_id
WHERE im.is_active = true
GROUP BY im.item_master_id, im.item_code, im.item_name
HAVING COUNT(iu.unit_id) > 0;
```

### Verify Permissions
```sql
SELECT r.role_id, p.permission_id
FROM role_permissions rp
JOIN roles r ON rp.role_id = r.role_id
JOIN permissions p ON rp.permission_id = p.permission_id
WHERE p.permission_id = 'VIEW_ITEMS'
  AND r.role_id IN ('ROLE_DENTIST', 'ROLE_RECEPTIONIST', 'ROLE_INVENTORY_MANAGER');
```

## Swagger UI Testing

### Access Swagger
Open browser: `http://localhost:8080/swagger-ui.html`

### Navigate to API 6.11
1. Find "Item Master Management" section
2. Expand "GET /api/v1/warehouse/items/{itemMasterId}/units"
3. Click "Try it out"

### Execute Tests
1. Enter itemMasterId (e.g., 1)
2. Select status (active/inactive/all)
3. Click "Execute"
4. Verify response matches expectations

## Test Summary Checklist

### Functional Tests
- [ ] Scenario 1: Default status (active)
- [ ] Scenario 2: Explicit active status
- [ ] Scenario 3: Inactive units only
- [ ] Scenario 4: All units
- [ ] Scenario 5: Invalid ID (404)
- [ ] Scenario 6: Inactive item (410)
- [ ] Scenario 7: Negative ID (400)
- [ ] Scenario 8: Zero ID (400)
- [ ] Scenario 9: Case insensitive status
- [ ] Scenario 10: Unknown status defaults

### Security Tests
- [ ] Scenario 11: No auth (401)
- [ ] Scenario 12: Doctor role (200)
- [ ] Scenario 13: Receptionist role (200)

### Data Integrity Tests
- [ ] Scenario 14: Base unit description
- [ ] Scenario 15: Sorting by displayOrder

## Performance Testing

### Load Test
```bash
# Using Apache Bench
ab -n 1000 -c 10 \
  -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/warehouse/items/1/units
```

**Expected:**
- Average response time: < 100ms
- No errors
- Consistent performance

### Stress Test
```bash
# 100 concurrent requests
ab -n 5000 -c 100 \
  -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/warehouse/items/1/units
```

**Expected:**
- No connection errors
- P95 latency < 500ms
- All responses valid

## Bug Reporting Template

If any test fails, report using this template:

```markdown
**Test Scenario:** Scenario X - [Name]

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happened]

**Request:**
```bash
[Exact curl command]
```

**Response:**
```json
[Actual response]
```

**Environment:**
- Java Version: 17
- Spring Boot Version: 3.2.10
- Database: PostgreSQL
- Branch: feat/BE-501-manage-treatment-plans

**Steps to Reproduce:**
1. [Step 1]
2. [Step 2]
...

**Additional Context:**
[Any relevant logs, screenshots, or SQL queries]
```

## Test Completion Sign-off

After completing all tests:

- [ ] All 15 scenarios passed
- [ ] No compilation errors
- [ ] No runtime exceptions
- [ ] All descriptions correctly generated
- [ ] All authorization checks working
- [ ] Performance acceptable
- [ ] Documentation accurate

**Tester Name:** _________________  
**Date:** _________________  
**Signature:** _________________

## Troubleshooting

### Common Issues

**Issue: 401 Unauthorized**
- Solution: Verify token is valid and not expired
- Check Authorization header format: "Bearer {token}"

**Issue: 404 Not Found**
- Solution: Verify item_master_id exists in database
- Run SQL query to find valid IDs

**Issue: Empty units array**
- Solution: Check if item has units configured
- Verify seed data loaded correctly

**Issue: Wrong description format**
- Solution: Check base unit exists and is properly marked
- Verify conversionRate is correct

## Post-Testing Actions

After all tests pass:

1. Document any bugs found and fixed
2. Update seed data if needed
3. Commit changes to Git
4. Update API documentation if behavior changed
5. Notify team of API availability

## Notes

- Replace `{token}` with actual JWT token
- Replace `{itemMasterId}` with actual item ID from database
- All tests assume seed data is loaded
- Tests are non-destructive except Scenario 3 (soft delete)
- For Scenario 3, restore data after testing or use dedicated test item
