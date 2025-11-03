# Supplier Paging & Search - Frontend Integration Guide

## 📋 Tổng quan

Hướng dẫn tích hợp **Paging** và **Search** cho module Supplier với các tính năng:

- ✅ **Paging**: 10 nhà cung cấp/trang, tự động tính số trang
- ✅ **Search**: Tìm theo tên, SĐT, email, địa chỉ (không phân biệt hoa/thường, dấu)
- ✅ **Auto Sort**: Mới nhất lên đầu (CREATE/UPDATE)

---

## 🎯 Yêu cầu chức năng

### 1. Paging

- **10 items/page** (cố định)
- **Auto tính số trang**: 30 nhà cung cấp → 3 trang, 22 nhà cung cấp → 3 trang (10 + 10 + 2)
- **Hiển thị**: `Trang 1 / 3`, `Showing 1-10 of 30 suppliers`

### 2. Search

- **Fields**: `supplierName`, `phoneNumber`, `email`, `address`
- **Case-insensitive**: `ABC` = `abc` = `Abc`
- **Accent-insensitive**: `Quận Phú Nhuận` = `quan phu nhuan`
- **Partial match**: Address chứa "Quận Phú Nhuận" → match

### 3. Sort (Auto)

- **Mới tạo lên đầu**: Tạo supplier thứ 11 → hiển thị vị trí #1
- **Mới update lên đầu**: Update supplier bất kỳ → đẩy lên vị trí #1
- **Logic**: Sort by `updatedAt DESC`, then `createdAt DESC`

---

## 🔌 API Endpoints

### 1. GET All Suppliers (With Paging)

**Endpoint:** `GET /api/v1/suppliers`

**Query Parameters:**

| Parameter     | Type   | Default | Description                           |
| ------------- | ------ | ------- | ------------------------------------- |
| page          | int    | 0       | Page number (0-indexed)               |
| size          | int    | 10      | Items per page (max: 100)             |
| sortBy        | string | null    | Field to sort (default: newest first) |
| sortDirection | string | DESC    | ASC or DESC                           |

**Example Request:**

```bash
# Get first page (newest first)
GET /api/v1/suppliers?page=0&size=10

# Get page 2
GET /api/v1/suppliers?page=1&size=10

# Sort by name A-Z
GET /api/v1/suppliers?page=0&size=10&sortBy=supplierName&sortDirection=ASC
```

**Response:**

```json
{
  "content": [
    {
      "supplierId": 11,
      "supplierName": "Công ty Mới Nhất",
      "phoneNumber": "0901111111",
      "email": "newest@example.com",
      "address": "New Address",
      "status": "ACTIVE",
      "notes": "Vừa tạo xong",
      "createdAt": "2025-11-03T15:30:00",
      "updatedAt": null
    }
    // ... 9 items more
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 30,
  "totalPages": 3,
  "last": false,
  "first": true,
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "empty": false
}
```

---

### 2. Search Suppliers (With Paging)

**Endpoint:** `GET /api/v1/suppliers/search`

**Query Parameters:**

| Parameter | Type   | Default | Description               |
| --------- | ------ | ------- | ------------------------- |
| keyword   | string | null    | Search keyword            |
| page      | int    | 0       | Page number (0-indexed)   |
| size      | int    | 10      | Items per page (max: 100) |

**Example Requests:**

```bash
# Search by name
GET /api/v1/suppliers/search?keyword=ABC&page=0&size=10

# Search by phone
GET /api/v1/suppliers/search?keyword=0901234567&page=0&size=10

# Search by email
GET /api/v1/suppliers/search?keyword=@gmail.com&page=0&size=10

# Search by address (partial)
GET /api/v1/suppliers/search?keyword=quận phú nhuận&page=0&size=10

# Empty keyword = get all (same as GET /api/v1/suppliers)
GET /api/v1/suppliers/search?page=0&size=10
```

**Response:** Same structure as GET all suppliers

---

## 💻 Frontend Implementation

### TypeScript Types

```typescript
// types/supplier.ts
export interface Supplier {
  supplierId: number;
  supplierName: string;
  phoneNumber: string;
  email: string | null;
  address: string;
  status: "ACTIVE" | "INACTIVE" | "SUSPENDED";
  notes: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface PageableResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  empty: boolean;
}

export type SupplierPageResponse = PageableResponse<Supplier>;
```

---

### API Service

```typescript
// services/supplierService.ts
import axios from "axios";
import { Supplier, SupplierPageResponse } from "@/types/supplier";

const API_BASE_URL = "http://localhost:8080/api/v1";

export const supplierService = {
  /**
   * Get all suppliers with pagination
   * @param page - Page number (0-indexed)
   * @param size - Items per page (default: 10)
   * @param sortBy - Field to sort by (optional)
   * @param sortDirection - ASC or DESC (default: DESC)
   */
  async getAllSuppliers(
    page: number = 0,
    size: number = 10,
    sortBy?: string,
    sortDirection: "ASC" | "DESC" = "DESC"
  ): Promise<SupplierPageResponse> {
    const params: any = { page, size, sortDirection };
    if (sortBy) params.sortBy = sortBy;

    const response = await axios.get<SupplierPageResponse>(
      `${API_BASE_URL}/suppliers`,
      {
        params,
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      }
    );
    return response.data;
  },

  /**
   * Search suppliers by keyword
   * @param keyword - Search keyword
   * @param page - Page number (0-indexed)
   * @param size - Items per page (default: 10)
   */
  async searchSuppliers(
    keyword: string,
    page: number = 0,
    size: number = 10
  ): Promise<SupplierPageResponse> {
    const response = await axios.get<SupplierPageResponse>(
      `${API_BASE_URL}/suppliers/search`,
      {
        params: { keyword, page, size },
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      }
    );
    return response.data;
  },

  /**
   * Create new supplier
   */
  async createSupplier(data: {
    supplierName: string;
    phoneNumber: string;
    email?: string;
    address: string;
    notes?: string;
  }): Promise<Supplier> {
    const response = await axios.post<Supplier>(
      `${API_BASE_URL}/suppliers`,
      data,
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      }
    );
    return response.data;
  },

  /**
   * Update supplier
   */
  async updateSupplier(id: number, data: Partial<Supplier>): Promise<Supplier> {
    const response = await axios.put<Supplier>(
      `${API_BASE_URL}/suppliers/${id}`,
      data,
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      }
    );
    return response.data;
  },
};
```

---

### React Hook

```typescript
// hooks/useSuppliers.ts
import { useState, useEffect, useCallback } from "react";
import { supplierService } from "@/services/supplierService";
import { Supplier, SupplierPageResponse } from "@/types/supplier";

export const useSuppliers = () => {
  const [data, setData] = useState<SupplierPageResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10); // Fixed 10 items per page
  const [searchKeyword, setSearchKeyword] = useState("");

  /**
   * Fetch suppliers (with or without search)
   */
  const fetchSuppliers = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      let response: SupplierPageResponse;

      if (searchKeyword.trim()) {
        // Search mode
        response = await supplierService.searchSuppliers(
          searchKeyword,
          currentPage,
          pageSize
        );
      } else {
        // Normal mode
        response = await supplierService.getAllSuppliers(currentPage, pageSize);
      }

      setData(response);
    } catch (err: any) {
      setError(err.response?.data?.message || "Failed to fetch suppliers");
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, searchKeyword]);

  /**
   * Auto fetch on page/search change
   */
  useEffect(() => {
    fetchSuppliers();
  }, [fetchSuppliers]);

  /**
   * Go to specific page
   */
  const goToPage = (page: number) => {
    if (data && page >= 0 && page < data.totalPages) {
      setCurrentPage(page);
    }
  };

  /**
   * Search with keyword
   */
  const search = (keyword: string) => {
    setSearchKeyword(keyword);
    setCurrentPage(0); // Reset to first page
  };

  /**
   * Refresh list (after create/update/delete)
   */
  const refresh = () => {
    setCurrentPage(0);
    fetchSuppliers();
  };

  return {
    // Data
    suppliers: data?.content || [],
    totalElements: data?.totalElements || 0,
    totalPages: data?.totalPages || 0,
    currentPage,
    pageSize,
    isFirstPage: data?.first || false,
    isLastPage: data?.last || false,

    // State
    loading,
    error,

    // Actions
    goToPage,
    search,
    refresh,
  };
};
```

---

### React Component Example

```tsx
// components/SupplierList.tsx
import React, { useState } from "react";
import { useSuppliers } from "@/hooks/useSuppliers";
import { supplierService } from "@/services/supplierService";

export const SupplierList: React.FC = () => {
  const {
    suppliers,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    isFirstPage,
    isLastPage,
    loading,
    error,
    goToPage,
    search,
    refresh,
  } = useSuppliers();

  const [searchInput, setSearchInput] = useState("");

  /**
   * Handle search submit
   */
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    search(searchInput);
  };

  /**
   * Handle create supplier
   */
  const handleCreate = async () => {
    try {
      await supplierService.createSupplier({
        supplierName: "New Supplier",
        phoneNumber: "0901234567",
        address: "New Address",
      });
      refresh(); // Refresh list - new supplier will appear at top
    } catch (err) {
      console.error("Create failed:", err);
    }
  };

  /**
   * Handle update supplier
   */
  const handleUpdate = async (id: number) => {
    try {
      await supplierService.updateSupplier(id, {
        notes: "Updated at " + new Date().toISOString(),
      });
      refresh(); // Refresh list - updated supplier will move to top
    } catch (err) {
      console.error("Update failed:", err);
    }
  };

  return (
    <div className="supplier-list">
      {/* Search Bar */}
      <div className="search-section">
        <form onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Tìm theo tên, SĐT, email, địa chỉ..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
          <button type="submit">Tìm kiếm</button>
          <button
            type="button"
            onClick={() => {
              setSearchInput("");
              search("");
            }}
          >
            Xóa
          </button>
        </form>
      </div>

      {/* Stats */}
      <div className="stats">
        <p>
          Hiển thị {currentPage * pageSize + 1} -{" "}
          {Math.min((currentPage + 1) * pageSize, totalElements)} /{" "}
          {totalElements} nhà cung cấp
        </p>
        <p>
          Trang {currentPage + 1} / {totalPages}
        </p>
      </div>

      {/* Loading */}
      {loading && <div>Đang tải...</div>}

      {/* Error */}
      {error && <div className="error">{error}</div>}

      {/* Table */}
      {!loading && !error && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Tên</th>
              <th>SĐT</th>
              <th>Email</th>
              <th>Địa chỉ</th>
              <th>Trạng thái</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {suppliers.map((supplier) => (
              <tr key={supplier.supplierId}>
                <td>{supplier.supplierId}</td>
                <td>{supplier.supplierName}</td>
                <td>{supplier.phoneNumber}</td>
                <td>{supplier.email || "-"}</td>
                <td>{supplier.address}</td>
                <td>{supplier.status}</td>
                <td>
                  <button onClick={() => handleUpdate(supplier.supplierId)}>
                    Update
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* Empty State */}
      {!loading && suppliers.length === 0 && (
        <div className="empty-state">Không có nhà cung cấp nào</div>
      )}

      {/* Pagination */}
      <div className="pagination">
        <button
          onClick={() => goToPage(currentPage - 1)}
          disabled={isFirstPage}
        >
          ← Trước
        </button>

        {/* Page numbers */}
        {Array.from({ length: totalPages }, (_, i) => (
          <button
            key={i}
            onClick={() => goToPage(i)}
            className={currentPage === i ? "active" : ""}
          >
            {i + 1}
          </button>
        ))}

        <button onClick={() => goToPage(currentPage + 1)} disabled={isLastPage}>
          Sau →
        </button>
      </div>

      {/* Create Button */}
      <div className="actions">
        <button onClick={handleCreate}>+ Thêm nhà cung cấp</button>
      </div>
    </div>
  );
};
```

---

## 🧪 Test Scenarios

### 1. Paging Test

```bash
# Scenario: 30 suppliers total
# Expected: 3 pages, 10 items each

# Page 1: Suppliers 1-10 (newest)
GET /api/v1/suppliers?page=0&size=10
# Response: totalPages=3, totalElements=30, number=0, first=true, last=false

# Page 2: Suppliers 11-20
GET /api/v1/suppliers?page=1&size=10
# Response: number=1, first=false, last=false

# Page 3: Suppliers 21-30
GET /api/v1/suppliers?page=2&size=10
# Response: number=2, first=false, last=true
```

### 2. Search Test

```bash
# Search by name (case-insensitive)
GET /api/v1/suppliers/search?keyword=công ty abc
# Match: "Công ty ABC", "công ty abc pharma"

# Search by phone
GET /api/v1/suppliers/search?keyword=0901234567
# Match: "0901234567"

# Search by address (accent-insensitive, partial)
GET /api/v1/suppliers/search?keyword=quan phu nhuan
# Match: "Quận Phú Nhuận", "09 Lam Sơn, Phường 5, Quận Phú Nhuận"

# Search by email domain
GET /api/v1/suppliers/search?keyword=@gmail.com
# Match all emails ending with @gmail.com
```

### 3. Create/Update Test

```bash
# Before: 10 suppliers, newest is ID=10

# Create supplier #11
POST /api/v1/suppliers
{
  "supplierName": "New Supplier",
  "phoneNumber": "0901111111",
  "address": "New Address"
}
# Response: supplierId=11, createdAt="2025-11-03T15:30:00"

# Get page 1
GET /api/v1/suppliers?page=0&size=10
# Response: content[0].supplierId = 11 (newest on top!)

# Update supplier #5
PUT /api/v1/suppliers/5
{
  "notes": "Just updated"
}
# Response: supplierId=5, updatedAt="2025-11-03T15:35:00"

# Get page 1 again
GET /api/v1/suppliers?page=0&size=10
# Response: content[0].supplierId = 5 (moved to top!)
```

---

## 📊 Pagination Math

```typescript
// Utils for pagination calculations
export const paginationUtils = {
  /**
   * Calculate total pages
   * @param totalElements - Total number of items
   * @param pageSize - Items per page
   * @returns Total pages needed
   */
  calculateTotalPages(totalElements: number, pageSize: number): number {
    return Math.ceil(totalElements / pageSize);
  },

  /**
   * Get display range
   * @param page - Current page (0-indexed)
   * @param pageSize - Items per page
   * @param totalElements - Total items
   * @returns { start, end }
   */
  getDisplayRange(page: number, pageSize: number, totalElements: number) {
    const start = page * pageSize + 1;
    const end = Math.min((page + 1) * pageSize, totalElements);
    return { start, end };
  },

  /**
   * Get page numbers to display (with ellipsis)
   * @param currentPage - Current page (0-indexed)
   * @param totalPages - Total pages
   * @param maxVisible - Max page numbers to show
   * @returns Array of page numbers or 'ellipsis'
   */
  getPageNumbers(
    currentPage: number,
    totalPages: number,
    maxVisible: number = 7
  ): (number | "ellipsis")[] {
    if (totalPages <= maxVisible) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    const pages: (number | "ellipsis")[] = [];
    const halfVisible = Math.floor((maxVisible - 3) / 2);

    // Always show first page
    pages.push(0);

    if (currentPage <= halfVisible + 1) {
      // Near start
      for (let i = 1; i < maxVisible - 2; i++) {
        pages.push(i);
      }
      pages.push("ellipsis");
    } else if (currentPage >= totalPages - halfVisible - 2) {
      // Near end
      pages.push("ellipsis");
      for (let i = totalPages - maxVisible + 2; i < totalPages - 1; i++) {
        pages.push(i);
      }
    } else {
      // Middle
      pages.push("ellipsis");
      for (
        let i = currentPage - halfVisible;
        i <= currentPage + halfVisible;
        i++
      ) {
        pages.push(i);
      }
      pages.push("ellipsis");
    }

    // Always show last page
    pages.push(totalPages - 1);

    return pages;
  },
};

// Examples:
// 30 total, 10 per page = 3 pages
console.log(paginationUtils.calculateTotalPages(30, 10)); // 3

// 22 total, 10 per page = 3 pages (10 + 10 + 2)
console.log(paginationUtils.calculateTotalPages(22, 10)); // 3

// Page 0, size 10, total 30 = "1 - 10"
console.log(paginationUtils.getDisplayRange(0, 10, 30)); // { start: 1, end: 10 }

// Page 2, size 10, total 22 = "21 - 22"
console.log(paginationUtils.getDisplayRange(2, 10, 22)); // { start: 21, end: 22 }
```

---

## ✅ Success Criteria

- ✅ GET `/suppliers` trả về 10 items/page
- ✅ Tạo supplier mới → refresh → supplier mới ở vị trí #1
- ✅ Update supplier → refresh → supplier đã update ở vị trí #1
- ✅ Search "quận phú nhuận" → match "Quận Phú Nhuận" (accent-insensitive)
- ✅ Search "ABC" → match "abc", "Abc" (case-insensitive)
- ✅ 30 suppliers → 3 pages hiển thị
- ✅ 22 suppliers → 3 pages (10 + 10 + 2)
- ✅ Pagination controls disabled đúng (first page, last page)

---

## 🚨 Common Issues

### Issue 1: Unaccent không hoạt động

**Symptom:** Search "quan phu nhuan" không match "Quận Phú Nhuận"

**Solution:**

1. Check extension enabled: `SELECT * FROM pg_extension WHERE extname = 'unaccent';`
2. Run migration V1_11
3. Restart application

### Issue 2: Updated item không lên đầu

**Symptom:** Update supplier nhưng vẫn ở vị trí cũ

**Solution:**

1. Verify `updatedAt` timestamp được set: Check entity `@PreUpdate`
2. Verify sort order: `updatedAt DESC, createdAt DESC`
3. Refresh list sau khi update

### Issue 3: Paging không chính xác

**Symptom:** totalPages sai, hoặc items bị duplicate

**Solution:**

1. Check `totalElements` value
2. Verify `Math.ceil(totalElements / pageSize)`
3. Check không có duplicate trong DB

---

**Last Updated:** November 3, 2025  
**Version:** 1.0  
**Author:** BE-601 Team
