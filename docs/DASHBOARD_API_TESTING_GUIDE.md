# Dashboard API Testing Guide

## Prerequisites
- Application must be running on `http://localhost:8080`
- User must be authenticated with ADMIN or MANAGER role
- Get JWT token from login endpoint first

## Authentication
```bash
# Login first to get JWT token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "your_password"
  }'

# Use the returned token in subsequent requests
TOKEN="your_jwt_token_here"
```

## 1. Test Overview Statistics

```bash
# Current month without comparison
curl -X GET "http://localhost:8080/api/v1/dashboard/overview?month=2026-01&compareWithPrevious=false" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# With month-over-month comparison
curl -X GET "http://localhost:8080/api/v1/dashboard/overview?month=2026-01&compareWithPrevious=true" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response Structure:**
```json
{
  "success": true,
  "message": "Dashboard overview retrieved successfully",
  "data": {
    "month": "2026-01",
    "previousMonth": "2025-12",
    "summary": {
      "totalRevenue": 0,
      "totalExpenses": 0,
      "netProfit": 0,
      "totalInvoices": 0,
      "totalAppointments": 0,
      "totalPatients": 0,
      "totalEmployees": 0
    },
    "revenue": {
      "current": 0,
      "previous": 0,
      "change": 0,
      "changePercent": 0.0
    },
    "expenses": {
      "current": 0,
      "previous": 0,
      "change": 0,
      "changePercent": 0.0
    },
    "invoices": {
      "total": 0,
      "paid": 0,
      "pending": 0,
      "cancelled": 0,
      "paidPercent": 0.0,
      "debt": 0
    },
    "appointments": {
      "total": 0,
      "completed": 0,
      "cancelled": 0,
      "noShow": 0,
      "completionRate": 0.0
    }
  }
}
```

## 2. Test Revenue & Expenses Statistics

```bash
# Current month
curl -X GET "http://localhost:8080/api/v1/dashboard/revenue-expenses?month=2026-01&compareWithPrevious=false" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# With comparison
curl -X GET "http://localhost:8080/api/v1/dashboard/revenue-expenses?month=2026-01&compareWithPrevious=true" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
- Revenue breakdown by type (appointment, treatment plan, supplemental)
- Daily revenue chart data
- Top 10 services by revenue
- Expense breakdown by type
- Daily expense chart data
- Top 10 exported items

## 3. Test Employee Statistics

```bash
# Default top 10 doctors
curl -X GET "http://localhost:8080/api/v1/dashboard/employees?month=2026-01" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# Custom limit (e.g., top 5)
curl -X GET "http://localhost:8080/api/v1/dashboard/employees?month=2026-01&topDoctors=5" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
- Top doctors by revenue with service count and appointment count
- Time-off statistics by type (PAID_LEAVE, SICK_LEAVE, EMERGENCY_LEAVE, UNPAID_LEAVE, OTHER)
- Time-off statistics by status (PENDING, APPROVED, REJECTED, CANCELLED)
- Top employees by time-off days taken

## 4. Test Warehouse Statistics

```bash
curl -X GET "http://localhost:8080/api/v1/dashboard/warehouse?month=2026-01" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
- Transaction stats (total, import count/value, export count/value)
- Transaction by status (pending, approved, rejected, cancelled)
- Daily transaction breakdown
- Inventory stats (total value, low stock items, expiring items)
- Top 10 imported items
- Top 10 exported items

## 5. Test Transaction Statistics

```bash
curl -X GET "http://localhost:8080/api/v1/dashboard/transactions?month=2026-01" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
- Invoice statistics (total count, total value, payment rate, debt)
- Invoice by status (pending, partial paid, paid, cancelled)
- Invoice by type (appointment, treatment plan, supplemental)
- Payment statistics (total count, total value)
- Payment by method (bank transfer/SEPAY, cash, card, other)
- Daily payment breakdown

## 6. Test Excel Export ✅

```bash
# Export Overview tab
curl -X GET "http://localhost:8080/api/v1/dashboard/export/overview?month=2026-01" \
  -H "Authorization: Bearer $TOKEN" \
  --output dashboard-overview-2026-01.xlsx

# Export Revenue-Expenses tab
curl -X GET "http://localhost:8080/api/v1/dashboard/export/revenue-expenses?month=2026-01" \
  -H "Authorization: Bearer $TOKEN" \
  --output dashboard-revenue-expenses-2026-01.xlsx

# Export Employees tab
curl -X GET "http://localhost:8080/api/v1/dashboard/export/employees?month=2026-01" \
  -H "Authorization: Bearer $TOKEN" \
  --output dashboard-employees-2026-01.xlsx

# Export Warehouse tab
curl -X GET "http://localhost:8080/api/v1/dashboard/export/warehouse?month=2026-01" \
  -H "Authorization: Bearer $TOKEN" \
  --output dashboard-warehouse-2026-01.xlsx

# Export Transactions tab
curl -X GET "http://localhost:8080/api/v1/dashboard/export/transactions?month=2026-01" \
  -H "Authorization: Bearer $TOKEN" \
  --output dashboard-transactions-2026-01.xlsx
```

**Expected:** Excel file (.xlsx) with professional formatting
**Available tabs:** `overview`, `revenue-expenses`, `employees`, `warehouse`, `transactions`

## Common Test Scenarios

### Test Different Months
```bash
# Previous month
curl -X GET "http://localhost:8080/api/v1/dashboard/overview?month=2025-12" \
  -H "Authorization: Bearer $TOKEN"

# Future month (should return empty data)
curl -X GET "http://localhost:8080/api/v1/dashboard/overview?month=2026-06" \
  -H "Authorization: Bearer $TOKEN"
```

### Test Invalid Inputs
```bash
# Invalid month format
curl -X GET "http://localhost:8080/api/v1/dashboard/overview?month=invalid" \
  -H "Authorization: Bearer $TOKEN"

# Missing required parameter
curl -X GET "http://localhost:8080/api/v1/dashboard/overview" \
  -H "Authorization: Bearer $TOKEN"
```

### Test Authorization
```bash
# No token (should return 401)
curl -X GET "http://localhost:8080/api/v1/dashboard/overview?month=2026-01"

# Invalid token (should return 401)
curl -X GET "http://localhost:8080/api/v1/dashboard/overview?month=2026-01" \
  -H "Authorization: Bearer invalid_token"

# Non-admin/manager role (should return 403 if implemented)
curl -X GET "http://localhost:8080/api/v1/dashboard/overview?month=2026-01" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN"
```

## Using Postman or Swagger

### Swagger UI
Navigate to: `http://localhost:8080/swagger-ui/index.html`

1. Find "Dashboard" tag
2. Authenticate using the lock icon (paste JWT token)
3. Test each endpoint with different parameters

### Postman Collection
Import the following endpoints:

1. **GET** `/api/v1/dashboard/overview`
   - Params: `month`, `compareWithPrevious`
   
2. **GET** `/api/v1/dashboard/revenue-expenses`
   - Params: `month`, `compareWithPrevious`
   
3. **GET** `/api/v1/dashboard/employees`
   - Params: `month`, `topDoctors`
   
4. **GET** `/api/v1/dashboard/warehouse`
   - Params: `month`
   
5. **GET** `/api/v1/dashboard/transactions`
   - Params: `month`
   
6. **GET** `/api/v1/dashboard/export/{tab}`
   - Path: `tab` (overview, revenue-expenses, employees, warehouse, transactions)
   - Params: `month`

---

## For Frontend Developers (JavaScript/Axios)

### Setup Axios Instance
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add token to all requests
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Get Overview Statistics
```javascript
const getOverview = async (month, compareWithPrevious = false) => {
  try {
    const response = await api.get('/dashboard/overview', {
      params: { month, compareWithPrevious }
    });
    return response.data.data; // Access nested data
  } catch (error) {
    console.error('Error fetching overview:', error);
    throw error;
  }
};

// Usage
const currentMonth = new Date().toISOString().substring(0, 7); // "2026-01"
const overview = await getOverview(currentMonth, true);
console.log(overview.summary.totalRevenue);
```

### Get Revenue & Expenses
```javascript
const getRevenueExpenses = async (month, compareWithPrevious = false) => {
  const response = await api.get('/dashboard/revenue-expenses', {
    params: { month, compareWithPrevious }
  });
  return response.data.data;
};
```

### Get Employee Statistics
```javascript
const getEmployees = async (month, topDoctors = 10) => {
  const response = await api.get('/dashboard/employees', {
    params: { month, topDoctors }
  });
  return response.data.data;
};
```

### Download Excel Export
```javascript
const downloadExcel = async (tab, month) => {
  try {
    const response = await api.get(`/dashboard/export/${tab}`, {
      params: { month },
      responseType: 'blob' // Important for file download
    });
    
    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `dashboard-${tab}-${month}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (error) {
    console.error('Error downloading Excel:', error);
  }
};

// Usage
await downloadExcel('overview', '2026-01');
```

### Handle Month Selection
```javascript
// Get current month in YYYY-MM format
const getCurrentMonth = () => {
  return new Date().toISOString().substring(0, 7);
};

// Get previous month
const getPreviousMonth = (month) => {
  const [year, monthNum] = month.split('-').map(Number);
  const date = new Date(year, monthNum - 2); // -2 because months are 0-indexed
  return date.toISOString().substring(0, 7);
};

// Example
const current = getCurrentMonth(); // "2026-01"
const previous = getPreviousMonth(current); // "2025-12"
```

### Error Handling
```javascript
const handleDashboardError = (error) => {
  if (error.response) {
    // Server responded with error
    switch (error.response.status) {
      case 401:
        // Redirect to login
        window.location.href = '/login';
        break;
      case 403:
        alert('Access denied. Admin/Manager role required.');
        break;
      case 400:
        alert('Invalid month format. Use YYYY-MM');
        break;
      default:
        alert('An error occurred. Please try again.');
    }
  } else {
    // Network error
    alert('Network error. Please check your connection.');
  }
};

// Usage
try {
  const data = await getOverview(month);
} catch (error) {
  handleDashboardError(error);
}
```

## Verification Checklist

✅ All endpoints return 200 OK with valid authentication  
✅ Response structure matches DTOs  
✅ Month-over-month comparisons show percentage changes  
✅ Empty data returns zeros/empty arrays (not errors)  
✅ Invalid month format returns 400 Bad Request  
✅ Missing authentication returns 401  
✅ Non-admin/manager roles return 403  
✅ All BigDecimal values are properly formatted  
✅ All counts are non-negative  
✅ Percentage calculations are correct  

## Expected Response Times

- Overview: < 500ms
- Revenue & Expenses: < 800ms (complex queries)
- Employees: < 600ms
- Warehouse: < 700ms
- Transactions: < 600ms

## Next Steps After Testing

1. ✅ Verify all endpoints work correctly
2. ✅ Excel export fully implemented
3. ⏳ Add indexes to database for better performance
4. ⏳ Implement caching for frequently accessed data
5. ⏳ Add comprehensive unit and integration tests
6. ⏳ Add error handling and validation
7. ⏳ Monitor query performance and optimize

## Architecture Notes

### Service Dependencies
- `DashboardController` directly injects all 6 services (no circular dependencies)
- `DashboardService` → orchestrates overview statistics only
- `DashboardExportService` → called directly by controller for Excel exports
- Each service is independent and can be tested in isolation

### Database Query Optimization
- All time-off queries use `TIMESTAMPDIFF(DAY, ...)` for Hibernate 6.4 compatibility
- Revenue queries only include PAID and PARTIAL_PAID invoices
- Warehouse queries distinguish between EXPIRED and DAMAGED items

---

## Troubleshooting

**Issue:** "No handler found for /api/v1/dashboard/overview"
- **Solution:** Check if application is running, verify controller is component-scanned

**Issue:** Circular dependency error on startup
- **Solution:** Ensure `DashboardController` injects `DashboardExportService` directly, not through `DashboardService`

**Issue:** "Access Denied" / 403 error
- **Solution:** Verify user has ADMIN or MANAGER authority in JWT claims

**Issue:** Empty data returned
- **Solution:** Normal if no data exists for the month, verify database has seed data

**Issue:** Slow response times
- **Solution:** Add database indexes on `created_at`, `payment_date`, `transaction_date` columns

**Issue:** DateTimeParseException
- **Solution:** Ensure month parameter is in YYYY-MM format

**Issue:** JPQL/SQL errors on time-off queries
- **Solution:** Repository uses `TIMESTAMPDIFF(DAY, start, end)` - ensure Hibernate 6.4+ compatibility

**Issue:** Excel export returns 500 error
- **Solution:** Verify Apache POI dependency (poi-ooxml) is included in pom.xml

---

## Technical Implementation Notes

### Query Performance
- Overview endpoint: Executes 8-10 database queries (can be optimized with caching)
- Revenue/Expenses: Complex aggregations with GROUP BY clauses
- Excel export: Retrieves all data for selected tab (may be slow for large datasets)

### Memory Considerations
- Excel export loads all data into memory before streaming
- Large datasets (>100k records) may require heap size tuning
- Consider pagination or date range limits for production

### Hibernate 6.4 Compatibility
- Uses `FUNCTION('TIMESTAMPDIFF', DAY, ...)` instead of `DATEDIFF`
- All native queries tested with PostgreSQL dialect
- Date range queries use inclusive start and end (00:00:00 to 23:59:59)
