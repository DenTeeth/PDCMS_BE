# Supplier Management API - Test Guide# Supplier Management API - Test Guide

## 📋 Overview## 📋 Overview

Module quản lý **Nhà cung cấp vật tư y tế** đơn giản với thông tin liên hệ cơ bản.Module quản lý **Nhà cung cấp vật tư y tế** với xác minh chứng chỉ GPNK (Giấy phép nhập khẩu) và GMP.

**Base URL:** `http://localhost:8080/api/v1/suppliers`**Base URL:** `http://localhost:8080/api/v1/suppliers`

**Authentication:** Bearer Token (JWT)**Authentication:** Bearer Token (JWT)

**Required Permissions:\*\***Required Permissions:\*\*

- `VIEW_SUPPLIER` - Xem danh sách và chi tiết nhà cung cấp- `VIEW_SUPPLIER` - Xem danh sách và chi tiết nhà cung cấp

- `CREATE_SUPPLIER` - Tạo nhà cung cấp mới- `CREATE_SUPPLIER` - Tạo nhà cung cấp mới

- `UPDATE_SUPPLIER` - Cập nhật thông tin nhà cung cấp- `UPDATE_SUPPLIER` - Cập nhật thông tin nhà cung cấp

- `DELETE_SUPPLIER` - Xóa nhà cung cấp- `DELETE_SUPPLIER` - Xóa nhà cung cấp

---

## 🔐 Authentication Setup## 🔐 Authentication Setup

### Get JWT Token (Login First)### Get JWT Token (Login First)

**Endpoint:** `POST /api/v1/auth/login`**Endpoint:** `POST /api/v1/auth/login`

**Request Body:\*\***Request Body:\*\*

`json`json

{{

"username": "admin", "username": "admin",

"password": "admin123" "password": "admin123"

}}

````



**Response:****Response:**



```json```json

{{

  "success": true,  "success": true,

  "message": "Login successful",  "message": "Login successful",

  "data": {  "data": {

    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",

    "type": "Bearer",    "type": "Bearer",

    "username": "admin",    "username": "admin",

    "roles": ["ROLE_ADMIN"]    "roles": ["ROLE_ADMIN"]

  }  }

}}

````

**⚠️ Important:** Copy token value và sử dụng cho tất cả các API calls sau.**⚠️ Important:** Copy token value và sử dụng cho tất cả các API calls sau.

---

## 📝 API 1: CREATE SUPPLIER## 📝 API 1: CREATE SUPPLIER

### Description### Description

Tạo nhà cung cấp mới với thông tin liên hệ cơ bản.Tạo nhà cung cấp mới với thông tin chứng chỉ GPNK/GPKD (BẮT BUỘC).

### Required Permission### Required Permission

- `CREATE_SUPPLIER`- `CREATE_SUPPLIER`

---

### 🟦 SWAGGER TEST### 🟦 SWAGGER TEST

1. Navigate to: `http://localhost:8080/swagger-ui.html`1. Navigate to: `http://localhost:8080/swagger-ui.html`

2. Click **Authorize** button (top right)2. Click **Authorize** button (top right)

3. Enter: `Bearer {your_token}` → Click **Authorize**3. Enter: `Bearer {your_token}` → Click **Authorize**

4. Expand **Warehouse - Suppliers** section4. Expand **Supplier Management** section

5. Find **POST /api/v1/suppliers** → Click **Try it out**5. Find **POST /api/v1/suppliers** → Click **Try it out**

6. Paste request body → Click **Execute**6. Paste request body → Click **Execute**

**Request Body:\*\***Request Body:\*\*

`json`json

{{

"supplierName": "Công ty TNHH Dược Phẩm ABC", "supplierName": "Công ty TNHH Dược Phẩm ABC",

"phoneNumber": "0901234567", "phoneNumber": "0901234567",

"email": "contact@abc-pharma.vn", "email": "contact@abc-pharma.vn",

"address": "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh", "address": "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",

"notes": "Nhà cung cấp uy tín, chất lượng tốt" "certificationNumber": "GPNK-2024-001",

} "registrationDate": "2024-01-15",

```"expiryDate": "2026-01-14",

  "notes": "GMP certified, WHO approved supplier"

**Expected Response: 201 Created**}

```

````json

{**Expected Response: 201 Created**

  "supplierId": 1,

  "supplierName": "Công ty TNHH Dược Phẩm ABC",```json

  "phoneNumber": "0901234567",{

  "email": "contact@abc-pharma.vn",  "success": true,

  "address": "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",  "message": "Supplier created successfully",

  "status": "ACTIVE",  "data": {

  "notes": "Nhà cung cấp uy tín, chất lượng tốt",    "supplierId": 1,

  "createdAt": "2024-11-02T10:30:00",    "supplierName": "Công ty TNHH Dược Phẩm ABC",

  "updatedAt": null    "phoneNumber": "0901234567",

}    "email": "contact@abc-pharma.vn",

```    "address": "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",

    "certificationNumber": "GPNK-2024-001",

---    "registrationDate": "2024-01-15",

    "expiryDate": "2026-01-14",

### 🟩 POSTMAN TEST    "isVerified": false,

    "verificationDate": null,

**Method:** `POST`    "verificationBy": null,

    "rating": 0.0,

**URL:** `http://localhost:8080/api/v1/suppliers`    "totalTransactions": 0,

    "lastTransactionDate": null,

**Headers:**    "status": "ACTIVE",

    "notes": "GMP certified, WHO approved supplier",

```    "createdAt": "2024-11-02T10:30:00",

Authorization: Bearer {your_jwt_token}    "updatedAt": "2024-11-02T10:30:00"

Content-Type: application/json  }

```}

````

**Body (JSON):**

---

````json

{### 🟧 POSTMAN TEST

  "supplierName": "Công ty CP Thiết Bị Y Tế XYZ",

  "phoneNumber": "0912345678",1. **Method:** `POST`

  "email": "sales@xyz-medical.com",2. **URL:** `http://localhost:8080/api/v1/suppliers`

  "address": "456 Lê Lợi, Quận 3, TP. Hồ Chí Minh",3. **Headers:**

  "notes": "Chuyên cung cấp thiết bị nha khoa"   ```

}   Authorization: Bearer {your_token}

```   Content-Type: application/json

````

**Expected Status Code:** `201 Created`4. **Body:** (raw JSON)

````json

---   {

  "supplierName": "Công ty TNHH Dược Phẩm XYZ",

### ❌ Error Cases     "phoneNumber": "0912345678",

  "email": "info@xyz-medical.vn",

**1. Missing Required Fields**     "address": "456 Lê Lợi, Quận 1, TP. Hồ Chí Minh",

  "certificationNumber": "GPKD-2024-002",

```json     "registrationDate": "2024-02-20",

{     "expiryDate": "2027-02-19",

"supplierName": "",     "notes": "ISO 13485 certified medical device supplier"

"phoneNumber": "0901234567"   }

}   ```

````

**Expected Status:** `201 Created`

**Response: 400 Bad Request**

---

````json

{### ❌ Error Scenarios

  "success": false,

  "message": "Validation failed",**1. Missing Certification Number:**

  "errors": {

    "supplierName": "Tên nhà cung cấp không được để trống",```json

    "address": "Địa chỉ không được để trống"{

  }  "supplierName": "Test Supplier",

}  "phoneNumber": "0909999999",

```  "address": "Test Address"

  // Missing certificationNumber

**2. Invalid Phone Format**}

````

```````json

{**Response: 400 Bad Request**

  "supplierName": "Công ty ABC",

  "phoneNumber": "123",```json

  "address": "123 Street"{

}  "success": false,

```  "message": "Validation failed",

  "errors": {

**Response: 400 Bad Request**    "certificationNumber": "Certification number is required"

  }

```json}

{```

  "success": false,

  "message": "Số điện thoại không hợp lệ"**2. Duplicate Supplier Name:**

}

``````json

{

**3. Duplicate Supplier Name**  "supplierName": "Công ty TNHH Dược Phẩm ABC", // Already exists

  "phoneNumber": "0901111111",

```json  "certificationNumber": "GPNK-2024-003"

{}

  "supplierName": "Công ty TNHH Dược Phẩm ABC",```

  "phoneNumber": "0999999999",

  "address": "New Address"**Response: 409 Conflict**

}

``````json

{

**Response: 409 Conflict**  "success": false,

  "message": "Supplier with this name already exists"

```json}

{```

  "success": false,

  "message": "Nhà cung cấp với tên 'Công ty TNHH Dược Phẩm ABC' đã tồn tại"**3. Duplicate Certification Number:**

}

``````json

{

**4. Duplicate Phone Number**  "supplierName": "New Supplier",

  "phoneNumber": "0901111111",

```json  "certificationNumber": "GPNK-2024-001" // Already exists

{}

  "supplierName": "New Supplier",```

  "phoneNumber": "0901234567",

  "address": "New Address"**Response: 409 Conflict**

}

``````json

{

**Response: 409 Conflict**  "success": false,

  "message": "Supplier with this certification number already exists"

```json}

{```

  "success": false,

  "message": "Nhà cung cấp với số điện thoại '0901234567' đã tồn tại"**4. Invalid Phone Format:**

}

``````json

{

---  "phoneNumber": "123" // Too short

}

## 📝 API 2: GET ALL SUPPLIERS (Pagination)```



### Description**Response: 400 Bad Request**



Lấy danh sách tất cả nhà cung cấp với pagination và sorting.```json

{

### Required Permission  "success": false,

  "message": "Invalid phone number format"

- `VIEW_SUPPLIER` hoặc `CREATE_SUPPLIER`}

```````

---

---

### 🟦 SWAGGER TEST

## 📋 API 2: GET ALL SUPPLIERS (with Pagination)

1. Navigate to Swagger UI

2. Authorize with Bearer token### Description

3. Expand **Warehouse - Suppliers**

4. Find **GET /api/v1/suppliers** → Click **Try it out**Lấy danh sách tất cả nhà cung cấp với phân trang và sắp xếp.

5. Set parameters:

   - `page`: 0 (first page)### Required Permission

   - `size`: 20 (items per page)

   - `sortBy`: supplierName- `VIEW_SUPPLIER`

   - `sortDirection`: ASC

6. Click **Execute**---

**Expected Response: 200 OK**### 🟦 SWAGGER TEST

````json1. Expand **POST /api/v1/suppliers** endpoint

{2. Click **Try it out**

  "content": [3. Fill parameters:

    {   - `page`: 0 (default)

      "supplierId": 1,   - `size`: 10 (default)

      "supplierName": "Công ty TNHH Dược Phẩm ABC",   - `sortBy`: `supplier_name` (or `expiry_date`, `rating`, `created_at`)

      "phoneNumber": "0901234567",   - `sortDirection`: `ASC` or `DESC`

      "email": "contact@abc-pharma.vn",4. Click **Execute**

      "address": "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",

      "status": "ACTIVE",**Example URL:**

      "notes": "Nhà cung cấp uy tín, chất lượng tốt",

      "createdAt": "2024-11-02T10:30:00",```

      "updatedAt": nullGET /api/v1/suppliers?page=0&size=10&sortBy=expiry_date&sortDirection=ASC

    },```

    {

      "supplierId": 2,**Expected Response: 200 OK**

      "supplierName": "Công ty CP Thiết Bị Y Tế XYZ",

      "phoneNumber": "0912345678",```json

      "email": "sales@xyz-medical.com",{

      "address": "456 Lê Lợi, Quận 3, TP. Hồ Chí Minh",  "success": true,

      "status": "ACTIVE",  "message": "Suppliers retrieved successfully",

      "notes": "Chuyên cung cấp thiết bị nha khoa",  "data": {

      "createdAt": "2024-11-02T11:00:00",    "content": [

      "updatedAt": null      {

    }        "supplierId": 1,

  ],        "supplierName": "Công ty TNHH Dược Phẩm ABC",

  "pageable": {        "phoneNumber": "0901234567",

    "pageNumber": 0,        "email": "contact@abc-pharma.vn",

    "pageSize": 20,        "address": "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",

    "sort": {        "certificationNumber": "GPNK-2024-001",

      "sorted": true,        "registrationDate": "2024-01-15",

      "unsorted": false,        "expiryDate": "2026-01-14",

      "empty": false        "isVerified": true,

    }        "verificationDate": "2024-01-20",

  },        "verificationBy": "Nguyễn Văn A (Admin)",

  "totalElements": 2,        "rating": 4.5,

  "totalPages": 1,        "totalTransactions": 25,

  "last": true,        "lastTransactionDate": "2024-10-28",

  "first": true,        "status": "ACTIVE",

  "numberOfElements": 2,        "notes": "GMP certified, WHO approved supplier",

  "size": 20,        "createdAt": "2024-01-15T10:30:00",

  "number": 0,        "updatedAt": "2024-10-28T14:20:00"

  "sort": {      },

    "sorted": true,      {

    "unsorted": false,        "supplierId": 2,

    "empty": false        "supplierName": "Công ty TNHH Dược Phẩm XYZ",

  },        "phoneNumber": "0912345678",

  "empty": false        "email": "info@xyz-medical.vn",

}        "address": "456 Lê Lợi, Quận 1, TP. Hồ Chí Minh",

```        "certificationNumber": "GPKD-2024-002",

        "registrationDate": "2024-02-20",

---        "expiryDate": "2027-02-19",

        "isVerified": false,

### 🟩 POSTMAN TEST        "verificationDate": null,

        "verificationBy": null,

**Method:** `GET`        "rating": 0.0,

        "totalTransactions": 0,

**URL:** `http://localhost:8080/api/v1/suppliers?page=0&size=20&sortBy=supplierName&sortDirection=ASC`        "lastTransactionDate": null,

        "status": "ACTIVE",

**Headers:**        "notes": "ISO 13485 certified medical device supplier",

        "createdAt": "2024-02-20T09:15:00",

```        "updatedAt": "2024-02-20T09:15:00"

Authorization: Bearer {your_jwt_token}      }

```    ],

    "page_number": 0,

**Query Parameters:**    "page_size": 10,

    "total_elements": 2,

| Parameter       | Type   | Default      | Description                 |    "total_pages": 1,

| --------------- | ------ | ------------ | --------------------------- |    "last": true

| page            | int    | 0            | Page number (0-indexed)     |  }

| size            | int    | 20           | Items per page (max: 100)   |}

| sortBy          | string | supplierName | Field to sort by            |```

| sortDirection   | string | ASC          | ASC or DESC                 |

---

**Expected Status Code:** `200 OK`

### 🟧 POSTMAN TEST

---

**Test Case 1: Default Pagination**

### 🔍 Sorting Options

1. **Method:** `GET`

You can sort by any field:2. **URL:** `http://localhost:8080/api/v1/suppliers`

3. **Headers:**

- `supplierId` - Sort by ID   ```

- `supplierName` - Sort by name (default)   Authorization: Bearer {your_token}

- `phoneNumber` - Sort by phone   ```

- `status` - Sort by status

- `createdAt` - Sort by creation date**Expected:** Page 0, Size 10, sorted by `supplier_name` ASC



**Example URLs:**---



```**Test Case 2: Sort by Expiry Date (Cảnh báo hết hạn)**

# Sort by creation date (newest first)

GET /api/v1/suppliers?page=0&size=20&sortBy=createdAt&sortDirection=DESC1. **Method:** `GET`

2. **URL:** `http://localhost:8080/api/v1/suppliers?page=0&size=20&sortBy=expiry_date&sortDirection=ASC`

# Sort by status3. **Headers:**

GET /api/v1/suppliers?page=0&size=20&sortBy=status&sortDirection=ASC   ```

```   Authorization: Bearer {your_token}

````

---

**Use Case:** Hiển thị nhà cung cấp có giấy phép sắp hết hạn lên đầu.

## 📝 API 3: GET SUPPLIER BY ID

---

### Description

**Test Case 3: Sort by Rating (Top Suppliers)**

Lấy chi tiết nhà cung cấp theo ID.

1. **Method:** `GET`

### Required Permission2. **URL:** `http://localhost:8080/api/v1/suppliers?page=0&size=10&sortBy=rating&sortDirection=DESC`

3. **Headers:**

- `VIEW_SUPPLIER` hoặc `CREATE_SUPPLIER` ```

  Authorization: Bearer {your_token}

--- ```

### 🟦 SWAGGER TEST**Use Case:** Hiển thị nhà cung cấp có đánh giá cao nhất.

1. Navigate to Swagger UI---

2. Authorize with Bearer token

3. Expand **Warehouse - Suppliers**### Query Parameters Reference

4. Find **GET /api/v1/suppliers/{supplierId}** → Click **Try it out**

5. Enter `supplierId`: 1| Parameter | Type | Default | Description |

6. Click **Execute**| --------------- | ------- | --------------- | ---------------------------------------------------------------------------------- |

| `page` | Integer | 0 | Số trang (0-indexed) |

**Expected Response: 200 OK**| `size` | Integer | 10 | Số lượng items mỗi trang |

| `sortBy` | String | `supplier_name` | Field để sắp xếp: `supplier_name`, `expiry_date`, `rating`, `created_at`, `status` |

```json| `sortDirection`| String  |`ASC`          | Hướng sắp xếp:`ASC`hoặc`DESC` |

{

"supplierId": 1,---

"supplierName": "Công ty TNHH Dược Phẩm ABC",

"phoneNumber": "0901234567",## 🔍 API 3: GET SUPPLIER BY ID

"email": "contact@abc-pharma.vn",

"address": "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",### Description

"status": "ACTIVE",

"notes": "Nhà cung cấp uy tín, chất lượng tốt",Lấy thông tin chi tiết của một nhà cung cấp theo UUID.

"createdAt": "2024-11-02T10:30:00",

"updatedAt": null### Required Permission

}

```- `VIEW_SUPPLIER`

---

### 🟩 POSTMAN TEST### 🟦 SWAGGER TEST

**Method:** `GET`1. Expand **GET /api/v1/suppliers/{id}** endpoint

2. Click **Try it out**

**URL:** `http://localhost:8080/api/v1/suppliers/1`3. Enter `id`: `1` (supplier ID from create response)

4. Click **Execute**

**Headers:**

**Example URL:**

````

Authorization: Bearer {your_jwt_token}```

```GET /api/v1/suppliers/1

````

**Expected Status Code:** `200 OK`

**Expected Response: 200 OK**

---

````json

### ❌ Error Case: Supplier Not Found{

  "success": true,

**URL:** `GET /api/v1/suppliers/9999`  "message": "Supplier retrieved successfully",

  "data": {

**Response: 404 Not Found**    "supplierId": 1,

    "supplierName": "Công ty TNHH Dược Phẩm ABC",

```json    "phoneNumber": "0901234567",

{    "email": "contact@abc-pharma.vn",

  "success": false,    "address": "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",

  "message": "Không tìm thấy nhà cung cấp với ID: 9999"    "certificationNumber": "GPNK-2024-001",

}    "registrationDate": "2024-01-15",

```    "expiryDate": "2026-01-14",

    "isVerified": true,

---    "verificationDate": "2024-01-20",

    "verificationBy": "Nguyễn Văn A (Admin)",

## 📝 API 4: UPDATE SUPPLIER    "rating": 4.5,

    "totalTransactions": 25,

### Description    "lastTransactionDate": "2024-10-28",

    "status": "ACTIVE",

Cập nhật thông tin nhà cung cấp.    "notes": "GMP certified, WHO approved supplier",

    "createdAt": "2024-01-15T10:30:00",

### Required Permission    "updatedAt": "2024-10-28T14:20:00"

  }

- `UPDATE_SUPPLIER`}

````

---

---

### 🟦 SWAGGER TEST

### 🟧 POSTMAN TEST

1. Navigate to Swagger UI

2. Authorize with Bearer token1. **Method:** `GET`

3. Expand **Warehouse - Suppliers**2. **URL:** `http://localhost:8080/api/v1/suppliers/{supplier_id}`

4. Find **PUT /api/v1/suppliers/{supplierId}** → Click **Try it out** - Replace `{supplier_id}` với ID thực tế (ví dụ: 1, 2, 3...)

5. Enter `supplierId`: 13. **Headers:**

6. Paste request body → Click **Execute** ```

   Authorization: Bearer {your_token}

**Request Body (Partial Update):** ```

````json**Example:**

{

  "phoneNumber": "0987654321",```

  "email": "new-contact@abc-pharma.vn",GET http://localhost:8080/api/v1/suppliers/1

  "status": "INACTIVE",```

  "notes": "Đã chuyển số hotline mới"

}**Expected Status:** `200 OK`

````

---

**Expected Response: 200 OK**

### ❌ Error Scenarios

````json

{**1. Supplier Not Found:**

  "supplierId": 1,

  "supplierName": "Công ty TNHH Dược Phẩm ABC",**Request:**

  "phoneNumber": "0987654321",

  "email": "new-contact@abc-pharma.vn",```

  "address": "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",GET /api/v1/suppliers/999

  "status": "INACTIVE",```

  "notes": "Đã chuyển số hotline mới",

  "createdAt": "2024-11-02T10:30:00",**Response: 404 Not Found**

  "updatedAt": "2024-11-02T15:45:00"

}```json

```{

  "success": false,

---  "message": "Supplier not found with ID: 999"

}

### 🟩 POSTMAN TEST```



**Method:** `PUT`**2. Invalid ID Format:**



**URL:** `http://localhost:8080/api/v1/suppliers/1`**Request:**



**Headers:**```

GET /api/v1/suppliers/abc

````

Authorization: Bearer {your_jwt_token}

Content-Type: application/json**Response: 400 Bad Request**

````

```json

**Body (JSON) - Update Full Info:**{

  "success": false,

```json  "message": "Invalid supplier ID format"

{}

  "supplierName": "Công ty TNHH Dược Phẩm ABC - Chi nhánh HN",```

  "phoneNumber": "0243567890",

  "email": "hanoi@abc-pharma.vn",---

  "address": "789 Hoàng Quốc Việt, Cầu Giấy, Hà Nội",

  "status": "ACTIVE",## ✏️ API 4: UPDATE SUPPLIER

  "notes": "Chi nhánh mới tại Hà Nội"

}### Description

````

Cập nhật thông tin nhà cung cấp (partial update - chỉ cần gửi fields cần update).

**Expected Status Code:** `200 OK`

### Required Permission

---

- `UPDATE_SUPPLIER`

### ⚙️ Update Rules

---

- **Partial Update:** Chỉ các fields được gửi lên sẽ được cập nhật

- **Unchanged Fields:** Giữ nguyên giá trị cũ### 🟦 SWAGGER TEST

- **Validation:** Phone number format được kiểm tra nếu có update

1. Expand **PUT /api/v1/suppliers/{id}** endpoint

**Example: Only Update Status**2. Click **Try it out**

3. Enter `id`: UUID của supplier cần update

```json4. Paste request body (chỉ fields cần update)

{5. Click **Execute**

  "status": "SUSPENDED"

}---

```

### Update Scenarios

---

**Scenario 1: Admin Verify Supplier (Xác minh nhà cung cấp)**

### ❌ Error Cases

**Request Body:**

**1. Duplicate Supplier Name**

````json

```json{

{  "isVerified": true,

  "supplierName": "Existing Supplier Name"  "verificationDate": "2024-11-02",

}  "verificationBy": "Nguyễn Văn A (Admin)"

```}

````

**Response: 409 Conflict**

**Use Case:** Admin đã kiểm tra GPNK, xác nhận nhà cung cấp hợp lệ.

````json

{---

  "success": false,

  "message": "Nhà cung cấp với tên 'Existing Supplier Name' đã tồn tại"**Scenario 2: Update Contact Information**

}

```**Request Body:**



**2. Duplicate Phone Number**```json

{

```json  "phoneNumber": "0901234568",

{  "email": "new-contact@abc-pharma.vn",

  "phoneNumber": "0901234567"  "address": "789 Hai Bà Trưng, Quận 3, TP. Hồ Chí Minh"

}}

````

**Response: 409 Conflict**---

````json**Scenario 3: Renew Certification (Gia hạn GPNK)**

{

  "success": false,**Request Body:**

  "message": "Nhà cung cấp với số điện thoại '0901234567' đã tồn tại"

}```json

```{

  "certificationNumber": "GPNK-2025-001",

**3. Supplier Not Found**  "registrationDate": "2025-01-15",

  "expiryDate": "2028-01-14",

**URL:** `PUT /api/v1/suppliers/9999`  "notes": "GMP renewed for 3 years, WHO approved"

}

**Response: 404 Not Found**```



```json---

{

  "success": false,**Scenario 4: Update Rating After Transactions**

  "message": "Không tìm thấy nhà cung cấp với ID: 9999"

}**Request Body:**

````

````json

---{

  "rating": 4.8,

## 📝 API 5: DELETE SUPPLIER  "notes": "Excellent delivery quality and timing"

}

### Description```



Xóa nhà cung cấp khỏi hệ thống.---



### Required Permission**Scenario 5: Suspend Supplier (Đình chỉ nhà cung cấp)**



- `DELETE_SUPPLIER`**Request Body:**



---```json

{

### 🟦 SWAGGER TEST  "status": "SUSPENDED",

  "notes": "Certificate expired, awaiting renewal. Do not place new orders."

1. Navigate to Swagger UI}

2. Authorize with Bearer token```

3. Expand **Warehouse - Suppliers**

4. Find **DELETE /api/v1/suppliers/{supplierId}** → Click **Try it out**---

5. Enter `supplierId`: 1

6. Click **Execute****Expected Response: 200 OK**



**Expected Response: 200 OK**```json

{

```json  "success": true,

{  "message": "Supplier updated successfully",

  "message": "Xóa nhà cung cấp thành công"  "data": {

}    "supplierId": 1,

```    "supplierName": "Công ty TNHH Dược Phẩm ABC",

    "phoneNumber": "0901234568",

---    "email": "new-contact@abc-pharma.vn",

    "address": "789 Hai Bà Trưng, Quận 3, TP. Hồ Chí Minh",

### 🟩 POSTMAN TEST    "certificationNumber": "GPNK-2024-001",

    "registrationDate": "2024-01-15",

**Method:** `DELETE`    "expiryDate": "2026-01-14",

    "isVerified": true,

**URL:** `http://localhost:8080/api/v1/suppliers/1`    "verificationDate": "2024-11-02",

    "verificationBy": "Nguyễn Văn A (Admin)",

**Headers:**    "rating": 4.5,

    "totalTransactions": 25,

```    "lastTransactionDate": "2024-10-28",

Authorization: Bearer {your_jwt_token}    "status": "ACTIVE",

```    "notes": "GMP certified, WHO approved supplier",

    "createdAt": "2024-01-15T10:30:00",

**Expected Status Code:** `200 OK`    "updatedAt": "2024-11-02T15:45:00"

  }

**Response Body:**}

````

```json

{---

  "message": "Xóa nhà cung cấp thành công"

}### 🟧 POSTMAN TEST

```

1. **Method:** `PUT`

---2. **URL:** `http://localhost:8080/api/v1/suppliers/{supplier_id}`

3. **Headers:**

### ❌ Error Case: Supplier Not Found ```

Authorization: Bearer {your_token}

**URL:** `DELETE /api/v1/suppliers/9999` Content-Type: application/json

````

**Response: 404 Not Found**4. **Body:** (raw JSON - partial update)

```json

```json   {

{     "isVerified": true,

"success": false,     "verificationDate": "2024-11-02",

"message": "Không tìm thấy nhà cung cấp với ID: 9999"     "verificationBy": "Admin User"

}   }

```   ```



---**Expected Status:** `200 OK`



## 🗂️ Data Model---



### Supplier Fields### ❌ Error Scenarios



| Field         | Type         | Required | Description                          |**1. Supplier Not Found:**

| ------------- | ------------ | -------- | ------------------------------------ |

| supplierId    | Long         | ✅       | Auto-increment ID (starts from 1)    |```json

| supplierName  | String(255)  | ✅       | Tên nhà cung cấp (unique)            |{

| phoneNumber   | String(20)   | ✅       | Số điện thoại (unique, format: VN)   |  "success": false,

| email         | String(100)  | ❌       | Email liên hệ                        |  "message": "Supplier not found with ID: {invalid_id}"

| address       | Text         | ✅       | Địa chỉ                              |}

| status        | String(20)   | ✅       | ACTIVE, INACTIVE, SUSPENDED          |```

| notes         | Text         | ❌       | Ghi chú                              |

| createdAt     | Timestamp    | ✅       | Thời gian tạo (auto)                 |**2. Duplicate Phone Number:**

| updatedAt     | Timestamp    | ❌       | Thời gian cập nhật cuối (auto)       |

```json

### Status Values{

"phoneNumber": "0901234567" // Already used by another supplier

- `ACTIVE` - Đang hoạt động (default)}

- `INACTIVE` - Tạm ngưng```

- `SUSPENDED` - Đình chỉ

**Response: 409 Conflict**

### Phone Number Format

```json

Accepts Vietnamese phone formats:{

"success": false,

- `0901234567` (10 digits starting with 0)  "message": "Supplier with this phone number already exists"

- `0243567890` (11 digits starting with 02)}

- `+84901234567` (international format)```



**Regex:** `^(\\+84|0)[0-9]{9,10}$`**3. Invalid Rating:**



---```json

{

## 🧪 Complete Test Workflow  "rating": 6.0 // Must be 0.0-5.0

}

### Step-by-step Testing Guide```



```bash**Response: 400 Bad Request**

# 1. LOGIN

POST /api/v1/auth/login```json

{{

"username": "admin",  "success": false,

"password": "admin123"  "message": "Rating must be between 0.0 and 5.0"

}}

# → Save JWT token```



# 2. CREATE SUPPLIER #1**4. Invalid Status:**

POST /api/v1/suppliers

Authorization: Bearer {token}```json

{{

"supplierName": "Công ty TNHH ABC",  "status": "INVALID_STATUS"

"phoneNumber": "0901234567",}

"email": "abc@example.com",```

"address": "123 Street, District 1, HCMC",

"notes": "Primary supplier"**Response: 400 Bad Request**

}

# → Expect: 201 Created, supplierId = 1```json

{

# 3. CREATE SUPPLIER #2  "success": false,

POST /api/v1/suppliers  "message": "Status must be one of: ACTIVE, INACTIVE, SUSPENDED"

{}

"supplierName": "Công ty CP XYZ",```

"phoneNumber": "0912345678",

"email": "xyz@example.com",---

"address": "456 Street, District 3, HCMC"

}## 🗑️ API 5: DELETE SUPPLIER

# → Expect: 201 Created, supplierId = 2

### Description

# 4. GET ALL SUPPLIERS

GET /api/v1/suppliers?page=0&size=20&sortBy=supplierName&sortDirection=ASCXóa nhà cung cấp khỏi hệ thống.

# → Expect: 200 OK, 2 suppliers in content array

**⚠️ Warning:**

# 5. GET SUPPLIER BY ID

GET /api/v1/suppliers/1- Không thể xóa nếu nhà cung cấp đã có giao dịch (`total_transactions > 0`)

# → Expect: 200 OK, supplier details- Nên set `status = "INACTIVE"` thay vì xóa vĩnh viễn



# 6. UPDATE SUPPLIER### Required Permission

PUT /api/v1/suppliers/1

{- `DELETE_SUPPLIER`

"phoneNumber": "0987654321",

"status": "INACTIVE"---

}

# → Expect: 200 OK, updatedAt timestamp changed### 🟦 SWAGGER TEST



# 7. DELETE SUPPLIER1. Expand **DELETE /api/v1/suppliers/{id}** endpoint

DELETE /api/v1/suppliers/22. Click **Try it out**

# → Expect: 200 OK with success message3. Enter `id`: Supplier ID cần xóa (ví dụ: 2)

4. Click **Execute**

# 8. VERIFY DELETION

GET /api/v1/suppliers/2**Example URL:**

# → Expect: 404 Not Found

````

DELETE /api/v1/suppliers/2

---```

## 🚨 Common Issues & Solutions**Expected Response: 200 OK**

### 1. 401 Unauthorized```json

{

**Problem:** Missing or invalid JWT token "success": true,

"message": "Supplier deleted successfully",

**Solution:** "data": null

}

- Login again to get fresh token```

- Check token format: `Bearer {token}`

- Verify token hasn't expired---

### 2. 403 Forbidden### 🟧 POSTMAN TEST

**Problem:** User lacks required permission1. **Method:** `DELETE`

2. **URL:** `http://localhost:8080/api/v1/suppliers/{supplier_id}`

**Solution:**3. **Headers:**

````

- Check user role (ADMIN or STAFF)   Authorization: Bearer {your_token}

- Verify permission assignment in seed data   ```

- Use ADMIN account for CREATE/UPDATE/DELETE operations

**Example:**

### 3. 400 Bad Request - Validation Error

````

**Problem:** Invalid input dataDELETE http://localhost:8080/api/v1/suppliers/2

````

**Solutions:**

**Expected Status:** `200 OK`

- Check all required fields are provided

- Verify phone number format (VN format)---

- Ensure supplier name max 255 characters

### ❌ Error Scenarios

### 4. 409 Conflict - Duplicate Entry

**1. Supplier Not Found:**

**Problem:** Supplier name or phone number already exists

**Response: 404 Not Found**

**Solutions:**

```json

- Use unique supplier name{

- Use different phone number  "success": false,

- Check existing suppliers before creating  "message": "Supplier not found with ID: {invalid_id}"

}

### 5. 404 Not Found```



**Problem:** Supplier ID doesn't exist**2. Supplier Has Transactions (Cannot Delete):**



**Solutions:****Response: 409 Conflict**



- Verify supplier ID from GET all suppliers```json

- Check if supplier was deleted{

- Use valid existing ID  "success": false,

  "message": "Cannot delete supplier with existing transactions. Set status to INACTIVE instead."

---}

````

## 📊 Sample Data for Testing

**Alternative:** Use UPDATE to set `status = "INACTIVE"`

````json

[```json

  {PUT /api/v1/suppliers/{id}

    "supplierName": "Công ty TNHH Dược Phẩm Hà Nội",{

    "phoneNumber": "0241234567",  "status": "INACTIVE",

    "email": "hanoi@pharma.vn",  "notes": "Supplier discontinued, marked inactive instead of deletion"

    "address": "100 Láng Hạ, Đống Đa, Hà Nội",}

    "notes": "Chuyên dược phẩm nhập khẩu"```

  },

  {---

    "supplierName": "Công ty CP Thiết Bị Y Tế TP.HCM",

    "phoneNumber": "0281234567",## 🧪 Complete Testing Workflow

    "email": "hcm@medical-equip.vn",

    "address": "200 Nguyễn Văn Linh, Quận 7, TP.HCM",### Step-by-Step Test Sequence

    "notes": "Thiết bị nha khoa cao cấp"

  },```

  {1. LOGIN

    "supplierName": "Công ty TNHH Vật Tư Nha Khoa Đà Nẵng",   POST /api/v1/auth/login

    "phoneNumber": "0236123456",   → Save JWT token

    "email": "danang@dental.vn",

    "address": "300 Hùng Vương, Hải Châu, Đà Nẵng",2. CREATE SUPPLIER #1

    "notes": "Vật tư tiêu hao"   POST /api/v1/suppliers

  }   → Save supplier_id_1

]

```3. CREATE SUPPLIER #2

   POST /api/v1/suppliers

---   → Save supplier_id_2



## ✅ Success Criteria4. GET ALL SUPPLIERS

   GET /api/v1/suppliers?page=0&size=10

Test được coi là hoàn thành khi:   → Verify 2 suppliers returned



- ✅ Tạo supplier thành công với Long ID (1, 2, 3...)5. GET SUPPLIER BY ID

- ✅ Response JSON đúng camelCase format   GET /api/v1/suppliers/{supplier_id_1}

- ✅ Pagination hoạt động (page, size, sort)   → Verify details match

- ✅ Validation lỗi đúng format (400, 404, 409)

- ✅ DELETE trả về message thay vì 200 OK rỗng6. UPDATE SUPPLIER (Admin Verify)

- ✅ Permission kiểm soát đúng (ADMIN vs STAFF)   PUT /api/v1/suppliers/{supplier_id_1}

- ✅ Unique constraints hoạt động (name, phone)   → Set is_verified = true

- ✅ Timestamp audit (createdAt, updatedAt) tự động

7. GET SUPPLIER BY ID (Verify Update)

---   GET /api/v1/suppliers/{supplier_id_1}

   → Check is_verified = true

## 🎯 Next Steps

8. UPDATE SUPPLIER (Change Status)

After completing Supplier API tests:   PUT /api/v1/suppliers/{supplier_id_2}

   → Set status = "INACTIVE"

1. **Inventory Module:** Test inventory management với supplier FK

2. **Integration Test:** Verify supplier → inventory relationship9. GET ALL SUPPLIERS (Sort by Status)

3. **Performance Test:** Load test với 1000+ suppliers   GET /api/v1/suppliers?sortBy=status&sortDirection=ASC

4. **Frontend Integration:** Connect React/Vue with API endpoints   → Verify ACTIVE suppliers first



---10. DELETE SUPPLIER

    DELETE /api/v1/suppliers/{supplier_id_2}

## 📞 Support    → Verify deletion successful



For issues or questions:11. GET ALL SUPPLIERS (Final Check)

    GET /api/v1/suppliers

- Check Swagger documentation: `http://localhost:8080/swagger-ui.html`    → Verify only 1 supplier remains

- Review error messages in response body```

- Check application logs for detailed stack traces

- Verify database state using PostgreSQL client---



---## 📊 Database Verification Queries



**Last Updated:** November 3, 2024  ### Check Suppliers in Database

**Version:** 2.0 (Simplified)

**Author:** BE-601 Team```sql

-- View all suppliers
SELECT
    supplier_name,
    phone_number,
    certification_number,
    expiry_date,
    is_verified,
    status,
    rating
FROM suppliers
ORDER BY created_at DESC;

-- Check certificates expiring soon (< 90 days)
SELECT
    supplier_name,
    certification_number,
    expiry_date,
    expiry_date - CURRENT_DATE AS days_until_expiry
FROM suppliers
WHERE expiry_date < CURRENT_DATE + INTERVAL '90 days'
  AND status = 'ACTIVE'
ORDER BY expiry_date ASC;

-- Top rated suppliers
SELECT
    supplier_name,
    rating,
    total_transactions,
    status
FROM suppliers
WHERE is_verified = TRUE
  AND status = 'ACTIVE'
ORDER BY rating DESC
LIMIT 10;

-- Unverified suppliers (pending admin verification)
SELECT
    supplier_name,
    certification_number,
    registration_date,
    created_at
FROM suppliers
WHERE is_verified = FALSE
ORDER BY created_at DESC;
````

---

## 🔍 Common Issues & Solutions

### Issue 1: 401 Unauthorized

**Cause:** JWT token expired or invalid

**Solution:**

```bash
# Re-login to get new token
POST /api/v1/auth/login
```

---

### Issue 2: 403 Forbidden

**Cause:** User không có permission

**Solution:**

- Check user roles: `ROLE_ADMIN` hoặc `ROLE_INVENTORY_MANAGER`
- Verify permissions in database:

```sql
SELECT p.permission_name
FROM user_permissions up
JOIN base_permissions p ON up.permission_id = p.permission_id
WHERE up.user_id = '{your_user_id}';
```

---

### Issue 3: 409 Duplicate Entry

**Cause:** Supplier name, phone, hoặc certification_number đã tồn tại

**Solution:**

```sql
-- Check existing suppliers
SELECT supplier_name, phone_number, certification_number
FROM suppliers
WHERE supplier_name = 'Công ty TNHH Dược Phẩm ABC'
   OR phone_number = '0901234567'
   OR certification_number = 'GPNK-2024-001';
```

---

### Issue 4: Pagination Returns Empty

**Cause:** `page` parameter quá lớn

**Solution:**

```bash
# Check total pages first
GET /api/v1/suppliers?page=0&size=10
# Response contains: "total_pages": 3

# Then request valid page
GET /api/v1/suppliers?page=2&size=10  # Not page=10
```

---

## 🎯 Best Practices

### 1. Always Validate Certification Expiry

```bash
# Before creating purchase orders, check supplier status
GET /api/v1/suppliers/{id}

# Verify:
# - is_verified = true
# - status = "ACTIVE"
# - expiry_date > CURRENT_DATE
```

### 2. Use Pagination for Large Lists

```bash
# DON'T: Get all suppliers at once
GET /api/v1/suppliers?size=10000

# DO: Use reasonable page sizes
GET /api/v1/suppliers?page=0&size=20
```

### 3. Sort by Expiry Date for Alerts

```bash
# Show suppliers needing certificate renewal
GET /api/v1/suppliers?sortBy=expiry_date&sortDirection=ASC
```

### 4. Soft Delete Instead of Hard Delete

```bash
# DON'T: DELETE /api/v1/suppliers/{id}

# DO: Mark inactive
PUT /api/v1/suppliers/{id}
{
  "status": "INACTIVE",
  "notes": "Supplier discontinued as of 2024-11-02"
}
```

---

## 📧 Contact & Support

- **API Documentation:** `http://localhost:8080/swagger-ui.html`
- **Module:** Warehouse Management - Suppliers
- **Version:** 1.0
- **Last Updated:** November 2, 2024

---

## ✅ Testing Checklist

- [ ] Login successful and JWT token obtained
- [ ] Create supplier with valid certification_number
- [ ] Create fails with duplicate name/phone/certification
- [ ] Get all suppliers returns paginated results
- [ ] Pagination works (page 0, 1, 2...)
- [ ] Sorting works (by name, expiry_date, rating, status)
- [ ] Get by ID returns correct supplier details
- [ ] Get by ID fails with 404 for invalid UUID
- [ ] Update supplier contact information
- [ ] Admin can verify supplier (set is_verified = true)
- [ ] Update supplier certification (renewal)
- [ ] Update supplier status (ACTIVE → INACTIVE → SUSPENDED)
- [ ] Update fails with duplicate phone/certification
- [ ] Delete supplier without transactions
- [ ] Delete fails for supplier with transactions
- [ ] 401 error when token missing/expired
- [ ] 403 error when user lacks permissions

---

**🎉 Happy Testing!**

Nếu gặp lỗi không nằm trong guide này, check logs tại `target/logs/application.log`.
