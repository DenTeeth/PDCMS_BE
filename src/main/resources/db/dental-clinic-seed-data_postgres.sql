-- ============================================
-- DENTAL CLINIC MANAGEMENT SYSTEM - SPRING BOOT VERSION
-- ============================================

-- ============================================
-- STEP 1 & 2: ROLES & PERMISSIONS
-- (Không thay đổi, giả định đã chạy)
-- ============================================

INSERT INTO roles (role_id, role_name, description, requires_specialization, is_active, created_at)
VALUES
('ROLE_ADMIN', 'ROLE_ADMIN', 'Quản trị viên hệ thống - Toàn quyền', FALSE, TRUE, NOW()),
('ROLE_DOCTOR', 'ROLE_DOCTOR', 'Bác sĩ nha khoa - Khám và điều trị', TRUE, TRUE, NOW()),
('ROLE_NURSE', 'ROLE_NURSE', 'Y tá hỗ trợ điều trị', TRUE, TRUE, NOW()),
('ROLE_RECEPTIONIST', 'ROLE_RECEPTIONIST', 'Tiếp đón và quản lý lịch hẹn', FALSE, TRUE, NOW()),
('ROLE_ACCOUNTANT', 'ROLE_ACCOUNTANT', 'Quản lý tài chính và thanh toán', FALSE, TRUE, NOW()),
('ROLE_INVENTORY_MANAGER', 'ROLE_INVENTORY_MANAGER', 'Quản lý vật tư và thuốc', FALSE, TRUE, NOW()),
('ROLE_MANAGER', 'ROLE_MANAGER', 'Quản lý vận hành và nhân sự', FALSE, TRUE, NOW()),
('ROLE_PATIENT', 'ROLE_PATIENT', 'Người bệnh - Xem hồ sơ cá nhân', FALSE, TRUE, NOW())
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name, module, description, created_at)
VALUES
('CREATE_ACCOUNT', 'CREATE_ACCOUNT', 'ACCOUNT', 'Tạo tài khoản mới', NOW()),
('VIEW_ACCOUNT', 'VIEW_ACCOUNT', 'ACCOUNT', 'Xem danh sách tài khoản', NOW()),
('UPDATE_ACCOUNT', 'UPDATE_ACCOUNT', 'ACCOUNT', 'Cập nhật tài khoản', NOW()),
('DELETE_ACCOUNT', 'DELETE_ACCOUNT', 'ACCOUNT', 'Xóa tài khoản', NOW()),
('CREATE_EMPLOYEE', 'CREATE_EMPLOYEE', 'EMPLOYEE', 'Tạo nhân viên mới', NOW()),
('VIEW_EMPLOYEE', 'VIEW_EMPLOYEE', 'EMPLOYEE', 'Xem danh sách nhân viên', NOW()),
('UPDATE_EMPLOYEE', 'UPDATE_EMPLOYEE', 'EMPLOYEE', 'Cập nhật nhân viên', NOW()),
('DELETE_EMPLOYEE', 'DELETE_EMPLOYEE', 'EMPLOYEE', 'Xóa nhân viên', NOW()),
('CREATE_PATIENT', 'CREATE_PATIENT', 'PATIENT', 'Tạo hồ sơ bệnh nhân', NOW()),
('VIEW_PATIENT', 'VIEW_PATIENT', 'PATIENT', 'Xem hồ sơ bệnh nhân', NOW()),
('UPDATE_PATIENT', 'UPDATE_PATIENT', 'PATIENT', 'Cập nhật hồ sơ bệnh nhân', NOW()),
('DELETE_PATIENT', 'DELETE_PATIENT', 'PATIENT', 'Xóa hồ sơ bệnh nhân', NOW()),
('CREATE_TREATMENT', 'CREATE_TREATMENT', 'TREATMENT', 'Tạo phác đồ điều trị', NOW()),
('VIEW_TREATMENT', 'VIEW_TREATMENT', 'TREATMENT', 'Xem phác đồ điều trị', NOW()),
('UPDATE_TREATMENT', 'UPDATE_TREATMENT', 'TREATMENT', 'Cập nhật phác đồ điều trị', NOW()),
('CREATE_APPOINTMENT', 'CREATE_APPOINTMENT', 'APPOINTMENT', 'Đặt lịch hẹn', NOW()),
('VIEW_APPOINTMENT', 'VIEW_APPOINTMENT', 'APPOINTMENT', 'Xem lịch hẹn', NOW()),
('UPDATE_APPOINTMENT', 'UPDATE_APPOINTMENT', 'APPOINTMENT', 'Cập nhật lịch hẹn', NOW()),
('DELETE_APPOINTMENT', 'DELETE_APPOINTMENT', 'APPOINTMENT', 'Hủy lịch hẹn', NOW()),
('VIEW_CONTACT', 'VIEW_CONTACT', 'CONTACT', 'Xem danh sách liên hệ khách hàng', NOW()),
('CREATE_CONTACT', 'CREATE_CONTACT', 'CONTACT', 'Tạo liên hệ khách hàng mới', NOW()),
('UPDATE_CONTACT', 'UPDATE_CONTACT', 'CONTACT', 'Cập nhật liên hệ khách hàng', NOW()),
('DELETE_CONTACT', 'DELETE_CONTACT', 'CONTACT', 'Xóa liên hệ khách hàng', NOW()),
('VIEW_CONTACT_HISTORY', 'VIEW_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Xem lịch sử liên hệ', NOW()),
('CREATE_CONTACT_HISTORY', 'CREATE_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Tạo lịch sử liên hệ', NOW()),
('UPDATE_CONTACT_HISTORY', 'UPDATE_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Cập nhật lịch sử liên hệ', NOW()),
('DELETE_CONTACT_HISTORY', 'DELETE_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Xóa lịch sử liên hệ', NOW()),
('CREATE_WORK_SHIFTS', 'CREATE_WORK_SHIFTS', 'WORK_SHIFTS', 'Tạo mẫu ca làm việc mới', NOW()),
('VIEW_WORK_SHIFTS', 'VIEW_WORK_SHIFTS', 'WORK_SHIFTS', 'Xem danh sách mẫu ca làm việc', NOW()),
('UPDATE_WORK_SHIFTS', 'UPDATE_WORK_SHIFTS', 'WORK_SHIFTS', 'Cập nhật mẫu ca làm việc', NOW()),
('DELETE_WORK_SHIFTS', 'DELETE_WORK_SHIFTS', 'WORK_SHIFTS', 'Xóa/vô hiệu hóa mẫu ca làm việc', NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- ============================================
-- STEP 3: ASSIGN PERMISSIONS TO ROLES
-- ============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_ADMIN', permission_id FROM permissions
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_PATIENT'), ('ROLE_DOCTOR', 'UPDATE_PATIENT'), ('ROLE_DOCTOR', 'CREATE_TREATMENT'),
('ROLE_DOCTOR', 'VIEW_TREATMENT'), ('ROLE_DOCTOR', 'UPDATE_TREATMENT'), ('ROLE_DOCTOR', 'VIEW_APPOINTMENT')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_RECEPTIONIST', 'CREATE_PATIENT'), ('ROLE_RECEPTIONIST', 'VIEW_PATIENT'), ('ROLE_RECEPTIONIST', 'UPDATE_PATIENT'),
('ROLE_RECEPTIONIST', 'CREATE_APPOINTMENT'), ('ROLE_RECEPTIONIST', 'VIEW_APPOINTMENT'), ('ROLE_RECEPTIONIST', 'UPDATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'DELETE_APPOINTMENT'), ('ROLE_RECEPTIONIST', 'VIEW_CONTACT'), ('ROLE_RECEPTIONIST', 'CREATE_CONTACT'),
('ROLE_RECEPTIONIST', 'UPDATE_CONTACT'), ('ROLE_RECEPTIONIST', 'DELETE_CONTACT'), ('ROLE_RECEPTIONIST', 'VIEW_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'CREATE_CONTACT_HISTORY'), ('ROLE_RECEPTIONIST', 'UPDATE_CONTACT_HISTORY'), ('ROLE_RECEPTIONIST', 'DELETE_CONTACT_HISTORY')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_PATIENT', 'VIEW_PATIENT'), ('ROLE_PATIENT', 'VIEW_TREATMENT'),
('ROLE_PATIENT', 'CREATE_APPOINTMENT'), ('ROLE_PATIENT', 'VIEW_APPOINTMENT')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_MANAGER', 'CREATE_EMPLOYEE'), ('ROLE_MANAGER', 'VIEW_EMPLOYEE'), ('ROLE_MANAGER', 'UPDATE_EMPLOYEE'),
('ROLE_MANAGER', 'DELETE_EMPLOYEE'), ('ROLE_MANAGER', 'CREATE_WORK_SHIFTS'), ('ROLE_MANAGER', 'VIEW_WORK_SHIFTS'),
('ROLE_MANAGER', 'UPDATE_WORK_SHIFTS'), ('ROLE_MANAGER', 'DELETE_WORK_SHIFTS'), ('ROLE_MANAGER', 'VIEW_PATIENT'),
('ROLE_MANAGER', 'VIEW_APPOINTMENT')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================
-- STEP 4: CREATE SPECIALIZATIONS
-- ============================================
INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
(1, 'SPEC001', 'Chỉnh nha', 'Orthodontics - Niềng răng, chỉnh hình răng mặt', TRUE, NOW()),
(2, 'SPEC002', 'Nội nha', 'Endodontics - Điều trị tủy, chữa răng sâu', TRUE, NOW()),
(3, 'SPEC003', 'Nha chu', 'Periodontics - Điều trị nướu, mô nha chu', TRUE, NOW()),
(4, 'SPEC004', 'Phục hồi răng', 'Prosthodontics - Làm răng giả, cầu răng, implant', TRUE, NOW()),
(5, 'SPEC005', 'Phẫu thuật hàm mặt', 'Oral Surgery - Nhổ răng khôn, phẫu thuật', TRUE, NOW()),
(6, 'SPEC006', 'Nha khoa trẻ em', 'Pediatric Dentistry - Chuyên khoa nhi', TRUE, NOW()),
(7, 'SPEC007', 'Răng thẩm mỹ', 'Cosmetic Dentistry - Tẩy trắng, bọc sứ', TRUE, NOW())
ON CONFLICT (specialization_id) DO NOTHING;

-- ============================================
-- STEP 5: CREATE ACCOUNTS (ID gán cứng: 1-10)
-- ============================================
-- Password: 123456 (BCrypt hashed)
-- $2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2
INSERT INTO accounts (account_id, account_code, username, email, password, status, created_at)
VALUES
(1, 'ACC001', 'admin', 'admin@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
(2, 'ACC002', 'nhasi1', 'nhasi1@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
(3, 'ACC003', 'nhasi2', 'nhasi2@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
(4, 'ACC004', 'letan', 'letan@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
(5, 'ACC005', 'ketoan', 'ketoan@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
(6, 'ACC006', 'yta', 'yta@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
(7, 'ACC007', 'manager', 'manager@dentalclinic.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
(8, 'ACC008', 'benhnhan1', 'benhnhan1@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
(9, 'ACC009', 'benhnhan2', 'benhnhan2@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW()),
(10, 'ACC010', 'benhnhan3', 'benhnhan3@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW())
ON CONFLICT (account_id) DO NOTHING;

-- ============================================
-- STEP 6: ASSIGN ROLES TO ACCOUNTS
-- ============================================
INSERT INTO account_roles (account_id, role_id)
VALUES
(1, 'ROLE_ADMIN'), (2, 'ROLE_DOCTOR'), (3, 'ROLE_DOCTOR'), (4, 'ROLE_RECEPTIONIST'),
(5, 'ROLE_ACCOUNTANT'), (6, 'ROLE_NURSE'), (7, 'ROLE_MANAGER'),
(8, 'ROLE_PATIENT'), (9, 'ROLE_PATIENT'), (10, 'ROLE_PATIENT')
ON CONFLICT (account_id, role_id) DO NOTHING;

-- ============================================
-- STEP 7: CREATE EMPLOYEES (ID gán cứng: 1-7)
-- ============================================
INSERT INTO employees (employee_id, account_id, role_id, employee_code, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES
(1, 1, 'ROLE_ADMIN', 'EMP001', 'Admin', 'Hệ thống', '0900000001', '1985-01-01', 'Phòng quản trị', TRUE, NOW()),
(2, 2, 'ROLE_DOCTOR', 'EMP002', 'Minh', 'Nguyễn Văn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', TRUE, NOW()),
(3, 3, 'ROLE_DOCTOR', 'EMP003', 'Lan', 'Trần Thị', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', TRUE, NOW()),
(4, 4, 'ROLE_RECEPTIONIST', 'EMP004', 'Mai', 'Lê Thị', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', TRUE, NOW()),
(5, 5, 'ROLE_ACCOUNTANT', 'EMP005', 'Tuấn', 'Hoàng Văn', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', TRUE, NOW()),
(6, 6, 'ROLE_NURSE', 'EMP006', 'Hoa', 'Phạm Thị', '0906789012', '1992-06-15', '111 Lý Thường Kiệt, Q10, TPHCM', TRUE, NOW()),
(7, 7, 'ROLE_MANAGER', 'EMP007', 'Quân', 'Trần Minh', '0909999999', '1980-10-10', '77 Phạm Ngọc Thạch, Q3, TPHCM', TRUE, NOW())
ON CONFLICT (employee_id) DO NOTHING;

-- ============================================
-- STEP 8: ASSIGN SPECIALIZATIONS
-- ============================================
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(2, 1), (2, 7), -- Bác sĩ 1 (ID 2)
(3, 2), (3, 4), -- Bác sĩ 2 (ID 3)
(6, 6)          -- Y tá (ID 6)
ON CONFLICT (employee_id, specialization_id) DO NOTHING;

-- ============================================
-- STEP 9: (Đã gộp vào STEP 5)
-- ============================================

-- ============================================
-- STEP 10: CREATE PATIENTS (ID gán cứng: 1-3)
-- ============================================
INSERT INTO patients (patient_id, account_id, patient_code, first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES
(1, 8, 'PAT001', 'Khang', 'Nguyễn Văn', 'benhnhan1@email.com', '0911111111', '1990-01-15', '123 Lê Văn Việt, Q9, TPHCM', 'MALE', TRUE, NOW(), NOW()),
(2, 9, 'PAT002', 'Lan', 'Trần Thị', 'benhnhan2@email.com', '0922222222', '1985-05-20', '456 Võ Văn Ngân, Thủ Đức, TPHCM', 'FEMALE', TRUE, NOW(), NOW()),
(3, 10, 'PAT003', 'Đức', 'Lê Minh', 'benhnhan3@email.com', '0933333333', '1995-12-10', '789 Đường D2, Bình Thạnh, TPHCM', 'MALE', TRUE, NOW(), NOW())
ON CONFLICT (patient_id) DO NOTHING;

-- ============================================
-- STEP 11: SEED DATA CHO ĐĂNG KÝ CA LÀM VIỆC
-- ============================================

-- STEP 11.1: TẠO CÁC MẪU CA LÀM VIỆC (work_shifts)
INSERT INTO work_shifts (work_shift_id, shift_name, start_time, end_time, category, is_active)
VALUES
('SLOT_MORN_HC', 'Ca Sáng (8h-16h)', '08:00:00', '16:00:00', 'NORMAL', TRUE),
('SLOT_AFTR_HC', 'Ca Chiều (13h-20h)', '13:00:00', '20:00:00', 'NORMAL', TRUE),
('SLOT_PART_MORN', 'Ca Part-time Sáng (8h-12h)', '08:00:00', '12:00:00', 'NORMAL', TRUE),
('SLOT_PART_AFTR', 'Ca Part-time Chiều (13h-17h)', '13:00:00', '17:00:00', 'NORMAL', TRUE)
ON CONFLICT (work_shift_id) DO NOTHING;

-- STEP 11.2: TẠO ĐĂNG KÝ CA (ID gán cứng: 1-3)
-- TODO: Uncomment when employee_shift_registrations table is created
-- INSERT INTO employee_shift_registrations (registration_id, employee_id, slot_id, effective_from, is_active)
-- VALUES
-- (1, 2, 'SLOT_PART_MORN', '2025-10-01', TRUE), -- Bác sĩ 1 (ID 2)
-- (2, 4, 'SLOT_AFTR_HC', '2025-10-01', TRUE),   -- Lễ tân (ID 4)
-- (3, 6, 'SLOT_PART_AFTR', '2025-10-01', TRUE)  -- Y tá (ID 6)
-- ON CONFLICT (registration_id) DO NOTHING;

-- STEP 11.3: TẠO NGÀY ĐĂNG KÝ
-- TODO: Uncomment when registration_days table is created
-- INSERT INTO registration_days (registration_id, day_of_week)
-- VALUES
-- (1, 'MONDAY'), (1, 'WEDNESDAY'), (1, 'FRIDAY'), -- Bác sĩ 1
-- (2, 'MONDAY'), (2, 'TUESDAY'), (2, 'WEDNESDAY'), (2, 'THURSDAY'), (2, 'FRIDAY'), -- Lễ tân
-- (3, 'SATURDAY'), (3, 'SUNDAY') -- Y tá
-- ON CONFLICT (registration_id, day_of_week) DO NOTHING;

-- ============================================
-- STEP 12: ĐỒNG BỘ HÓA CÁC CHUỖI (SEQUENCES)
-- ============================================
-- Cập nhật giá trị auto-increment để các lệnh INSERT
-- thủ công trong tương lai không bị xung đột.
-- Chúng ta +1 so với ID lớn nhất đã chèn.
-- (Nếu bảng trống, nó sẽ báo lỗi, nhưng trong trường hợp này là an toàn)

SELECT setval(pg_get_serial_sequence('accounts', 'account_id'), COALESCE((SELECT MAX(account_id) FROM accounts), 0));
SELECT setval(pg_get_serial_sequence('employees', 'employee_id'), COALESCE((SELECT MAX(employee_id) FROM employees), 0));
SELECT setval(pg_get_serial_sequence('patients', 'patient_id'), COALESCE((SELECT MAX(patient_id) FROM patients), 0));
SELECT setval(pg_get_serial_sequence('specializations', 'specialization_id'), COALESCE((SELECT MAX(specialization_id) FROM specializations), 0));
-- TODO: Uncomment when employee_shift_registrations table is created
-- SELECT setval(pg_get_serial_sequence('employee_shift_registrations', 'registration_id'), COALESCE((SELECT MAX(registration_id) FROM employee_shift_registrations), 0));

