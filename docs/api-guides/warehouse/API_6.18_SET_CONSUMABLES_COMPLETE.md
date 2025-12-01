# API 6.18: Set Service Consumables (BOM) - Bulk

## Overview

API thiết lập định mức tiêu hao vật tư (Bill of Materials) cho dịch vụ nha khoa. Hỗ trợ thiết lập đồng loạt cho nhiều dịch vụ cùng lúc.

**Strategy**: Upsert (Update if exists, Insert if not)

- Nếu consumable đã tồn tại (service_id + item_master_id): Cập nhật số lượng và ghi chú
- Nếu chưa tồn tại: Tạo mới bản ghi

## Endpoint

```
POST /api/v1/warehouse/consumables
```

## Permission Required

**MANAGE_CONSUMABLES**

- Roles có permission này: ROLE_ADMIN, ROLE_MANAGER
- ROLE_RECEPTIONIST, ROLE_DOCTOR, ROLE_NURSE không có quyền

## Request Body

```json
[
  {
    "serviceId": 1,
    "consumables": [
      {
        "itemMasterId": 101,
        "quantityPerService": 2.5,
        "unitId": 1,
        "notes": "Sử dụng loại composite cao cấp"
      },
      {
        "itemMasterId": 102,
        "quantityPerService": 1.0,
        "unitId": 2,
        "notes": null
      }
    ]
  },
  {
    "serviceId": 2,
    "consumables": [
      {
        "itemMasterId": 103,
        "quantityPerService": 0.5,
        "unitId": 1,
        "notes": "Chỉ dùng cho trường hợp răng hàm"
      }
    ]
  }
]
```

## Validation Rules

1. **serviceId**: Not null, must exist in services table
2. **consumables**: Not empty array, minimum 1 item
3. **itemMasterId**: Not null, must exist in item_masters table
4. **quantityPerService**: Not null, must be >= 0.01
5. **unitId**: Not null, must exist in item_units table and belong to the item_master
6. **notes**: Optional, can be null

## Business Rules

1. Unique constraint: (service_id, item_master_id) - One item can only appear once per service
2. Upsert behavior:
   - Existing record: Updates quantity_per_service, unit_id, notes
   - New record: Inserts new service_consumable
3. Transaction: All-or-nothing - If any service/item not found, entire request fails

## Response (Success)

**HTTP 200 OK**

```json
{
  "statusCode": 200,
  "message": "Service consumables set successfully",
  "data": "5 consumable records were set successfully"
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
    "consumables[0].quantityPerService": "must be greater than or equal to 0.01",
    "consumables[0].itemMasterId": "must not be null"
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

### 4. Invalid Item/Unit (400)

```json
{
  "statusCode": 400,
  "message": "Item or unit not found in database",
  "data": null
}
```

## Test Scenarios

### Test Case 1: Successful Bulk Set

**Request:**

```bash
curl -X POST http://localhost:8080/api/v1/warehouse/consumables \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "serviceId": 1,
      "consumables": [
        {
          "itemMasterId": 1,
          "quantityPerService": 2.0,
          "unitId": 1,
          "notes": "Composite resin for filling"
        },
        {
          "itemMasterId": 2,
          "quantityPerService": 1.0,
          "unitId": 2,
          "notes": "Anesthetic"
        }
      ]
    },
    {
      "serviceId": 2,
      "consumables": [
        {
          "itemMasterId": 3,
          "quantityPerService": 0.5,
          "unitId": 1,
          "notes": null
        }
      ]
    }
  ]'
```

**Expected Response:** HTTP 200 with "3 consumable records were set successfully"

### Test Case 2: Update Existing Consumable (Upsert)

**Setup:** Run Test Case 1 first to create initial records

**Request:**

```bash
curl -X POST http://localhost:8080/api/v1/warehouse/consumables \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "serviceId": 1,
      "consumables": [
        {
          "itemMasterId": 1,
          "quantityPerService": 3.5,
          "unitId": 1,
          "notes": "Increased quantity for better quality"
        }
      ]
    }
  ]'
```

**Expected Behavior:**

- Updates existing service_consumables record (service_id=1, item_master_id=1)
- New quantity: 3.5 (was 2.0)
- New notes: "Increased quantity for better quality"
- Response: HTTP 200 with "1 consumable records were set successfully"

### Test Case 3: Service Not Found (404)

**Request:**

```bash
curl -X POST http://localhost:8080/api/v1/warehouse/consumables \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "serviceId": 999999,
      "consumables": [
        {
          "itemMasterId": 1,
          "quantityPerService": 1.0,
          "unitId": 1,
          "notes": null
        }
      ]
    }
  ]'
```

**Expected Response:** HTTP 404 with "Service not found with ID: 999999"

### Test Case 4: Validation Error - Empty Consumables Array (400)

**Request:**

```bash
curl -X POST http://localhost:8080/api/v1/warehouse/consumables \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "serviceId": 1,
      "consumables": []
    }
  ]'
```

**Expected Response:** HTTP 400 with validation error about empty array

### Test Case 5: Validation Error - Negative Quantity (400)

**Request:**

```bash
curl -X POST http://localhost:8080/api/v1/warehouse/consumables \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "serviceId": 1,
      "consumables": [
        {
          "itemMasterId": 1,
          "quantityPerService": -1.0,
          "unitId": 1,
          "notes": null
        }
      ]
    }
  ]'
```

**Expected Response:** HTTP 400 with validation error about minimum value

### Test Case 6: Validation Error - Missing Required Fields (400)

**Request:**

```bash
curl -X POST http://localhost:8080/api/v1/warehouse/consumables \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "serviceId": 1,
      "consumables": [
        {
          "itemMasterId": null,
          "quantityPerService": null,
          "unitId": 1,
          "notes": null
        }
      ]
    }
  ]'
```

**Expected Response:** HTTP 400 with validation errors for null fields

### Test Case 7: Insufficient Permissions (403)

**Setup:** Use token from ROLE_RECEPTIONIST or ROLE_DOCTOR (without MANAGE_CONSUMABLES permission)

**Request:**

```bash
curl -X POST http://localhost:8080/api/v1/warehouse/consumables \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer RECEPTIONIST_TOKEN" \
  -d '[
    {
      "serviceId": 1,
      "consumables": [
        {
          "itemMasterId": 1,
          "quantityPerService": 1.0,
          "unitId": 1,
          "notes": null
        }
      ]
    }
  ]'
```

**Expected Response:** HTTP 403 Access denied

### Test Case 8: Invalid Item Master ID (400)

**Request:**

```bash
curl -X POST http://localhost:8080/api/v1/warehouse/consumables \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '[
    {
      "serviceId": 1,
      "consumables": [
        {
          "itemMasterId": 999999,
          "quantityPerService": 1.0,
          "unitId": 1,
          "notes": null
        }
      ]
    }
  ]'
```

**Expected Response:** HTTP 400 or 500 with foreign key constraint error

## Database Impact

**Table:** `service_consumables`

**Insert Example:**

```sql
INSERT INTO service_consumables (service_id, item_master_id, quantity_per_service, unit_id, notes, created_at, updated_at)
VALUES (1, 101, 2.5, 1, 'Sử dụng loại composite cao cấp', NOW(), NOW())
ON CONFLICT (service_id, item_master_id) DO UPDATE
SET quantity_per_service = EXCLUDED.quantity_per_service,
    unit_id = EXCLUDED.unit_id,
    notes = EXCLUDED.notes,
    updated_at = NOW();
```

## Use Cases

1. **Initial BOM Setup**: Warehouse manager thiết lập định mức tiêu hao cho dịch vụ mới
2. **Bulk Import**: Import BOM từ Excel/CSV cho nhiều dịch vụ cùng lúc
3. **Quantity Update**: Cập nhật số lượng vật tư theo kinh nghiệm thực tế
4. **Material Substitution**: Thay đổi loại vật tư/đơn vị tính cho dịch vụ

## Comparison with API 6.19

| Feature              | API 6.18 (POST)                 | API 6.19 (PUT)                |
| -------------------- | ------------------------------- | ----------------------------- |
| **Strategy**         | Upsert (update/insert)          | Replace (delete all + insert) |
| **Scope**            | Multiple services               | Single service                |
| **Existing Records** | Preserved if not in request     | All deleted                   |
| **Use Case**         | Bulk setup, incremental updates | Complete BOM reconfiguration  |
| **Performance**      | Efficient for large bulk        | Simple for full replacement   |

## Notes

- API này không xóa consumables hiện có nếu không được include trong request
- Nếu muốn xóa toàn bộ và tạo mới, sử dụng API 6.19 PUT
- Transaction đảm bảo tính nhất quán: tất cả thành công hoặc tất cả rollback
- Unique constraint ngăn chặn duplicate item trong cùng 1 service
