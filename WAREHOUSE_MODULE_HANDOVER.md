# 📦 WAREHOUSE MODULE - TÀI LIỆU BÀN GIAO KỸ THUẬT

**Ngày bàn giao:** 24/11/2025  
**Người bàn giao:** ThanhCQ1  
**Người nhận:** Backend Teammate  
**Branch:** `warehouse`  
**Version:** V3 (ERP-Compliant Architecture)

---

## 📑 MỤC LỤC

1. [Tổng quan Module](#1-tổng-quan-module)
2. [Kiến trúc hệ thống](#2-kiến-trúc-hệ-thống)
3. [Database Schema](#3-database-schema)
4. [Business Logic quan trọng](#4-business-logic-quan-trọng)
5. [API Endpoints](#5-api-endpoints)
6. [Use Cases thực tế](#6-use-cases-thực-tế)
7. [Testing & Seed Data](#7-testing--seed-data)
8. [Lưu ý quan trọng khi maintain](#8-lưu-ý-quan-trọng-khi-maintain)
9. [Roadmap & TODO](#9-roadmap--todo)

---

## 1. TỔNG QUAN MODULE

### 1.1. Mục đích
Module **Warehouse Management** quản lý toàn bộ vật tư nha khoa bao gồm:
- 📥 Nhập kho từ nhà cung cấp
- 📤 Xuất kho cho điều trị
- 📊 Thống kê tồn kho theo thời gian thực
- 🔔 Cảnh báo hết hạn sử dụng (HSD)
- 📈 Phân tích xu hướng nhập/xuất

### 1.2. Đặc điểm riêng của Warehouse nha khoa

#### **2 loại kho:**
1. **COLD (Kho lạnh)**: Vật tư có HSD (thuốc, composite, sealer, bonding agent)
   - Bắt buộc có `expiry_date`
   - Áp dụng FEFO (First Expired First Out)
   
2. **NORMAL (Kho thường)**: Dụng cụ không hết hạn (amalgam, dụng cụ kim loại)
   - Không bắt buộc HSD (nhưng sau mentor feedback → BẮT BUỘC cho TẤT CẢ)

#### **Phân loại vật tư:**
- **Vật tư tiêu hao** (Consumables): Thuốc, composite, amalgam, găng tay
- **Dụng cụ** (Tools): Kìm, kéo, máy khoan (trước đây `is_tool=TRUE` không cần HSD)

### 1.3. Mentor Feedback - Thiết kế ERP Chuẩn (Đã implement 100%)

**4 vấn đề nghiêm trọng đã fix:**

1. ❌ **Thiếu `item_code` trong transaction items**
   - ✅ **Fixed**: Thêm `item_code` vào `storage_transaction_items`
   - Auto-populate từ `ItemMaster.itemCode` khi tạo transaction
   - Warehouse staff có thể nhận diện vật tư ngay trên phiếu nhập/xuất

2. ❌ **Không hỗ trợ đơn vị đo lường hierarchy (Hộp → Vỉ → Viên)**
   - ✅ **Fixed**: Tạo bảng `item_units` với `conversion_rate`
   - Hỗ trợ giao dịch linh hoạt: "Xuất 2 vỉ từ hộp 10 vỉ"
   - VD: Amoxicillin có 3 units: Hộp (100) → Vỉ (10) → Viên (1)

3. ❌ **Expiry date không bắt buộc cho tools**
   - ✅ **Fixed**: `expiry_date NOT NULL` cho TẤT CẢ vật tư
   - Xóa exception cho `is_tool=TRUE`
   - Compliance với quy định quản lý thiết bị y tế

4. ❌ **Không tracking parent-child batches**
   - ✅ **Fixed**: Thêm `parent_batch_id` vào `item_batches`
   - Seed data có 28 ví dụ parent-child (batches 196-223)
   - Hỗ trợ truy vết: "Vỉ #197-206 xuất từ Hộp #196"

---

## 2. KIẾN TRÚC HỆ THỐNG

### 2.1. Package Structure

```
com.dental.clinic.management.warehouse/
├── controller/
│   ├── InventoryController.java         // API tồn kho
│   ├── StorageInOutController.java      // API nhập/xuất
│   ├── SupplierController.java          // API nhà cung cấp
│   └── ItemUnitController.java          // 🆕 API đơn vị đo
├── domain/                               // Entities (8 tables)
│   ├── ItemMaster.java                  // Định nghĩa vật tư (Master data)
│   ├── ItemBatch.java                   // Lô hàng (Physical inventory)
│   ├── ItemUnit.java                    // 🆕 Đơn vị đo (Hộp/Vỉ/Viên)
│   ├── ItemCategory.java                // Phân loại vật tư
│   ├── Supplier.java                    // Nhà cung cấp
│   ├── SupplierItem.java                // Mapping supplier-item
│   ├── StorageTransaction.java          // Phiếu nhập/xuất (Header)
│   └── StorageTransactionItem.java      // Chi tiết từng dòng (Line items)
├── dto/
│   ├── request/
│   │   ├── ImportRequest.java           // Payload nhập kho
│   │   ├── ExportRequest.java           // Payload xuất kho
│   │   ├── CreateItemMasterRequest.java
│   │   └── CreateSupplierRequest.java
│   ├── response/
│   │   ├── InventorySummaryResponse.java
│   │   ├── TransactionResponse.java     // 🔥 Có itemCode + unitName
│   │   ├── ItemUnitResponse.java        // 🆕 DTO cho unit hierarchy
│   │   └── StorageStatsResponse.java
├── service/
│   ├── InventoryService.java            // Tồn kho & thống kê
│   ├── StorageInOutService.java         // Nhập/xuất kho
│   ├── SupplierService.java             // CRUD suppliers
│   └── ItemUnitService.java             // 🆕 Quản lý đơn vị đo
├── repository/
│   ├── ItemMasterRepository.java
│   ├── ItemBatchRepository.java
│   ├── ItemUnitRepository.java          // 🆕
│   ├── StorageTransactionRepository.java
│   └── SupplierRepository.java
├── mapper/
│   └── StorageTransactionMapper.java    // Entity → DTO (có itemCode mapping)
├── enums/
│   ├── WarehouseType.java               // COLD vs NORMAL
│   ├── TransactionType.java             // IMPORT vs EXPORT
│   └── StockStatus.java                 // OUT_OF_STOCK, LOW_STOCK, NORMAL
└── exception/
    ├── ItemMasterNotFoundException.java
    ├── InsufficientStockException.java
    ├── ExpiryDateRequiredException.java // 🔥 Validation HSD
    └── BatchNotFoundException.java
```

### 2.2. Layer Responsibilities

#### **Controller Layer**
- REST API endpoints
- Request validation (`@Valid`)
- Authentication/Authorization check
- Gọi Service layer

#### **Service Layer** (Business logic)
- Transaction management (`@Transactional`)
- Business rules validation
- Tính toán số lượng tồn kho
- FEFO algorithm
- Tạo mã transaction (PN-20250117-001)

#### **Repository Layer**
- JPA queries
- Custom JPQL/Native queries
- Không chứa business logic

#### **Domain Layer**
- JPA entities
- Database mapping
- `@PrePersist`, `@PreUpdate` hooks

---

## 3. DATABASE SCHEMA

### 3.1. Core Tables

#### **item_masters** (Định nghĩa vật tư - Master Data)
```sql
CREATE TABLE item_masters (
    item_master_id BIGSERIAL PRIMARY KEY,
    item_code VARCHAR(50) UNIQUE NOT NULL,        -- 🔥 VD: "DP001", "VC002"
    item_name VARCHAR(255) NOT NULL,               -- "Amoxicillin 500mg"
    description TEXT,
    category_id BIGINT REFERENCES item_categories,
    warehouse_type VARCHAR(10) NOT NULL,           -- 'COLD' | 'NORMAL'
    unit_of_measure VARCHAR(50),                   -- "Hộp 100 viên" (legacy)
    min_stock_level INT DEFAULT 0,
    max_stock_level INT DEFAULT 0,
    is_tool BOOLEAN DEFAULT FALSE,                 -- Legacy (không còn dùng)
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

**Lưu ý:**
- `item_code`: Mã nhận diện vật tư (warehouse staff dùng để scan/nhập liệu)
- `warehouse_type`: Quyết định có cần HSD hay không (nhưng giờ ALL items cần HSD)
- `is_tool`: Deprecated sau mentor feedback, nhưng giữ lại cho backward compatibility

---

#### **item_units** 🆕 (Đơn vị đo lường - Hierarchy)
```sql
CREATE TABLE item_units (
    unit_id BIGSERIAL PRIMARY KEY,
    item_master_id BIGINT NOT NULL REFERENCES item_masters,
    unit_name VARCHAR(50) NOT NULL,                -- "Hộp", "Vỉ", "Viên"
    conversion_rate INT NOT NULL,                  -- Tỷ lệ quy đổi về base unit
    is_base_unit BOOLEAN DEFAULT FALSE,            -- TRUE = đơn vị nhỏ nhất
    display_order INT,                             -- Sort order (1=lớn nhất)
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

**Ví dụ hierarchy (Amoxicillin 500mg):**
```
unit_id | item_master_id | unit_name | conversion_rate | is_base_unit | display_order
--------|----------------|-----------|-----------------|--------------|---------------
   1    |       24       |   Hộp     |      100        |    FALSE     |      1
   2    |       24       |   Vỉ      |       10        |    FALSE     |      2
   3    |       24       |   Viên    |        1        |    TRUE      |      3
```

**Logic quy đổi:**
- 1 Hộp = 100 Viên (conversionRate = 100)
- 1 Vỉ = 10 Viên (conversionRate = 10)
- 1 Viên = 1 (base unit, conversionRate = 1)

**Ứng dụng:**
- Frontend dropdown chọn đơn vị khi nhập/xuất
- Convert: "Xuất 2 Hộp" → 200 Viên
- Báo cáo tồn kho theo đơn vị linh hoạt

---

#### **item_batches** (Lô hàng - Physical Inventory)
```sql
CREATE TABLE item_batches (
    batch_id BIGSERIAL PRIMARY KEY,
    item_master_id BIGINT NOT NULL REFERENCES item_masters,
    lot_number VARCHAR(100) NOT NULL,              -- Số lô (VD: "AMOX-2025-C")
    quantity_on_hand INT NOT NULL DEFAULT 0,       -- 🔥 SỐ LƯỢNG TỒN KHO
    parent_batch_id BIGINT REFERENCES item_batches, -- 🆕 Parent batch (hierarchy)
    expiry_date DATE NOT NULL,                     -- 🔥 BẮT BUỘC (sau mentor feedback)
    supplier_id BIGINT REFERENCES suppliers,
    imported_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(item_master_id, lot_number)
);
```

**Điểm quan trọng:**
- `quantity_on_hand`: Đây là nơi DUY NHẤT lưu số lượng tồn kho
- `expiry_date NOT NULL`: Bắt buộc cho TẤT CẢ vật tư (kể cả tools)
- `parent_batch_id`: Hỗ trợ parent-child tracking
  - VD: Batch #196 (Hộp 100 viên) → 10 child batches #197-206 (mỗi vỉ 10 viên)

**Parent-Child Workflow:**
```
1. Nhập kho: 1 Hộp Amoxicillin 100 viên → Batch #196 (parent)
2. Chia nhỏ: Tạo 10 batches vỉ → Batches #197-206 (children)
   - Mỗi batch child có parent_batch_id = 196
3. Xuất kho: Xuất 2 vỉ → Lấy từ batch #197, #198
4. Truy vết: Vỉ #197 xuất từ Hộp #196 (lot_number: AMOX-2025-C)
```

---

#### **storage_transactions** (Phiếu Nhập/Xuất - Header)
```sql
CREATE TABLE storage_transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    transaction_code VARCHAR(50) UNIQUE NOT NULL,  -- "PN-20250117-001"
    transaction_type VARCHAR(20) NOT NULL,         -- 'IMPORT' | 'EXPORT'
    transaction_date TIMESTAMP NOT NULL,
    supplier_id BIGINT REFERENCES suppliers,       -- Chỉ dùng cho IMPORT
    notes TEXT,
    created_by BIGINT REFERENCES employees,
    created_at TIMESTAMP NOT NULL
);
```

**Mã phiếu format:**
- Import: `PN-YYYYMMDD-XXX` (Phiếu Nhập)
- Export: `PX-YYYYMMDD-XXX` (Phiếu Xuất)

---

#### **storage_transaction_items** (Chi tiết từng dòng)
```sql
CREATE TABLE storage_transaction_items (
    transaction_item_id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES storage_transactions,
    batch_id BIGINT NOT NULL REFERENCES item_batches,
    item_code VARCHAR(50),                         -- 🆕 Auto-populated từ ItemMaster
    unit_id BIGINT REFERENCES item_units,          -- 🆕 Đơn vị giao dịch (nullable)
    quantity_change INT NOT NULL,                  -- Dương = Nhập, Âm = Xuất
    notes TEXT
);
```

**Điểm quan trọng:**
- `item_code`: Tự động copy từ `item_masters.item_code` khi tạo transaction
  - Lý do: Warehouse staff cần xem item_code trên phiếu mà không JOIN
- `unit_id`: Optional, dùng khi giao dịch theo đơn vị cụ thể (Vỉ, Hộp)
  - NULL = sử dụng base unit mặc định
- `quantity_change`: 
  - Import: `+50` (nhập 50 cái)
  - Export: `-20` (xuất 20 cái)

---

### 3.2. Relationship Diagram

```
item_masters (1) ----< (N) item_batches
     |                        |
     | (1)                    | (1)
     |                        |
     v (N)                    v (N)
item_units         storage_transaction_items
                            |
                            | (N)
                            |
                            v (1)
                   storage_transactions
                            |
                            | (N)
                            v (1)
                        suppliers

PARENT-CHILD BATCHES:
item_batches.parent_batch_id → item_batches.batch_id (self-referencing)
```

---

### 3.3. Index & Constraints

**Unique Constraints:**
```sql
UNIQUE(item_master_id, lot_number) -- item_batches
UNIQUE(item_code)                  -- item_masters
UNIQUE(transaction_code)           -- storage_transactions
```

**Indexes (nên tạo):**
```sql
CREATE INDEX idx_batches_expiry ON item_batches(expiry_date);
CREATE INDEX idx_batches_item ON item_batches(item_master_id);
CREATE INDEX idx_batches_parent ON item_batches(parent_batch_id);
CREATE INDEX idx_units_item ON item_units(item_master_id);
CREATE INDEX idx_trans_date ON storage_transactions(transaction_date);
CREATE INDEX idx_trans_type ON storage_transactions(transaction_type);
```

---

## 4. BUSINESS LOGIC QUAN TRỌNG

### 4.1. Import Flow (Nhập kho)

```java
// File: StorageInOutService.java - importItems()

1. Validate Supplier tồn tại
2. Tạo Transaction Header (mã PN-YYYYMMDD-XXX)
3. FOR EACH item trong request:
   a. Validate ItemMaster tồn tại
   b. 🔥 Check expiry_date NOT NULL (bắt buộc cho ALL items)
   c. Tìm hoặc tạo Batch mới (theo lot_number)
   d. 🔥 Auto-populate item_code từ ItemMaster
   e. quantity_on_hand += quantity (tăng tồn kho)
   f. Tạo TransactionItem (+quantity)
4. Save Transaction
5. Return TransactionResponse (có itemCode + unitName)
```

**Code snippet:**
```java
// 🔥 Mentor feedback: Expiry date BẮT BUỘC cho ALL items
if (itemDto.getExpiryDate() == null) {
    throw new ExpiryDateRequiredException(itemMaster.getItemName());
}

// 🔥 Auto-populate item_code
StorageTransactionItem transactionItem = StorageTransactionItem.builder()
    .batch(batch)
    .itemCode(itemMaster.getItemCode())  // ← Tự động lấy từ master
    .unit(unit)                           // ← Optional unit
    .quantityChange(itemDto.getQuantity())
    .notes(itemDto.getNotes())
    .build();
```

---

### 4.2. Export Flow (Xuất kho)

```java
// File: StorageInOutService.java - exportItems()

1. Tạo Transaction Header (mã PX-YYYYMMDD-XXX)
2. FOR EACH item trong request:
   a. Validate ItemMaster tồn tại
   b. Get batches theo FEFO (hết hạn sớm nhất trước)
   c. Phân bổ số lượng xuất theo từng batch:
      - Batch 1: Lấy hết → còn thiếu
      - Batch 2: Lấy tiếp → đủ rồi STOP
   d. FOR EACH batch được chọn:
      - Check quantity_on_hand >= quantityToTake
      - 🔥 Auto-populate item_code
      - quantity_on_hand -= quantityToTake (giảm tồn)
      - Tạo TransactionItem (-quantity)
3. Save Transaction
4. Return TransactionResponse
```

**FEFO Algorithm:**
```java
List<ItemBatch> batches = itemBatchRepository
    .findByItemMaster_ItemMasterIdOrderByExpiryDateAsc(itemMasterId);
// ↑ Sắp xếp theo expiry_date tăng dần → lấy batch sắp hết hạn trước
```

---

### 4.3. Inventory Summary Logic

```java
// File: InventoryService.java - getInventorySummary()

SELECT 
    im.item_master_id,
    im.item_code,
    im.item_name,
    im.warehouse_type,
    SUM(ib.quantity_on_hand) as total_quantity,  -- 🔥 Tổng từ TẤT CẢ batches
    MIN(ib.expiry_date) as nearest_expiry,       -- Batch sắp hết hạn nhất
    CASE 
        WHEN SUM(ib.quantity_on_hand) = 0 THEN 'OUT_OF_STOCK'
        WHEN SUM(ib.quantity_on_hand) < im.min_stock_level THEN 'LOW_STOCK'
        WHEN SUM(ib.quantity_on_hand) > im.max_stock_level THEN 'OVERSTOCK'
        ELSE 'NORMAL'
    END as stock_status
FROM item_masters im
LEFT JOIN item_batches ib ON im.item_master_id = ib.item_master_id
GROUP BY im.item_master_id
```

**Stock Status Logic:**
- `OUT_OF_STOCK`: Tổng quantity = 0
- `LOW_STOCK`: Tổng < min_stock_level
- `OVERSTOCK`: Tổng > max_stock_level
- `NORMAL`: Nằm trong khoảng min-max

---

### 4.4. Unit Conversion Logic 🆕

```java
// File: ItemUnitService.java - convertQuantity()

VD: Convert 2 Hộp → ? Viên

1. Get fromUnit (Hộp, conversionRate=100)
2. Get toUnit (Viên, conversionRate=1)
3. Validate cùng item_master_id
4. Convert:
   baseQuantity = 2 * 100 = 200 Viên
   result = 200 / 1 = 200 Viên
```

**Công thức:**
```
baseQuantity = quantity × fromUnit.conversionRate
resultQuantity = baseQuantity ÷ toUnit.conversionRate
```

---

## 5. API ENDPOINTS

### 5.1. Base URL
```
http://localhost:8080/api/v3/warehouse
```

---

### 5.2. Inventory APIs

#### **GET /api/v3/warehouse/summary**
Lấy tồn kho tổng hợp

**Query Params:**
- `stockStatus` (optional): `OUT_OF_STOCK` | `LOW_STOCK` | `NORMAL` | `OVERSTOCK`
- `warehouseType` (optional): `COLD` | `NORMAL`
- `categoryId` (optional): Filter theo category

**Response:**
```json
{
  "items": [
    {
      "itemMasterId": 24,
      "itemCode": "DP001",
      "itemName": "Amoxicillin 500mg",
      "categoryName": "Dược phẩm",
      "warehouseType": "COLD",
      "totalQuantity": 450,
      "stockStatus": "NORMAL",
      "nearestExpiryDate": "2025-09-30"
    }
  ],
  "totalItems": 42,
  "outOfStockCount": 3,
  "lowStockCount": 8
}
```

---

#### **GET /api/v3/warehouse/batches/{itemMasterId}**
Lấy tất cả batches của 1 item (theo FEFO)

**Response:**
```json
{
  "batches": [
    {
      "batchId": 196,
      "lotNumber": "AMOX-2025-C",
      "quantityOnHand": 100,
      "expiryDate": "2025-09-30",
      "supplierName": "Công ty Dược ABC",
      "parentBatchId": null
    },
    {
      "batchId": 197,
      "lotNumber": "AMOX-2025-C-V01",
      "quantityOnHand": 10,
      "expiryDate": "2025-09-30",
      "supplierName": "Công ty Dược ABC",
      "parentBatchId": 196  // 🔥 Child của batch #196
    }
  ]
}
```

---

### 5.3. Import/Export APIs

#### **POST /api/v3/warehouse/import**
Nhập kho

**Request Body:**
```json
{
  "supplierId": 2,
  "notes": "Nhập lô thuốc tháng 11",
  "items": [
    {
      "itemMasterId": 24,
      "lotNumber": "AMOX-2025-D",
      "quantity": 500,
      "expiryDate": "2026-11-30",  // 🔥 BẮT BUỘC
      "unitId": 1                   // 🆕 Optional (Hộp)
    }
  ]
}
```

**Response:**
```json
{
  "transactionId": 92,
  "transactionCode": "PN-20251124-001",
  "transactionType": "IMPORT",
  "transactionDate": "2025-11-24T14:30:00",
  "supplierName": "Công ty Dược ABC",
  "createdByName": "Nguyễn Văn A",
  "items": [
    {
      "transactionItemId": 159,
      "itemCode": "DP001",           // 🔥 Auto-populated
      "itemName": "Amoxicillin 500mg",
      "unitName": "Hộp",             // 🔥 From ItemUnit
      "lotNumber": "AMOX-2025-D",
      "quantityChange": 500,
      "notes": null
    }
  ]
}
```

---

#### **POST /api/v3/warehouse/export**
Xuất kho (FEFO tự động)

**Request Body:**
```json
{
  "notes": "Xuất cho phòng điều trị A",
  "items": [
    {
      "itemMasterId": 24,
      "quantity": 50,
      "unitId": 2  // 🆕 Xuất theo Vỉ
    }
  ]
}
```

**Logic:**
- Hệ thống tự động chọn batch hết hạn sớm nhất
- Nếu 1 batch không đủ → lấy từ nhiều batches
- Trả về danh sách batches đã xuất

---

### 5.4. Item Unit APIs 🆕

#### **GET /api/v3/warehouse/items/{itemMasterId}/units**
Lấy danh sách đơn vị đo của item

**Response:**
```json
{
  "units": [
    {
      "unitId": 1,
      "unitName": "Hộp",
      "conversionRate": 100,
      "isBaseUnit": false,
      "displayOrder": 1
    },
    {
      "unitId": 2,
      "unitName": "Vỉ",
      "conversionRate": 10,
      "isBaseUnit": false,
      "displayOrder": 2
    },
    {
      "unitId": 3,
      "unitName": "Viên",
      "conversionRate": 1,
      "isBaseUnit": true,
      "displayOrder": 3
    }
  ]
}
```

**Use case:**
- Frontend dropdown chọn đơn vị khi nhập/xuất
- Hiển thị theo thứ tự lớn → nhỏ (displayOrder)

---

#### **GET /api/v3/warehouse/items/{itemMasterId}/units/base**
Lấy base unit (đơn vị nhỏ nhất)

**Response:**
```json
{
  "unitId": 3,
  "unitName": "Viên",
  "conversionRate": 1,
  "isBaseUnit": true,
  "displayOrder": 3
}
```

---

#### **GET /api/v3/warehouse/items/units/convert**
Quy đổi số lượng giữa 2 đơn vị

**Query Params:**
- `fromUnitId=1` (Hộp)
- `toUnitId=3` (Viên)
- `quantity=2`

**Response:**
```json
{
  "convertedQuantity": 200
}
```

**Logic:**
```
2 Hộp (conversionRate=100) → 200 Viên (conversionRate=1)
```

---

### 5.5. Analytics APIs

#### **GET /api/v3/warehouse/analytics/storage-stats**
Thống kê nhập/xuất theo tháng

**Query Params:**
- `month=11`
- `year=2025`

**Response:**
```json
{
  "month": 11,
  "year": 2025,
  "totalImports": 25,
  "totalExports": 18,
  "topImportedItems": [
    {
      "itemName": "Găng tay nitrile",
      "quantity": 500,
      "value": 5000000
    }
  ],
  "topExportedItems": [...]
}
```

---

## 6. USE CASES THỰC TẾ

### Use Case 1: Nhập kho 1 Hộp Amoxicillin

**Scenario:** Nhân viên kho nhận 1 hộp Amoxicillin 100 viên từ nhà cung cấp

**Request:**
```json
POST /api/v3/warehouse/import
{
  "supplierId": 2,
  "notes": "Lô mới tháng 11",
  "items": [
    {
      "itemMasterId": 24,
      "lotNumber": "AMOX-2025-D",
      "quantity": 100,
      "expiryDate": "2026-11-30",
      "unitId": 1  // Hộp
    }
  ]
}
```

**Backend xử lý:**
1. Tạo Transaction: `PN-20251124-001`
2. Tìm/tạo Batch: `AMOX-2025-D`
3. Auto-populate `item_code = "DP001"`
4. Update `quantity_on_hand += 100`
5. Tạo TransactionItem (+100)

**Response:**
```json
{
  "transactionCode": "PN-20251124-001",
  "items": [
    {
      "itemCode": "DP001",
      "itemName": "Amoxicillin 500mg",
      "unitName": "Hộp",
      "quantityChange": 100
    }
  ]
}
```

---

### Use Case 2: Xuất 20 viên Amoxicillin (FEFO)

**Scenario:** Bác sĩ kê đơn 20 viên cho bệnh nhân

**Request:**
```json
POST /api/v3/warehouse/export
{
  "items": [
    {
      "itemMasterId": 24,
      "quantity": 20,
      "unitId": 3  // Viên (base unit)
    }
  ]
}
```

**Backend xử lý:**
1. Query batches: `ORDER BY expiry_date ASC`
2. Batch sắp hết hạn: `AMOX-2024-D` (expiry: 2025-04-30, qty: 8)
   - Lấy 8 viên → còn thiếu 12
3. Batch tiếp theo: `AMOX-2025-C` (expiry: 2026-09-30, qty: 38)
   - Lấy 12 viên → đủ
4. Tạo 2 TransactionItems:
   - Item 1: batch #26, -8 viên
   - Item 2: batch #25, -12 viên

**Response:**
```json
{
  "transactionCode": "PX-20251124-001",
  "items": [
    {
      "itemCode": "DP001",
      "unitName": "Viên",
      "lotNumber": "AMOX-2024-D",
      "quantityChange": -8
    },
    {
      "itemCode": "DP001",
      "unitName": "Viên",
      "lotNumber": "AMOX-2025-C",
      "quantityChange": -12
    }
  ]
}
```

---

### Use Case 3: Chia nhỏ Hộp thành Vỉ (Parent-Child)

**Scenario:** Warehouse staff mở hộp 100 viên ra 10 vỉ

**Seed Data Example (đã có sẵn):**
```sql
-- Parent batch: Hộp 100 viên
INSERT INTO item_batches (batch_id, item_master_id, lot_number, quantity_on_hand, expiry_date)
VALUES (196, 24, 'AMOX-2025-C', 100, '2026-09-30');

-- Child batches: 10 Vỉ (mỗi vỉ 10 viên)
INSERT INTO item_batches (batch_id, item_master_id, lot_number, quantity_on_hand, parent_batch_id, expiry_date)
VALUES 
(197, 24, 'AMOX-2025-C-V01', 10, 196, '2026-09-30'),
(198, 24, 'AMOX-2025-C-V02', 10, 196, '2026-09-30'),
...
(206, 24, 'AMOX-2025-C-V10', 10, 196, '2026-09-30');
```

**Truy vết:**
```sql
SELECT * FROM item_batches WHERE parent_batch_id = 196;
-- → 10 batches vỉ (197-206)
```

**Xuất 2 vỉ:**
```json
POST /api/v3/warehouse/export
{
  "items": [
    {
      "itemMasterId": 24,
      "quantity": 2,
      "unitId": 2  // Vỉ
    }
  ]
}
```

Backend tự động:
1. Convert: 2 Vỉ × 10 = 20 Viên
2. Lấy từ batch #197 (10 viên) + batch #198 (10 viên)
3. Update parent batch #196: `quantity_on_hand -= 20`

---

### Use Case 4: Kiểm tra tồn kho thấp (LOW_STOCK)

**Request:**
```
GET /api/v3/warehouse/summary?stockStatus=LOW_STOCK
```

**Backend xử lý:**
```sql
SELECT * FROM item_masters im
LEFT JOIN item_batches ib ON im.item_master_id = ib.item_master_id
GROUP BY im.item_master_id
HAVING SUM(ib.quantity_on_hand) < im.min_stock_level
```

**Response:**
```json
{
  "items": [
    {
      "itemCode": "DP002",
      "itemName": "Lidocaine 2%",
      "totalQuantity": 5,
      "minStockLevel": 20,
      "stockStatus": "LOW_STOCK"
    }
  ]
}
```

---

## 7. TESTING & SEED DATA

### 7.1. Seed Data Location
```
src/main/resources/db/dental-clinic-seed-data.sql
```

### 7.2. Dữ liệu quan trọng

#### **Item Units (24 records, lines 3242-3287)**
```sql
-- Amoxicillin: Hộp → Vỉ → Viên
(1, 24, 'Hộp', 100, FALSE, 1),
(2, 24, 'Vỉ', 10, FALSE, 2),
(3, 24, 'Viên', 1, TRUE, 3),

-- Găng tay: Thùng → Hộp → Đôi
(10, 16, 'Thùng', 1000, FALSE, 1),
(11, 16, 'Hộp', 100, FALSE, 2),
(12, 16, 'Đôi', 1, TRUE, 3)
```

---

#### **Parent-Child Batches (28 records, lines 3961-4016)**

**Amoxicillin (item 24):**
- Batch #196: Parent (Hộp 100 viên)
- Batches #197-206: 10 children (mỗi vỉ 10 viên)

**Lidocaine (item 23):**
- Batch #207: Parent (Hộp 50 ống)
- Batches #208-212: 5 children (mỗi ống)

**Găng tay (item 16):**
- Batch #213: Parent (Thùng 1000 chiếc)
- Batches #214-223: 10 children (mỗi hộp 100 chiếc)

---

#### **Transactions (6 records, lines 4027-4058)**
- Transactions #92-94: Import parent batches
- Transactions #95-97: Export child units (demo FEFO + unit-based)

---

#### **UPDATE Statements (lines 4458-4502)**
```sql
-- 🔥 Populate item_code for all transaction_items
UPDATE storage_transaction_items sti
SET item_code = (
    SELECT im.item_code
    FROM item_batches ib
    JOIN item_masters im ON ib.item_master_id = im.item_master_id
    WHERE ib.batch_id = sti.batch_id
);

-- Link child batches to parents
UPDATE item_batches SET parent_batch_id = 196 WHERE batch_id BETWEEN 197 AND 206;
UPDATE item_batches SET parent_batch_id = 207 WHERE batch_id BETWEEN 208 AND 212;
UPDATE item_batches SET parent_batch_id = 213 WHERE batch_id BETWEEN 214 AND 223;

-- Set expiry_date for NULL values
UPDATE item_batches SET expiry_date = CURRENT_DATE + INTERVAL '10 years'
WHERE expiry_date IS NULL AND item_master_id IN (...tools...);

UPDATE item_batches SET expiry_date = CURRENT_DATE + INTERVAL '5 years'
WHERE expiry_date IS NULL;
```

---

### 7.3. Testing Checklist

#### **API Tests:**
- [ ] Import với expiry_date NULL → Expect 400 Error
- [ ] Import với unitId valid → Success với unitName trong response
- [ ] Export FEFO: Lấy batch sắp hết hạn trước
- [ ] Export không đủ stock → Expect InsufficientStockException
- [ ] Get inventory summary: Check tính toán stockStatus
- [ ] Get item units: Verify displayOrder (Hộp → Vỉ → Viên)
- [ ] Convert quantity: 2 Hộp → 200 Viên

#### **Database Tests:**
- [ ] Insert batch với expiry_date NULL → Constraint violation
- [ ] Parent-child relationship: batch #197 có parent_batch_id = 196
- [ ] item_code auto-populated trong transaction_items

---

## 8. LƯU Ý QUAN TRỌNG KHI MAINTAIN

### 8.1. KHÔNG BAO GIỜ được làm

❌ **Xóa validation expiry_date NOT NULL**
- Mentor feedback: TẤT CẢ items phải có HSD
- Compliance với quy định quản lý thiết bị y tế

❌ **Bỏ qua FEFO trong export**
- Chuẩn ERP: Hàng sắp hết hạn phải xuất trước
- Tránh lãng phí do quá hạn

❌ **Quên auto-populate item_code**
- Warehouse staff cần item_code trên mọi phiếu
- Không được để NULL

❌ **Hard-delete batches có quantity > 0**
- Soft-delete hoặc validate quantity = 0 trước khi xóa

---

### 8.2. Best Practices

✅ **Transaction Management:**
```java
@Transactional  // Luôn dùng cho import/export
public TransactionResponse importItems(ImportRequest request) {
    // Nếu có Exception → Auto rollback
}
```

✅ **DTO Mapping:**
```java
// Luôn populate itemCode + unitName
.itemCode(item.getBatch().getItemMaster().getItemCode())
.unitName(item.getUnit() != null ? item.getUnit().getUnitName() : null)
```

✅ **Error Handling:**
```java
// Custom exceptions cho business logic
if (batch.getQuantityOnHand() < quantity) {
    throw new InsufficientStockException(itemName, quantity, available);
}
```

✅ **Logging:**
```java
log.info("Import transaction created: {} with {} items", code, items.size());
log.warn("Low stock detected: {} (current: {}, min: {})", itemName, current, min);
```

---

### 8.3. Performance Optimization

**Nên làm:**
1. **Eager fetch cho dropdown data:**
   ```java
   @EntityGraph(attributePaths = {"category", "supplier"})
   List<ItemMaster> findAll();
   ```

2. **Pagination cho inventory summary:**
   ```java
   Page<InventorySummaryResponse> getInventorySummary(Pageable pageable);
   ```

3. **Cache cho static data:**
   ```java
   @Cacheable("categories")
   List<ItemCategory> getAllCategories();
   ```

4. **Batch insert cho import nhiều items:**
   ```java
   @Modifying
   @Query("INSERT INTO ...")
   void batchInsert(List<...> items);
   ```

---

## 9. ROADMAP & TODO

### 9.1. Completed Features ✅

- [x] Database schema với 8 tables
- [x] Item units hierarchy (Hộp/Vỉ/Viên)
- [x] Parent-child batch tracking
- [x] Expiry date mandatory validation
- [x] item_code auto-populate
- [x] FEFO algorithm
- [x] Import/Export APIs
- [x] Inventory summary với stockStatus
- [x] Transaction history
- [x] ItemUnitService + APIs
- [x] Seed data (24 units, 28 parent-child batches)

---

### 9.2. Pending Tasks ⏳

#### **High Priority:**
1. **Batch Adjustment API** (Điều chỉnh tồn kho)
   - Trường hợp: Kiểm kê phát hiện sai lệch
   - Endpoint: `POST /api/v3/warehouse/adjustment`
   - Logic: Tạo transaction type ADJUSTMENT (cần thêm vào enum)

2. **Expiry Alert API** (Cảnh báo sắp hết hạn)
   - Endpoint: `GET /api/v3/warehouse/alerts/expiring?days=30`
   - Logic: Query batches có `expiry_date < NOW() + 30 days`

3. **Transaction History Filter** (Lịch sử giao dịch)
   - Endpoint: `GET /api/v3/warehouse/transactions`
   - Filters: `type`, `dateFrom`, `dateTo`, `itemMasterId`, `supplierId`

4. **Item Master CRUD APIs** (Đã có Controller, chưa implement Service)
   - `POST /api/v3/warehouse/items`
   - `PUT /api/v3/warehouse/items/{id}`
   - `DELETE /api/v3/warehouse/items/{id}`

---

#### **Medium Priority:**
5. **Barcode/QR Support**
   - Thêm field `barcode` vào `item_masters`
   - API scan barcode → trả về item info

6. **Batch Transfer** (Chuyển kho)
   - VD: Chuyển từ kho tổng → kho phòng khám
   - Endpoint: `POST /api/v3/warehouse/transfer`

7. **Supplier Performance Report**
   - Thống kê chất lượng nhà cung cấp
   - Số lần nhập, tỷ lệ hàng lỗi, thời gian giao hàng

---

#### **Low Priority:**
8. **Advanced Analytics**
   - Dự báo nhu cầu nhập kho (ML)
   - Tối ưu mức tồn kho (min/max)
   - ABC Analysis (phân loại vật tư theo giá trị)

9. **Mobile App Support**
   - API scan QR để nhập/xuất nhanh
   - Push notification cho expiry alerts

---

### 9.3. Code Quality Improvements

- [ ] Unit tests cho Service layer (coverage 80%+)
- [ ] Integration tests cho API endpoints
- [ ] API documentation với OpenAPI/Swagger
- [ ] Validation messages i18n (Vietnamese/English)
- [ ] Audit log cho sensitive operations (DELETE, ADJUSTMENT)

---

## 10. CONTACT & SUPPORT

**Người bàn giao:** ThanhCQ1  
**Email:** [your-email]  
**Teams/Slack:** [your-handle]

**Tài liệu liên quan:**
- `WAREHOUSE_API_INTEGRATION_GUIDE.md` - API specs chi tiết
- `FE_WAREHOUSE_INTEGRATION_CHECKLIST.md` - Frontend checklist
- `CRONJOB_TEST_API_GUIDE.md` - Scheduled jobs (nếu có)

**Cách debug:**
1. Check logs: `log.info` / `log.warn` / `log.error`
2. Database: Query `item_batches` để verify quantity
3. Breakpoint: `StorageInOutService` line 89 (expiry validation)
4. Seed data: Re-run SQL script nếu data bị lỗi

---

## 📝 APPENDIX: Quick Commands

### Maven Build
```bash
mvn clean install -DskipTests
```

### Run Application
```bash
mvn spring-boot:run
```

### Database Reset (Local)
```sql
-- Drop all warehouse tables
DROP TABLE storage_transaction_items CASCADE;
DROP TABLE storage_transactions CASCADE;
DROP TABLE item_batches CASCADE;
DROP TABLE item_units CASCADE;
DROP TABLE supplier_items CASCADE;
DROP TABLE item_masters CASCADE;
DROP TABLE suppliers CASCADE;
DROP TABLE item_categories CASCADE;

-- Re-run seed data
\i src/main/resources/db/dental-clinic-seed-data.sql
```

### Git Workflow
```bash
# Pull latest changes
git pull origin warehouse

# Create feature branch
git checkout -b feature/warehouse-adjustment-api

# Commit with prefix
git commit -m "feat(warehouse): Add batch adjustment API"

# Push to remote
git push origin feature/warehouse-adjustment-api
```

---

**Chúc bạn maintain thành công! 🚀**

Nếu có thắc mắc, cứ hỏi ThanhCQ1 hoặc tham khảo seed data để hiểu luồng nghiệp vụ.
