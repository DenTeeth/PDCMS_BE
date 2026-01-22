# API 6.6.5: Update Warehouse Transaction Notes

## ✅ RESOLVED - New Endpoint Implemented

### Root Cause
The `PUT /api/v1/warehouse/transactions/{id}` endpoint **does not exist** in the backend. The 500 error occurred because the FE was calling a non-existent endpoint. There was no endpoint to update transaction notes.

---

## 📝 API Specification

### Endpoint
```
PATCH /api/v1/warehouse/transactions/{id}/notes
```

### HTTP Method
`PATCH`

### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Long | ✅ Yes | ID của phiếu giao dịch kho (Transaction ID) |

### Request Headers
```
Content-Type: application/json
Authorization: Bearer {token}
```

### Request Body
```json
{
  "notes": "string"
}
```

**Schema**:
- `notes` (String, Optional): Ghi chú cho phiếu kho. Có thể là chuỗi rỗng hoặc null.

### Response Status Codes
| Code | Description |
|------|-------------|
| 200 | Cập nhật ghi chú thành công |
| 400 | Transaction không tồn tại |
| 401 | Chưa xác thực |
| 403 | Không có quyền UPDATE_WAREHOUSE |

### Success Response (200 OK)

**For IMPORT Transaction**:
```json
{
  "statusCode": 200,
  "message": "Cập nhật ghi chú thành công",
  "data": {
    "transactionId": 6,
    "transactionCode": "PN-20260122-001",
    "transactionType": "IMPORT",
    "transactionDate": "2026-01-22",
    "approvalStatus": "APPROVED",
    "invoiceNumber": "INV-2026-001",
    "supplierId": 1,
    "supplierName": "ABC Medical Supplies",
    "notes": "Updated notes here",
    "totalValue": 5000000,
    "paidAmount": 2000000,
    "remainingDebt": 3000000,
    "paymentStatus": "PARTIAL",
    "createdBy": "EMP001",
    "createdAt": "2026-01-22T08:30:00",
    "approvedBy": "EMP002",
    "approvedAt": "2026-01-22T09:00:00",
    "items": [
      {
        "transactionItemId": 10,
        "itemMasterId": 5,
        "itemCode": "MAT-001",
        "itemName": "Composite Resin",
        "unitName": "syringe",
        "lotNumber": "BATCH-2026-001",
        "quantityChange": 10,
        "unitPrice": 500000,
        "totalLineValue": 5000000,
        "expiryDate": "2027-12-31",
        "batchId": 15,
        "currentQuantityOnHand": 25
      }
    ]
  }
}
```

**For EXPORT Transaction**:
```json
{
  "statusCode": 200,
  "message": "Cập nhật ghi chú thành công",
  "data": {
    "transactionId": 7,
    "transactionCode": "PX-20260122-002",
    "transactionType": "EXPORT",
    "transactionDate": "2026-01-22",
    "approvalStatus": "APPROVED",
    "appointmentId": 123,
    "patientId": 456,
    "patientName": "Nguyen Van A",
    "notes": "Exported for treatment",
    "createdBy": "EMP003",
    "createdAt": "2026-01-22T10:15:00",
    "items": [
      {
        "transactionItemId": 20,
        "itemMasterId": 5,
        "itemCode": "MAT-001",
        "itemName": "Composite Resin",
        "unitName": "syringe",
        "lotNumber": "BATCH-2026-001",
        "quantityChange": -2,
        "expiryDate": "2027-12-31",
        "batchId": 15,
        "currentQuantityOnHand": 23
      }
    ]
  }
}
```

### Error Response (400 Bad Request)
```json
{
  "statusCode": 400,
  "message": "Transaction with ID 999 not found",
  "error": "TRANSACTION_NOT_FOUND"
}
```

---

## 🔧 Implementation Details

### Backend Changes Made

#### 1. Created DTO
**File**: `src/main/java/com/dental/clinic/management/warehouse/dto/request/UpdateTransactionNotesRequest.java`

```java
package com.dental.clinic.management.warehouse.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionNotesRequest {
    private String notes;
}
```

#### 2. Added Controller Method
**File**: `src/main/java/com/dental/clinic/management/warehouse/controller/TransactionHistoryController.java`

```java
/**
 * API 6.6.5: Update Transaction Notes
 */
@PatchMapping("/transactions/{id}/notes")
@PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('UPDATE_WAREHOUSE')")
@Operation(summary = "Cập nhật ghi chú phiếu kho", description = """
    Cập nhật ghi chú cho phiếu giao dịch kho.
    
    **Business Logic:**
    - Cho phép cập nhật notes ở mọi trạng thái
    - Không thay đổi trạng thái phiếu
    - Không ảnh hưởng đến tồn kho
    - Notes có thể để trống
    
    **Permissions:**
    - UPDATE_WAREHOUSE: Quyền cập nhật phiếu kho
    """)
@ApiMessage("Cập nhật ghi chú thành công")
public ResponseEntity<?> updateTransactionNotes(
    @Parameter(description = "ID của phiếu giao dịch") @PathVariable Long id,
    @RequestBody UpdateTransactionNotesRequest request) {
    
    log.info("PATCH /api/v1/warehouse/transactions/{}/notes - Update notes", id);
    
    Object response = transactionHistoryService.updateTransactionNotes(id, request.getNotes());
    
    log.info("Transaction notes updated - ID: {}", id);
    
    return ResponseEntity.ok(response);
}
```

#### 3. Implemented Service Method
**File**: `src/main/java/com/dental/clinic/management/warehouse/service/TransactionHistoryService.java`

```java
/**
 * Update transaction notes (API 6.6.5)
 */
@Transactional
public Object updateTransactionNotes(Long id, String notes) {
    log.info("Updating transaction notes - ID: {}", id);
    
    StorageTransaction transaction = transactionRepository.findById(id)
        .orElseThrow(() -> new BadRequestException(
            "TRANSACTION_NOT_FOUND",
            "Transaction with ID " + id + " not found"));
    
    // Update notes (can be null or empty)
    transaction.setNotes(notes);
    transactionRepository.save(transaction);
    
    log.info("Transaction notes updated - ID: {}, Code: {}",
        id, transaction.getTransactionCode());
    
    boolean hasViewCostPermission = hasPermission(AuthoritiesConstants.VIEW_WAREHOUSE_COST);
    return mapToDetailResponse(transaction, hasViewCostPermission);
}
```

---

## 💻 Frontend Integration Guide

### Update Storage Service
**File**: `src/services/storageService.ts`

**Before**:
```typescript
updateNotes: async (id: number, notes: string): Promise<StorageTransactionV3> => {
  const notesValue = notes || '';
  
  // ❌ This was calling a non-existent endpoint
  const response = await api.put(`${TRANSACTION_BASE}/${id}`, { notes: notesValue }, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  return response.data.data;
}
```

**After**:
```typescript
updateNotes: async (id: number, notes: string): Promise<StorageTransactionV3> => {
  const response = await api.patch(
    `${TRANSACTION_BASE}/${id}/notes`,  // ✅ Changed to /notes endpoint
    { notes: notes || '' },
    { headers: { 'Content-Type': 'application/json' } }
  );
  return response.data.data;
}
```

### Usage Example in React Component

```typescript
const handleUpdateNotes = async (transactionId: number, newNotes: string) => {
  try {
    const updated = await storageService.updateNotes(transactionId, newNotes);
    console.log('Notes updated successfully:', updated);
    message.success('Cập nhật ghi chú thành công');
    // Refresh the transaction list or detail
  } catch (error) {
    console.error('Failed to update notes:', error);
    message.error('Không thể cập nhật ghi chú');
  }
};
```

---

## 🧪 Testing with Postman/cURL

### Test Case 1: Update Notes with Content

**Request**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/warehouse/transactions/6/notes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "notes": "Đã kiểm tra hàng hóa, chất lượng tốt"
  }'
```

**Expected Response**: 200 OK with full transaction details

### Test Case 2: Update Notes with Empty String

**Request**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/warehouse/transactions/6/notes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "notes": ""
  }'
```

**Expected Response**: 200 OK (notes set to empty string)

### Test Case 3: Update Notes for Non-existent Transaction

**Request**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/warehouse/transactions/9999/notes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "notes": "Test"
  }'
```

**Expected Response**: 400 Bad Request
```json
{
  "statusCode": 400,
  "message": "Transaction with ID 9999 not found",
  "error": "TRANSACTION_NOT_FOUND"
}
```

---

## 📊 Business Logic

### Features
- ✅ Allows updating notes at **any transaction status** (DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, CANCELLED)
- ✅ Does **not change** transaction status
- ✅ Does **not affect** inventory quantities
- ✅ Notes can be empty string or null
- ✅ Requires `UPDATE_WAREHOUSE` permission
- ✅ Returns full transaction details after update
- ✅ Respects `VIEW_COST` permission for financial data visibility

### Use Cases
1. **Adding context**: Thêm ghi chú về chất lượng hàng hóa sau khi nhận
2. **Documentation**: Ghi lại lý do xuất kho đặc biệt
3. **Tracking**: Theo dõi vấn đề phát sinh trong quá trình giao dịch
4. **Clear notes**: Xóa ghi chú không còn cần thiết (set empty string)

---

## 🔐 Permissions Required

| Permission | Required | Description |
|------------|----------|-------------|
| `UPDATE_WAREHOUSE` | ✅ Yes | Quyền cập nhật thông tin phiếu kho |
| `VIEW_WAREHOUSE` | Auto | Tự động có khi có UPDATE_WAREHOUSE |
| `VIEW_COST` | Optional | Nếu có: xem thông tin tài chính (unitPrice, totalValue, debt) |

---

## 📚 Related Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/v1/warehouse/transactions` | List all transactions |
| GET | `/api/v1/warehouse/transactions/{id}` | Get transaction detail |
| POST | `/api/v1/warehouse/transactions/{id}/approve` | Approve transaction |
| POST | `/api/v1/warehouse/transactions/{id}/reject` | Reject transaction |
| POST | `/api/v1/warehouse/transactions/{id}/cancel` | Cancel transaction |
| **PATCH** | **`/api/v1/warehouse/transactions/{id}/notes`** | **Update notes (NEW)** |

---

## 📁 Files Modified/Created

### Created
- [UpdateTransactionNotesRequest.java](src/main/java/com/dental/clinic/management/warehouse/dto/request/UpdateTransactionNotesRequest.java)

### Modified
- [TransactionHistoryController.java](src/main/java/com/dental/clinic/management/warehouse/controller/TransactionHistoryController.java) - Added `updateTransactionNotes()` method
- [TransactionHistoryService.java](src/main/java/com/dental/clinic/management/warehouse/service/TransactionHistoryService.java) - Implemented `updateTransactionNotes()` service method

---

**Status**: ✅ RESOLVED  
**API Version**: 6.6.5  
**Feature**: Update Warehouse Transaction Notes  
**Priority**: 🔴 High (blocking feature)  
**Created**: 2026-01-22  
**Resolved**: 2026-01-22  
**Developer**: Backend Team

---

## 📋 Appendix: Original Problem & Resolution Details

<details>
<summary>Click to expand original problem description and how each question was resolved</summary>

### What FE Was Trying to Call
```
PUT /api/v1/warehouse/transactions/{id}
```
**Status**: ❌ Endpoint did not exist → 500 Internal Server Error

### Symptoms
When attempting to update notes for warehouse transactions (import/export receipts) via `PUT /api/v1/warehouse/transactions/{id}`, the API consistently returns a **500 Internal Server Error**, regardless of whether notes are sent in the request body or as query parameters.

### Console Logs from FE
```
[updateNotes] Starting update: {id: 6, notesLength: 0, notesPreview: ''}
[updateNotes] Attempt 1: Sending notes in request body
PUT /api/v1/warehouse/transactions/6
Status: 500 Internal Server Error
Message: "Lỗi hệ thống nội bộ"

[updateNotes] Attempt 2: Retrying with query params...
PUT /api/v1/warehouse/transactions/6?notes=
Status: 500 Internal Server Error
Message: "Lỗi hệ thống nội bộ"
```

### Original FE Implementation
```typescript
updateNotes: async (id: number, notes: string): Promise<StorageTransactionV3> => {
  const notesValue = notes || '';
  
  // Attempt 1: Send notes in request body
  const response = await api.put(`${TRANSACTION_BASE}/${id}`, { notes: notesValue }, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  return response.data.data;
}
```

---

### ✅ Resolution: All Questions Answered

#### Question 1: Expected Request Body Format?
**Asked**: "Only `{ notes: string }`? Full transaction object? Partial update?"  
**Answer**: ✅ **Only `{ notes: string }` required** - Simple partial update, no other fields needed

#### Question 2: Are Price Fields Required?
**Asked**: "Are `unitPrice`, `totalLineValue` required when updating notes?"  
**Answer**: ✅ **NO** - Price fields not required at all, only notes field needed

#### Question 3: Why 500 Instead of 400?
**Asked**: "Is this a validation error? Database constraint violation?"  
**Answer**: ✅ **500 because the endpoint didn't exist** - Now returns proper 400 for invalid transaction ID, 403 for missing permission

#### Question 4: Required vs Optional Fields?
**Asked**: "Can we update only notes? Need all transaction data?"  
**Answer**: ✅ **Only notes field required** - No other transaction data needed, notes can even be null/empty

#### Question 5: Separate Endpoint Needed?
**Asked**: "Should we use `PATCH /api/v1/warehouse/transactions/{id}/notes`?"  
**Answer**: ✅ **YES - Implemented exactly this** - New dedicated endpoint for updating notes only

---

### ✅ Hypotheses Resolution

#### Hypothesis 1: Missing Required Fields
**Theory**: Endpoint requires full transaction object  
**Reality**: ✅ **RESOLVED** - No full object needed, endpoint didn't exist at all

#### Hypothesis 2: Price Fields Validation
**Theory**: `unitPrice` and `totalLineValue` required even for notes update  
**Reality**: ✅ **RESOLVED** - Price fields not required, new endpoint only needs notes

#### Hypothesis 3: Backend Validation Error
**Theory**: Validation fails silently, returns 500 instead of 400  
**Reality**: ✅ **RESOLVED** - Now returns proper error codes: 400 (not found), 403 (no permission), 200 (success)

---

### ✅ Solution Chosen: Option 3 - Separate Notes Endpoint

From the 3 proposed solutions:
1. ❌ FE Sends Full Transaction Object - Rejected (too much data, error-prone)
2. ❌ BE Accepts Partial Update - Not chosen (would need full PUT endpoint implementation)
3. ✅ **Separate Notes Endpoint - IMPLEMENTED** - Clean, focused, RESTful approach

**Result**: `PATCH /api/v1/warehouse/transactions/{id}/notes` endpoint created

</details>

---

## 💡 Summary

This API endpoint was created to resolve a critical issue where the frontend was unable to update warehouse transaction notes because the backend had no endpoint for this functionality. The new `PATCH /api/v1/warehouse/transactions/{id}/notes` endpoint provides a clean, RESTful way to update notes without affecting transaction status or inventory.
