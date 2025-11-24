# Backend API Enhancement - Service Management

**Date:** 2025-11-24  
**Version:** v1.1  
**Type:** ENHANCEMENT  
**Impact:** NON-BREAKING (Backward compatible)  
**For:** Frontend Team

---

## 📋 Summary

**What Changed:** Booking Service API (`/api/v1/booking/services`) now supports **category filtering** and includes **category information** in responses.

**Why:** Frontend requested ability to filter and group services by category (same as V17 Service API capability).

**Impact:** Frontend can now use a **single API** (Booking API) for both READ and WRITE operations, instead of using two separate APIs.

---

## ✨ What's New

### 1. New Fields in `ServiceResponse` DTO

**Added 3 category fields:**

```typescript
interface ServiceResponse {
  serviceId: number;
  serviceCode: string;
  serviceName: string;
  description: string;
  defaultDurationMinutes: number;
  defaultBufferMinutes: number;
  price: number;
  specializationId: number;
  specializationName: string;
  
  // ⭐ NEW FIELDS
  categoryId: number;          // Service category ID
  categoryCode: string;         // Service category code (e.g., "GENERAL")
  categoryName: string;         // Service category name (e.g., "Nha khoa tổng quát")
  
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
```

---

### 2. New Filter Parameter: `categoryId`

**GET `/api/v1/booking/services`** now accepts `categoryId` parameter.

#### Before (v1.0):
```bash
GET /api/v1/booking/services?page=0&size=10&isActive=true&specializationId=1
```

#### After (v1.1):
```bash
GET /api/v1/booking/services?page=0&size=10&categoryId=5&isActive=true
```

---

## 🔄 API Changes

### Endpoint: GET `/api/v1/booking/services`

**Method:** GET  
**Path:** `/api/v1/booking/services`  
**Auth:** Required (`VIEW_SERVICE` permission)

#### Query Parameters (Updated):

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `page` | number | No | Page number (0-indexed) | `0` |
| `size` | number | No | Page size (max 100) | `10` |
| `sortBy` | string | No | Sort field | `serviceId` |
| `sortDirection` | string | No | Sort direction (ASC/DESC) | `ASC` |
| `isActive` | boolean | No | Filter by active status | `true` |
| **`categoryId`** ⭐ | **number** | **No** | **Filter by category** | **`5`** |
| `specializationId` | number | No | Filter by specialization | `1` |
| `keyword` | string | No | Search by name/code | `cạo vôi` |

#### Response (Updated):

```json
{
  "content": [
    {
      "serviceId": 1,
      "serviceCode": "SV-CAOVOI",
      "serviceName": "Cạo vôi răng và Đánh bóng",
      "description": "Lấy sạch vôi răng...",
      "defaultDurationMinutes": 30,
      "defaultBufferMinutes": 10,
      "price": 300000,
      "specializationId": 1,
      "specializationName": "Nha khoa tổng quát",
      
      // ⭐ NEW FIELDS
      "categoryId": 5,
      "categoryCode": "GENERAL",
      "categoryName": "Nha khoa tổng quát",
      
      "isActive": true,
      "createdAt": "2025-10-29T10:30:00",
      "updatedAt": "2025-10-29T15:45:00"
    }
  ],
  "pageable": { ... },
  "totalElements": 45,
  "totalPages": 5
}
```

---

## 💻 Frontend Implementation Guide

### Option 1: Update Existing API Service (Recommended)

**File:** `src/services/serviceService.ts`

```typescript
// Update interface
interface ServiceResponse {
  serviceId: number;
  serviceCode: string;
  serviceName: string;
  description: string;
  defaultDurationMinutes: number;
  defaultBufferMinutes: number;
  price: number;
  specializationId?: number;
  specializationName?: string;
  
  // Add new fields
  categoryId?: number;
  categoryCode?: string;
  categoryName?: string;
  
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Update getAllServices method
async getAllServices(params: {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  isActive?: boolean;
  categoryId?: number;        // ⭐ Add this
  specializationId?: number;
  keyword?: string;
}): Promise<Page<ServiceResponse>> {
  const response = await axios.get('/api/v1/booking/services', { params });
  return response.data;
}
```

---

### Option 2: Use Category Filter in Admin Page

**File:** `src/pages/admin/services/page.tsx`

```typescript
import { useState, useEffect } from 'react';
import { Select } from 'antd';
import serviceService from '@/services/serviceService';
import serviceCategoryService from '@/services/serviceCategoryService';

export default function ServicesPage() {
  const [categoryId, setCategoryId] = useState<number>();
  const [categories, setCategories] = useState<ServiceCategory[]>([]);
  const [services, setServices] = useState<ServiceResponse[]>([]);

  // Load categories on mount
  useEffect(() => {
    const fetchCategories = async () => {
      const response = await serviceCategoryService.getAll();
      setCategories(response.data);
    };
    fetchCategories();
  }, []);

  // Load services when category filter changes
  useEffect(() => {
    const fetchServices = async () => {
      const response = await serviceService.getAllServices({
        page: 0,
        size: 20,
        categoryId: categoryId,  // ⭐ Use new filter
        isActive: true
      });
      setServices(response.content);
    };
    fetchServices();
  }, [categoryId]);

  return (
    <div>
      <h1>Service Management</h1>
      
      {/* Category Filter Dropdown */}
      <Select 
        placeholder="Filter by category"
        value={categoryId} 
        onChange={setCategoryId}
        allowClear
        style={{ width: 200 }}
      >
        {categories.map(cat => (
          <Select.Option key={cat.categoryId} value={cat.categoryId}>
            {cat.categoryName}
          </Select.Option>
        ))}
      </Select>

      {/* Services Table */}
      <Table
        dataSource={services}
        columns={[
          { title: 'Code', dataIndex: 'serviceCode' },
          { title: 'Name', dataIndex: 'serviceName' },
          // ⭐ Display category name
          { title: 'Category', dataIndex: 'categoryName' },
          { title: 'Price', dataIndex: 'price' },
          { title: 'Status', dataIndex: 'isActive' }
        ]}
      />
    </div>
  );
}
```

---

### Option 3: Group Services by Category

```typescript
// Group services by category
const groupedServices = services.reduce((acc, service) => {
  const categoryName = service.categoryName || 'Uncategorized';
  if (!acc[categoryName]) {
    acc[categoryName] = [];
  }
  acc[categoryName].push(service);
  return acc;
}, {} as Record<string, ServiceResponse[]>);

// Render grouped
{Object.entries(groupedServices).map(([categoryName, categoryServices]) => (
  <div key={categoryName}>
    <h3>{categoryName}</h3>
    <ul>
      {categoryServices.map(service => (
        <li key={service.serviceId}>
          {service.serviceName} - {service.price.toLocaleString()} VND
        </li>
      ))}
    </ul>
  </div>
))}
```

---

## 🔄 Migration Guide

### Before (Using 2 APIs):

```typescript
// READ: Use V17 API for category filtering
const readServices = async (categoryId: number) => {
  return axios.get('/api/v1/services', {
    params: { categoryId, isActive: true }
  });
};

// WRITE: Use Booking API for CRUD
const createService = async (data) => {
  return axios.post('/api/v1/booking/services', data);
};
```

### After (Using 1 API):

```typescript
// ⭐ Now you can use Booking API for BOTH
const getAllServices = async (categoryId?: number) => {
  return axios.get('/api/v1/booking/services', {
    params: { categoryId, isActive: true }
  });
};

const createService = async (data) => {
  return axios.post('/api/v1/booking/services', data);
};

const updateService = async (code: string, data) => {
  return axios.put(`/api/v1/booking/services/${code}`, data);
};

const deleteService = async (id: number) => {
  return axios.delete(`/api/v1/booking/services/${id}`);
};
```

---

## ✅ Testing Checklist

### 1. Test New Category Fields in Response

```bash
# Get all services - should include categoryId, categoryCode, categoryName
curl -X GET "http://localhost:8080/api/v1/booking/services?page=0&size=5" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "content": [
    {
      "serviceId": 1,
      "categoryId": 5,          // ⭐ Should be present
      "categoryCode": "GENERAL", // ⭐ Should be present
      "categoryName": "Nha khoa tổng quát" // ⭐ Should be present
    }
  ]
}
```

---

### 2. Test Category Filter

```bash
# Filter by category ID = 5
curl -X GET "http://localhost:8080/api/v1/booking/services?categoryId=5" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected:** Only services with `categoryId = 5` returned.

---

### 3. Test Combined Filters

```bash
# Filter by category + specialization + active status
curl -X GET "http://localhost:8080/api/v1/booking/services?categoryId=5&specializationId=1&isActive=true" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected:** Services matching ALL filters.

---

### 4. Test Backward Compatibility

```bash
# Old request without categoryId - should still work
curl -X GET "http://localhost:8080/api/v1/booking/services?isActive=true&specializationId=1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected:** Works as before, with new category fields included.

---

## 🐛 Known Issues / Limitations

### None - Fully backward compatible ✅

- ✅ Old requests without `categoryId` still work
- ✅ New fields are always present (can be `null` if service has no category)
- ✅ No breaking changes to existing endpoints
- ✅ No changes to request/response structure (only additions)

---

## 📊 Performance Impact

**Minimal - No significant performance change:**

- ✅ Category join already existed in database (just not exposed in API)
- ✅ LAZY fetch for category (only loaded when accessed)
- ✅ Index on `category_id` column (fast filtering)
- ✅ No N+1 query issues (single query with JOIN)

---

## 🔐 Security & Permissions

**No changes:**

- ✅ Same permissions as before: `VIEW_SERVICE`, `CREATE_SERVICE`, etc.
- ✅ No new permissions required
- ✅ Category filter respects existing authorization rules

---

## 📚 Related Documentation

- [Service API Architecture Clarification](./SERVICE_API_ARCHITECTURE_CLARIFICATION.md)
- [Service Management API Guide](./docs/api-guides/booking/service/402_Service_Management_API_Complete_Guide.md)
- [Service Category API Guide](./docs/api-guides/service-category/Service_Category_API_Guide.md)

---

## 🤝 Support

**Questions?** Contact Backend Team:
- Slack: `#backend-support`
- Email: backend-team@dental-clinic.com

**Issues?** Create ticket with label `service-api`

---

## 📝 Example: Complete Frontend Component

```typescript
// src/pages/admin/services/ServicesManagement.tsx
import { useState, useEffect } from 'react';
import { Table, Select, Button, Space, message } from 'antd';
import serviceService from '@/services/serviceService';
import serviceCategoryService from '@/services/serviceCategoryService';

export default function ServicesManagement() {
  const [services, setServices] = useState<ServiceResponse[]>([]);
  const [categories, setCategories] = useState<ServiceCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  });
  
  // Filters
  const [categoryId, setCategoryId] = useState<number>();
  const [isActive, setIsActive] = useState<boolean>(true);

  // Load categories
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await serviceCategoryService.getAll();
        setCategories(response.data);
      } catch (error) {
        message.error('Failed to load categories');
      }
    };
    fetchCategories();
  }, []);

  // Load services
  const fetchServices = async (page = 1) => {
    setLoading(true);
    try {
      const response = await serviceService.getAllServices({
        page: page - 1,
        size: pagination.pageSize,
        categoryId: categoryId,
        isActive: isActive,
        sortBy: 'serviceId',
        sortDirection: 'ASC'
      });
      
      setServices(response.content);
      setPagination(prev => ({
        ...prev,
        current: page,
        total: response.totalElements
      }));
    } catch (error) {
      message.error('Failed to load services');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchServices(1);
  }, [categoryId, isActive]);

  const columns = [
    {
      title: 'Code',
      dataIndex: 'serviceCode',
      key: 'serviceCode',
      width: 120
    },
    {
      title: 'Name',
      dataIndex: 'serviceName',
      key: 'serviceName'
    },
    {
      title: 'Category',
      dataIndex: 'categoryName',
      key: 'categoryName',
      width: 180,
      render: (text: string, record: ServiceResponse) => (
        <span>{record.categoryName || '-'}</span>
      )
    },
    {
      title: 'Price',
      dataIndex: 'price',
      key: 'price',
      width: 120,
      render: (price: number) => `${price.toLocaleString()} VND`
    },
    {
      title: 'Duration',
      key: 'duration',
      width: 100,
      render: (record: ServiceResponse) => 
        `${record.defaultDurationMinutes + record.defaultBufferMinutes} min`
    },
    {
      title: 'Status',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 100,
      render: (isActive: boolean) => (
        <span style={{ color: isActive ? 'green' : 'red' }}>
          {isActive ? 'Active' : 'Inactive'}
        </span>
      )
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 150,
      render: (record: ServiceResponse) => (
        <Space>
          <Button size="small" onClick={() => handleEdit(record)}>
            Edit
          </Button>
          <Button size="small" danger onClick={() => handleDelete(record.serviceId)}>
            Delete
          </Button>
        </Space>
      )
    }
  ];

  return (
    <div>
      <h1>Service Management</h1>
      
      <Space style={{ marginBottom: 16 }}>
        <Select
          placeholder="All Categories"
          value={categoryId}
          onChange={setCategoryId}
          allowClear
          style={{ width: 200 }}
        >
          {categories.map(cat => (
            <Select.Option key={cat.categoryId} value={cat.categoryId}>
              {cat.categoryName}
            </Select.Option>
          ))}
        </Select>

        <Select
          value={isActive}
          onChange={setIsActive}
          style={{ width: 150 }}
        >
          <Select.Option value={true}>Active</Select.Option>
          <Select.Option value={false}>Inactive</Select.Option>
        </Select>

        <Button type="primary" onClick={() => handleCreate()}>
          Create Service
        </Button>
      </Space>

      <Table
        dataSource={services}
        columns={columns}
        loading={loading}
        rowKey="serviceId"
        pagination={{
          ...pagination,
          onChange: fetchServices
        }}
      />
    </div>
  );
}
```

---

**Last Updated:** 2025-11-24  
**Backend Version:** v1.1  
**Status:** ✅ READY FOR FRONTEND INTEGRATION
