# Service API Architecture Clarification

**Date:** 2025-11-24
**Status:** 🟢 **OFFICIAL BACKEND CLARIFICATION**
**For:** Frontend Team

---

## Executive Summary

**Decision:** ✅ **Use BOTH APIs - They serve different purposes**

### Quick Answer for Frontend:

| Use Case                                              | API to Use      | Endpoint                          |
| ----------------------------------------------------- | --------------- | --------------------------------- |
| **Admin CRUD** (Create/Edit/Delete services)          | **Booking API** | `/api/v1/booking/services`        |
| **Public Price List** (No auth)                       | **V17 API**     | `/api/v1/public/services/grouped` |
| **Service Selection** (Treatment Plans, Appointments) | **V17 API**     | `/api/v1/services/grouped`        |
| **Admin Dashboard** (List with category filter)       | **V17 API**     | `/api/v1/services?categoryId=X`   |

---

## Architecture Explained

### 🎯 Purpose & Design Intent

These are **NOT duplicate APIs** - they serve different domains:

#### 1️⃣ **V17 Service API** (`/api/v1/services`)

**Domain:** Service Catalog & Configuration
**Purpose:** Read-only service browsing for operational use
**Controller:** `service/controller/DentalServiceController.java`

**Use Cases:**

- ✅ Public website price list display
- ✅ Treatment plan service selection
- ✅ Appointment booking service dropdown
- ✅ Admin dashboard service overview with category grouping

**Key Features:**

- ✅ Category-based grouping (`GroupedServicesResponse`)
- ✅ Public endpoint (no auth) for price lists
- ✅ `categoryId` filter support
- ✅ Optimized for read-heavy operations
- ❌ **No CRUD operations** (by design - services are configured separately)

---

#### 2️⃣ **Booking Service API** (`/api/v1/booking/services`)

**Domain:** Service Configuration & Management
**Purpose:** Admin CRUD operations for service setup
**Controller:** `booking_appointment/controller/ServiceController.java`

**Use Cases:**

- ✅ Admin create/update/delete services
- ✅ Service-Specialization mapping management
- ✅ Service status toggling (activate/deactivate)
- ✅ Doctor specialization-based filtering

**Key Features:**

- ✅ Full CRUD operations (POST/PUT/DELETE/PATCH)
- ✅ `specializationId` filter (for doctor-specific services)
- ✅ `/my-specializations` endpoint for doctors
- ✅ Optimized for admin management
- ⚠️ **Missing `categoryId`** (not needed for booking context)

---

## DTO Comparison

### V17 API - `DentalServiceDTO`

```java
DentalServiceDTO {
  Long serviceId;
  String serviceCode;
  String serviceName;
  String description;
  BigDecimal price;
  Integer durationMinutes;       // ⭐ Single duration field
  Integer displayOrder;          // ⭐ For UI ordering
  Boolean isActive;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  ServiceCategoryDTO.Brief category; // ⭐ Nested category object
}
```

**Strengths:**

- ✅ Has `categoryId` via `category.categoryId`
- ✅ Has `displayOrder` for consistent UI ordering
- ✅ Simpler duration model (single field)

---

### Booking API - `ServiceResponse`

```java
ServiceResponse {
  Integer serviceId;
  String serviceCode;
  String serviceName;
  String description;
  Integer defaultDurationMinutes; // ⭐ Duration + Buffer split
  Integer defaultBufferMinutes;
  BigDecimal price;
  Integer specializationId;       // ⭐ Specialization-based filtering
  String specializationName;
  Boolean isActive;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}
```

**Strengths:**

- ✅ Has `specializationId` for doctor filtering
- ✅ Separate `buffer` time for appointment scheduling
- ✅ Full CRUD support via separate endpoints

**Weakness:**

- ❌ Missing `categoryId` (cannot group by category)

---

## Frontend Implementation Guide

### ✅ Recommended Frontend Architecture

```typescript
// src/services/serviceService.ts

class ServiceService {
  // 1️⃣ Use V17 API for READ operations (has categoryId)
  async getServicesForDisplay(filters: {
    categoryId?: number;
    isActive?: boolean;
    search?: string;
  }): Promise<DentalServiceDTO[]> {
    return axios.get("/api/v1/services", { params: filters });
  }

  async getPublicPriceList(): Promise<GroupedServicesResponse> {
    return axios.get("/api/v1/public/services/grouped");
  }

  async getGroupedServicesForBooking(): Promise<GroupedServicesResponse> {
    return axios.get("/api/v1/services/grouped");
  }

  // 2️⃣ Use Booking API for WRITE operations (has CRUD)
  async createService(data: CreateServiceRequest): Promise<ServiceResponse> {
    return axios.post("/api/v1/booking/services", data);
  }

  async updateService(
    serviceCode: string,
    data: UpdateServiceRequest
  ): Promise<ServiceResponse> {
    return axios.put(`/api/v1/booking/services/${serviceCode}`, data);
  }

  async deleteService(serviceId: number): Promise<void> {
    return axios.delete(`/api/v1/booking/services/${serviceId}`);
  }

  async toggleService(serviceId: number): Promise<ServiceResponse> {
    return axios.patch(`/api/v1/booking/services/${serviceId}/toggle`);
  }
}
```

---

### Page-by-Page Usage

#### 1. **Public Price List Page** (`/pricing`)

```typescript
// Use V17 public API (no auth required)
const services = await serviceService.getPublicPriceList();
// Returns: GroupedServicesResponse.Public[]
// Groups by category, minimal fields (name, price only)
```

---

#### 2. **Admin Service Management Page** (`/admin/services`)

**LIST/READ:**

```typescript
// Use V17 API for better filtering
const services = await serviceService.getServicesForDisplay({
  categoryId: selectedCategoryId,
  isActive: true,
  search: searchQuery,
});
// Returns: Page<DentalServiceDTO> with category info
```

**CREATE/UPDATE/DELETE:**

```typescript
// Use Booking API for CRUD operations
await serviceService.createService({
  serviceCode: 'SV-001',
  serviceName: 'Cạo vôi răng',
  specializationId: 1,
  defaultDurationMinutes: 30,
  defaultBufferMinutes: 10,
  price: 300000
});

await serviceService.updateService('SV-001', { ... });
await serviceService.deleteService(123);
```

---

#### 3. **Treatment Plan Service Selection** (`/treatment-plans/create`)

```typescript
// Use V17 grouped API (easier to render by category)
const groupedServices = await serviceService.getGroupedServicesForBooking();
// Returns: GroupedServicesResponse.Internal[]
// Groups by category with full details (id, code, duration)
```

---

#### 4. **Appointment Booking Service Dropdown** (`/appointments/create`)

```typescript
// Option A: Use V17 grouped API
const groupedServices = await serviceService.getGroupedServicesForBooking();

// Option B: Use Booking API filtered by doctor's specialization
const doctorServices = await axios.get(
  "/api/v1/booking/services/my-specializations"
);
```

---

## 🔧 Backend Enhancement Recommendations

### Priority 1: Add `categoryId` to Booking API DTO ⭐

**Impact:** Frontend can use single API for admin CRUD
**Effort:** Low (15 minutes)

**Changes Required:**

#### 1. Update `ServiceResponse.java`:

```java
@Schema(description = "Service category ID", example = "5")
private Long categoryId;

@Schema(description = "Service category code", example = "GENERAL")
private String categoryCode;

@Schema(description = "Service category name", example = "Nha khoa tổng quát")
private String categoryName;
```

#### 2. Update `AppointmentDentalServiceService.java`:

```java
// In toServiceResponse() mapping method
.categoryId(service.getCategory() != null ? service.getCategory().getCategoryId() : null)
.categoryCode(service.getCategory() != null ? service.getCategory().getCategoryCode() : null)
.categoryName(service.getCategory() != null ? service.getCategory().getCategoryName() : null)
```

#### 3. Add `categoryId` filter to endpoint:

```java
@GetMapping
public ResponseEntity<Page<ServiceResponse>> getAllServices(
    @RequestParam(required = false) Long categoryId,  // ADD THIS
    @RequestParam(required = false) Integer specializationId,
    // ... existing params
)
```

**After this change:** Frontend can use Booking API for both READ and WRITE operations.

---

### Priority 2: Document Proper API Usage (DONE ✅)

This document serves as official backend clarification.

---

### Priority 3: Consider Future Consolidation (Low Priority)

**Long-term (v2.0):** Merge both APIs into single `/api/v2/services` with:

- ✅ Full CRUD operations
- ✅ Both `categoryId` and `specializationId` filters
- ✅ Public/Internal/Admin endpoints under one controller

**Not urgent** - current dual-API approach works well.

---

## Answers to FE Team Questions

### ❓ Q1: Which API should FE use?

**Answer:** ✅ **Use BOTH - they serve different purposes**

- **Read operations** (List/Filter/Display): V17 API (`/api/v1/services`)

  - Has `categoryId` filter
  - Has grouped endpoints
  - Optimized for browsing

- **Write operations** (Create/Edit/Delete): Booking API (`/api/v1/booking/services`)
  - Has POST/PUT/DELETE endpoints
  - Has toggle endpoint
  - Has specialization filters

**After Priority 1 enhancement:** Can optionally use Booking API for both.

---

### ❓ Q2: Should Booking API be deprecated?

**Answer:** ❌ **No - they serve different domains**

- V17 API = Service **Catalog** (read-heavy)
- Booking API = Service **Management** (write-heavy)

This separation follows **Domain-Driven Design** principles.

---

### ❓ Q3: Can Booking API include `categoryId`?

**Answer:** ✅ **Yes - see Priority 1 enhancement above**

This is a quick fix (15 minutes) that would allow frontend flexibility.

---

## Service Category Admin UI Requirements

### Backend APIs (Already Complete ✅)

```bash
GET    /api/v1/service-categories              # List all
GET    /api/v1/service-categories/{id}         # Get by ID
POST   /api/v1/service-categories              # Create
PATCH  /api/v1/service-categories/{id}         # Update
DELETE /api/v1/service-categories/{id}         # Soft delete
POST   /api/v1/service-categories/reorder      # Reorder
```

**Permissions:** `VIEW_SERVICE`, `CREATE_SERVICE`, `UPDATE_SERVICE`, `DELETE_SERVICE`

### Frontend Requirements

✅ Service: `ServiceCategoryService` (already implemented)
✅ Types: `ServiceCategory` interface (already defined)
❌ **Missing:** Admin page `/admin/service-categories`

**Page Features Needed:**

1. **List View:**

   - Table: Category Name, Code, Service Count, Display Order, Status
   - Actions: Edit, Delete, Toggle Status
   - Drag-drop reordering

2. **Create/Edit Modal:**

   - Fields: Category Name, Code, Description, Display Order
   - Validation: Required fields, unique code

3. **Delete Confirmation:**
   - Soft delete (sets `isActive = false`)
   - Warning if category has active services

**Priority:** Medium (not blocking - can wait until Priority 1 is implemented)

---

## Summary & Action Items

### ✅ For Frontend Team:

1. ✅ **Immediate:** Use dual-API approach (V17 for READ, Booking for WRITE)
2. ⏳ **Next Sprint:** Create Service Category admin page
3. 🔮 **After BE Priority 1:** Optionally migrate to single Booking API

### 🔧 For Backend Team:

1. ⭐ **Priority 1:** Add `categoryId` to `ServiceResponse` DTO (15 min)
2. ✅ **Priority 2:** Document API usage (DONE - this document)
3. 🔮 **Long-term:** Consider API consolidation in v2.0

---

## Code Examples for Frontend

### Example 1: Admin Service List with Category Filter

```typescript
// /admin/services/page.tsx
const [categoryId, setCategoryId] = useState<number>();
const [services, setServices] = useState<DentalServiceDTO[]>([]);

// Use V17 API for listing (has categoryId)
useEffect(() => {
  const fetchServices = async () => {
    const response = await axios.get("/api/v1/services", {
      params: {
        categoryId: categoryId,
        isActive: true,
        page: 0,
        size: 20,
      },
    });
    setServices(response.data.content);
  };
  fetchServices();
}, [categoryId]);

// Render category filter dropdown
<Select value={categoryId} onChange={setCategoryId}>
  {categories.map((cat) => (
    <Option key={cat.categoryId} value={cat.categoryId}>
      {cat.categoryName}
    </Option>
  ))}
</Select>;
```

### Example 2: Admin Create Service Form

```typescript
// /admin/services/create-modal.tsx
const handleCreateService = async (values: CreateServiceRequest) => {
  try {
    // Use Booking API for create operation
    const response = await axios.post("/api/v1/booking/services", {
      serviceCode: values.serviceCode,
      serviceName: values.serviceName,
      specializationId: values.specializationId,
      defaultDurationMinutes: values.duration,
      defaultBufferMinutes: values.buffer,
      price: values.price,
      description: values.description,
    });

    message.success("Service created successfully");
    onSuccess(response.data);
  } catch (error) {
    message.error("Failed to create service");
  }
};
```

### Example 3: Treatment Plan Service Selection

```typescript
// /treatment-plans/components/ServiceSelector.tsx
const [groupedServices, setGroupedServices] = useState<
  GroupedServicesResponse[]
>([]);

useEffect(() => {
  const fetchServices = async () => {
    // Use V17 grouped API (easier to render by category)
    const response = await axios.get("/api/v1/services/grouped");
    setGroupedServices(response.data);
  };
  fetchServices();
}, []);

// Render grouped by category
{
  groupedServices.map((group) => (
    <div key={group.categoryId}>
      <h3>{group.categoryName}</h3>
      {group.services.map((service) => (
        <ServiceCard
          key={service.serviceId}
          code={service.serviceCode}
          name={service.serviceName}
          duration={service.durationMinutes}
          price={service.price}
        />
      ))}
    </div>
  ));
}
```

---

## Conclusion

**Status:** ✅ **RESOLVED - Architecture Clarified**

- ✅ Two APIs serve different purposes (not duplicates)
- ✅ Frontend should use both APIs strategically
- ✅ Backend will add `categoryId` to Booking API (Priority 1)
- ✅ Service Category admin UI is FE task (medium priority)

**Next Steps:**

1. Backend: Implement Priority 1 enhancement
2. Frontend: Use dual-API approach immediately
3. Frontend: Create Service Category admin page

---

**Document Owner:** Backend Team
**Last Updated:** 2025-11-24
**Questions?** Contact Backend Team via Slack
