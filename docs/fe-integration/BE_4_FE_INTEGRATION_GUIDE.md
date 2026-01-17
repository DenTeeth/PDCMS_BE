# BE_4: Quick Reference Guide for FE Integration

## 🎯 What Changed?

### Services Now Have Scheduling Constraints

Every service can now have these 4 optional fields:

```typescript
interface DentalService {
  // ... existing fields ...
  minimumPreparationDays?: number;  // Min days before service can be done
  recoveryDays?: number;             // Recovery time after service
  spacingDays?: number;              // Min days between same service
  maxAppointmentsPerDay?: number;    // Daily limit (null = no limit)
}
```

---

## 📝 Service Management UI Updates

### Create/Edit Service Form

Add these 4 new fields to your service form:

```tsx
<FormGroup>
  <Label>Ngày chuẩn bị tối thiểu (days)</Label>
  <Input 
    type="number" 
    name="minimumPreparationDays"
    min={0}
    defaultValue={0}
    placeholder="0 = không yêu cầu"
  />
  <FormText>Số ngày tối thiểu cần chuẩn bị trước khi thực hiện dịch vụ này</FormText>
</FormGroup>

<FormGroup>
  <Label>Ngày hồi phục (days)</Label>
  <Input 
    type="number" 
    name="recoveryDays"
    min={0}
    defaultValue={0}
    placeholder="0 = không cần hồi phục"
  />
  <FormText>Thời gian hồi phục sau khi thực hiện dịch vụ này</FormText>
</FormGroup>

<FormGroup>
  <Label>Khoảng cách giữa các lần (days)</Label>
  <Input 
    type="number" 
    name="spacingDays"
    min={0}
    defaultValue={0}
    placeholder="0 = không giới hạn"
  />
  <FormText>Khoảng cách tối thiểu giữa 2 lần thực hiện dịch vụ này</FormText>
</FormGroup>

<FormGroup>
  <Label>Giới hạn số lượng/ngày</Label>
  <Input 
    type="number" 
    name="maxAppointmentsPerDay"
    min={1}
    placeholder="Không giới hạn"
  />
  <FormText>Số lượng appointment tối đa mỗi ngày (để trống = không giới hạn)</FormText>
</FormGroup>
```

---

## 🔥 API Request Examples

### Create Service with Constraints

**POST** `/api/services`

```json
{
  "serviceCode": "IMPLANT_FULL",
  "serviceName": "Cấy ghép Implant toàn hàm",
  "description": "Cấy ghép implant cho toàn bộ hàm răng",
  "defaultDurationMinutes": 120,
  "defaultBufferMinutes": 30,
  "price": 50000000,
  "minimumPreparationDays": 7,      // ← NEW
  "recoveryDays": 14,                // ← NEW
  "spacingDays": 30,                 // ← NEW
  "maxAppointmentsPerDay": 2,        // ← NEW (nullable)
  "isActive": true
}
```

### Update Service Constraints

**PATCH** `/api/services/{serviceCode}`

```json
{
  "minimumPreparationDays": 10,
  "recoveryDays": 7,
  "spacingDays": 21,
  "maxAppointmentsPerDay": 3
}
```

---

## ⚠️ Error Handling

### New Error Messages You'll See

When creating appointments, BE will now validate constraints. Handle these errors:

#### 1. Holiday Error
```json
{
  "error": "APPOINTMENT_CONSTRAINT_VIOLATION",
  "message": "Cannot create appointment on 2025-12-25 - it is a holiday"
}
```

**FE Action**: 
- Show error toast
- Highlight the date picker
- Suggest next available working day

```tsx
if (error.error === 'APPOINTMENT_CONSTRAINT_VIOLATION') {
  if (error.message.includes('holiday')) {
    toast.error('Ngày này là ngày nghỉ lễ. Vui lòng chọn ngày làm việc khác.');
    // Optionally: Call API to get next working day
  }
}
```

#### 2. Max Appointments Reached
```json
{
  "error": "APPOINTMENT_CONSTRAINT_VIOLATION",
  "message": "Maximum appointments per day reached for service 'Cấy ghép Implant' on 2025-12-20 (2/2)"
}
```

**FE Action**:
```tsx
if (error.message.includes('Maximum appointments per day reached')) {
  toast.error('Đã đủ số lượng appointment cho dịch vụ này trong ngày. Vui lòng chọn ngày khác.');
  setDatePickerError(true);
}
```

#### 3. Constraint Violation (Prep/Recovery/Spacing)
```json
{
  "error": "APPOINTMENT_CONSTRAINT_VIOLATION",
  "message": "Service 'Cấy ghép Implant' requires minimum 7 days preparation. Last appointment was on 2025-12-15 (3 days ago)"
}
```

**FE Action**:
```tsx
if (error.message.includes('requires minimum') || 
    error.message.includes('recovery') || 
    error.message.includes('spacing')) {
  
  // Extract the requirement from message
  const match = error.message.match(/(\d+) days/);
  const requiredDays = match ? match[1] : 'several';
  
  toast.error(`Dịch vụ này yêu cầu ${requiredDays} ngày ${
    error.message.includes('preparation') ? 'chuẩn bị' :
    error.message.includes('recovery') ? 'hồi phục' :
    'giãn cách'
  }. Vui lòng chọn ngày sau.`);
}
```

---

## 📅 Treatment Plan Auto-Scheduling

### What Happens Automatically

When FE creates a treatment plan from template:

**POST** `/api/treatment-plans/create-from-template`
```json
{
  "patientId": 123,
  "templateId": 5,
  "startDate": "2025-12-15"
}
```

**BE automatically**:
1. ✅ Calculates appointment dates based on `estimatedDurationDays`
2. ✅ Skips all holidays (Tết, Christmas, etc.)
3. ✅ Respects service constraints (prep, recovery, spacing)
4. ✅ Creates appointments on working days only

**FE receives**:
```json
{
  "treatmentPlanId": 456,
  "startDate": "2025-12-15",
  "expectedEndDate": "2027-06-20",  // Auto-calculated, skipped holidays
  "phases": [
    {
      "phaseName": "Chuẩn bị",
      "appointments": [
        {
          "scheduledDate": "2025-12-15",  // Working day
          "serviceName": "Tư vấn"
        }
      ]
    },
    {
      "phaseName": "Thực hiện",
      "appointments": [
        {
          "scheduledDate": "2026-01-22",  // Skipped Tết holidays
          "serviceName": "Cấy ghép"
        }
      ]
    }
  ]
}
```

### Display Treatment Plan Calendar

```tsx
function TreatmentPlanCalendar({ phases }) {
  return (
    <Calendar>
      {phases.map(phase => 
        phase.appointments.map(apt => (
          <CalendarEvent 
            key={apt.appointmentId}
            date={apt.scheduledDate}
            title={apt.serviceName}
            // All dates are guaranteed to be working days
            isWorkingDay={true}  // No need to check
          />
        ))
      )}
    </Calendar>
  );
}
```

---

## 🎨 UI/UX Recommendations

### Service List Display

Show constraint badges on service cards:

```tsx
function ServiceCard({ service }) {
  return (
    <Card>
      <CardBody>
        <CardTitle>{service.serviceName}</CardTitle>
        <CardText>{service.description}</CardText>
        
        {/* Constraint badges */}
        <div className="constraints">
          {service.minimumPreparationDays > 0 && (
            <Badge color="info">
              📅 Chuẩn bị: {service.minimumPreparationDays} ngày
            </Badge>
          )}
          {service.recoveryDays > 0 && (
            <Badge color="warning">
              🩹 Hồi phục: {service.recoveryDays} ngày
            </Badge>
          )}
          {service.spacingDays > 0 && (
            <Badge color="primary">
              ⏱️ Giãn cách: {service.spacingDays} ngày
            </Badge>
          )}
          {service.maxAppointmentsPerDay && (
            <Badge color="danger">
              🚫 Giới hạn: {service.maxAppointmentsPerDay}/ngày
            </Badge>
          )}
        </div>
      </CardBody>
    </Card>
  );
}
```

### Date Picker with Holiday Indication

```tsx
function AppointmentDatePicker({ serviceId, onDateSelected }) {
  const [holidays, setHolidays] = useState([]);
  
  useEffect(() => {
    // Fetch holidays for current year
    fetch('/api/holidays?year=2025')
      .then(res => res.json())
      .then(data => setHolidays(data.map(h => h.holidayDate)));
  }, []);
  
  const isHoliday = (date) => {
    return holidays.includes(formatDate(date, 'yyyy-MM-dd'));
  };
  
  return (
    <DatePicker
      selected={selectedDate}
      onChange={onDateSelected}
      filterDate={date => !isHoliday(date)}  // Disable holidays
      dayClassName={date => 
        isHoliday(date) ? 'holiday-date' : undefined
      }
      highlightDates={holidays.map(d => new Date(d))}
    />
  );
}
```

```css
.holiday-date {
  background-color: #ffebee;
  color: #c62828;
  text-decoration: line-through;
  cursor: not-allowed;
}
```

### Appointment Booking Validation Feedback

```tsx
function AppointmentForm({ patientId, serviceId }) {
  const [errors, setErrors] = useState({});
  
  const handleSubmit = async (data) => {
    try {
      await createAppointment(data);
      toast.success('Appointment created successfully!');
    } catch (error) {
      if (error.response?.status === 400) {
        const message = error.response.data.message;
        
        // Show user-friendly error
        setErrors({
          date: message,
          suggestion: calculateNextAvailableDate(data.appointmentDateTime)
        });
        
        toast.error('Không thể tạo appointment', {
          description: message
        });
      }
    }
  };
  
  return (
    <Form onSubmit={handleSubmit}>
      <FormGroup>
        <Label>Ngày hẹn</Label>
        <Input 
          type="datetime-local" 
          name="appointmentDateTime"
          invalid={!!errors.date}
        />
        {errors.date && (
          <FormFeedback>
            {errors.date}
            {errors.suggestion && (
              <div className="mt-2">
                💡 Gợi ý: Ngày khả dụng tiếp theo là {errors.suggestion}
              </div>
            )}
          </FormFeedback>
        )}
      </FormGroup>
    </Form>
  );
}
```

---

## 🧪 Testing Checklist

### Manual Testing Steps

1. **Create Service with Constraints**
   - [ ] Create service with all 4 constraint fields
   - [ ] Verify fields appear in GET response
   - [ ] Update constraints via PATCH
   - [ ] Verify updates persist

2. **Holiday Validation**
   - [ ] Try to book appointment on Tết (should fail)
   - [ ] Try to book appointment on Christmas (should fail)
   - [ ] Verify error message mentions holiday
   - [ ] Date picker should disable holidays

3. **Max Appointments Per Day**
   - [ ] Set service max to 2
   - [ ] Create 2 appointments on same date
   - [ ] Try to create 3rd appointment (should fail)
   - [ ] Verify error shows count (2/2)

4. **Preparation Days**
   - [ ] Create service with 7 days preparation
   - [ ] Book 1st appointment for patient
   - [ ] Try to book 2nd appointment 3 days later (should fail)
   - [ ] Try to book 7+ days later (should succeed)

5. **Treatment Plan Auto-Schedule**
   - [ ] Create template with multiple services
   - [ ] Create plan starting before Tết
   - [ ] Verify no appointments on Tết dates
   - [ ] Verify dates respect service constraints

---

## 📞 Need Help?

### Common Questions

**Q: Do I need to validate holidays on FE?**  
A: No, but showing them visually improves UX. BE validates everything.

**Q: What if user picks a holiday?**  
A: BE will reject with error. Show user-friendly message and suggest alternative.

**Q: Can I override constraints?**  
A: No, constraints are enforced by BE. Contact admin to adjust service settings.

**Q: How to get next available date?**  
A: Use error message or call new endpoint: `GET /api/appointments/next-available?serviceId=X&afterDate=Y`

---

## 🚀 Deployment Notes

### Database Migration Required

Before deploying FE changes, ensure BE has run migration:

```sql
-- Check if columns exist
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'services' 
AND column_name IN (
  'minimum_preparation_days',
  'recovery_days', 
  'spacing_days', 
  'max_appointments_per_day'
);
```

Should return 4 rows. If not, contact BE team.

---

## 📚 Related Documentation

- Full Implementation Guide: `docs/BE_4_TREATMENT_PLAN_AUTO_SCHEDULING_IMPLEMENTATION.md`
- API Reference: `docs/api-guides/booking/service/`
- Holiday Management: `docs/api-guides/holiday/`

---

**Last Updated**: December 11, 2025  
**BE Version**: BE_4  
**Status**: ✅ Ready for FE Integration
