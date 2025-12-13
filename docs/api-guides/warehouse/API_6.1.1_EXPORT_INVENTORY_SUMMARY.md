# API 6.1.1 - Export Inventory Summary to Excel

## Overview

API export báo cáo tồn kho ra file Excel (.xlsx) với formatting đặc biệt để highlight các item có tồn kho thấp hoặc hết hàng.

## Endpoint

```
GET /api/v1/warehouse/summary/export
```

## Features

### 1. Bold Formatting for Low Stock Items

Các item có `stockStatus` = `LOW_STOCK` hoặc `OUT_OF_STOCK` sẽ được in **đậm (bold)** toàn bộ row để dễ nhận diện.

### 2. Filters Support

API support tất cả filters giống như API 6.1:

- `search` - Tìm kiếm theo tên hoặc mã item
- `stockStatus` - Lọc theo trạng thái (OUT_OF_STOCK, LOW_STOCK, NORMAL, OVERSTOCK)
- `warehouseType` - Lọc theo loại kho (COLD, NORMAL)
- `categoryId` - Lọc theo danh mục

### 3. Export All Data

API sẽ export **TẤT CẢ** data match với filters (không phân trang).

## Request

### Method

```
GET
```

### Headers

```
Authorization: Bearer <access_token>
```

### Query Parameters

| Parameter     | Type   | Required | Description                                | Example   |
| ------------- | ------ | -------- | ------------------------------------------ | --------- |
| search        | String | No       | Tìm kiếm theo tên/mã item                  | "gạc"     |
| stockStatus   | Enum   | No       | OUT_OF_STOCK, LOW_STOCK, NORMAL, OVERSTOCK | LOW_STOCK |
| warehouseType | Enum   | No       | COLD, NORMAL                               | NORMAL    |
| categoryId    | Long   | No       | ID danh mục                                | 1         |

### Example Request

```http
GET /api/v1/warehouse/summary/export?stockStatus=LOW_STOCK&warehouseType=NORMAL
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

## Response

### Success (200 OK)

```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename=inventory_summary.xlsx
```

Returns binary file (.xlsx)

### Excel Structure

#### Headers (Bold, Dark Blue Background, White Text)

| Column              | Description           |
| ------------------- | --------------------- |
| STT                 | Số thứ tự             |
| Item Code           | Mã vật tư             |
| Item Name           | Tên vật tư            |
| Category            | Danh mục              |
| Warehouse Type      | Loại kho              |
| Unit                | Đơn vị                |
| Total Quantity      | Tổng số lượng         |
| Min Stock           | Mức tồn tối thiểu     |
| Max Stock           | Mức tồn tối đa        |
| Stock Status        | Trạng thái            |
| Nearest Expiry Date | Ngày hết hạn gần nhất |

#### Data Rows

**Normal items** (NORMAL, OVERSTOCK):

- Regular font
- Black text

**Low/Out of stock items** (LOW_STOCK, OUT_OF_STOCK):

- **BOLD font** (toàn bộ row)
- Black text
- Dễ dàng nhận diện để xử lý gấp

### Example Excel Output

```
STT | Item Code | Item Name           | Total | Stock Status
----+-----------+---------------------+-------+--------------
1   | VT-001    | Gạc y tế           | 120   | NORMAL
2   | VT-002    | Bông gòn y tế      | 25    | LOW_STOCK     (BOLD)
3   | VT-003    | Băng keo y tế      | 0     | OUT_OF_STOCK  (BOLD)
4   | VT-004    | Găng tay y tế      | 300   | OVERSTOCK
```

## Permissions Required

```java
@PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_WAREHOUSE')")
```

Users need one of:

- Role: `ADMIN`
- Permission: `VIEW_WAREHOUSE`

## Business Rules

### BR-01: Export All Matching Data

- Không áp dụng pagination
- Export TẤT CẢ items match với filters

### BR-02: Bold Formatting for Urgent Items

- Items với `stockStatus = LOW_STOCK` → BOLD
- Items với `stockStatus = OUT_OF_STOCK` → BOLD
- Giúp warehouse keeper dễ dàng identify items cần xử lý gấp

### BR-03: Frozen Header

- Header row bị "đóng băng" (freeze pane)
- Khi scroll xuống vẫn thấy header

### BR-04: Auto-sized Columns

- Tất cả columns tự động adjust width theo content

## Implementation Details

### Service Layer

```java
// WarehouseExcelExportService.java

public byte[] exportInventorySummary(InventorySummaryResponse response) {
    // Create bold styles for low/out of stock items
    CellStyle boldDataStyle = createBoldDataStyle(workbook);
    CellStyle boldNumberStyle = createBoldNumberStyle(workbook);

    // Check stock status and apply bold if needed
    boolean isLowOrOutOfStock = item.getStockStatus() == StockStatus.LOW_STOCK ||
                                 item.getStockStatus() == StockStatus.OUT_OF_STOCK;

    CellStyle rowDataStyle = isLowOrOutOfStock ? boldDataStyle : dataStyle;
    CellStyle rowNumberStyle = isLowOrOutOfStock ? boldNumberStyle : numberStyle;

    // Apply to all cells in the row
    createCell(row, 1, item.getItemCode(), rowDataStyle);
    createCell(row, 2, item.getItemName(), rowDataStyle);
    // ...
}
```

### Controller Layer

```java
// WarehouseInventoryController.java

@GetMapping("/summary/export")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_WAREHOUSE')")
public ResponseEntity<byte[]> exportInventorySummary(
    @RequestParam(required = false) String search,
    @RequestParam(required = false) StockStatus stockStatus,
    @RequestParam(required = false) WarehouseType warehouseType,
    @RequestParam(required = false) Long categoryId) {

    // Get ALL data (no pagination)
    Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
    InventorySummaryResponse response = inventoryService.getInventorySummaryV2(
        search, stockStatus, warehouseType, categoryId, pageable);

    // Generate Excel
    byte[] excelBytes = excelExportService.exportInventorySummary(response);

    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=inventory_summary.xlsx")
        .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .body(excelBytes);
}
```

## Testing Guide

### Test Case 1: Export All Items

```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/summary/export" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output inventory_summary.xlsx
```

**Expected:**

- Download file `inventory_summary.xlsx`
- Contains all inventory items
- Low stock items are BOLD

### Test Case 2: Export Only Low Stock Items

```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/summary/export?stockStatus=LOW_STOCK" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output low_stock_items.xlsx
```

**Expected:**

- Only LOW_STOCK items
- All rows are BOLD

### Test Case 3: Export with Search Filter

```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/summary/export?search=gạc" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output search_results.xlsx
```

**Expected:**

- Items matching "gạc" in name or code
- Low stock items within results are BOLD

### Test Case 4: Export by Warehouse Type

```bash
curl -X GET "http://localhost:8080/api/v1/warehouse/summary/export?warehouseType=NORMAL&categoryId=1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output normal_warehouse.xlsx
```

**Expected:**

- Only NORMAL warehouse items
- Category 1 items only
- Low stock items are BOLD

## Verification Checklist

- [ ] Downloaded Excel file opens successfully
- [ ] Headers are bold with dark blue background
- [ ] LOW_STOCK items display with BOLD text
- [ ] OUT_OF_STOCK items display with BOLD text
- [ ] NORMAL items display with regular text
- [ ] Columns are auto-sized properly
- [ ] Header row is frozen (stays visible when scrolling)
- [ ] Filters (search, stockStatus, warehouseType, categoryId) work correctly
- [ ] All data exported (not paginated)
- [ ] Permission check works (403 without VIEW_WAREHOUSE)

## Error Responses

### 401 Unauthorized

```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

### 403 Forbidden

```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

**Cause:** User không có role ADMIN hoặc permission VIEW_WAREHOUSE

### 500 Internal Server Error

```json
{
  "error": "Internal Server Error",
  "message": "Failed to export inventory summary: ..."
}
```

**Cause:** Lỗi khi generate Excel file

## Related APIs

- **API 6.1** - GET /api/v1/warehouse/summary (Get paginated inventory)
- **API 6.3.1** - GET /api/v1/warehouse/alerts/expiring/export (Export expiring items)
- **API 6.6.1** - GET /api/v1/warehouse/transactions/export (Export transactions)

## Notes

### Performance Considerations

- API export ALL data → Có thể chậm nếu có nhiều items (>1000)
- Recommend: Sử dụng filters để giảm data size
- Warehouse keeper nên export theo:
  - `stockStatus=LOW_STOCK` → Chỉ export items cần xử lý
  - `warehouseType=NORMAL` + `stockStatus=LOW_STOCK` → Export specific warehouse

### Excel Formatting

- Bold formatting chỉ apply cho LOW_STOCK và OUT_OF_STOCK
- Không có màu background (chỉ bold text)
- Dễ print và đọc trên giấy

### File Size

- Typical: 50-100 KB cho 100-500 items
- Large: 200-500 KB cho 1000-2000 items

## Changelog

### 2025-12-11 - Initial Implementation

- Added Excel export with bold formatting for low/out of stock items
- Support all filters from API 6.1
- Export all data (no pagination)
- Auto-sized columns and frozen header row

---

**Status:** Implemented and Tested
**Version:** 1.0
**Last Updated:** 2025-12-11
