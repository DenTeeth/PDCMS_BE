# INVENTORY API TEST GUIDE

Complete guide for testing Inventory Management APIs using Postman and Swagger UI.

---

## 📋 TABLE OF CONTENTS

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [API Endpoints](#api-endpoints)
4. [Business Rules](#business-rules)
5. [Testing with Postman](#testing-with-postman)
6. [Testing with Swagger UI](#testing-with-swagger-ui)
7. [Common Errors](#common-errors)

---

## 🎯 OVERVIEW

**Base URL**: `/api/v1/inventory`

**Authentication**: JWT Bearer Token required for all endpoints

**Permissions**:

- `VIEW_INVENTORY`: View inventory items (ADMIN, STAFF, INVENTORY_MANAGER)
- `CREATE_INVENTORY`: Create new inventory (ADMIN, INVENTORY_MANAGER)
- `UPDATE_INVENTORY`: Update inventory (ADMIN, INVENTORY_MANAGER)
- `DELETE_INVENTORY`: Delete inventory (ADMIN only)

**Key Features**:

- Auto-increment ID starting from 1 (Long type)
- camelCase JSON format for FE compatibility
- COLD warehouse validation (requires expiryDate)
- Stock quantity must be > 0
- Duplicate item name detection

---

## ✅ PREREQUISITES

1. **Application running**: `http://localhost:8080`
2. **Database migrations**: V1_8 (suppliers) and V1_9 (inventory) executed
3. **Authentication**:
   - Login via `/api/v1/auth/login`
   - Copy JWT token from response
4. **Test data**: At least one supplier created (supplierId = 1)

---

## 📡 API ENDPOINTS

### 1. CREATE INVENTORY ✨

**POST** `/api/v1/inventory`

**Permission**: `CREATE_INVENTORY` (ADMIN, INVENTORY_MANAGER)

**Request Body**:

```json
{
  "supplierId": 1,
  "itemName": "Thuốc tê Lidocaine 2%",
  "warehouseType": "COLD",
  "category": "Thuốc tê",
  "unitPrice": 150000,
  "unitOfMeasure": "HOP",
  "stockQuantity": 100,
  "minStockLevel": 20,
  "maxStockLevel": 500,
  "expiryDate": "2026-12-31",
  "isCertified": true,
  "certificationDate": "2024-11-01",
  "status": "ACTIVE",
  "notes": "Bảo quản ở nhiệt độ 2-8°C"
}
```

**Response (201 Created)**:

```json
{
  "inventoryId": 1,
  "supplierId": 1,
  "itemName": "Thuốc tê Lidocaine 2%",
  "warehouseType": "COLD",
  "category": "Thuốc tê",
  "unitPrice": 150000.0,
  "unitOfMeasure": "HOP",
  "stockQuantity": 100,
  "minStockLevel": 20,
  "maxStockLevel": 500,
  "expiryDate": "2026-12-31",
  "isCertified": true,
  "certificationDate": "2024-11-01",
  "status": "ACTIVE",
  "notes": "Bảo quản ở nhiệt độ 2-8°C",
  "createdAt": "2024-11-02T22:00:00",
  "updatedAt": "2024-11-02T22:00:00"
}
```

---

### 2. GET ALL INVENTORY 📋

**GET** `/api/v1/inventory?page=0&size=20&sortBy=itemName&sortDirection=ASC`

**Permission**: `VIEW_INVENTORY` (ADMIN, STAFF, INVENTORY_MANAGER)

**Query Parameters**:

- `page`: Page number (0-indexed, default: 0)
- `size`: Items per page (default: 20)
- `sortBy`: Sort field (default: `itemName`)
  - Options: `itemName`, `unitPrice`, `stockQuantity`, `createdAt`, `updatedAt`
- `sortDirection`: `ASC` or `DESC` (default: `ASC`)

**Response (200 OK)**:

```json
{
  "content": [
    {
      "inventoryId": 1,
      "supplierId": 1,
      "itemName": "Bông gạc vô trùng",
      "warehouseType": "NORMAL",
      "category": "Vật tư tiêu hao",
      "unitPrice": 50000.0,
      "unitOfMeasure": "GOI",
      "stockQuantity": 200,
      "minStockLevel": 50,
      "maxStockLevel": 1000,
      "expiryDate": null,
      "isCertified": false,
      "certificationDate": null,
      "status": "ACTIVE",
      "notes": null,
      "createdAt": "2024-11-02T21:00:00",
      "updatedAt": "2024-11-02T21:00:00"
    },
    {
      "inventoryId": 2,
      "supplierId": 1,
      "itemName": "Thuốc tê Lidocaine 2%",
      "warehouseType": "COLD",
      "category": "Thuốc tê",
      "unitPrice": 150000.0,
      "unitOfMeasure": "HOP",
      "stockQuantity": 100,
      "minStockLevel": 20,
      "maxStockLevel": 500,
      "expiryDate": "2026-12-31",
      "isCertified": true,
      "certificationDate": "2024-11-01",
      "status": "ACTIVE",
      "notes": "Bảo quản ở nhiệt độ 2-8°C",
      "createdAt": "2024-11-02T22:00:00",
      "updatedAt": "2024-11-02T22:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "first": true,
  "empty": false
}
```

---

### 3. GET INVENTORY BY WAREHOUSE TYPE 🏢

**GET** `/api/v1/inventory/warehouse-type/{warehouseType}`

**Permission**: `VIEW_INVENTORY`

**Path Variable**:

- `warehouseType`: `COLD` or `NORMAL`

**Example**: `/api/v1/inventory/warehouse-type/COLD`

**Response (200 OK)**:

```json
[
  {
    "inventoryId": 2,
    "supplierId": 1,
    "itemName": "Thuốc tê Lidocaine 2%",
    "warehouseType": "COLD",
    "category": "Thuốc tê",
    "unitPrice": 150000.0,
    "unitOfMeasure": "HOP",
    "stockQuantity": 100,
    "minStockLevel": 20,
    "maxStockLevel": 500,
    "expiryDate": "2026-12-31",
    "isCertified": true,
    "certificationDate": "2024-11-01",
    "status": "ACTIVE",
    "notes": "Bảo quản ở nhiệt độ 2-8°C",
    "createdAt": "2024-11-02T22:00:00",
    "updatedAt": "2024-11-02T22:00:00"
  }
]
```

---

### 4. SEARCH INVENTORY BY NAME 🔍

**GET** `/api/v1/inventory/search?itemName=thuốc`

**Permission**: `VIEW_INVENTORY`

**Query Parameter**:

- `itemName`: Search keyword (case-insensitive, partial match)

**Example**: `/api/v1/inventory/search?itemName=Lidocaine`

**Response (200 OK)**: Array of matching inventory items

---

### 5. GET INVENTORY BY ID 🔍

**GET** `/api/v1/inventory/{inventoryId}`

**Permission**: `VIEW_INVENTORY`

**Example**: `/api/v1/inventory/1`

**Response (200 OK)**: Single inventory object (same structure as create response)

---

### 6. UPDATE INVENTORY ✏️

**PUT** `/api/v1/inventory/{inventoryId}`

**Permission**: `UPDATE_INVENTORY` (ADMIN, INVENTORY_MANAGER)

**Request Body** (all fields optional):

```json
{
  "unitPrice": 160000,
  "stockQuantity": 150,
  "minStockLevel": 30,
  "notes": "Đã cập nhật giá từ nhà cung cấp"
}
```

**Response (200 OK)**: Updated inventory object

---

### 7. DELETE INVENTORY 🗑️

**DELETE** `/api/v1/inventory/{inventoryId}`

**Permission**: `DELETE_INVENTORY` (ADMIN only)

**Example**: `/api/v1/inventory/1`

**Response (200 OK)**:

```json
{
  "message": "Xóa vật tư thành công"
}
```

---

## ⚖️ BUSINESS RULES

### 1. Warehouse Type Validation

**COLD Warehouse**:

- **MUST** have `expiryDate` (required field)
- Used for: Medicines, vaccines, temperature-sensitive items
- Error if missing: `"Kho lạnh (COLD) bắt buộc phải có ngày hết hạn (expiryDate)"`

**NORMAL Warehouse**:

- `expiryDate` is optional
- Used for: General supplies, equipment, tools

### 2. Stock Quantity Rules

- **MUST be > 0** on create
- Can be 0 on update (but not negative)
- Error if ≤ 0 on create: `"Số lượng tồn kho phải lớn hơn 0"`

### 3. Duplicate Detection

- `itemName` must be **UNIQUE**
- Error if duplicate: `"Vật tư với tên '...' đã tồn tại"`

### 4. Enum Values

**WarehouseType**:

- `COLD`
- `NORMAL`

**UnitOfMeasure**:

- `CAI` (Cái)
- `HOP` (Hộp)
- `LO` (Lọ)
- `GOI` (Gói)
- `CHAI` (Chai)
- `THUNG` (Thùng)

**Status**:

- `ACTIVE` (Đang hoạt động)
- `INACTIVE` (Ngừng hoạt động)
- `OUT_OF_STOCK` (Hết hàng)

---

## 🧪 TESTING WITH POSTMAN

### Setup

1. **Import Collection**: Create new collection "Inventory API"
2. **Set Authorization**: Bearer Token = `{{jwt_token}}`
3. **Set Base URL**: `http://localhost:8080/api/v1/inventory`

### Test Sequence

#### Test 1: Create COLD Inventory (WITH expiryDate) ✅

```
POST /api/v1/inventory
Body:
{
  "supplierId": 1,
  "itemName": "Vaccine COVID-19",
  "warehouseType": "COLD",
  "unitPrice": 500000,
  "unitOfMeasure": "LO",
  "stockQuantity": 50,
  "expiryDate": "2025-06-30"
}
Expected: 201 Created
```

#### Test 2: Create COLD Inventory (WITHOUT expiryDate) ❌

```
POST /api/v1/inventory
Body:
{
  "supplierId": 1,
  "itemName": "Thuốc tê ABC",
  "warehouseType": "COLD",
  "unitPrice": 150000,
  "unitOfMeasure": "HOP",
  "stockQuantity": 100
  // Missing expiryDate
}
Expected: 400 Bad Request
Error: "Kho lạnh (COLD) bắt buộc phải có ngày hết hạn (expiryDate)"
```

#### Test 3: Create NORMAL Inventory (without expiryDate) ✅

```
POST /api/v1/inventory
Body:
{
  "supplierId": 1,
  "itemName": "Găng tay y tế",
  "warehouseType": "NORMAL",
  "unitPrice": 80000,
  "unitOfMeasure": "HOP",
  "stockQuantity": 500
}
Expected: 201 Created
```

#### Test 4: Create with Duplicate itemName ❌

```
POST /api/v1/inventory
Body:
{
  "supplierId": 1,
  "itemName": "Găng tay y tế", // Already exists
  "warehouseType": "NORMAL",
  "unitPrice": 90000,
  "unitOfMeasure": "HOP",
  "stockQuantity": 300
}
Expected: 409 Conflict
Error: "Vật tư với tên 'Găng tay y tế' đã tồn tại"
```

#### Test 5: Create with stockQuantity = 0 ❌

```
POST /api/v1/inventory
Body:
{
  "supplierId": 1,
  "itemName": "Bông gạc",
  "warehouseType": "NORMAL",
  "unitPrice": 50000,
  "unitOfMeasure": "GOI",
  "stockQuantity": 0 // Invalid
}
Expected: 400 Bad Request
Error: "Số lượng tồn kho phải lớn hơn 0"
```

#### Test 6: Get All Inventory (Paginated)

```
GET /api/v1/inventory?page=0&size=10&sortBy=itemName&sortDirection=ASC
Expected: 200 OK with pagination
```

#### Test 7: Filter by Warehouse Type

```
GET /api/v1/inventory/warehouse-type/COLD
Expected: 200 OK with array of COLD items only
```

#### Test 8: Search by Name

```
GET /api/v1/inventory/search?itemName=thuốc
Expected: 200 OK with items containing "thuốc"
```

#### Test 9: Update Inventory

```
PUT /api/v1/inventory/1
Body:
{
  "unitPrice": 550000,
  "stockQuantity": 45,
  "notes": "Đã điều chỉnh giá"
}
Expected: 200 OK with updated data
```

#### Test 10: Delete Inventory

```
DELETE /api/v1/inventory/1
Expected: 200 OK
Response: {"message": "Xóa vật tư thành công"}
```

---

## 🌐 TESTING WITH SWAGGER UI

### Access Swagger

**URL**: `http://localhost:8080/swagger-ui/index.html`

### Authorize

1. Click **"Authorize"** button (top right)
2. Enter: `Bearer <your_jwt_token>`
3. Click **"Authorize"** → **"Close"**

### Test Endpoints

Navigate to **"Warehouse - Inventory"** section:

1. **POST /api/v1/inventory**: Click "Try it out" → Fill JSON → "Execute"
2. **GET /api/v1/inventory**: Test pagination parameters
3. **GET /api/v1/inventory/warehouse-type/{warehouseType}**: Select COLD or NORMAL
4. **GET /api/v1/inventory/search**: Enter search keyword
5. **GET /api/v1/inventory/{inventoryId}**: Enter ID = 1
6. **PUT /api/v1/inventory/{inventoryId}**: Enter ID + update JSON
7. **DELETE /api/v1/inventory/{inventoryId}**: Enter ID to delete

---

## ❌ COMMON ERRORS

### 1. Missing expiryDate for COLD warehouse

**Error**:

```json
{
  "statusCode": 400,
  "message": "Kho lạnh (COLD) bắt buộc phải có ngày hết hạn (expiryDate)",
  "error": "INVALID_WAREHOUSE_DATA"
}
```

**Fix**: Add `"expiryDate": "2025-12-31"` to request body

---

### 2. Duplicate Item Name

**Error**:

```json
{
  "statusCode": 409,
  "message": "Vật tư với tên 'Thuốc tê Lidocaine 2%' đã tồn tại",
  "error": "DUPLICATE_INVENTORY"
}
```

**Fix**: Change `itemName` to a unique value

---

### 3. Stock Quantity ≤ 0

**Error**:

```json
{
  "statusCode": 400,
  "message": "Số lượng tồn kho phải lớn hơn 0",
  "error": "INVALID_WAREHOUSE_DATA"
}
```

**Fix**: Set `"stockQuantity": 1` or higher

---

### 4. Inventory Not Found

**Error**:

```json
{
  "statusCode": 404,
  "message": "Không tìm thấy vật tư với ID: 999",
  "error": "INVENTORY_NOT_FOUND"
}
```

**Fix**: Use valid `inventoryId` from database

---

### 5. Invalid Supplier ID

**Error**:

```json
{
  "statusCode": 500,
  "message": "Foreign key violation: supplier_id does not exist"
}
```

**Fix**: Create supplier first, then use valid `supplierId`

---

### 6. Permission Denied

**Error**:

```json
{
  "statusCode": 403,
  "message": "Access denied"
}
```

**Fix**: Login with ADMIN or INVENTORY_MANAGER role

---

## 📊 FIELD DESCRIPTIONS (for FE Display)

| Field               | Type          | Required | Description                       | Display Name (VI) |
| ------------------- | ------------- | -------- | --------------------------------- | ----------------- |
| `inventoryId`       | Long          | Auto     | ID tự tăng                        | Mã vật tư         |
| `supplierId`        | Long          | ✅       | FK to suppliers                   | Nhà cung cấp      |
| `itemName`          | String        | ✅       | Tên vật tư (unique)               | Tên vật tư        |
| `warehouseType`     | Enum          | ✅       | COLD/NORMAL                       | Loại kho          |
| `category`          | String        | ❌       | Nhóm vật tư                       | Nhóm              |
| `unitPrice`         | BigDecimal    | ✅       | Đơn giá                           | Đơn giá           |
| `unitOfMeasure`     | Enum          | ✅       | CAI/HOP/LO/GOI/CHAI/THUNG         | Đơn vị tính       |
| `stockQuantity`     | Integer       | ✅       | Số lượng tồn (> 0)                | Tồn kho           |
| `minStockLevel`     | Integer       | ❌       | Mức tồn tối thiểu                 | Tồn kho tối thiểu |
| `maxStockLevel`     | Integer       | ❌       | Mức tồn tối đa                    | Tồn kho tối đa    |
| `expiryDate`        | LocalDate     | ⚠️\*     | Ngày hết hạn (\*required if COLD) | Hạn sử dụng       |
| `isCertified`       | Boolean       | ❌       | Đã chứng nhận                     | Chứng nhận        |
| `certificationDate` | LocalDate     | ❌       | Ngày chứng nhận                   | Ngày chứng nhận   |
| `status`            | Enum          | ❌       | ACTIVE/INACTIVE/OUT_OF_STOCK      | Trạng thái        |
| `notes`             | String        | ❌       | Ghi chú                           | Ghi chú           |
| `createdAt`         | LocalDateTime | Auto     | Ngày tạo                          | Ngày tạo          |
| `updatedAt`         | LocalDateTime | Auto     | Ngày cập nhật                     | Cập nhật lần cuối |

---

## 🎨 UI SUGGESTIONS FOR FE

### Table View (Danh sách vật tư)

**Columns**:

- Mã vật tư (inventoryId)
- Tên vật tư (itemName)
- Loại kho (warehouseType) - Badge: 🧊 COLD / 📦 NORMAL
- Nhóm (category)
- Đơn vị (unitOfMeasure)
- Đơn giá (unitPrice) - Format: 150,000 đ
- Tồn kho (stockQuantity) - Color: Red if ≤ minStockLevel
- Hạn sử dụng (expiryDate) - Color: Red if expiring soon
- Trạng thái (status) - Badge: Green/Gray/Red
- Actions (Xem, Sửa, Xóa)

### Detail View (Chi tiết vật tư)

**4 Sections**:

1. **Thông tin cơ bản**: itemName, warehouseType, category, supplierId
2. **Giá & Đơn vị**: unitPrice, unitOfMeasure
3. **Tồn kho**: stockQuantity, minStockLevel, maxStockLevel, status
4. **Chứng nhận & Hạn sử dụng**: expiryDate, isCertified, certificationDate, notes

### Color Coding

- **warehouseType**:
  - COLD: Blue badge with ❄️ icon
  - NORMAL: Green badge with 📦 icon
- **status**:
  - ACTIVE: Green badge
  - INACTIVE: Gray badge
  - OUT_OF_STOCK: Red badge
- **stockQuantity**:
  - Normal: Black text
  - Low stock (≤ minStockLevel): Orange text with ⚠️ icon
  - Out of stock (0): Red text
- **expiryDate**:
  - Expiring soon (<30 days): Orange text
  - Expired: Red text with ⚠️ icon

---

## 🔐 PERMISSIONS MATRIX

| Action                   | ADMIN | INVENTORY_MANAGER | STAFF |
| ------------------------ | ----- | ----------------- | ----- |
| View Inventory           | ✅    | ✅                | ✅    |
| Create Inventory         | ✅    | ✅                | ❌    |
| Update Inventory         | ✅    | ✅                | ❌    |
| Delete Inventory         | ✅    | ❌                | ❌    |
| Filter by Warehouse Type | ✅    | ✅                | ✅    |
| Search by Name           | ✅    | ✅                | ✅    |

---

## 📝 NOTES

- All API responses use **camelCase** for FE compatibility
- ID starts from **1** (auto-increment)
- COLD warehouse **requires** expiryDate
- stockQuantity validation on create (> 0) vs update (≥ 0)
- Duplicate itemName detection prevents duplicates
- Supplier FK constraint requires valid supplierId

---

**Happy Testing!** 🎉

For any issues, check:

1. Migration V1_9 executed successfully
2. JWT token is valid and has correct permissions
3. Supplier with ID exists in database
4. Request body follows camelCase format
