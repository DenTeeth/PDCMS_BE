-- =====================================================
-- BE-403: Appointment Management - Seed Data for Testing
-- =====================================================
-- Purpose: Comprehensive test data for P3.1 (Available Times) and P3.2 (Create Appointment)
-- Coverage: Employees, Patients, Services, Rooms, Shifts, Room-Services, Specializations
-- =====================================================

-- Clean up existing test data (optional - uncomment if needed)
-- DELETE FROM appointment_participants WHERE appointment_id IN (SELECT appointment_id FROM appointments WHERE appointment_code LIKE 'APT-TEST-%');
-- DELETE FROM appointment_services WHERE appointment_id IN (SELECT appointment_id FROM appointments WHERE appointment_code LIKE 'APT-TEST-%');
-- DELETE FROM appointments WHERE appointment_code LIKE 'APT-TEST-%';
-- DELETE FROM employee_shifts WHERE employee_id IN (SELECT employee_id FROM employees WHERE employee_code LIKE 'TEST-%');
-- DELETE FROM room_services WHERE room_id IN (SELECT room_id FROM rooms WHERE room_code LIKE 'TEST-%');
-- DELETE FROM employees WHERE employee_code LIKE 'TEST-%';
-- DELETE FROM patients WHERE patient_code LIKE 'TEST-%';
-- DELETE FROM services WHERE service_code LIKE 'TEST-%';
-- DELETE FROM rooms WHERE room_code LIKE 'TEST-%';
-- DELETE FROM specializations WHERE specialization_code LIKE 'TEST-%';

-- =====================================================
-- 1. SPECIALIZATIONS (Chuyên khoa)
-- =====================================================
INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
    (901, 'TEST-IMPLANT', 'Test Implant Specialist', 'Chuyên khoa Cấy ghép Implant (Test)', true, CURRENT_TIMESTAMP),
    (902, 'TEST-ORTHO', 'Test Orthodontics', 'Chuyên khoa Chỉnh nha (Test)', true, CURRENT_TIMESTAMP),
    (903, 'TEST-GENERAL', 'Test General Dentistry', 'Nha khoa tổng quát (Test)', true, CURRENT_TIMESTAMP)
ON CONFLICT (specialization_id) DO NOTHING;

-- =====================================================
-- 2. EMPLOYEES (Bác sĩ & Phụ tá)
-- =====================================================
-- Test Doctor 1: Implant Specialist
INSERT INTO employees (employee_id, employee_code, full_name, email, phone, is_active, created_at)
VALUES
    (9001, 'TEST-DR-IMPLANT', 'Dr. Nguyen Van Implant (Test)', 'test.dr.implant@dental.com', '0901234001', true, CURRENT_TIMESTAMP)
ON CONFLICT (employee_id) DO NOTHING;

-- Test Doctor 2: Orthodontist
INSERT INTO employees (employee_id, employee_code, full_name, email, phone, is_active, created_at)
VALUES
    (9002, 'TEST-DR-ORTHO', 'Dr. Tran Thi Ortho (Test)', 'test.dr.ortho@dental.com', '0901234002', true, CURRENT_TIMESTAMP)
ON CONFLICT (employee_id) DO NOTHING;

-- Test Doctor 3: General Dentist
INSERT INTO employees (employee_id, employee_code, full_name, email, phone, is_active, created_at)
VALUES
    (9003, 'TEST-DR-GENERAL', 'Dr. Le Van General (Test)', 'test.dr.general@dental.com', '0901234003', true, CURRENT_TIMESTAMP)
ON CONFLICT (employee_id) DO NOTHING;

-- Test Assistant 1
INSERT INTO employees (employee_id, employee_code, full_name, email, phone, is_active, created_at)
VALUES
    (9004, 'TEST-PT-001', 'Phu Ta Binh (Test)', 'test.pt.binh@dental.com', '0901234004', true, CURRENT_TIMESTAMP)
ON CONFLICT (employee_id) DO NOTHING;

-- Test Assistant 2
INSERT INTO employees (employee_id, employee_code, full_name, email, phone, is_active, created_at)
VALUES
    (9005, 'TEST-PT-002', 'Phu Ta An (Test)', 'test.pt.an@dental.com', '0901234005', true, CURRENT_TIMESTAMP)
ON CONFLICT (employee_id) DO NOTHING;

-- =====================================================
-- 3. EMPLOYEE SPECIALIZATIONS (Many-to-Many)
-- =====================================================
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
    (9001, 901), -- Dr Implant has Implant spec
    (9001, 903), -- Dr Implant also has General spec
    (9002, 902), -- Dr Ortho has Ortho spec
    (9003, 903)  -- Dr General has General spec
ON CONFLICT DO NOTHING;

-- =====================================================
-- 4. PATIENTS (Bệnh nhân)
-- =====================================================
INSERT INTO patients (patient_id, patient_code, full_name, date_of_birth, gender, phone, email, is_active, created_at)
VALUES
    (9001, 'TEST-BN-001', 'Nguyen Van Test Patient A', '1985-05-15', 'male', '0912345001', 'test.patient.a@gmail.com', true, CURRENT_TIMESTAMP),
    (9002, 'TEST-BN-002', 'Tran Thi Test Patient B', '1990-08-20', 'female', '0912345002', 'test.patient.b@gmail.com', true, CURRENT_TIMESTAMP),
    (9003, 'TEST-BN-003', 'Le Van Test Patient C', '1978-12-10', 'male', '0912345003', 'test.patient.c@gmail.com', true, CURRENT_TIMESTAMP)
ON CONFLICT (patient_id) DO NOTHING;

-- =====================================================
-- 5. SERVICES (Dịch vụ)
-- =====================================================
INSERT INTO services (service_id, service_code, service_name, specialization_id, default_duration_minutes, default_buffer_minutes, base_price, is_active, created_at)
VALUES
    (9001, 'TEST-SV-IMPLANT', 'Test: Cam tru Implant', 901, 60, 15, 15000000, true, CURRENT_TIMESTAMP),
    (9002, 'TEST-SV-NANGXOANG', 'Test: Nang xoang', 901, 45, 15, 8000000, true, CURRENT_TIMESTAMP),
    (9003, 'TEST-SV-SCALING', 'Test: Lay cao rang', 903, 30, 10, 500000, true, CURRENT_TIMESTAMP),
    (9004, 'TEST-SV-BRACKET', 'Test: Nieng rang mac cai', 902, 90, 20, 25000000, true, CURRENT_TIMESTAMP)
ON CONFLICT (service_id) DO NOTHING;

-- =====================================================
-- 6. ROOMS (Phòng khám)
-- =====================================================
INSERT INTO rooms (room_id, room_code, room_name, room_type, is_active, created_at)
VALUES
    ('TEST-ROOM-IMPLANT-01', 'TEST-P-IMPLANT-01', 'Test: Phong Implant 01', 'SURGERY', true, CURRENT_TIMESTAMP),
    ('TEST-ROOM-IMPLANT-02', 'TEST-P-IMPLANT-02', 'Test: Phong Implant 02', 'SURGERY', true, CURRENT_TIMESTAMP),
    ('TEST-ROOM-GENERAL-01', 'TEST-P-GENERAL-01', 'Test: Phong Kham Tong Quat 01', 'STANDARD', true, CURRENT_TIMESTAMP),
    ('TEST-ROOM-ORTHO-01', 'TEST-P-ORTHO-01', 'Test: Phong Chinh Nha 01', 'ORTHODONTICS', true, CURRENT_TIMESTAMP)
ON CONFLICT (room_id) DO NOTHING;

-- =====================================================
-- 7. ROOM_SERVICES (V16 - Junction Table)
-- =====================================================
-- Room Implant 01: Supports Implant services
INSERT INTO room_services (room_id, service_id)
VALUES
    ('TEST-ROOM-IMPLANT-01', 9001), -- Implant
    ('TEST-ROOM-IMPLANT-01', 9002)  -- Nang xoang
ON CONFLICT DO NOTHING;

-- Room Implant 02: Supports Implant services
INSERT INTO room_services (room_id, service_id)
VALUES
    ('TEST-ROOM-IMPLANT-02', 9001), -- Implant
    ('TEST-ROOM-IMPLANT-02', 9002)  -- Nang xoang
ON CONFLICT DO NOTHING;

-- Room General 01: Supports general services
INSERT INTO room_services (room_id, service_id)
VALUES
    ('TEST-ROOM-GENERAL-01', 9003)  -- Scaling
ON CONFLICT DO NOTHING;

-- Room Ortho 01: Supports ortho services
INSERT INTO room_services (room_id, service_id)
VALUES
    ('TEST-ROOM-ORTHO-01', 9004)  -- Bracket
ON CONFLICT DO NOTHING;

-- =====================================================
-- 8. WORK SHIFTS (Need to get actual work_shift_id from database)
-- =====================================================
-- Assuming work_shift_id 1 = Morning Shift (8:00-12:00)
-- Assuming work_shift_id 2 = Afternoon Shift (13:00-17:00)
-- Assuming work_shift_id 3 = Full Day (8:00-17:00)
-- NOTE: Adjust these IDs based on your actual work_shifts table!

-- =====================================================
-- 9. EMPLOYEE_SHIFTS (Lịch làm việc)
-- =====================================================
-- Test date: 2025-11-15 (Friday)
-- Dr Implant: Full day
INSERT INTO employee_shifts (employee_id, work_date, work_shift_id, created_at)
SELECT 9001, DATE '2025-11-15', work_shift_id, CURRENT_TIMESTAMP
FROM work_shifts
WHERE shift_name = 'Full Day' OR start_time = '08:00:00' AND end_time = '17:00:00'
LIMIT 1
ON CONFLICT DO NOTHING;

-- Dr Ortho: Morning only
INSERT INTO employee_shifts (employee_id, work_date, work_shift_id, created_at)
SELECT 9002, DATE '2025-11-15', work_shift_id, CURRENT_TIMESTAMP
FROM work_shifts
WHERE shift_name LIKE '%Morning%' OR (start_time = '08:00:00' AND end_time = '12:00:00')
LIMIT 1
ON CONFLICT DO NOTHING;

-- Dr General: Full day
INSERT INTO employee_shifts (employee_id, work_date, work_shift_id, created_at)
SELECT 9003, DATE '2025-11-15', work_shift_id, CURRENT_TIMESTAMP
FROM work_shifts
WHERE shift_name = 'Full Day' OR start_time = '08:00:00' AND end_time = '17:00:00'
LIMIT 1
ON CONFLICT DO NOTHING;

-- Assistant 1: Full day
INSERT INTO employee_shifts (employee_id, work_date, work_shift_id, created_at)
SELECT 9004, DATE '2025-11-15', work_shift_id, CURRENT_TIMESTAMP
FROM work_shifts
WHERE shift_name = 'Full Day' OR start_time = '08:00:00' AND end_time = '17:00:00'
LIMIT 1
ON CONFLICT DO NOTHING;

-- Assistant 2: Afternoon only
INSERT INTO employee_shifts (employee_id, work_date, work_shift_id, created_at)
SELECT 9005, DATE '2025-11-15', work_shift_id, CURRENT_TIMESTAMP
FROM work_shifts
WHERE shift_name LIKE '%Afternoon%' OR (start_time = '13:00:00' AND end_time = '17:00:00')
LIMIT 1
ON CONFLICT DO NOTHING;

-- =====================================================
-- 10. SAMPLE EXISTING APPOINTMENT (To test conflict detection)
-- =====================================================
-- Dr Implant is BUSY from 10:00-11:00 on 2025-11-15
INSERT INTO appointments (appointment_code, patient_id, employee_id, room_id,
                         appointment_start_time, appointment_end_time,
                         expected_duration_minutes, status, created_at)
VALUES
    ('APT-TEST-EXISTING-001', 9001, 9001, 'TEST-ROOM-IMPLANT-01',
     TIMESTAMP '2025-11-15 10:00:00', TIMESTAMP '2025-11-15 11:00:00',
     60, 'SCHEDULED', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Link service to existing appointment
INSERT INTO appointment_services (appointment_id, service_id)
SELECT a.appointment_id, 9001
FROM appointments a
WHERE a.appointment_code = 'APT-TEST-EXISTING-001'
ON CONFLICT DO NOTHING;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================
-- Uncomment to verify data insertion:

-- SELECT * FROM specializations WHERE specialization_code LIKE 'TEST-%';
-- SELECT * FROM employees WHERE employee_code LIKE 'TEST-%';
-- SELECT * FROM employee_specializations WHERE employee_id >= 9001 AND employee_id <= 9005;
-- SELECT * FROM patients WHERE patient_code LIKE 'TEST-%';
-- SELECT * FROM services WHERE service_code LIKE 'TEST-%';
-- SELECT * FROM rooms WHERE room_code LIKE 'TEST-%';
-- SELECT * FROM room_services WHERE room_id LIKE 'TEST-%';
-- SELECT * FROM employee_shifts WHERE employee_id >= 9001 AND employee_id <= 9005 AND work_date = '2025-11-15';
-- SELECT * FROM appointments WHERE appointment_code LIKE 'APT-TEST-%';

-- =====================================================
-- TESTING SCENARIOS
-- =====================================================

-- Scenario 1: P3.1 - Find available times for Dr Implant on 2025-11-15
-- GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=TEST-DR-IMPLANT&serviceCodes=TEST-SV-IMPLANT
-- Expected: Slots from 8:00-10:00, 11:00-17:00 (excluding 10:00-11:00 busy time)
-- Available rooms: TEST-P-IMPLANT-01, TEST-P-IMPLANT-02

-- Scenario 2: P3.2 - Create appointment successfully
-- POST /api/v1/appointments
-- Body: { patientCode: "TEST-BN-002", employeeCode: "TEST-DR-IMPLANT", roomCode: "TEST-P-IMPLANT-01", serviceCodes: ["TEST-SV-IMPLANT"], appointmentStartTime: "2025-11-15T14:00:00" }
-- Expected: 201 Created

-- Scenario 3: P3.2 - Conflict with existing appointment
-- POST /api/v1/appointments
-- Body: { patientCode: "TEST-BN-003", employeeCode: "TEST-DR-IMPLANT", roomCode: "TEST-P-IMPLANT-02", serviceCodes: ["TEST-SV-IMPLANT"], appointmentStartTime: "2025-11-15T10:00:00" }
-- Expected: 409 EMPLOYEE_SLOT_TAKEN

-- Scenario 4: P3.2 - Room not compatible
-- POST /api/v1/appointments
-- Body: { patientCode: "TEST-BN-001", employeeCode: "TEST-DR-IMPLANT", roomCode: "TEST-P-GENERAL-01", serviceCodes: ["TEST-SV-IMPLANT"], appointmentStartTime: "2025-11-15T09:00:00" }
-- Expected: 409 ROOM_NOT_COMPATIBLE

-- Scenario 5: P3.2 - Doctor not qualified
-- POST /api/v1/appointments
-- Body: { patientCode: "TEST-BN-001", employeeCode: "TEST-DR-GENERAL", roomCode: "TEST-P-IMPLANT-01", serviceCodes: ["TEST-SV-IMPLANT"], appointmentStartTime: "2025-11-15T09:00:00" }
-- Expected: 409 EMPLOYEE_NOT_QUALIFIED

-- Scenario 6: P3.1 with participants
-- GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=TEST-DR-IMPLANT&serviceCodes=TEST-SV-IMPLANT&participantCodes=TEST-PT-001
-- Expected: Considers assistant availability

-- =====================================================
-- CLEANUP (Run after testing)
-- =====================================================
-- Uncomment to remove test data:

/*
DELETE FROM appointment_participants WHERE appointment_id IN (SELECT appointment_id FROM appointments WHERE appointment_code LIKE 'APT-TEST-%');
DELETE FROM appointment_services WHERE appointment_id IN (SELECT appointment_id FROM appointments WHERE appointment_code LIKE 'APT-TEST-%');
DELETE FROM appointments WHERE appointment_code LIKE 'APT-TEST-%';
DELETE FROM employee_shifts WHERE employee_id IN (SELECT employee_id FROM employees WHERE employee_code LIKE 'TEST-%');
DELETE FROM room_services WHERE room_id IN (SELECT room_id FROM rooms WHERE room_code LIKE 'TEST-%');
DELETE FROM employee_specializations WHERE employee_id IN (SELECT employee_id FROM employees WHERE employee_code LIKE 'TEST-%');
DELETE FROM employees WHERE employee_code LIKE 'TEST-%';
DELETE FROM patients WHERE patient_code LIKE 'TEST-%';
DELETE FROM services WHERE service_code LIKE 'TEST-%';
DELETE FROM rooms WHERE room_code LIKE 'TEST-%';
DELETE FROM specializations WHERE specialization_code LIKE 'TEST-%';
*/
