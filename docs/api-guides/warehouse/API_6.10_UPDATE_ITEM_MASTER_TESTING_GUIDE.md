# API 6.10: Update Item Master - Testing Guide

## Testing Status

**Current Status**: BLOCKED - Application startup fails due to pre-existing seed data bug

**Issue**: `storage_transactions` INSERT statements in seed data missing required columns  
**Line**: #328 in dental-clinic-seed-data.sql  
**Error**: SQL script execution failure during app initialization  
**Impact**: Cannot start application for integration testing  
**Related to API 6.10**: NO - Pre-existing bug in unrelated code

## Prerequisites

### Fix Required Before Testing
The seed data bug must be fixed before API 6.10 can be tested with a running application. The error occurs in storage_transactions INSERT statements which are missing columns that the table schema requires.

### Alternative Testing Approaches
1. **Option A**: Fix seed data and restart application
2. **Option B**: Skip seed data loading (set `spring.sql.init.mode=never`)
3. **Option C**: Create minimal test data programmatically
4. **Option D**: Use unit tests with mocked data

### Environment Setup
- Java 17
- PostgreSQL database
- Spring Boot 3.2.10
- Maven

### Required Test Data
For comprehensive testing, you need:
1. **Item with stock > 0**: To test Safety Lock mechanism
2. **Item with stock = 0**: To test free mode (all changes allowed)
3. **User with UPDATE_ITEMS permission**: ROLE_INVENTORY_MANAGER
4. **User without UPDATE_ITEMS**: ROLE_DOCTOR (for RBAC test)

## Test Scenarios

### Scenario 1: Update Item Name (Safe, Any Stock Level)

**Objective**: Verify basic item details can be updated regardless of stock

**Prerequisites**:
- Item exists with ID = 1
- Any stock level

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol Tablets 500mg UPDATED",
    "description": "Pain reliever and fever reducer - new formulation",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "isPrescriptionRequired": true,
    "defaultShelfLifeDays": 730,
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 10.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 1,
        "isDefaultImportUnit": true,
        "isDefaultExportUnit": false
      },
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 1.0,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 2,
        "isDefaultImportUnit": false,
        "isDefaultExportUnit": true
      }
    ]
  }'
```

**Expected Response**: 200 OK
```json
{
  "itemMasterId": 1,
  "itemCode": "MED-PARA-500",
  "itemName": "Paracetamol Tablets 500mg UPDATED",
  "totalQuantity": 500,
  "updatedAt": "2025-11-27T20:30:00",
  "updatedBy": "SYSTEM",
  "safetyLockApplied": true,
  "units": [...]
}
```

**Verification**:
- Response status is 200
- itemName updated in response
- safetyLockApplied is true (if stock > 0) or false (if stock = 0)
- units array contains all units

### Scenario 2: Adjust Stock Levels (Safe, Any Stock Level)

**Objective**: Verify stock alert levels can be adjusted

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 150,
    "maxStockLevel": 1500,
    "units": [...]
  }'
```

**Expected Response**: 200 OK  
**Min stock**: 150  
**Max stock**: 1500

**Verification**:
- Response status is 200
- minStockLevel = 150
- maxStockLevel = 1500

### Scenario 3: Rename Unit (Safe, Even with Stock)

**Objective**: Verify unit names can be changed when stock exists

**Prerequisites**:
- Item has stock > 0
- Unit with unitId = 1 exists with name "Hop"

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "units": [
      {
        "unitId": 1,
        "unitName": "Carton",
        "conversionRate": 10.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 1
      },
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 1.0,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 2
      }
    ]
  }'
```

**Expected Response**: 200 OK  
**Unit name**: "Carton" (changed from "Hop")

**Verification**:
- Response status is 200
- Unit with unitId=1 has name "Carton"
- safetyLockApplied is true
- No error occurred (rename is allowed)

### Scenario 4: Add New Unit (Safe, Even with Stock)

**Objective**: Verify new units can be added when stock exists

**Prerequisites**:
- Item has stock > 0

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 10.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 2
      },
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 1.0,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 3
      },
      {
        "unitId": null,
        "unitName": "Pallet",
        "conversionRate": 100.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 1
      }
    ]
  }'
```

**Expected Response**: 200 OK  
**New unit**: "Pallet" with conversion rate 100.0

**Verification**:
- Response status is 200
- units array contains 3 units
- New unit "Pallet" has valid unitId (auto-generated)
- safetyLockApplied is true

### Scenario 5: Soft Delete Unit (Safe, Even with Stock)

**Objective**: Verify units can be soft deleted (isActive = false) when stock exists

**Prerequisites**:
- Item has stock > 0
- Unit with unitId = 1 exists

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 10.0,
        "isBaseUnit": false,
        "isActive": false,
        "displayOrder": 1
      },
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 1.0,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 2
      }
    ]
  }'
```

**Expected Response**: 200 OK  
**Unit isActive**: false for unitId=1

**Verification**:
- Response status is 200
- Unit with unitId=1 has isActive = false
- Unit still exists in database
- safetyLockApplied is true

### Scenario 6: Change Conversion Rate (BLOCKED with Stock)

**Objective**: Verify Safety Lock blocks conversion rate changes when stock exists

**Prerequisites**:
- Item has stock > 0
- Unit with unitId = 1 has conversionRate = 10.0

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 12.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 1
      },
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 1.0,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 2
      }
    ]
  }'
```

**Expected Response**: 409 CONFLICT
```json
{
  "timestamp": "2025-11-27T20:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Safety Lock: Cannot modify units when stock exists. Blocked changes: Cannot change conversion rate for unit 'Hop' (current: 10.0, new: 12.0)",
  "path": "/api/v1/warehouse/items/1"
}
```

**Verification**:
- Response status is 409
- Error message mentions "Safety Lock"
- Error message lists specific blocked change
- Database unchanged (conversion rate still 10.0)

### Scenario 7: Change Base Unit (BLOCKED with Stock)

**Objective**: Verify Safety Lock blocks base unit flag changes when stock exists

**Prerequisites**:
- Item has stock > 0
- Unit with unitId = 1 has isBaseUnit = false

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 10.0,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 1
      },
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 1.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 2
      }
    ]
  }'
```

**Expected Response**: 409 CONFLICT  
**Error message**: Contains "Cannot change base unit status"

**Verification**:
- Response status is 409
- Error message mentions base unit change
- Database unchanged

### Scenario 8: Hard Delete Unit (BLOCKED with Stock)

**Objective**: Verify Safety Lock blocks unit deletion when stock exists

**Prerequisites**:
- Item has stock > 0
- Item has 2 units: unitId=1 and unitId=2

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "units": [
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 1.0,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 1
      }
    ]
  }'
```

**Expected Response**: 409 CONFLICT  
**Error message**: Contains "Cannot delete unit" and suggests soft delete

**Verification**:
- Response status is 409
- Error message mentions unit deletion blocked
- Database unchanged (both units still exist)

### Scenario 9: Free Mode - All Changes Allowed (Stock = 0)

**Objective**: Verify all changes are allowed when no stock exists

**Prerequisites**:
- Item has stock = 0 (cached_total_quantity = 0)

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/2 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "New Item Name",
    "description": "Completely restructured",
    "categoryId": 2,
    "warehouseType": "COLD",
    "minStockLevel": 50,
    "maxStockLevel": 500,
    "units": [
      {
        "unitId": null,
        "unitName": "Box",
        "conversionRate": 20.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 1
      },
      {
        "unitId": null,
        "unitName": "Piece",
        "conversionRate": 1.0,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 2
      }
    ]
  }'
```

**Expected Response**: 200 OK  
**safetyLockApplied**: false  
**Changes**: All changes applied successfully

**Verification**:
- Response status is 200
- safetyLockApplied is false
- All fields updated
- New unit hierarchy created

### Scenario 10: Validation Error - Min >= Max

**Objective**: Verify validation rejects invalid stock levels

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 1000,
    "maxStockLevel": 100,
    "units": [...]
  }'
```

**Expected Response**: 400 BAD REQUEST  
**Error message**: "Min stock level must be less than max stock level"

**Verification**:
- Response status is 400
- Error message clear and helpful
- Database unchanged

### Scenario 11: Validation Error - No Base Unit

**Objective**: Verify validation requires exactly one base unit

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 10.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 1
      },
      {
        "unitId": 2,
        "unitName": "Vi",
        "conversionRate": 1.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 2
      }
    ]
  }'
```

**Expected Response**: 400 BAD REQUEST  
**Error message**: "Exactly one base unit is required"

**Verification**:
- Response status is 400
- Database unchanged

### Scenario 12: Validation Error - Duplicate Unit Names

**Objective**: Verify validation prevents duplicate unit names

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Paracetamol 500mg",
    "description": "Pain reliever",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 100,
    "maxStockLevel": 1000,
    "units": [
      {
        "unitId": 1,
        "unitName": "Hop",
        "conversionRate": 10.0,
        "isBaseUnit": false,
        "isActive": true,
        "displayOrder": 1
      },
      {
        "unitId": 2,
        "unitName": "Hop",
        "conversionRate": 1.0,
        "isBaseUnit": true,
        "isActive": true,
        "displayOrder": 2
      }
    ]
  }'
```

**Expected Response**: 400 BAD REQUEST  
**Error message**: Contains "duplicated"

**Verification**:
- Response status is 400
- Database unchanged

### Scenario 13: Not Found - Invalid Item ID

**Objective**: Verify 404 error for non-existent item

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/999999 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "itemName": "Test",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 10,
    "maxStockLevel": 100,
    "units": [...]
  }'
```

**Expected Response**: 404 NOT FOUND  
**Error message**: "Item master with ID 999999 not found"

**Verification**:
- Response status is 404
- Error message clear

### Scenario 14: RBAC Test - Forbidden Without Permission

**Objective**: Verify authorization check

**Prerequisites**:
- User token without UPDATE_ITEMS permission (e.g., ROLE_DOCTOR)

**Request**:
```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer DOCTOR_JWT_TOKEN" \
  -d '{
    "itemName": "Test",
    "categoryId": 1,
    "warehouseType": "NORMAL",
    "minStockLevel": 10,
    "maxStockLevel": 100,
    "units": [...]
  }'
```

**Expected Response**: 403 FORBIDDEN  
**Error message**: "Access denied"

**Verification**:
- Response status is 403
- Database unchanged

### Scenario 15: RBAC Test - Success with Permission

**Objective**: Verify authorized user can update

**Prerequisites**:
- User token with UPDATE_ITEMS permission (ROLE_INVENTORY_MANAGER)

**Request**: Same as Scenario 1

**Expected Response**: 200 OK

**Verification**:
- Response status is 200
- Update successful

## Performance Testing

### Response Time Benchmarks
- Update without stock: < 100ms (target)
- Update with stock (Safety Lock): < 150ms (target)
- Update with 10 units: < 200ms (target)

### Test Method
Use Apache Bench or JMeter:
```bash
ab -n 100 -c 10 -T 'application/json' -H 'Authorization: Bearer TOKEN' \
   -p update_request.json \
   http://localhost:8080/api/v1/warehouse/items/1
```

### Metrics to Collect
- Average response time
- 95th percentile
- 99th percentile
- Throughput (requests/sec)
- Error rate

## Database Verification

### After Successful Update
```sql
-- Verify item master updated
SELECT * FROM item_masters WHERE item_master_id = 1;

-- Verify units updated
SELECT unit_id, unit_name, conversion_rate, is_base_unit, is_active
FROM item_units
WHERE item_master_id = 1
ORDER BY display_order;

-- Verify no orphaned units
SELECT * FROM item_units WHERE item_master_id NOT IN (SELECT item_master_id FROM item_masters);
```

### After Soft Delete
```sql
-- Verify unit still exists but inactive
SELECT * FROM item_units WHERE unit_id = 1;
-- Expected: is_active = false

-- Verify transaction history preserved
SELECT * FROM import_item_batches WHERE unit_id = 1;
-- Expected: Records still exist
```

## Test Data Setup

### Create Test Items (SQL)
```sql
-- Item with stock (for Safety Lock testing)
INSERT INTO item_masters (item_code, item_name, category_id, warehouse_type, min_stock_level, max_stock_level, cached_total_quantity, unit_of_measure, is_active, created_at, updated_at)
VALUES ('TEST-001', 'Test Item With Stock', 1, 'NORMAL', 10, 100, 500, 'Piece', TRUE, NOW(), NOW());

-- Item without stock (for free mode testing)
INSERT INTO item_masters (item_code, item_name, category_id, warehouse_type, min_stock_level, max_stock_level, cached_total_quantity, unit_of_measure, is_active, created_at, updated_at)
VALUES ('TEST-002', 'Test Item No Stock', 1, 'NORMAL', 10, 100, 0, 'Piece', TRUE, NOW(), NOW());

-- Units for TEST-001
INSERT INTO item_units (item_master_id, unit_name, conversion_rate, is_base_unit, is_active, display_order, created_at, updated_at)
VALUES 
  ((SELECT item_master_id FROM item_masters WHERE item_code = 'TEST-001'), 'Box', 10.0, FALSE, TRUE, 1, NOW(), NOW()),
  ((SELECT item_master_id FROM item_masters WHERE item_code = 'TEST-001'), 'Piece', 1.0, TRUE, TRUE, 2, NOW(), NOW());

-- Units for TEST-002
INSERT INTO item_units (item_master_id, unit_name, conversion_rate, is_base_unit, is_active, display_order, created_at, updated_at)
VALUES 
  ((SELECT item_master_id FROM item_masters WHERE item_code = 'TEST-002'), 'Box', 10.0, FALSE, TRUE, 1, NOW(), NOW()),
  ((SELECT item_master_id FROM item_masters WHERE item_code = 'TEST-002'), 'Piece', 1.0, TRUE, TRUE, 2, NOW(), NOW());
```

## Cleanup After Testing

```sql
-- Delete test items
DELETE FROM item_units WHERE item_master_id IN (
  SELECT item_master_id FROM item_masters WHERE item_code LIKE 'TEST-%'
);
DELETE FROM item_masters WHERE item_code LIKE 'TEST-%';
```

## Test Report Template

```markdown
# API 6.10 Test Report

**Date**: YYYY-MM-DD  
**Tester**: Name  
**Environment**: Dev/Staging/Prod

## Test Results Summary
- Total Scenarios: 15
- Passed: X
- Failed: Y
- Blocked: Z

## Detailed Results
| Scenario | Status | Notes |
|----------|--------|-------|
| 1. Update Item Name | PASS | Response time: 85ms |
| 2. Adjust Stock Levels | PASS | |
| ... | ... | ... |

## Issues Found
1. **Issue #1**: Description
   - Severity: High/Medium/Low
   - Steps to reproduce
   - Expected vs Actual

## Performance Metrics
- Average response time: Xms
- 95th percentile: Xms
- Throughput: X req/s

## Recommendations
- List any recommendations

## Sign-off
- [ ] All critical scenarios passed
- [ ] Performance meets targets
- [ ] Ready for production
```

## Known Limitations

### Blocked Testing
- **Application Startup**: Fails due to seed data bug (unrelated to API 6.10)
- **Workaround**: Fix seed data or skip seed data loading
- **Priority**: HIGH - Must fix before testing

### Missing Test Coverage
- Unit tests: 0%
- Integration tests: 0%
- Need to add automated tests

### Manual Testing Required
- All scenarios must be tested manually due to blocked app startup
- Recommend creating Postman collection for repeatable testing
- Consider adding to CI/CD pipeline once automated tests added

## Conclusion

API 6.10 implementation is code-complete and compiles successfully. Comprehensive testing blocked by unrelated seed data issue. Once resolved, follow this guide to thoroughly test all scenarios including Success cases, Safety Lock enforcement, Validation errors, and RBAC permissions.

**Testing Priority**: HIGH  
**Estimated Testing Time**: 2-3 hours  
**Prerequisite**: Fix seed data bug first
