# API 6.19: Update Service Consumables (BOM) - Replace

## Overview

API chỉnh sửa định mức tiêu hao vật tư (Bill of Materials) cho 1 dịch vụ nha khoa cụ thể. Sử dụng chiến lược thay thế hoàn toàn (replace).

**Strategy**: Replace (Delete all + Insert new)

- Xóa toàn bộ consumables hiện có của service
- Thêm mới toàn bộ consumables từ request
- Thích hợp cho việc cấu hình lại BOM hoàn toàn

## Endpoint

```
PUT /api/v1/warehouse/consumables/services/{serviceId}
```

## Permission Required

**MANAGE_CONSUMABLES**

- Roles có permission này: ROLE_ADMIN, ROLE_MANAGER
- ROLE_RECEPTIONIST, ROLE_DOCTOR, ROLE_NURSE không có quyền

## Path Parameter

- **serviceId** (required): ID của dịch vụ cần cập nhật BOM

## Request Body

```json
[
  {
    "itemMasterId": 101,
    "quantityPerService": 3.0,
    "unitId": 1,
    "notes": "Updated quantity based on practice"
  },
  {
    "itemMasterId": 105,
    "quantityPerService": 1.5,
    "unitId": 2,
    "notes": "New material added"
  }
]
```

## Validation Rules

1. **serviceId** (path param): Must exist in services table
2. **consumables array**: Not empty, minimum 1 item
3. **itemMasterId**: Not null, must exist in item_masters table
4. **quantityPerService**: Not null, must be >= 0.01
5. **unitId**: Not null, must exist in item_units table and belong to the item_master
6. **notes**: Optional, can be null

## Business Rules

1. **Complete Replacement**: All existing consumables are deleted before inserting new ones
2. **Atomic Operation**: Transaction ensures all-or-nothing behavior
3. **No Partial Update**: Cannot add/remove individual items - must send complete new BOM
4. **Empty Service**: If service has no consumables, this API creates the first BOM

## Response (Success)

**HTTP 200 OK**

```json
{
  "statusCode": 200,
  "message": "Service consumables updated successfully",
  "data": "2 consumable records were updated successfully"
}
```

## Error Responses

### 1. Service Not Found (404)

```json
{
  "statusCode": 404,
  "message": "Service not found with ID: 999",
  "data": null
}
```

### 2. Validation Error (400)

```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "data": {
    "consumables[0].quantityPerService": "must be greater than or equal to 0.01"
  }
}
```

### 3. Insufficient Permissions (403)

```json
{
  "statusCode": 403,
  "message": "Access denied",
  "data": null
}
```

### 4. Empty Array (400)

```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "data": {
    "consumables": "must not be empty"
  }
}
```

## Test Scenarios

### Test Case 1: Complete BOM Replacement

**Setup:** Service 1 có 2 consumables hiện tại (item 1, item 2)

**Request:**

```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "itemMasterId": 3,
      "quantityPerService": 4.0,
      "unitId": 1,
      "notes": "New material A"
    },
    {
      "itemMasterId": 4,
      "quantityPerService": 2.5,
      "unitId": 2,
      "notes": "New material B"
    },
    {
      "itemMasterId": 5,
      "quantityPerService": 1.0,
      "unitId": 1,
      "notes": "New material C"
    }
  ]'
```

**Expected Behavior:**

- DELETE all existing consumables (item 1, item 2 removed)
- INSERT 3 new consumables (item 3, 4, 5)
- Response: HTTP 200 with "3 consumable records were updated successfully"

**Verification:**

```bash
curl -X GET http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

Should return only items 3, 4, 5 (not 1, 2)

### Test Case 2: Add First BOM to Empty Service

**Setup:** Service 10 chưa có consumables nào

**Request:**

```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/consumables/services/10 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "itemMasterId": 1,
      "quantityPerService": 1.0,
      "unitId": 1,
      "notes": "First BOM configuration"
    }
  ]'
```

**Expected Response:** HTTP 200 with "1 consumable records were updated successfully"

### Test Case 3: Service Not Found (404)

**Request:**

```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/consumables/services/999999 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "itemMasterId": 1,
      "quantityPerService": 1.0,
      "unitId": 1,
      "notes": null
    }
  ]'
```

**Expected Response:** HTTP 404 with "Service not found with ID: 999999"

### Test Case 4: Empty Consumables Array (400)

**Request:**

```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[]'
```

**Expected Response:** HTTP 400 with validation error about empty array

### Test Case 5: Validation Error - Invalid Quantity (400)

**Request:**

```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "itemMasterId": 1,
      "quantityPerService": 0.001,
      "unitId": 1,
      "notes": null
    }
  ]'
```

**Expected Response:** HTTP 400 with validation error (quantity must be >= 0.01)

### Test Case 6: Missing Required Fields (400)

**Request:**

```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "itemMasterId": 1,
      "quantityPerService": null,
      "unitId": null,
      "notes": "Missing required fields"
    }
  ]'
```

**Expected Response:** HTTP 400 with validation errors for null fields

### Test Case 7: Insufficient Permissions (403)

**Setup:** Use token from ROLE_RECEPTIONIST (without MANAGE_CONSUMABLES)

**Request:**

```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer RECEPTIONIST_TOKEN" \
  -d '[
    {
      "itemMasterId": 1,
      "quantityPerService": 1.0,
      "unitId": 1,
      "notes": null
    }
  ]'
```

**Expected Response:** HTTP 403 Access denied

### Test Case 8: Duplicate Item in Same Request (400)

**Request:**

```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "itemMasterId": 1,
      "quantityPerService": 2.0,
      "unitId": 1,
      "notes": "First"
    },
    {
      "itemMasterId": 1,
      "quantityPerService": 3.0,
      "unitId": 1,
      "notes": "Duplicate"
    }
  ]'
```

**Expected Response:** HTTP 500 or 400 with unique constraint violation error

### Test Case 9: Invalid Item Master ID (400)

**Request:**

```bash
curl -X PUT http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "itemMasterId": 999999,
      "quantityPerService": 1.0,
      "unitId": 1,
      "notes": null
    }
  ]'
```

**Expected Response:** HTTP 400 or 500 with foreign key constraint error

## Database Impact

**Table:** `service_consumables`

**Operation Sequence:**

```sql
-- Step 1: Delete all existing consumables
DELETE FROM service_consumables WHERE service_id = 1;

-- Step 2: Insert new consumables
INSERT INTO service_consumables (service_id, item_master_id, quantity_per_service, unit_id, notes, created_at, updated_at)
VALUES
  (1, 3, 4.0, 1, 'New material A', NOW(), NOW()),
  (1, 4, 2.5, 2, 'New material B', NOW(), NOW()),
  (1, 5, 1.0, 1, 'New material C', NOW(), NOW());
```

## Use Cases

1. **Complete BOM Overhaul**: Thay đổi toàn bộ vật tư cho dịch vụ
2. **Material Substitution**: Thay thế hoàn toàn danh sách vật tư (không giữ lại vật tư cũ)
3. **Remove Old Materials**: Loại bỏ vật tư cũ và thêm vật tư mới
4. **Simplify BOM**: Giảm số lượng vật tư từ nhiều xuống ít

## Comparison with API 6.18

| Feature              | API 6.18 (POST)                   | API 6.19 (PUT)                                       |
| -------------------- | --------------------------------- | ---------------------------------------------------- |
| **HTTP Method**      | POST                              | PUT                                                  |
| **Endpoint**         | `/api/v1/warehouse/consumables`   | `/api/v1/warehouse/consumables/services/{serviceId}` |
| **Strategy**         | Upsert (update/insert)            | Replace (delete all + insert)                        |
| **Scope**            | Multiple services                 | Single service                                       |
| **Existing Records** | Preserved if not in request       | All deleted                                          |
| **Use Case**         | Bulk setup, incremental updates   | Complete BOM reconfiguration                         |
| **Request Body**     | Array of {serviceId, consumables} | Array of consumables only                            |
| **Idempotency**      | Partially (depends on data)       | Yes (same result on multiple calls)                  |

## When to Use API 6.19 vs 6.18

**Use API 6.19 (PUT) when:**

- Cần thay thế hoàn toàn BOM của 1 dịch vụ
- Muốn xóa tất cả vật tư cũ
- Có danh sách vật tư mới hoàn chỉnh
- Đơn giản hóa logic (không cần kiểm tra existing)

**Use API 6.18 (POST) when:**

- Cần thiết lập BOM cho nhiều dịch vụ cùng lúc
- Chỉ muốn cập nhật một số vật tư cụ thể
- Không muốn ảnh hưởng đến vật tư khác
- Bulk import từ file Excel/CSV

## Notes

- API này xóa toàn bộ consumables cũ - Không thể khôi phục
- Transaction đảm bảo tính nhất quán: nếu insert fail, delete sẽ rollback
- Request body không bao gồm serviceId (lấy từ path parameter)
- Unique constraint vẫn áp dụng: không thể có duplicate item trong request
- Nếu muốn xóa toàn bộ BOM (không insert gì), không nên dùng API này (cần API DELETE riêng)
