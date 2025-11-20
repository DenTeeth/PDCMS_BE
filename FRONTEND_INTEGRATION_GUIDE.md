# Frontend Integration Guide: Treatment Plan Workflows with Specialization

**Backend Branch**: feat/BE-501-manage-treatment-plans  
**Status**: ✅ Backend Ready, ⚠️ Frontend Integration Required  
**Priority**: 🔴 CRITICAL - Blocking Production Deployment

---

## Overview

This guide provides step-by-step instructions for frontend developers to integrate the NEW treatment plan creation workflows with automatic specialization validation.

### What Changed?
1. ✅ **NEW API**: `/api/v1/booking/services/my-specializations` - Auto-filters services by doctor's JWT token
2. ✅ **Validation**: Backend now validates doctor specializations at plan creation (fail-fast)
3. ⚠️ **Breaking Change**: Plans with incompatible specializations will be rejected (HTTP 400)

### Why This Matters?
**Before**: Doctors could create treatment plans with services they're not qualified to perform. Plans would fail later at booking stage, causing confusion.

**After**: System validates immediately at plan creation, preventing invalid plans from being created.

---

## Quick Start

### 1. Update Service Selection API

**OLD CODE (Remove This)**:
```javascript
// src/api/services.js
export const getDentalServices = async (filters) => {
  // This returns ALL services without filtering
  return api.get('/api/v1/booking/services', { params: filters });
};
```

**NEW CODE (Use This)**:
```javascript
// src/api/services.js
export const getDentalServicesForCurrentDoctor = async (filters) => {
  // This automatically filters by doctor's specializations from JWT token
  return api.get('/api/v1/booking/services/my-specializations', { 
    params: filters 
  });
};
```

### 2. Update Treatment Plan Creation Forms

**Component: TreatmentPlanForm.jsx**

**OLD CODE**:
```jsx
const [services, setServices] = useState([]);

useEffect(() => {
  // Loads all services
  getDentalServices().then(data => setServices(data));
}, []);

return (
  <ServiceSelector 
    options={services}  // Shows all 54 services
    onChange={handleServiceSelect}
  />
);
```

**NEW CODE**:
```jsx
const [services, setServices] = useState([]);
const [loading, setLoading] = useState(false);
const [error, setError] = useState(null);

useEffect(() => {
  const loadServices = async () => {
    try {
      setLoading(true);
      // Automatically filtered by doctor's specializations
      const data = await getDentalServicesForCurrentDoctor();
      setServices(data.content);
      setError(null);
    } catch (err) {
      setError('Failed to load services');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };
  
  loadServices();
}, []);

return (
  <>
    {loading && <Spinner />}
    {error && <Alert type="error">{error}</Alert>}
    <ServiceSelector 
      options={services}  // Only shows compatible services (e.g., 12 for bacsi2)
      onChange={handleServiceSelect}
      disabled={loading}
    />
  </>
);
```

### 3. Add Error Handling for Validation

**Component: TreatmentPlanForm.jsx**

```jsx
const handleSubmit = async (formData) => {
  try {
    setLoading(true);
    const response = await createCustomTreatmentPlan(formData);
    
    showNotification({
      type: 'success',
      title: 'Plan Created',
      message: `Plan ${response.planCode} created successfully`
    });
    
    navigate(`/treatment-plans/${response.planId}`);
    
  } catch (error) {
    // Handle specialization validation errors
    if (error.response?.status === 400) {
      const message = error.response.data.message;
      
      if (message.includes('specialization')) {
        showNotification({
          type: 'error',
          title: 'Incompatible Services',
          message: 'Some services require specializations you don\'t have',
          description: (
            <div>
              <p className="mb-2">Details:</p>
              <pre className="text-xs bg-gray-100 p-2 rounded">
                {message}
              </pre>
              <p className="mt-2 text-sm">
                💡 Tip: Use the filtered service list to avoid this error.
              </p>
            </div>
          ),
          duration: 10000
        });
      } else {
        showNotification({
          type: 'error',
          title: 'Validation Error',
          message: message
        });
      }
    } else {
      showNotification({
        type: 'error',
        title: 'Error',
        message: 'Failed to create treatment plan'
      });
    }
  } finally {
    setLoading(false);
  }
};
```

---

## API Reference

### 1. Get Services for Current Doctor

**Endpoint**: `GET /api/v1/booking/services/my-specializations`

**Authentication**: ✅ Required (JWT Token)

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | int | No | 0 | Page number (0-indexed) |
| size | int | No | 20 | Items per page |
| sortBy | string | No | serviceId | Sort field |
| sortDirection | string | No | asc | Sort direction (asc/desc) |
| isActive | boolean | No | null | Filter by active status |
| keyword | string | No | null | Search in name/code |

**Request Example**:
```bash
GET /api/v1/booking/services/my-specializations?page=0&size=10&isActive=true
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response Example**:
```json
{
  "content": [
    {
      "serviceId": 8,
      "serviceCode": "ENDO_TREAT_POST",
      "serviceName": "Điều trị nội nha răng sau (1-3 ống tủy)",
      "price": 1800000.00,
      "isActive": true,
      "categoryId": 2,
      "categoryName": "Endodontics",
      "specializationId": 2,
      "specializationName": "Nội nha"
    }
  ],
  "totalElements": 12,
  "totalPages": 2,
  "size": 10,
  "number": 0
}
```

**How It Works**:
1. Extracts doctor from JWT token
2. Gets doctor's specializations (e.g., [2, 7, 8])
3. Filters services matching ANY of these specializations
4. Returns paginated, sorted results

**Frontend Code**:
```javascript
// src/api/services.js
export const getDentalServicesForCurrentDoctor = async ({
  page = 0,
  size = 20,
  sortBy = 'serviceId',
  sortDirection = 'asc',
  isActive = null,
  keyword = null
}) => {
  const params = {
    page,
    size,
    sortBy,
    sortDirection,
    ...(isActive !== null && { isActive }),
    ...(keyword && { keyword })
  };
  
  const response = await api.get(
    '/api/v1/booking/services/my-specializations',
    { params }
  );
  
  return response.data;
};
```

### 2. Create Custom Treatment Plan

**Endpoint**: `POST /api/v1/patients/{patientCode}/treatment-plans/custom`

**Authentication**: ✅ Required (JWT Token)

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| patientCode | string | Yes | Patient code (e.g., BN-1001) |

**Request Body**:
```json
{
  "planName": "Custom Dental Treatment Plan",
  "doctorEmployeeCode": "EMP002",
  "discountAmount": 0,
  "paymentType": "PHASED",
  "phases": [
    {
      "phaseName": "Initial Phase",
      "phaseNumber": 1,
      "items": [
        {
          "serviceCode": "GEN_EXAM",
          "quantity": 1,
          "toothNumber": null,
          "surface": null
        },
        {
          "serviceCode": "ENDO_TREAT_POST",
          "quantity": 1,
          "toothNumber": "16",
          "surface": null
        }
      ]
    }
  ]
}
```

**Success Response (201 Created)**:
```json
{
  "planId": 12,
  "planCode": "PLAN-20251120-002",
  "planName": "Custom Dental Treatment Plan",
  "patientCode": "BN-1001",
  "doctorEmployeeCode": "EMP002",
  "totalPrice": 2100000.00,
  "approvalStatus": "DRAFT",
  "createdDate": "2025-11-20T10:30:00"
}
```

**Error Response (400 Bad Request)**:
```json
{
  "type": "https://www.jhipster.tech/problem/problem-with-message",
  "title": "Bad Request",
  "status": 400,
  "message": "Doctor EMP002 (Trịnh Công Thái) cannot create this treatment plan.\nDoctor's specializations: [Nội nha (ID:2), Răng thẩm mỹ (ID:7), STANDARD (ID:8)].\nMissing required specializations for 1 service(s):\nService 'ORTHO_RETAINER_REMOV' (Làm hàm duy trì tháo lắp) requires specialization 'Chỉnh nha' (ID: 1)",
  "path": "/api/v1/patients/BN-1001/treatment-plans/custom",
  "entityName": "TreatmentPlan",
  "errorKey": "doctorSpecializationMismatch"
}
```

**Frontend Code**:
```javascript
// src/api/treatmentPlans.js
export const createCustomTreatmentPlan = async (patientCode, planData) => {
  const response = await api.post(
    `/api/v1/patients/${patientCode}/treatment-plans/custom`,
    planData
  );
  
  return response.data;
};
```

### 3. Create Plan from Template

**Endpoint**: `POST /api/v1/patients/{patientCode}/treatment-plans`

**Validation**: ✅ Validates doctor has template's required specialization

**Request Body**:
```json
{
  "templateCode": "TPL_SCALING_COMPREHENSIVE",
  "employeeCode": "EMP001",
  "discountAmount": 0,
  "paymentType": "FULL"
}
```

**Error Response (400 Bad Request)**:
```json
{
  "status": 400,
  "message": "Doctor EMP001 (Nguyễn Văn) cannot use this template.\nTemplate requires specialization 'Nội nha' (ID: 2).\nDoctor's specializations: [Nha chu (ID:3), Phục hồi răng (ID:4), STANDARD (ID:8)]",
  "errorKey": "doctorSpecializationMismatch"
}
```

---

## UI Components

### 1. Doctor Specialization Badge

Display doctor's specializations in the UI so users understand their capabilities.

**Component: DoctorProfileBadge.jsx**
```jsx
import { Badge, Avatar, Tooltip } from '@/components/ui';

export const DoctorProfileBadge = ({ doctor }) => {
  return (
    <div className="flex items-center gap-3 p-4 bg-white rounded-lg shadow">
      <Avatar 
        src={doctor.photoUrl} 
        alt={doctor.fullName}
        size="lg"
      />
      
      <div className="flex-1">
        <h3 className="font-semibold text-gray-900">
          {doctor.fullName}
        </h3>
        <p className="text-sm text-gray-500">
          {doctor.employeeCode}
        </p>
        
        <div className="flex gap-2 mt-2">
          {doctor.specializations.map(spec => (
            <Tooltip 
              key={spec.id}
              content={`Can perform services requiring ${spec.name}`}
            >
              <Badge variant="blue" size="sm">
                {spec.name}
              </Badge>
            </Tooltip>
          ))}
        </div>
      </div>
    </div>
  );
};
```

### 2. Service Selector with Specialization Info

Show which specialization each service requires.

**Component: ServiceSelector.jsx**
```jsx
import { Select, Badge } from '@/components/ui';

export const ServiceSelector = ({ services, value, onChange }) => {
  return (
    <Select
      value={value}
      onChange={onChange}
      options={services}
      getOptionLabel={(service) => (
        <div className="flex items-center justify-between">
          <span>{service.serviceName}</span>
          {service.specializationName && (
            <Badge variant="gray" size="xs">
              {service.specializationName}
            </Badge>
          )}
        </div>
      )}
      getOptionValue={(service) => service.serviceCode}
      placeholder="Select service..."
      searchable
    />
  );
};
```

### 3. Validation Error Modal

User-friendly modal for specialization validation errors.

**Component: SpecializationErrorModal.jsx**
```jsx
import { Modal, Button, Alert } from '@/components/ui';

export const SpecializationErrorModal = ({ isOpen, onClose, error }) => {
  const parseError = (errorMessage) => {
    const lines = errorMessage.split('\n');
    const doctorSpecs = lines.find(l => l.includes('Doctor\'s specializations'));
    const services = lines.filter(l => l.includes('Service \''));
    
    return { doctorSpecs, services };
  };
  
  const { doctorSpecs, services } = parseError(error);
  
  return (
    <Modal isOpen={isOpen} onClose={onClose} size="lg">
      <Modal.Header>
        <h2 className="text-xl font-bold text-red-600">
          ⚠️ Incompatible Services
        </h2>
      </Modal.Header>
      
      <Modal.Body>
        <Alert type="warning" className="mb-4">
          You're trying to add services that require specializations you don't have.
        </Alert>
        
        <div className="space-y-4">
          <div>
            <h3 className="font-semibold mb-2">Your Specializations:</h3>
            <p className="text-sm text-gray-700">{doctorSpecs}</p>
          </div>
          
          <div>
            <h3 className="font-semibold mb-2">Incompatible Services:</h3>
            <ul className="space-y-2">
              {services.map((service, index) => (
                <li key={index} className="text-sm text-red-600 flex items-start">
                  <span className="mr-2">•</span>
                  <span>{service.replace('Service ', '')}</span>
                </li>
              ))}
            </ul>
          </div>
          
          <Alert type="info">
            💡 <strong>Tip:</strong> The service selector automatically shows only 
            services you're qualified to perform. Use it to avoid this error.
          </Alert>
        </div>
      </Modal.Body>
      
      <Modal.Footer>
        <Button onClick={onClose} variant="primary">
          Got it
        </Button>
      </Modal.Footer>
    </Modal>
  );
};
```

---

## Testing Guide

### Manual Testing Checklist

#### Test 1: Service Filtering
- [ ] Login as doctor (bacsi1 or bacsi2)
- [ ] Navigate to "Create Treatment Plan"
- [ ] Open service selector
- [ ] Verify only compatible services shown
- [ ] Check count matches backend (e.g., 12 services for bacsi2)

#### Test 2: Custom Plan Creation (Success)
- [ ] Login as bacsi2 (specs: 2, 7, 8)
- [ ] Select services with specs 2, 7, or 8 only
- [ ] Submit plan
- [ ] Verify plan created successfully
- [ ] Check plan shows DRAFT status

#### Test 3: Custom Plan Creation (Error)
- [ ] Login as bacsi2
- [ ] Manually add service with spec 1 (if possible)
- [ ] Submit plan
- [ ] Verify error modal appears
- [ ] Check error message lists incompatible services
- [ ] Verify plan NOT created in database

#### Test 4: Template Selection
- [ ] Login as doctor
- [ ] View available templates
- [ ] Verify only templates matching specializations shown
- [ ] Try to create plan from incompatible template (via API)
- [ ] Verify rejection with clear error

### Automated Testing

**Test File: services.test.js**
```javascript
import { render, screen, waitFor } from '@testing-library/react';
import { getDentalServicesForCurrentDoctor } from '@/api/services';
import { ServiceSelector } from '@/components/ServiceSelector';

jest.mock('@/api/services');

describe('Service Filtering', () => {
  it('should load filtered services for doctor', async () => {
    const mockServices = [
      { serviceCode: 'GEN_EXAM', serviceName: 'General Exam', specializationId: 8 },
      { serviceCode: 'ENDO_TREAT_POST', serviceName: 'Root Canal', specializationId: 2 }
    ];
    
    getDentalServicesForCurrentDoctor.mockResolvedValue({
      content: mockServices,
      totalElements: 2
    });
    
    render(<ServiceSelector />);
    
    await waitFor(() => {
      expect(screen.getByText('General Exam')).toBeInTheDocument();
      expect(screen.getByText('Root Canal')).toBeInTheDocument();
    });
    
    expect(getDentalServicesForCurrentDoctor).toHaveBeenCalledTimes(1);
  });
});
```

**Test File: treatmentPlanForm.test.js**
```javascript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TreatmentPlanForm } from '@/components/TreatmentPlanForm';
import { createCustomTreatmentPlan } from '@/api/treatmentPlans';

jest.mock('@/api/treatmentPlans');

describe('Treatment Plan Validation', () => {
  it('should show error for incompatible services', async () => {
    createCustomTreatmentPlan.mockRejectedValue({
      response: {
        status: 400,
        data: {
          message: 'Doctor EMP002 cannot create this plan.\nMissing specialization for service ORTHO_RETAINER_REMOV'
        }
      }
    });
    
    render(<TreatmentPlanForm patientCode="BN-1001" />);
    
    // Fill form and submit
    await userEvent.type(screen.getByLabelText('Plan Name'), 'Test Plan');
    fireEvent.click(screen.getByText('Submit'));
    
    await waitFor(() => {
      expect(screen.getByText('Incompatible Services')).toBeInTheDocument();
      expect(screen.getByText(/ORTHO_RETAINER_REMOV/)).toBeInTheDocument();
    });
  });
});
```

---

## Migration Strategy

### Phase 1: Backend Testing (✅ COMPLETE)
- [x] Backend validation implemented
- [x] API endpoints tested
- [x] Documentation created

### Phase 2: Frontend Integration (⏳ IN PROGRESS)
- [ ] Update service API calls
- [ ] Add error handling
- [ ] Update UI components
- [ ] Test with real data

### Phase 3: User Acceptance Testing
- [ ] Deploy to staging
- [ ] Train users on new workflow
- [ ] Collect feedback
- [ ] Fix issues

### Phase 4: Production Deployment
- [ ] Deploy backend
- [ ] Deploy frontend
- [ ] Monitor errors
- [ ] Support users

---

## Common Issues & Solutions

### Issue 1: "Services not loading"
**Symptoms**: Service selector empty or shows loading forever

**Causes**:
- JWT token missing/expired
- Doctor account not properly authenticated
- Backend endpoint not accessible

**Solutions**:
```javascript
// Check token exists
const token = localStorage.getItem('token');
if (!token) {
  navigate('/login');
  return;
}

// Add timeout to API call
const controller = new AbortController();
const timeoutId = setTimeout(() => controller.abort(), 5000);

try {
  const services = await getDentalServicesForCurrentDoctor({ 
    signal: controller.signal 
  });
} catch (error) {
  if (error.name === 'AbortError') {
    showError('Request timeout. Please try again.');
  }
} finally {
  clearTimeout(timeoutId);
}
```

### Issue 2: "Validation error not showing"
**Symptoms**: Plan submission fails silently

**Causes**:
- Error response not properly parsed
- Error modal not rendered
- Error message format changed

**Solutions**:
```javascript
// Log full error for debugging
catch (error) {
  console.error('Plan creation failed:', {
    status: error.response?.status,
    data: error.response?.data,
    headers: error.response?.headers
  });
  
  // Show generic error if specific handling fails
  showError(
    error.response?.data?.message || 
    'Failed to create treatment plan. Please try again.'
  );
}
```

### Issue 3: "Services showing for wrong specialization"
**Symptoms**: Services for other specializations appearing in selector

**Causes**:
- Using old API endpoint
- Backend cache not cleared
- Multiple API calls interfering

**Solutions**:
```javascript
// Verify using correct endpoint
console.log('Fetching services from:', 
  '/api/v1/booking/services/my-specializations');

// Clear cache on login
const handleLogin = async (credentials) => {
  await login(credentials);
  
  // Clear any cached service data
  sessionStorage.removeItem('cachedServices');
  queryClient.invalidateQueries('services');
};
```

---

## Performance Optimization

### 1. Cache Service List

Services rarely change, so cache them per session:

```javascript
// src/hooks/useServices.js
import { useQuery } from 'react-query';
import { getDentalServicesForCurrentDoctor } from '@/api/services';

export const useServices = (filters = {}) => {
  return useQuery(
    ['services', 'my-specializations', filters],
    () => getDentalServicesForCurrentDoctor(filters),
    {
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 30 * 60 * 1000, // 30 minutes
      refetchOnWindowFocus: false
    }
  );
};

// Usage in component
const { data: services, isLoading, error } = useServices();
```

### 2. Debounce Search

If service selector has search, debounce API calls:

```javascript
import { useDebouncedValue } from '@/hooks/useDebouncedValue';

const [searchTerm, setSearchTerm] = useState('');
const debouncedSearch = useDebouncedValue(searchTerm, 300);

const { data: services } = useServices({ 
  keyword: debouncedSearch 
});
```

### 3. Virtualize Long Lists

For many services, use virtual scrolling:

```javascript
import { FixedSizeList as List } from 'react-window';

const ServiceList = ({ services, onSelect }) => {
  const Row = ({ index, style }) => (
    <div style={style} onClick={() => onSelect(services[index])}>
      {services[index].serviceName}
    </div>
  );
  
  return (
    <List
      height={400}
      itemCount={services.length}
      itemSize={50}
      width="100%"
    >
      {Row}
    </List>
  );
};
```

---

## API Security Notes

### JWT Token Handling

**DO**:
- ✅ Store token in httpOnly cookie or secure storage
- ✅ Refresh token before expiration
- ✅ Clear token on logout
- ✅ Validate token on every request

**DON'T**:
- ❌ Store token in localStorage (XSS vulnerable)
- ❌ Send token in URL parameters
- ❌ Log token values
- ❌ Cache responses with sensitive data

### Request Interceptor

```javascript
// src/api/axios.js
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL,
  timeout: 10000
});

// Add token to every request
api.interceptors.request.use(
  (config) => {
    const token = getToken(); // From secure storage
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle token expiration
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token expired, try refresh
      await refreshToken();
      return api.request(error.config);
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## Support & Contact

### For Backend Issues
- **Documentation**: See `SPECIALIZATION_VALIDATION_FIX.md`
- **API Tests**: See `COMPLETE_TEST_SUMMARY.md`
- **Logs**: Check `app.log` for validation errors

### For Frontend Issues
- **This Guide**: Re-read relevant sections
- **Test Data**: Use bacsi1, bacsi2 accounts for testing
- **Debugging**: Enable console logs in browser DevTools

### Common Questions

**Q: Why don't I see all services?**  
A: You only see services matching YOUR specializations. This is intentional to prevent errors.

**Q: Can I bypass validation for testing?**  
A: No. Validation is enforced server-side. Update doctor's specializations in database if needed.

**Q: What if doctor has no specializations?**  
A: Backend will return 400 error. Every doctor must have at least STANDARD (ID: 8) specialization.

**Q: Can I create plans for other doctors?**  
A: Only if you have their JWT token. Validation checks token's embedded specializations.

---

## Appendix: Test Credentials

### Test Doctors

| Username | Password | Specializations | Service Count | Use Case |
|----------|----------|-----------------|---------------|----------|
| bacsi1 | 123456 | 3, 4, 8 (Nha chu, Phục hồi, Standard) | 18 | Periodontics & Restorative |
| bacsi2 | 123456 | 2, 7, 8 (Nội nha, Thẩm mỹ, Standard) | 12 | Endodontics & Cosmetic |

### Test Patients

| Patient Code | Name | Status |
|--------------|------|--------|
| BN-1001 | Nguyễn Văn A | Active |
| BN-1002 | Trần Thị B | Active |
| BN-1003 | Lê Văn C | Active |

### Test Services

| Service Code | Name | Spec ID | Spec Name |
|--------------|------|---------|-----------|
| GEN_EXAM | General Examination | 8 | STANDARD |
| ENDO_TREAT_POST | Root Canal (Posterior) | 2 | Nội nha |
| SCALING_L1 | Teeth Scaling Level 1 | 3 | Nha chu |
| VENEER_EMAX | E-max Veneer | 7 | Răng thẩm mỹ |
| ORTHO_RETAINER_REMOV | Removable Retainer | 1 | Chỉnh nha |

---

**Document Version**: 1.0  
**Last Updated**: November 20, 2025  
**Status**: Ready for Frontend Implementation  
**Next Review**: After UAT completion

