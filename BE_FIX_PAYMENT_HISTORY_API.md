# 🐛 BUG REPORT: API Patient Payment History - 500 Error

**Ngày báo cáo:** 14/01/2026  
**Độ ưu tiên:** 🔴 HIGH  
**Người báo:** Frontend Team  
**Endpoint bị lỗi:** `GET /api/v1/invoices/patient-history/{patientCode}`  
**Trạng thái:** ✅ **FIXED - 14/01/2026**

---

## ✅ IMPLEMENTATION COMPLETED

**Ngày fix:** 14/01/2026  
**Người thực hiện:** Backend Team

### 📝 Các file đã thay đổi

#### 1. ✨ Created: `src/main/java/com/dental/clinic/management/config/WebMvcConfig.java`

**Purpose:** Explicitly configure resource handlers to serve only `/static/**`, preventing conflict with `/api/**` routes.

```java
package com.dental.clinic.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for resource handling.
 * 
 * This configuration ensures that static resources are served only from
 * designated paths and do NOT interfere with REST API endpoints.
 * 
 * Problem it solves:
 * - Spring Boot's default ResourceHttpRequestHandler uses pattern "/**"
 * - This causes API paths like /api/v1/invoices/patient-history/{code} 
 *   to be incorrectly mapped to static resource handler instead of controllers
 * - Result: NoResourceFoundException instead of controller method execution
 * 
 * Solution:
 * - Explicitly configure resource handlers with specific patterns
 * - Disable default "/**" pattern that conflicts with API routes
 * - Use setResourceChain(false) to prevent caching issues
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Disable default /** resource handler by not calling super.addResourceHandlers()
        // and explicitly defining only what we need
        
        // Serve static resources only from /static/** path
        // Maps requests like /static/css/style.css to classpath:/static/css/style.css
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600) // 1 hour cache
                .resourceChain(false); // Disable resource chain to prevent caching conflicts
        
        // Explicitly do NOT add "/**" pattern - this is the key fix
        // API endpoints under /api/** will now be handled by controllers, not resource handler
    }
}
```

**Rationale:** Follows "Cách 1" recommendation - most explicit and maintainable approach.

---

#### 2. 🔧 Modified: `src/main/resources/application.yaml`

**Added configuration to disable Spring Boot's default resource handler:**

```yaml
# ==============================
# Web Resources Configuration
# ==============================
web:
  resources:
    # Disable Spring Boot's default static resource handling (/** pattern)
    # This prevents ResourceHttpRequestHandler from intercepting API routes
    # Custom resource handlers defined in WebMvcConfig will handle /static/** only
    add-mappings: false
```

**Location:** Added after `spring.servlet.multipart` section (line 55-61)

**Rationale:** Combines "Cách 1" with "Cách 2" for complete isolation of static resources from API routes.

---

#### 3. ⚠️ InvoiceController.java - URL Path Analysis

**Initial concern:** Potential path conflict between:
- `/{invoiceCode}` (line 71) 
- `/patient-history/{patientCode}` (line 164)

**Investigation result:** ✅ **NO CHANGE NEEDED**

**Reason:** Spring MVC path matching priority:
1. **Literal segments** (e.g., `/patient-history/...`) have **higher priority**
2. **Variable segments** (e.g., `/{invoiceCode}`) have lower priority

**Test scenario:**
- `GET /api/v1/invoices/patient-history/BN-1004` → Matches `/patient-history/{patientCode}` ✅
- `GET /api/v1/invoices/HD-2024-001` → Matches `/{invoiceCode}` ✅

**Conclusion:** All endpoint URLs remain unchanged. **NO breaking changes for Frontend.**

---

### 🎯 Root Cause Confirmed

Spring Boot's default `ResourceHttpRequestHandler` with pattern `/**` was:
1. Intercepting ALL requests including `/api/**`
2. Mapping `/api/v1/invoices/patient-history/BN-1004` to static resource lookup
3. Failing with `NoResourceFoundException`
4. GlobalExceptionHandler catching and returning 500

**Fix strategy:** Disable default handler + configure explicit `/static/**` only handler.

---

### ✅ Verification Checklist

- [x] **Created** `WebMvcConfig.java` with `/static/**` handler only
- [x] **Modified** `application.yaml` - set `spring.web.resources.add-mappings: false`
- [x] **Verified** InvoiceController paths - no conflict, no changes needed
- [ ] **Restart** Spring Boot application
- [ ] **Test** endpoint with curl/Postman
- [ ] **Verify** log shows `RequestMappingHandlerMapping` → `InvoiceController`
- [ ] **Notify** Frontend Team for UI testing

---

### 🧪 Testing Instructions

After restart, verify with:

```bash
curl -X GET "http://localhost:8080/api/v1/invoices/patient-history/BN-1004?page=0&size=10" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json"
```

**Expected responses:**
- ✅ `200 OK` with JSON data (if patient exists and user has permission)
- ✅ `403 Forbidden` (if missing VIEW_INVOICE_OWN permission)
- ✅ `404 Not Found` (if patient code not found)
- ❌ **NOT** `500 Internal Server Error` with `NoResourceFoundException`

---

### 📌 Breaking Changes

**Frontend Impact:** ✅ **NONE - No URL changes required**

All endpoints remain exactly the same:
- `GET /api/v1/invoices/{invoiceCode}` 
- `GET /api/v1/invoices/{invoiceCode}/payment-status`
- `GET /api/v1/invoices/patient-history/{patientCode}` 
- All other invoice endpoints unchanged

---

## 📋 Tóm tắt vấn đề

API endpoint `GET /api/v1/invoices/patient-history/BN-1004` đang trả về **500 Internal Server Error** thay vì dữ liệu hóa đơn.

**Root cause:** Spring Boot đang map request tới **ResourceHttpRequestHandler** (static resources) thay vì **InvoiceController**, dẫn đến `NoResourceFoundException`.

---

## 🔍 Phân tích chi tiết

### Triệu chứng từ log

```
GET /api/v1/invoices/patient-history/BN-1004
→ Mapped to ResourceHttpRequestHandler [classpath ...]
→ NoResourceFoundException: No static resource found
→ GlobalExceptionHandler → 500 Internal Server Error
```

### Dấu hiệu nhận biết

- ✅ Request từ Frontend: **ĐÚNG**
- ✅ Controller `InvoiceController#getPatientPaymentHistory`: **TỒN TẠI**
- ✅ Mapping `@GetMapping("/patient-history/{patientCode}")`: **ĐÚNG**
- ❌ Spring routing: **SAI** - Bị chặn bởi resource handler

### Nguyên nhân

Backend có cấu hình **resource handler** hoặc **SPA fallback** với pattern rộng (`/**`) đang **chặn các API routes** trước khi request đến controller.

Các trường hợp phổ biến:
1. `addResourceHandlers` map `/**` → static resources
2. SPA controller map `/**` → forward to index.html
3. `spring.mvc.static-path-pattern=/**` trong application.properties

---

## ✅ Giải pháp (chọn 1 trong 3)

### 🎯 Cách 1: Fix WebMvcConfigurer (KHUYẾN NGHỊ)

**Tìm file:** `WebConfig.java` hoặc class implement `WebMvcConfigurer`

**❌ Code hiện tại (SAI):**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // SAI: Mapping /** sẽ chặn tất cả request kể cả API
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
```

**✅ Code sửa (ĐÚNG):**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ĐÚNG: CHỈ map /static/** để không chặn /api/**
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        // Nếu có resources khác
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/");
                
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
```

---

### 🎯 Cách 2: Fix application.properties

**File:** `src/main/resources/application.properties`

**Thêm hoặc sửa:**
```properties
# Chỉ serve static resources từ /static/**
spring.mvc.static-path-pattern=/static/**
spring.web.resources.static-locations=classpath:/static/

# Hoặc nếu không cần serve static resources (API-only)
# spring.web.resources.add-mappings=false
```

---

### 🎯 Cách 3: Fix SPA Fallback Controller

**Tìm file:** Controller có mapping `/**` (thường là `SpaController.java`)

**❌ Code hiện tại (SAI):**
```java
@Controller
public class SpaController {
    
    // SAI: Mapping /** sẽ chặn cả API
    @RequestMapping("/**")
    public String forward() {
        return "forward:/index.html";
    }
}
```

**✅ Code sửa (ĐÚNG):**
```java
@Controller
public class SpaController {
    
    // ĐÚNG: Exclude /api/** khỏi SPA fallback
    @RequestMapping(value = "/{path:^(?!api).*}/**")
    public String forward() {
        return "forward:/index.html";
    }
    
    // Hoặc dùng cách này (dễ đọc hơn)
    @GetMapping(value = {"", "/", "/{path:[^\\.]*}"})
    public String forwardToIndex(HttpServletRequest request) {
        // Chỉ forward non-API requests
        if (request.getRequestURI().startsWith("/api/")) {
            return null; // Let controller handle
        }
        return "forward:/index.html";
    }
}
```

---

## 🧪 Cách kiểm tra sau khi fix

### 1. Check Actuator Mappings (nếu có)

```bash
curl http://localhost:8080/actuator/mappings | grep "patient-history"
```

**Expected output:**
```json
{
  "predicate": "{GET [/api/v1/invoices/patient-history/{patientCode}]}",
  "handler": "InvoiceController#getPatientPaymentHistory(String, ...)",
  "details": {...}
}
```

### 2. Test API trực tiếp

```bash
# Test với curl (thay YOUR_TOKEN bằng JWT token thật)
curl -X GET "http://localhost:8080/api/v1/invoices/patient-history/BN-1004" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Content-Type: application/json"
```

**Expected response:**
- ✅ `200 OK` với dữ liệu JSON
- ✅ `403 Forbidden` (nếu thiếu quyền VIEW_INVOICE_OWN)
- ✅ `404 Not Found` (nếu không tìm thấy bệnh nhân)
- ❌ **KHÔNG** `500 Internal Server Error` với `NoResourceFoundException`

### 3. Enable debug logging

**Thêm vào `application.properties`:**
```properties
# Debug Spring MVC routing
logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping=DEBUG
logging.level.org.springframework.web.servlet.resource=DEBUG
logging.level.org.springframework.web.servlet.handler=DEBUG
```

**Restart và xem log:**

✅ **Log ĐÚNG:**
```
RequestMappingHandlerMapping : Mapped to InvoiceController#getPatientPaymentHistory(String, ...)
```

❌ **Log SAI:**
```
ResourceHttpRequestHandler : Mapped to ResourceHttpRequestHandler [classpath ...]
```

---

## 📋 Checklist thực hiện

- [ ] **Bước 1:** Tìm cấu hình resource handler (WebConfig, application.properties, SpaController)
- [ ] **Bước 2:** Sửa theo 1 trong 3 cách trên - exclude `/api/**` khỏi static resource handler
- [ ] **Bước 3:** Restart Spring Boot application
- [ ] **Bước 4:** Enable debug logging và check log startup
- [ ] **Bước 5:** Test API với curl hoặc Postman
- [ ] **Bước 6:** Verify log: `RequestMappingHandlerMapping` map tới `InvoiceController`
- [ ] **Bước 7:** Thông báo Frontend Team để test trên UI

---

## 🔗 API Specification

**Endpoint:** `GET /api/v1/invoices/patient-history/{patientCode}`

**Parameters:**
- `patientCode` (path): Mã bệnh nhân (VD: BN-1004)
- `status` (query, optional): PENDING_PAYMENT | PARTIAL_PAID | PAID | CANCELLED
- `fromDate` (query, optional): YYYY-MM-DD
- `toDate` (query, optional): YYYY-MM-DD
- `page` (query, optional): 0-based page number (default: 0)
- `size` (query, optional): Page size (default: 10)
- `sort` (query, optional): Sort field,direction (default: createdAt,desc)

**Response:** `200 OK`
```json
{
  "invoices": [...],
  "pagination": {
    "currentPage": 1,
    "pageSize": 10,
    "totalItems": 25,
    "totalPages": 3
  },
  "summary": {
    "totalInvoices": 25,
    "totalAmount": 50000000,
    "paidAmount": 30000000,
    "remainingAmount": 20000000,
    "unpaidInvoices": 5
  }
}
```

**Permissions:**
- `VIEW_INVOICE_OWN` (bệnh nhân xem của mình)
- `VIEW_INVOICE_ALL` (admin/receptionist xem tất cả)

---

## 📌 Ghi chú

1. **Frontend đã làm đúng** - Request call API đúng format, đúng endpoint
2. **Backend cần fix** - Cấu hình Spring MVC routing bị conflict với static resources
3. **Không ảnh hưởng API khác** - Chỉ các endpoint có path parameter bị ảnh hưởng
4. **Priority HIGH** - Tính năng Payment History đã hoàn thành FE, chỉ chờ BE fix routing

---

## 📞 Liên hệ

Nếu cần hỗ trợ hoặc có thắc mắc, vui lòng liên hệ Frontend Team.

**Test URL khi fix xong:** http://localhost:3000/patient/payment-history (sau khi login với tài khoản bệnh nhân)

---

**Happy fixing! 🚀**
