# Service Management API - Complete Guide (Module BE-402)

## 📋 Table of Contents
1. [Overview](#overview)
2. [API Endpoints Summary](#api-endpoints-summary)
3. [P2.1 - Get All Services (Paginated)](#p21---get-all-services-paginated)
4. [P2.2 - Create New Service](#p22---create-new-service)
5. [P2.3 - Get Service by Code](#p23---get-service-by-code)
6. [P2.4 - Update Service](#p24---update-service)
7. [P2.5 - Delete Service (Soft Delete)](#p25---delete-service-soft-delete)
8. [Data Models](#data-models)
9. [Error Handling](#error-handling)
10. [Postman Testing Guide](#postman-testing-guide)

---

## Overview

**Module**: Service Management (BE-402)  
**Purpose**: Quản lý danh mục dịch vụ đơn lẻ (cạo vôi, nhổ răng...), bao gồm thời gian thực hiện, thời gian đệm (buffer) và giá. Đây là đầu vào cốt lõi để tính toán thời lượng của một lịch hẹn.  
**Base URL**: `/api/v1/services`  
**Authentication**: Required (Bearer Token)

### Business Context

Services (dịch vụ nha khoa) là các dịch vụ đơn lẻ như:
- **Cạo vôi răng** - Duration: 30 phút, Buffer: 10 phút
- **Nhổ răng** - Duration: 45 phút, Buffer: 15 phút
- **Cắm trụ Implant** - Duration: 120 phút, Buffer: 30 phút

**Key Features**:
- Mỗi service có **default duration** (thời gian thực hiện) và **buffer** (thời gian đệm để dọn dẹp)
- Service có thể thuộc về một **chuyên khoa** (Specialization) hoặc NULL (general)
- Soft delete: Service không thể xóa cứng vì đã có appointments cũ tham chiếu

---

## API Endpoints Summary

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| GET | `/api/v1/services` | `VIEW_SERVICE` | Get all services (paginated + filters) |
| POST | `/api/v1/services` | `CREATE_SERVICE` | Create new service |
| GET | `/api/v1/services/{serviceCode}` | `VIEW_SERVICE` | Get service by code |
| PUT | `/api/v1/services/{serviceCode}` | `UPDATE_SERVICE` | Update service |
| DELETE | `/api/v1/services/{serviceCode}` | `DELETE_SERVICE` | Soft delete service |

---

## P2.1 - Get All Services (Paginated)

### Request

```http
GET /api/v1/services?page=0&size=10&sortBy=serviceName&sortDirection=ASC&isActive=true&specializationId=1&keyword=cạo
Authorization: Bearer {access_token}
```

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `size` | Integer | No | 10 | Page size (max 100) |
| `sortBy` | String | No | serviceName | Sort field: `serviceName`, `serviceCode`, `price` |
| `sortDirection` | String | No | ASC | Sort direction: `ASC`, `DESC` |
| `isActive` | Boolean | No | null | Filter by status: `true`, `false`, `null` (all) |
| `specializationId` | Integer | No | null | Filter by specialization ID |
| `keyword` | String | No | null | Search in service code or name |

### Response (200 OK)

```json
{
  "content": [
    {
      "serviceId": 1,
      "serviceCode": "SV-CAOVOI",
      "serviceName": "Cạo vôi răng và Đánh bóng",
      "description": "Lấy sạch vôi răng và mảng bám bằng máy siêu âm",
      "defaultDurationMinutes": 30,
      "defaultBufferMinutes": 10,
      "price": 300000.00,
      "specializationId": 1,
      "specializationName": "Nha khoa tổng quát",
      "isActive": true,
      "createdAt": "2024-11-01T10:00:00",
      "updatedAt": "2024-11-01T10:00:00"
    },
    {
      "serviceId": 2,
      "serviceCode": "SV-NHORANG",
      "serviceName": "Nhổ răng thường",
      "description": "Nhổ răng sữa hoặc răng vĩnh viễn đơn giản",
      "defaultDurationMinutes": 45,
      "defaultBufferMinutes": 15,
      "price": 500000.00,
      "specializationId": 2,
      "specializationName": "Phẫu thuật nha khoa",
      "isActive": true,
      "createdAt": "2024-11-01T10:30:00",
      "updatedAt": "2024-11-01T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 25,
  "totalPages": 3,
  "last": false,
  "first": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 10,
  "empty": false
}
```

### Curl Example

```bash
curl -X GET "http://localhost:8080/api/v1/services?page=0&size=10&isActive=true" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## P2.2 - Create New Service

### Request

```http
POST /api/v1/services
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "serviceCode": "SV-CAOVOI",
  "serviceName": "Cạo vôi răng và Đánh bóng",
  "description": "Lấy sạch vôi răng và mảng bám bằng máy siêu âm",
  "defaultDurationMinutes": 30,
  "defaultBufferMinutes": 10,
  "price": 300000,
  "specializationId": 1,
  "isActive": true
}
```

### Request Body

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `serviceCode` | String | **Yes** | Unique, max 20 chars | Mã dịch vụ |
| `serviceName` | String | **Yes** | Not blank, max 255 chars | Tên dịch vụ |
| `description` | String | No | - | Mô tả chi tiết |
| `defaultDurationMinutes` | Integer | **Yes** | >= 1 | Thời gian thực hiện (phút) |
| `defaultBufferMinutes` | Integer | **Yes** | >= 0 | Thời gian đệm dọn dẹp (phút) |
| `price` | BigDecimal | **Yes** | >= 0 | Giá dịch vụ (VND) |
| `specializationId` | Integer | No | Must exist if provided | ID chuyên khoa (nullable) |
| `isActive` | Boolean | No | Default: true | Trạng thái hoạt động |

### Business Logic & Validation

1. **serviceCode unique**: Nếu trùng → 400 Bad Request (Error Code: `SERVICE_CODE_EXISTS`)
2. **defaultDurationMinutes**: Phải > 0
3. **defaultBufferMinutes**: Phải >= 0
4. **price**: Phải >= 0
5. **specializationId**: Nếu cung cấp (khác NULL), phải tồn tại trong `specializations` table → 400 Bad Request (Error Code: `SPECIALIZATION_NOT_FOUND`)
6. **Cho phép specializationId = NULL**: Service có thể không thuộc chuyên khoa nào (general service)

### Response (201 Created)

```json
{
  "serviceId": 30,
  "serviceCode": "SV-CAOVOI",
  "serviceName": "Cạo vôi răng và Đánh bóng",
  "description": "Lấy sạch vôi răng và mảng bám bằng máy siêu âm",
  "defaultDurationMinutes": 30,
  "defaultBufferMinutes": 10,
  "price": 300000.00,
  "specializationId": 1,
  "specializationName": "Nha khoa tổng quát",
  "isActive": true,
  "createdAt": "2024-11-03T14:30:00",
  "updatedAt": "2024-11-03T14:30:00"
}
```

### Error Responses

#### 400 Bad Request - Service code exists
```json
{
  "timestamp": "2024-11-03T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Service code already exists: SV-CAOVOI",
  "entityName": "service",
  "errorKey": "SERVICE_CODE_EXISTS"
}
```

#### 400 Bad Request - Specialization not found
```json
{
  "timestamp": "2024-11-03T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Specialization not found with ID: 999",
  "entityName": "specialization",
  "errorKey": "SPECIALIZATION_NOT_FOUND"
}
```

#### 400 Bad Request - Validation errors
```json
{
  "timestamp": "2024-11-03T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "serviceCode": "Service code is required",
    "serviceName": "Service name is required",
    "defaultDurationMinutes": "Duration must be at least 1 minute",
    "price": "Price cannot be negative"
  }
}
```

### Curl Example

```bash
curl -X POST "http://localhost:8080/api/v1/services" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "serviceCode": "SV-CAOVOI",
    "serviceName": "Cạo vôi răng và Đánh bóng",
    "defaultDurationMinutes": 30,
    "defaultBufferMinutes": 10,
    "price": 300000,
    "specializationId": 1
  }'
```

---

## P2.3 - Get Service by Code

### Request

```http
GET /api/v1/services/SV-CAOVOI
Authorization: Bearer {access_token}
```

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `serviceCode` | String | Service code (e.g., SV-CAOVOI) |

### Response (200 OK)

```json
{
  "serviceId": 1,
  "serviceCode": "SV-CAOVOI",
  "serviceName": "Cạo vôi răng và Đánh bóng",
  "description": "Lấy sạch vôi răng và mảng bám bằng máy siêu âm",
  "defaultDurationMinutes": 30,
  "defaultBufferMinutes": 10,
  "price": 300000.00,
  "specializationId": 1,
  "specializationName": "Nha khoa tổng quát",
  "isActive": true,
  "createdAt": "2024-11-01T10:00:00",
  "updatedAt": "2024-11-01T10:00:00"
}
```

### Error Responses

#### 404 Not Found
```json
{
  "timestamp": "2024-11-03T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Service not found with code: SV-INVALID",
  "entityName": "service",
  "errorKey": "notfound"
}
```

### Curl Example

```bash
curl -X GET "http://localhost:8080/api/v1/services/SV-CAOVOI" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## P2.4 - Update Service

### Request

```http
PUT /api/v1/services/SV-CAOVOI
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "serviceCode": "SV-CAOVOI-UPDATED",
  "serviceName": "Cạo vôi răng và Đánh bóng (VIP)",
  "description": "Lấy sạch vôi răng với công nghệ mới nhất",
  "defaultDurationMinutes": 35,
  "defaultBufferMinutes": 10,
  "price": 350000,
  "specializationId": 1,
  "isActive": true
}
```

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `serviceCode` | String | Current service code |

### Request Body (All fields optional)

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `serviceCode` | String | Unique (except itself) | Mã dịch vụ mới |
| `serviceName` | String | - | Tên dịch vụ mới |
| `description` | String | - | Mô tả mới |
| `defaultDurationMinutes` | Integer | >= 1 | Thời gian thực hiện mới |
| `defaultBufferMinutes` | Integer | >= 0 | Thời gian đệm mới |
| `price` | BigDecimal | >= 0 | Giá mới |
| `specializationId` | Integer | Must exist if provided | ID chuyên khoa mới |
| `isActive` | Boolean | - | Trạng thái mới |

### Business Logic & Validation

1. **Service phải tồn tại**: Tìm theo serviceCode → 404 if not found
2. **serviceCode unique**: Nếu đổi code mới và trùng → 400 Bad Request (Error Code: `SERVICE_CODE_EXISTS`)
3. **Các validation khác**: Tương tự như API Create (duration, price, specializationId)

### Response (200 OK)

```json
{
  "serviceId": 1,
  "serviceCode": "SV-CAOVOI-UPDATED",
  "serviceName": "Cạo vôi răng và Đánh bóng (VIP)",
  "description": "Lấy sạch vôi răng với công nghệ mới nhất",
  "defaultDurationMinutes": 35,
  "defaultBufferMinutes": 10,
  "price": 350000.00,
  "specializationId": 1,
  "specializationName": "Nha khoa tổng quát",
  "isActive": true,
  "createdAt": "2024-11-01T10:00:00",
  "updatedAt": "2024-11-03T14:45:00"
}
```

### Error Responses

#### 404 Not Found
```json
{
  "timestamp": "2024-11-03T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Service not found with code: SV-INVALID",
  "entityName": "service",
  "errorKey": "notfound"
}
```

#### 400 Bad Request - Code conflict
```json
{
  "timestamp": "2024-11-03T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Service code already exists: SV-NHORANG",
  "entityName": "service",
  "errorKey": "SERVICE_CODE_EXISTS"
}
```

### Curl Example

```bash
curl -X PUT "http://localhost:8080/api/v1/services/SV-CAOVOI" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "Cạo vôi răng và Đánh bóng (VIP)",
    "price": 350000
  }'
```

---

## P2.5 - Delete Service (Soft Delete)

### Request

```http
DELETE /api/v1/services/SV-CAOVOI
Authorization: Bearer {access_token}
```

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `serviceCode` | String | Service code to delete |

### Business Logic

- **Soft Delete**: Set `is_active = false` trong database
- **An toàn**: Các appointments cũ (`appointment_services`) và treatment plans cũ (`patient_plan_items`) đã tham chiếu `service_id` sẽ không bị ảnh hưởng
- **Impact**: Service không thể được chọn khi:
  - Tạo appointment mới
  - Thêm vào treatment plan mới
  - Gán cho room (room-service compatibility)

### Response (204 No Content)

No response body.

### Error Responses

#### 404 Not Found
```json
{
  "timestamp": "2024-11-03T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Service not found with code: SV-INVALID",
  "entityName": "service",
  "errorKey": "notfound"
}
```

### Curl Example

```bash
curl -X DELETE "http://localhost:8080/api/v1/services/SV-CAOVOI" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Data Models

### ServiceResponse

```typescript
interface ServiceResponse {
  serviceId: number;                    // Auto-generated ID
  serviceCode: string;                  // Unique code (e.g., SV-CAOVOI)
  serviceName: string;                  // Display name
  description: string | null;           // Detailed description
  defaultDurationMinutes: number;       // Duration (minutes)
  defaultBufferMinutes: number;         // Buffer time (minutes)
  price: number;                        // Price (VND)
  specializationId: number | null;      // FK to specializations (nullable)
  specializationName: string | null;    // Specialization display name
  isActive: boolean;                    // Active status
  createdAt: string;                    // ISO 8601 datetime
  updatedAt: string;                    // ISO 8601 datetime
}
```

### CreateServiceRequest

```typescript
interface CreateServiceRequest {
  serviceCode: string;                  // Required, unique
  serviceName: string;                  // Required
  description?: string;                 // Optional
  defaultDurationMinutes: number;       // Required, >= 1
  defaultBufferMinutes: number;         // Required, >= 0
  price: number;                        // Required, >= 0
  specializationId?: number;            // Optional, must exist if provided
  isActive?: boolean;                   // Optional, default: true
}
```

### UpdateServiceRequest

```typescript
interface UpdateServiceRequest {
  serviceCode?: string;                 // Optional, unique (except itself)
  serviceName?: string;                 // Optional
  description?: string;                 // Optional
  defaultDurationMinutes?: number;      // Optional, >= 1
  defaultBufferMinutes?: number;        // Optional, >= 0
  price?: number;                       // Optional, >= 0
  specializationId?: number;            // Optional, must exist if provided
  isActive?: boolean;                   // Optional
}
```

---

## Error Handling

### Common HTTP Status Codes

| Status Code | Description | Common Causes |
|-------------|-------------|---------------|
| 200 OK | Success | Request processed successfully |
| 201 Created | Resource created | Service created successfully |
| 204 No Content | Success (no body) | Service deleted successfully |
| 400 Bad Request | Validation error | Missing required fields, duplicate code, invalid specialization |
| 401 Unauthorized | Authentication failed | Missing/invalid token |
| 403 Forbidden | Permission denied | User doesn't have required permission |
| 404 Not Found | Resource not found | Service doesn't exist |
| 500 Internal Server Error | Server error | Database error, unexpected exception |

### Error Response Format

```json
{
  "timestamp": "2024-11-03T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Service code already exists: SV-CAOVOI",
  "entityName": "service",
  "errorKey": "SERVICE_CODE_EXISTS",
  "path": "/api/v1/services"
}
```

### Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `SERVICE_CODE_EXISTS` | 400 | Service code already exists (duplicate) |
| `SPECIALIZATION_NOT_FOUND` | 400 | Specialization ID not found |
| `notfound` | 404 | Service not found |

---

## Postman Testing Guide

### Setup

1. **Import Environment Variables**
   - `base_url`: `http://localhost:8080`
   - `access_token`: Your JWT access token
   - `test_service_code`: `SV-TEST-001`

2. **Login First** (to get access token)
   ```
   POST {{base_url}}/api/v1/auth/login
   Body: {
     "username": "admin",
     "password": "password123"
   }
   ```
   Copy `accessToken` from response to `access_token` variable.

### Test Scenarios

#### Scenario 1: Complete Service CRUD Flow

**Step 1**: Get all services (before creation)
```
GET {{base_url}}/api/v1/services?page=0&size=10
Authorization: Bearer {{access_token}}
```
✅ Expected: 200 OK with service list

**Step 2**: Create new service
```
POST {{base_url}}/api/v1/services
Authorization: Bearer {{access_token}}
Body: {
  "serviceCode": "{{test_service_code}}",
  "serviceName": "Test Service - Cạo vôi",
  "description": "Test service for Postman",
  "defaultDurationMinutes": 30,
  "defaultBufferMinutes": 10,
  "price": 300000,
  "specializationId": 1,
  "isActive": true
}
```
✅ Expected: 201 Created with service details
📝 Save `serviceId` to environment variable

**Step 3**: Get service by code
```
GET {{base_url}}/api/v1/services/{{test_service_code}}
Authorization: Bearer {{access_token}}
```
✅ Expected: 200 OK with service details

**Step 4**: Update service
```
PUT {{base_url}}/api/v1/services/{{test_service_code}}
Authorization: Bearer {{access_token}}
Body: {
  "serviceName": "Test Service - Cạo vôi (UPDATED)",
  "price": 350000
}
```
✅ Expected: 200 OK with updated service

**Step 5**: Soft delete service
```
DELETE {{base_url}}/api/v1/services/{{test_service_code}}
Authorization: Bearer {{access_token}}
```
✅ Expected: 204 No Content

**Step 6**: Verify service is inactive
```
GET {{base_url}}/api/v1/services/{{test_service_code}}
Authorization: Bearer {{access_token}}
```
✅ Expected: 200 OK with `isActive: false`

#### Scenario 2: Error Handling Tests

**Test 1**: Create service with duplicate code
```
POST {{base_url}}/api/v1/services
Body: { "serviceCode": "SV-CAOVOI", ... } (existing code)
```
❌ Expected: 400 Bad Request (SERVICE_CODE_EXISTS)

**Test 2**: Create service with invalid specialization
```
POST {{base_url}}/api/v1/services
Body: { "specializationId": 999, ... }
```
❌ Expected: 400 Bad Request (SPECIALIZATION_NOT_FOUND)

**Test 3**: Create service with invalid duration
```
POST {{base_url}}/api/v1/services
Body: { "defaultDurationMinutes": 0, ... }
```
❌ Expected: 400 Bad Request (validation error)

**Test 4**: Get non-existent service
```
GET {{base_url}}/api/v1/services/INVALID-CODE
```
❌ Expected: 404 Not Found

**Test 5**: Update with duplicate code
```
PUT {{base_url}}/api/v1/services/SV-CAOVOI
Body: { "serviceCode": "SV-NHORANG" } (existing code)
```
❌ Expected: 400 Bad Request (SERVICE_CODE_EXISTS)

**Test 6**: No authentication
```
GET {{base_url}}/api/v1/services
(no Authorization header)
```
❌ Expected: 401 Unauthorized

**Test 7**: Wrong permission (Doctor trying to create)
```
POST {{base_url}}/api/v1/services
Authorization: Bearer {{doctor_token}}
Body: {...}
```
❌ Expected: 403 Forbidden (Doctor không có quyền CREATE_SERVICE)

#### Scenario 3: Filtering and Search

**Test 1**: Filter by active status
```
GET {{base_url}}/api/v1/services?isActive=true&page=0&size=10
```
✅ Expected: Only active services

**Test 2**: Filter by specialization
```
GET {{base_url}}/api/v1/services?specializationId=1&page=0&size=10
```
✅ Expected: Services belonging to specialization 1

**Test 3**: Search by keyword
```
GET {{base_url}}/api/v1/services?keyword=cạo
```
✅ Expected: Services with "cạo" in name or code

**Test 4**: Combined filters
```
GET {{base_url}}/api/v1/services?isActive=true&specializationId=1&keyword=vôi
```
✅ Expected: Active services in specialization 1 containing "vôi"

**Test 5**: Sort by price descending
```
GET {{base_url}}/api/v1/services?sortBy=price&sortDirection=DESC
```
✅ Expected: Services sorted by price (highest first)

---

## Permission Matrix

| API Endpoint | Required Permission | Roles with Access |
|--------------|---------------------|-------------------|
| GET /api/v1/services | `VIEW_SERVICE` | ADMIN, MANAGER, RECEPTIONIST, DOCTOR |
| GET /api/v1/services/{code} | `VIEW_SERVICE` | ADMIN, MANAGER, RECEPTIONIST, DOCTOR |
| POST /api/v1/services | `CREATE_SERVICE` | ADMIN, MANAGER |
| PUT /api/v1/services/{code} | `UPDATE_SERVICE` | ADMIN, MANAGER |
| DELETE /api/v1/services/{code} | `DELETE_SERVICE` | ADMIN, MANAGER |

---

## Business Rules Summary

### 1. Service Code
- Must be unique across all services (active + inactive)
- Cannot be changed if it causes conflict with existing code
- Format: Typically `SV-XXXXX` (e.g., SV-CAOVOI, SV-NHORANG)

### 2. Duration & Buffer
- **defaultDurationMinutes**: Thời gian bác sĩ thực hiện dịch vụ (>= 1 phút)
- **defaultBufferMinutes**: Thời gian dọn dẹp, chuẩn bị giữa các ca (>= 0 phút)
- **Total Time** = Duration + Buffer (dùng để tính slot cho appointment)

### 3. Specialization
- **NULL allowed**: Service có thể không thuộc chuyên khoa (general service)
- **Must exist**: Nếu cung cấp specializationId, phải tồn tại trong database
- **Example**: 
  - "Cạo vôi" → Specialization: "Nha khoa tổng quát" (ID: 1)
  - "Cắm trụ Implant" → Specialization: "Implant" (ID: 3)

### 4. Soft Delete
- Service không thể xóa cứng (hard delete) vì:
  - Appointments cũ đã tham chiếu (appointment_services)
  - Treatment plans cũ đã tham chiếu (patient_plan_items)
- Soft delete: Set `is_active = false`
- Impact: Không thể chọn service này khi tạo appointment/treatment plan mới

### 5. Price
- Giá niêm yết mặc định (VND)
- Có thể override khi tạo treatment plan (patient_plan_items.unit_price)
- Phải >= 0 (có thể free service = 0)

---

## Frontend Integration Example

```typescript
// TypeScript/React example
import axios from 'axios';

interface Service {
  serviceId: number;
  serviceCode: string;
  serviceName: string;
  description: string | null;
  defaultDurationMinutes: number;
  defaultBufferMinutes: number;
  price: number;
  specializationId: number | null;
  specializationName: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface CreateServiceDTO {
  serviceCode: string;
  serviceName: string;
  description?: string;
  defaultDurationMinutes: number;
  defaultBufferMinutes: number;
  price: number;
  specializationId?: number;
  isActive?: boolean;
}

// Get all active services for dropdown
async function getActiveServices(): Promise<Service[]> {
  const response = await axios.get('/api/v1/services', {
    params: {
      isActive: true,
      page: 0,
      size: 100
    },
    headers: {
      Authorization: `Bearer ${getAccessToken()}`
    }
  });
  return response.data.content;
}

// Create new service
async function createService(data: CreateServiceDTO): Promise<Service> {
  const response = await axios.post('/api/v1/services', data, {
    headers: {
      Authorization: `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    }
  });
  return response.data;
}

// Update service
async function updateService(serviceCode: string, data: Partial<CreateServiceDTO>): Promise<Service> {
  const response = await axios.put(`/api/v1/services/${serviceCode}`, data, {
    headers: {
      Authorization: `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    }
  });
  return response.data;
}

// Delete service (soft delete)
async function deleteService(serviceCode: string): Promise<void> {
  await axios.delete(`/api/v1/services/${serviceCode}`, {
    headers: {
      Authorization: `Bearer ${getAccessToken()}`
    }
  });
}

// Usage example
const services = await getActiveServices();
console.log(`Found ${services.length} active services`);

const newService = await createService({
  serviceCode: 'SV-CAOVOI',
  serviceName: 'Cạo vôi răng',
  defaultDurationMinutes: 30,
  defaultBufferMinutes: 10,
  price: 300000,
  specializationId: 1
});
console.log(`Created service: ${newService.serviceName}`);
```

---

## Database Schema

### services table

```sql
CREATE TABLE services (
    service_id SERIAL PRIMARY KEY,
    service_code VARCHAR(20) UNIQUE NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    description TEXT,
    default_duration_minutes INTEGER NOT NULL,
    default_buffer_minutes INTEGER NOT NULL DEFAULT 15,
    price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    specialization_id INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_service_specialization FOREIGN KEY (specialization_id)
        REFERENCES specializations(specialization_id) ON DELETE SET NULL
);
```

### Related Tables

- **appointment_services**: Links services to appointments (many-to-many)
- **patient_plan_items**: Links services to treatment plans
- **room_services**: Links services to rooms (V16 - compatibility mapping)
- **specializations**: Categorizes services by medical specialty

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| V1 | 2024-11-01 | Initial Service Management APIs | BE Team |
| V2 | 2024-11-03 | Changed endpoints from serviceId to serviceCode | BE Team |

---

## Contact & Support

- **Backend Team**: backend@dentalclinic.com
- **API Issues**: Create issue in JIRA (Project: PDCMS)
- **Documentation**: https://docs.dentalclinic.com/api/services

---

**Last Updated**: November 3, 2024  
**Document Version**: 2.0
