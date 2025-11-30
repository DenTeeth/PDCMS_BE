# API 6.17: Get Service Consumables - Complete Implementation Guide

**Version**: V30  
**Author**: Backend Team  
**Date**: November 30, 2025  
**Status**: IMPLEMENTED

---

## Overview

API 6.17 returns the **Bill of Materials (BOM)** for a dental service - list of consumable items required with real-time stock availability and cost information. This API answers critical questions:

1. **Can we perform this service?** (Stock availability check)
2. **How much does this service cost in materials?** (COGS calculation)
3. **What items do we need to prepare?** (Material planning)

**Key Features**:
- Real-time stock availability check (OK, LOW, OUT_OF_STOCK)
- Total consumable cost calculation (COGS)
- Warning flag for insufficient stock
- Lean response (only essential fields)

---

## Business Rules

### BR-1: Stock Status Logic
```
OUT_OF_STOCK: currentStock <= 0
LOW: currentStock < requiredQuantity (not enough for this service)
OK: currentStock >= requiredQuantity (sufficient)
```

### BR-2: Cost Calculation
- `unitPrice`: Latest market price from `item_masters.current_market_price`
- `totalCost`: `quantity × unitPrice`
- `totalConsumableCost`: Sum of all item totalCosts

### BR-3: Warning Flag
- `hasInsufficientStock = true`: If ANY item has status LOW or OUT_OF_STOCK
- Use this to block appointment booking or show warning to staff

---

## API Specification

### Endpoint
```
GET /api/v1/warehouse/consumables/services/{serviceId}
```

### Authorization
```java
@PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('VIEW_WAREHOUSE', 'VIEW_SERVICES')")
```

**Who can use**:
- Warehouse Manager (VIEW_WAREHOUSE)
- Doctors/Dentists (VIEW_SERVICES)  
- Receptionists (VIEW_SERVICES)
- Admin (all permissions)

### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| serviceId | Long | Yes | Service ID (from `services` table) |

### Request Headers
| Header | Value | Required |
|--------|-------|----------|
| Authorization | Bearer {token} | Yes |

---

## Success Response (200 OK)

### Example: Service with Sufficient Stock

**Request**:
```bash
curl -X GET http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "statusCode": 200,
  "message": "Consumables retrieved successfully",
  "data": {
    "serviceId": 1,
    "serviceName": "Kham tong quat & Tu van",
    "totalConsumableCost": 7000.00,
    "hasInsufficientStock": false,
    "consumables": [
      {
        "itemMasterId": 1,
        "itemCode": "CON-GLOVE-01",
        "itemName": "Gang tay y te",
        "quantity": 1.00,
        "unitName": "Đôi",
        "currentStock": 500,
        "stockStatus": "OK",
        "unitPrice": 5000.00,
        "totalCost": 5000.00
      },
      {
        "itemMasterId": 2,
        "itemCode": "CON-MASK-01",
        "itemName": "Khau trang y te",
        "quantity": 1.00,
        "unitName": "Cái",
        "currentStock": 800,
        "stockStatus": "OK",
        "unitPrice": 2000.00,
        "totalCost": 2000.00
      }
    ]
  }
}
```

### Example: Service with Insufficient Stock (Warning)

**Request**:
```bash
curl -X GET http://localhost:8080/api/v1/warehouse/consumables/services/3 \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "statusCode": 200,
  "message": "Consumables retrieved successfully",
  "data": {
    "serviceId": 3,
    "serviceName": "Tram rang Composite",
    "totalConsumableCost": 125000.00,
    "hasInsufficientStock": true,
    "consumables": [
      {
        "itemMasterId": 10,
        "itemCode": "MAT-COMP-01",
        "itemName": "Tram Composite",
        "quantity": 8.00,
        "unitName": "g",
        "currentStock": 150,
        "stockStatus": "OK",
        "unitPrice": 15000.00,
        "totalCost": 120000.00
      },
      {
        "itemMasterId": 5,
        "itemCode": "CON-GAUZE-01",
        "itemName": "Bong gac phau thuat",
        "quantity": 2.00,
        "unitName": "Gói",
        "currentStock": 1,
        "stockStatus": "LOW",
        "unitPrice": 2500.00,
        "totalCost": 5000.00
      }
    ]
  }
}
```

---

## Error Responses

### 404 Not Found - Service Does Not Exist

**Condition**: Service ID not found in `services` table

**Response**:
```json
{
  "timestamp": "2025-11-30T10:30:00.123Z",
  "status": 404,
  "error": "Not Found",
  "message": "Service not found with id: 999",
  "path": "/api/v1/warehouse/consumables/services/999"
}
```

**Exception**: `ServiceNotFoundException`

---

### 404 Not Found - No Consumables Defined

**Condition**: Service exists but no consumables configured in `service_consumables` table

**Response**:
```json
{
  "timestamp": "2025-11-30T10:30:00.123Z",
  "status": 404,
  "error": "Not Found",
  "message": "No consumables defined for service ID: 5. Please configure consumables in service management.",
  "path": "/api/v1/warehouse/consumables/services/5"
}
```

**Exception**: `NoConsumablesDefinedException`

**Action**: Admin needs to configure consumables for this service

---

### 403 Forbidden - Insufficient Permissions

**Condition**: User lacks `VIEW_WAREHOUSE` or `VIEW_SERVICES` permission

**Response**:
```json
{
  "timestamp": "2025-11-30T10:30:00.123Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/warehouse/consumables/services/1"
}
```

**Solution**: User needs `VIEW_WAREHOUSE` or `VIEW_SERVICES` permission

---

## Test Scenarios

### Test Data (from seed data)

**Services with Consumables**:
| Service ID | Service Code | Service Name | Has Consumables |
|------------|--------------|--------------|-----------------|
| 1 | GEN_EXAM | Kham tong quat & Tu van | Yes (2 items) |
| 3 | SCALING_L1 | Cao voi rang - Muc 1 | Yes (4 items) |
| 6 | FILLING_COMP | Tram rang Composite | Yes (6 items) |
| 8 | EXTRACT_MILK | Nhoi rang sua | Yes (3 items) |
| 2 | GEN_XRAY_PERI | Chup X-Quang | No (not configured) |

### Scenario 1: Success - Service with Sufficient Stock

**Precondition**: Service GEN_EXAM (ID=1) has consumables with enough stock

**Request**:
```bash
curl -X GET http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Authorization: Bearer $TOKEN" \
  -v
```

**Expected Response**:
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "statusCode": 200,
  "message": "Consumables retrieved successfully",
  "data": {
    "serviceId": 1,
    "serviceName": "Kham tong quat & Tu van",
    "totalConsumableCost": 7000.00,
    "hasInsufficientStock": false,
    "consumables": [
      {
        "itemCode": "CON-GLOVE-01",
        "itemName": "Gang tay y te",
        "quantity": 1.00,
        "currentStock": 500,
        "stockStatus": "OK",
        "totalCost": 5000.00
      },
      {
        "itemCode": "CON-MASK-01",
        "itemName": "Khau trang y te",
        "quantity": 1.00,
        "currentStock": 800,
        "stockStatus": "OK",
        "totalCost": 2000.00
      }
    ]
  }
}
```

**Verification**:
- `hasInsufficientStock` should be `false`
- All items should have `stockStatus: "OK"`
- `totalConsumableCost` = sum of all `totalCost`

---

### Scenario 2: 404 Not Found - Service Does Not Exist

**Request**:
```bash
curl -X GET http://localhost:8080/api/v1/warehouse/consumables/services/999 \
  -H "Authorization: Bearer $TOKEN" \
  -v
```

**Expected Response**:
```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "timestamp": "2025-11-30T10:30:00.123Z",
  "status": 404,
  "error": "Not Found",
  "message": "Service not found with id: 999",
  "path": "/api/v1/warehouse/consumables/services/999"
}
```

---

### Scenario 3: 404 - No Consumables Defined

**Precondition**: Service GEN_XRAY_PERI (ID=2) exists but has no consumables configured

**Request**:
```bash
curl -X GET http://localhost:8080/api/v1/warehouse/consumables/services/2 \
  -H "Authorization: Bearer $TOKEN" \
  -v
```

**Expected Response**:
```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "timestamp": "2025-11-30T10:30:00.123Z",
  "status": 404,
  "error": "Not Found",
  "message": "No consumables defined for service ID: 2. Please configure consumables in service management.",
  "path": "/api/v1/warehouse/consumables/services/2"
}
```

---

### Scenario 4: Success with Stock Warning

**Precondition**: Export some items from service SCALING_L1 to create LOW stock condition

**Setup**:
```sql
-- Manually reduce stock for testing
UPDATE item_masters 
SET cached_total_quantity = 1 
WHERE item_code = 'CON-GAUZE-01';
```

**Request**:
```bash
curl -X GET http://localhost:8080/api/v1/warehouse/consumables/services/3 \
  -H "Authorization: Bearer $TOKEN" \
  -v
```

**Expected Response**:
```json
{
  "statusCode": 200,
  "message": "Consumables retrieved successfully",
  "data": {
    "serviceId": 3,
    "serviceName": "Cao voi rang - Muc 1",
    "totalConsumableCost": 52500.00,
    "hasInsufficientStock": true,
    "consumables": [
      {
        "itemCode": "MAT-POL-01",
        "itemName": "So danh bong",
        "quantity": 15.00,
        "stockStatus": "OK"
      },
      {
        "itemCode": "CON-GAUZE-01",
        "itemName": "Bong gac phau thuat",
        "quantity": 3.00,
        "currentStock": 1,
        "stockStatus": "LOW"
      }
    ]
  }
}
```

**Verification**:
- `hasInsufficientStock` should be `true`
- Item with code `CON-GAUZE-01` should have `stockStatus: "LOW"`
- Frontend should show warning: "Cannot perform service - insufficient materials"

---

### Scenario 5: 403 Forbidden - Insufficient Permissions

**Precondition**: Login as user with no warehouse or service permissions (e.g., patient account)

**Request**:
```bash
# Login as patient
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "patient1",
    "password": "password123"
  }'

# Try to access consumables
curl -X GET http://localhost:8080/api/v1/warehouse/consumables/services/1 \
  -H "Authorization: Bearer $PATIENT_TOKEN" \
  -v
```

**Expected Response**:
```
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "timestamp": "2025-11-30T10:30:00.123Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/warehouse/consumables/services/1"
}
```

---

## Implementation Details

### Database Schema

**service_consumables table** (added in V30):
```sql
CREATE TABLE service_consumables (
    link_id SERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    item_master_id INTEGER NOT NULL,
    quantity_per_service DECIMAL(10,2) NOT NULL,
    unit_id INTEGER NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(service_id, item_master_id)
);
```

### Service Layer Logic

```java
public ServiceConsumablesResponse getServiceConsumables(Long serviceId) {
    // 1. Validate service exists (404 if not found)
    DentalService service = dentalServiceRepository.findById(serviceId)
        .orElseThrow(() -> new ServiceNotFoundException(serviceId));
    
    // 2. Get consumables (404 if empty)
    List<ServiceConsumable> consumables = serviceConsumableRepository
        .findByServiceIdWithDetails(serviceId);
    if (consumables.isEmpty()) {
        throw new NoConsumablesDefinedException(serviceId);
    }
    
    // 3. Enrich with stock and cost data
    List<ConsumableItemResponse> responses = consumables.stream()
        .map(sc -> {
            ItemMaster item = sc.getItemMaster();
            Integer currentStock = item.getCachedTotalQuantity();
            String stockStatus = determineStockStatus(currentStock, quantity);
            BigDecimal unitPrice = item.getCurrentMarketPrice();
            BigDecimal totalCost = unitPrice.multiply(quantity);
            
            return ConsumableItemResponse.builder()
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .quantity(sc.getQuantityPerService())
                .unitName(sc.getUnit().getUnitName())
                .currentStock(currentStock)
                .stockStatus(stockStatus)
                .unitPrice(unitPrice)
                .totalCost(totalCost)
                .build();
        })
        .collect(Collectors.toList());
    
    // 4. Calculate totals
    boolean hasInsufficientStock = responses.stream()
        .anyMatch(r -> "LOW".equals(r.getStockStatus()) 
                    || "OUT_OF_STOCK".equals(r.getStockStatus()));
    
    BigDecimal totalCost = responses.stream()
        .map(ConsumableItemResponse::getTotalCost)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    return ServiceConsumablesResponse.builder()
        .serviceId(serviceId)
        .serviceName(service.getServiceName())
        .totalConsumableCost(totalCost)
        .hasInsufficientStock(hasInsufficientStock)
        .consumables(responses)
        .build();
}
```

---

## Frontend Integration

### Use Case 1: Check Before Booking Appointment

```javascript
async function checkServiceAvailability(serviceId) {
  const response = await fetch(
    `http://localhost:8080/api/v1/warehouse/consumables/services/${serviceId}`,
    {
      headers: {
        'Authorization': `Bearer ${getToken()}`,
      },
    }
  );
  
  if (response.ok) {
    const data = await response.json();
    
    if (data.data.hasInsufficientStock) {
      // Show warning
      showWarning(
        'Material Shortage Warning',
        `This service requires materials that are currently insufficient in stock. ` +
        `Check details before confirming appointment.`,
        data.data.consumables.filter(item => 
          item.stockStatus === 'LOW' || item.stockStatus === 'OUT_OF_STOCK'
        )
      );
    } else {
      // OK to book
      proceedWithBooking(serviceId);
    }
  }
}
```

### Use Case 2: Display Material Cost (For Admin/Manager)

```javascript
async function displayServiceCostBreakdown(serviceId) {
  const response = await fetch(
    `http://localhost:8080/api/v1/warehouse/consumables/services/${serviceId}`,
    {
      headers: {
        'Authorization': `Bearer ${getToken()}`,
      },
    }
  );
  
  if (response.ok) {
    const data = await response.json();
    
    // Display cost breakdown
    console.log(`Service: ${data.data.serviceName}`);
    console.log(`Total Material Cost (COGS): ${formatCurrency(data.data.totalConsumableCost)}`);
    
    data.data.consumables.forEach(item => {
      console.log(`  - ${item.itemName}: ${item.quantity} ${item.unitName} × ${formatCurrency(item.unitPrice)} = ${formatCurrency(item.totalCost)}`);
    });
  }
}
```

---

## Best Practices

### When to Use This API

1. **Before Booking Appointment**: Check if clinic has enough materials
2. **Treatment Planning**: Calculate total cost including consumables
3. **Inventory Planning**: Know which items to restock based on upcoming appointments
4. **Financial Reporting**: Calculate COGS (Cost of Goods Sold) per service

### Performance Considerations

- API uses `JOIN FETCH` to avoid N+1 queries
- Stock data from cached field (`cached_total_quantity`) - no runtime aggregation
- Response size: ~100-500 bytes per consumable item (very lean)

### Error Handling Pattern

```javascript
try {
  const response = await getServiceConsumables(serviceId);
  
  if (response.data.hasInsufficientStock) {
    // Show warning but allow proceed (optional block)
    showWarning('Some materials are low in stock');
  }
  
} catch (error) {
  if (error.status === 404) {
    if (error.message.includes('No consumables defined')) {
      showError('Service configuration incomplete. Contact admin.');
    } else {
      showError('Service not found');
    }
  } else if (error.status === 403) {
    showError('Access denied. Contact administrator.');
  }
}
```

---

## Troubleshooting

### Problem: Always returns 404 "No consumables defined"
**Solution**: 
1. Check if `service_consumables` table has data for that service
2. Verify seed data was loaded: `SELECT * FROM service_consumables WHERE service_id = <id>;`
3. If empty, admin needs to configure consumables via service management UI

### Problem: Stock status always shows "OK" even when stock is low
**Solution**:
1. Check if `item_masters.cached_total_quantity` is updated
2. Run cache refresh: Import/export transactions should auto-update this field
3. Manual refresh: `SELECT * FROM item_masters WHERE item_code = '<code>';`

### Problem: `totalConsumableCost` is 0 or NULL
**Solution**:
1. Check if `item_masters.current_market_price` is set
2. Import transactions should set this price automatically
3. Manual fix: `UPDATE item_masters SET current_market_price = <price> WHERE item_code = '<code>';`

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| V30 | Nov 30, 2025 | Initial implementation of API 6.17 |

---

## Related APIs

- **API 6.1**: Inventory Summary (check overall stock)
- **API 6.2**: Item Batches (detailed batch-level stock info)
- **API 6.4**: Import Transaction (restock items)
- **API 6.8**: Get Item Masters (view all items)

---

**End of API 6.17 Complete Implementation Guide**
