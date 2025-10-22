-- ============================================
-- HỆ THỐNG QUẢN LÝ PHÒNG KHÁM NHA KHOA
-- Dental Clinic Management System - Seed Data
-- ============================================
-- File này chứa dữ liệu mẫu để khởi tạo hệ thống
-- Bao gồm: Base Roles, Roles, Permissions, Accounts, Employees, Patients
-- ============================================

-- ============================================
-- BƯỚC 1: TẠO BASE ROLES (3 loại cố định)
-- ============================================
-- Base roles xác định:
-- - Layout FE (AdminLayout/EmployeeLayout/PatientLayout)
-- - Default home path (có thể override ở role level)
-- ============================================

INSERT INTO base_roles (base_role_id, base_role_name, default_home_path, description, is_active, created_at)
VALUES
(1, 'admin', '/admin/dashboard', 'Admin Portal - Quản trị viên hệ thống', TRUE, NOW()),
(2, 'employee', '/app/dashboard', 'Employee Portal - Nhân viên phòng khám', TRUE, NOW()),
(3, 'patient', '/patient/dashboard', 'Patient Portal - Bệnh nhân', TRUE, NOW())
ON CONFLICT (base_role_id) DO NOTHING;

-- ============================================
-- BƯỚC 2: TẠO CÁC VAI TRÒ (ROLES)
-- ============================================
-- Mỗi role có:
-- - base_role_id: FK đến base_roles (admin=1, employee=2, patient=3)
-- - home_path_override: Override path (NULL = dùng default từ base_roles)
--
-- Ví dụ:
--   ROLE_DOCTOR: base_role_id=2 (employee), home_path_override='/app/schedule' → Redirect /app/schedule
--   ROLE_NURSE: base_role_id=2 (employee), home_path_override=NULL → Redirect /app/dashboard (default)
-- ============================================

INSERT INTO roles (role_id, role_name, base_role_id, home_path_override, description, requires_specialization, is_active, created_at)
VALUES
-- Admin Portal (base_role_id = 1)
('ROLE_ADMIN', 'ROLE_ADMIN', 1, NULL,
'Quản trị viên hệ thống - Toàn quyền quản lý', FALSE, TRUE, NOW()),

-- Employee Portal (base_role_id = 2)
('ROLE_DOCTOR', 'ROLE_DOCTOR', 2, NULL,
'Bác sĩ nha khoa - Khám và điều trị bệnh nhân', TRUE, TRUE, NOW()),

('ROLE_NURSE', 'ROLE_NURSE', 2, NULL,
'Y tá - Hỗ trợ điều trị và chăm sóc bệnh nhân', TRUE, TRUE, NOW()),

('ROLE_RECEPTIONIST', 'ROLE_RECEPTIONIST', 2, NULL,
'Lễ tân - Tiếp đón và quản lý lịch hẹn', FALSE, TRUE, NOW()),

('ROLE_ACCOUNTANT', 'ROLE_ACCOUNTANT', 2, NULL,
'Kế toán - Quản lý tài chính và thanh toán', FALSE, TRUE, NOW()),

('ROLE_INVENTORY_MANAGER', 'ROLE_INVENTORY_MANAGER', 2, NULL,
'Quản lý kho - Quản lý vật tư và thuốc men', FALSE, TRUE, NOW()),

('ROLE_MANAGER', 'ROLE_MANAGER', 2, NULL,
'Quản lý - Quản lý vận hành và nhân sự', FALSE, TRUE, NOW()),

-- Patient Portal (base_role_id = 3)
('ROLE_PATIENT', 'ROLE_PATIENT', 3, NULL,
'Bệnh nhân - Xem hồ sơ và đặt lịch khám', FALSE, TRUE, NOW())
ON CONFLICT (role_id) DO NOTHING;

-- ============================================
-- BƯỚC 3: TẠO CÁC QUYỀN (PERMISSIONS)
-- ============================================
-- Mỗi permission có:
-- - module: Nhóm chức năng (dùng để group trong sidebar)
-- - path: Đường dẫn trang (NULL = quyền hành động, không hiển thị sidebar)
-- - display_order: Thứ tự hiển thị trong sidebar
-- - parent_permission_id: Quyền cha (để tạo hierarchy: VIEW_ALL > VIEW_OWN)
-- ============================================

-- MODULE: ACCOUNT (Quản lý tài khoản)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_ACCOUNT', 'VIEW_ACCOUNT', 'ACCOUNT', 'Xem danh sách tài khoản', '/admin/accounts', 10, NULL, TRUE, NOW()),
('CREATE_ACCOUNT', 'CREATE_ACCOUNT', 'ACCOUNT', 'Tạo tài khoản mới', NULL, 11, NULL, TRUE, NOW()),
('UPDATE_ACCOUNT', 'UPDATE_ACCOUNT', 'ACCOUNT', 'Cập nhật tài khoản', NULL, 12, NULL, TRUE, NOW()),
('DELETE_ACCOUNT', 'DELETE_ACCOUNT', 'ACCOUNT', 'Xóa tài khoản', NULL, 13, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: EMPLOYEE (Quản lý nhân viên)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_EMPLOYEE', 'VIEW_EMPLOYEE', 'EMPLOYEE', 'Xem danh sách nhân viên', '/app/employees', 20, NULL, TRUE, NOW()),
('CREATE_EMPLOYEE', 'CREATE_EMPLOYEE', 'EMPLOYEE', 'Tạo nhân viên mới', NULL, 21, NULL, TRUE, NOW()),
('UPDATE_EMPLOYEE', 'UPDATE_EMPLOYEE', 'EMPLOYEE', 'Cập nhật thông tin nhân viên', NULL, 22, NULL, TRUE, NOW()),
('DELETE_EMPLOYEE', 'DELETE_EMPLOYEE', 'EMPLOYEE', 'Xóa nhân viên', NULL, 23, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: PATIENT (Quản lý bệnh nhân)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_PATIENT', 'VIEW_PATIENT', 'PATIENT', 'Xem danh sách bệnh nhân', '/app/patients', 30, NULL, TRUE, NOW()),
('CREATE_PATIENT', 'CREATE_PATIENT', 'PATIENT', 'Tạo hồ sơ bệnh nhân mới', NULL, 31, NULL, TRUE, NOW()),
('UPDATE_PATIENT', 'UPDATE_PATIENT', 'PATIENT', 'Cập nhật hồ sơ bệnh nhân', NULL, 32, NULL, TRUE, NOW()),
('DELETE_PATIENT', 'DELETE_PATIENT', 'PATIENT', 'Xóa hồ sơ bệnh nhân', NULL, 33, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: TREATMENT (Điều trị)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_TREATMENT', 'VIEW_TREATMENT', 'TREATMENT', 'Xem danh sách điều trị', '/app/treatments', 40, NULL, TRUE, NOW()),
('CREATE_TREATMENT', 'CREATE_TREATMENT', 'TREATMENT', 'Tạo phác đồ điều trị mới', NULL, 41, NULL, TRUE, NOW()),
('UPDATE_TREATMENT', 'UPDATE_TREATMENT', 'TREATMENT', 'Cập nhật phác đồ điều trị', NULL, 42, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: APPOINTMENT (Lịch hẹn)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_APPOINTMENT', 'VIEW_APPOINTMENT', 'APPOINTMENT', 'Xem danh sách lịch hẹn', '/app/appointments', 50, NULL, TRUE, NOW()),
('CREATE_APPOINTMENT', 'CREATE_APPOINTMENT', 'APPOINTMENT', 'Đặt lịch hẹn mới', NULL, 51, NULL, TRUE, NOW()),
('UPDATE_APPOINTMENT', 'UPDATE_APPOINTMENT', 'APPOINTMENT', 'Cập nhật lịch hẹn', NULL, 52, NULL, TRUE, NOW()),
('DELETE_APPOINTMENT', 'DELETE_APPOINTMENT', 'APPOINTMENT', 'Hủy lịch hẹn', NULL, 53, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: CONTACT (Liên hệ khách hàng)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_CONTACT', 'VIEW_CONTACT', 'CONTACT', 'Xem danh sách liên hệ', '/app/contacts', 60, NULL, TRUE, NOW()),
('CREATE_CONTACT', 'CREATE_CONTACT', 'CONTACT', 'Tạo liên hệ mới', NULL, 61, NULL, TRUE, NOW()),
('UPDATE_CONTACT', 'UPDATE_CONTACT', 'CONTACT', 'Cập nhật liên hệ', NULL, 62, NULL, TRUE, NOW()),
('DELETE_CONTACT', 'DELETE_CONTACT', 'CONTACT', 'Xóa liên hệ', NULL, 63, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: CONTACT_HISTORY (Lịch sử liên hệ)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_CONTACT_HISTORY', 'VIEW_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Xem lịch sử liên hệ', '/app/contact-history', 70, NULL, TRUE, NOW()),
('CREATE_CONTACT_HISTORY', 'CREATE_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Tạo lịch sử liên hệ', NULL, 71, NULL, TRUE, NOW()),
('UPDATE_CONTACT_HISTORY', 'UPDATE_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Cập nhật lịch sử', NULL, 72, NULL, TRUE, NOW()),
('DELETE_CONTACT_HISTORY', 'DELETE_CONTACT_HISTORY', 'CONTACT_HISTORY', 'Xóa lịch sử', NULL, 73, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: WORK_SHIFTS (Ca làm việc)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_WORK_SHIFTS', 'VIEW_WORK_SHIFTS', 'WORK_SHIFTS', 'Xem danh sách ca làm việc', '/app/work-shifts', 80, NULL, TRUE, NOW()),
('CREATE_WORK_SHIFTS', 'CREATE_WORK_SHIFTS', 'WORK_SHIFTS', 'Tạo mẫu ca mới', NULL, 81, NULL, TRUE, NOW()),
('UPDATE_WORK_SHIFTS', 'UPDATE_WORK_SHIFTS', 'WORK_SHIFTS', 'Cập nhật ca làm việc', NULL, 82, NULL, TRUE, NOW()),
('DELETE_WORK_SHIFTS', 'DELETE_WORK_SHIFTS', 'WORK_SHIFTS', 'Xóa ca làm việc', NULL, 83, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: REGISTRATION (Đăng ký ca - VÍ DỤ Parent-Child Permission)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Parent permission: Xem TẤT CẢ đăng ký ca
('VIEW_REGISTRATION_ALL', 'VIEW_REGISTRATION_ALL', 'REGISTRATION',
'Xem tất cả đăng ký ca (bao gồm của mình và người khác)', '/app/registrations', 90, NULL, TRUE, NOW()),

-- Child permission: Chỉ xem đăng ký ca CỦA MÌNH (sẽ bị ẩn khỏi sidebar nếu có parent)
('VIEW_REGISTRATION_OWN', 'VIEW_REGISTRATION_OWN', 'REGISTRATION',
'Chỉ xem đăng ký ca của chính mình', '/app/my-registrations', 91, 'VIEW_REGISTRATION_ALL', TRUE, NOW()),

-- Action permissions (không có path, không hiển thị sidebar)
('CREATE_REGISTRATION', 'CREATE_REGISTRATION', 'REGISTRATION', 'Tạo đăng ký ca mới', NULL, 92, NULL, TRUE, NOW()),
('UPDATE_REGISTRATION', 'UPDATE_REGISTRATION', 'REGISTRATION', 'Cập nhật đăng ký ca', NULL, 93, NULL, TRUE, NOW()),
('DELETE_REGISTRATION', 'DELETE_REGISTRATION', 'REGISTRATION', 'Xóa đăng ký ca', NULL, 94, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: TIME_OFF (Nghỉ phép - VÍ DỤ Parent-Child Permission)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Parent permission: Xem TẤT CẢ nghỉ phép
('VIEW_TIME_OFF_ALL', 'VIEW_TIME_OFF_ALL', 'TIME_OFF',
'Xem tất cả yêu cầu nghỉ phép (bao gồm của mình và người khác)', '/app/time-off-requests', 100, NULL, TRUE, NOW()),

-- Child permission: Chỉ xem nghỉ phép CỦA MÌNH
('VIEW_TIME_OFF_OWN', 'VIEW_TIME_OFF_OWN', 'TIME_OFF',
'Chỉ xem yêu cầu nghỉ phép của chính mình', '/app/my-time-off', 101, 'VIEW_TIME_OFF_ALL', TRUE, NOW()),

-- Action permissions
('CREATE_TIME_OFF', 'CREATE_TIME_OFF', 'TIME_OFF', 'Tạo yêu cầu nghỉ phép', NULL, 102, NULL, TRUE, NOW()),
('APPROVE_TIME_OFF', 'APPROVE_TIME_OFF', 'TIME_OFF', 'Phê duyệt yêu cầu nghỉ phép', NULL, 103, NULL, TRUE, NOW()),
('REJECT_TIME_OFF', 'REJECT_TIME_OFF', 'TIME_OFF', 'Từ chối yêu cầu nghỉ phép', NULL, 104, NULL, TRUE, NOW()),
('CANCEL_TIME_OFF_OWN', 'CANCEL_TIME_OFF_OWN', 'TIME_OFF', 'Hủy yêu cầu nghỉ phép của bản thân', NULL, 105, NULL, TRUE, NOW()),
('CANCEL_TIME_OFF_PENDING', 'CANCEL_TIME_OFF_PENDING', 'TIME_OFF', 'Hủy yêu cầu nghỉ phép đang chờ', NULL, 106, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: TIME_OFF_MANAGEMENT (Quản lý Nghỉ phép)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_TIMEOFF_TYPE_ALL', 'VIEW_TIMEOFF_TYPE_ALL', 'TIME_OFF_MANAGEMENT', 'Xem/Quản lý các loại nghỉ phép', '/admin/time-off-types', 110, NULL, TRUE, NOW()),
('CREATE_TIMEOFF_TYPE', 'CREATE_TIMEOFF_TYPE', 'TIME_OFF_MANAGEMENT', 'Tạo loại nghỉ phép mới', NULL, 111, NULL, TRUE, NOW()),
('UPDATE_TIMEOFF_TYPE', 'UPDATE_TIMEOFF_TYPE', 'TIME_OFF_MANAGEMENT', 'Cập nhật loại nghỉ phép', NULL, 112, NULL, TRUE, NOW()),
('DELETE_TIMEOFF_TYPE', 'DELETE_TIMEOFF_TYPE', 'TIME_OFF_MANAGEMENT', 'Xóa/vô hiệu hóa loại nghỉ phép', NULL, 113, NULL, TRUE, NOW()),
('VIEW_LEAVE_BALANCE_ALL', 'VIEW_LEAVE_BALANCE_ALL', 'TIME_OFF_MANAGEMENT', 'Xem số dư nghỉ phép của nhân viên', '/admin/leave-balances', 114, NULL, TRUE, NOW()),
('ADJUST_LEAVE_BALANCE', 'ADJUST_LEAVE_BALANCE', 'TIME_OFF_MANAGEMENT', 'Điều chỉnh số dư nghỉ phép', NULL, 115, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: OVERTIME (Tăng ca)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_OT_ALL', 'VIEW_OT_ALL', 'OVERTIME', 'Xem tất cả yêu cầu Tăng ca', '/app/overtime-requests', 120, NULL, TRUE, NOW()),
('VIEW_OT_OWN', 'VIEW_OT_OWN', 'OVERTIME', 'Xem yêu cầu tăng ca của bản thân', '/app/my-overtime', 121, 'VIEW_OT_ALL', TRUE, NOW()),
('CREATE_OT', 'CREATE_OT', 'OVERTIME', 'Tạo yêu cầu tăng ca', NULL, 122, NULL, TRUE, NOW()),
('APPROVE_OT', 'APPROVE_OT', 'OVERTIME', 'Phê duyệt yêu cầu tăng ca', NULL, 123, NULL, TRUE, NOW()),
('REJECT_OT', 'REJECT_OT', 'OVERTIME', 'Từ chối yêu cầu tăng ca', NULL, 124, NULL, TRUE, NOW()),
('CANCEL_OT_OWN', 'CANCEL_OT_OWN', 'OVERTIME', 'Hủy yêu cầu tăng ca của bản thân', NULL, 125, NULL, TRUE, NOW()),
('CANCEL_OT_PENDING', 'CANCEL_OT_PENDING', 'OVERTIME', 'Hủy yêu cầu tăng ca đang chờ', NULL, 126, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;


-- ============================================
-- BƯỚC 4: PHÂN QUYỀN CHO CÁC VAI TRÒ
-- ============================================

-- Admin có TẤT CẢ quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_ADMIN', permission_id FROM permissions WHERE is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Bác sĩ (Doctor)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_PATIENT'),
('ROLE_DOCTOR', 'UPDATE_PATIENT'),
('ROLE_DOCTOR', 'VIEW_TREATMENT'),
('ROLE_DOCTOR', 'CREATE_TREATMENT'),
('ROLE_DOCTOR', 'UPDATE_TREATMENT'),
('ROLE_DOCTOR', 'VIEW_APPOINTMENT'),
('ROLE_DOCTOR', 'VIEW_REGISTRATION_OWN'), -- Chỉ xem đăng ký ca của mình
('ROLE_DOCTOR', 'VIEW_TIME_OFF_OWN'), -- Chỉ xem nghỉ phép của mình
('ROLE_DOCTOR', 'CREATE_TIME_OFF'),
('ROLE_DOCTOR', 'CANCEL_TIME_OFF_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Y tá (Nurse)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_NURSE', 'VIEW_PATIENT'),
('ROLE_NURSE', 'VIEW_TREATMENT'),
('ROLE_NURSE', 'VIEW_APPOINTMENT'),
('ROLE_NURSE', 'VIEW_REGISTRATION_OWN'),
('ROLE_NURSE', 'VIEW_TIME_OFF_OWN'),
('ROLE_NURSE', 'CREATE_TIME_OFF'),
('ROLE_NURSE', 'CANCEL_TIME_OFF_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Lễ tân (Receptionist)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_RECEPTIONIST', 'VIEW_PATIENT'),
('ROLE_RECEPTIONIST', 'CREATE_PATIENT'),
('ROLE_RECEPTIONIST', 'UPDATE_PATIENT'),
('ROLE_RECEPTIONIST', 'VIEW_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'CREATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'UPDATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'DELETE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'VIEW_CONTACT'),
('ROLE_RECEPTIONIST', 'CREATE_CONTACT'),
('ROLE_RECEPTIONIST', 'UPDATE_CONTACT'),
('ROLE_RECEPTIONIST', 'DELETE_CONTACT'),
('ROLE_RECEPTIONIST', 'VIEW_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'CREATE_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'UPDATE_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'DELETE_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'VIEW_REGISTRATION_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_TIME_OFF_OWN'),
('ROLE_RECEPTIONIST', 'CREATE_TIME_OFF'),
('ROLE_RECEPTIONIST', 'CANCEL_TIME_OFF_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Quản lý (Manager)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_MANAGER', 'VIEW_EMPLOYEE'),
('ROLE_MANAGER', 'CREATE_EMPLOYEE'),
('ROLE_MANAGER', 'UPDATE_EMPLOYEE'),
('ROLE_MANAGER', 'DELETE_EMPLOYEE'),
('ROLE_MANAGER', 'VIEW_PATIENT'),
('ROLE_MANAGER', 'VIEW_APPOINTMENT'),
('ROLE_MANAGER', 'VIEW_WORK_SHIFTS'),
('ROLE_MANAGER', 'CREATE_WORK_SHIFTS'),
('ROLE_MANAGER', 'UPDATE_WORK_SHIFTS'),
('ROLE_MANAGER', 'DELETE_WORK_SHIFTS'),
('ROLE_MANAGER', 'VIEW_REGISTRATION_ALL'), -- Xem TẤT CẢ đăng ký ca
('ROLE_MANAGER', 'CREATE_REGISTRATION'),
('ROLE_MANAGER', 'UPDATE_REGISTRATION'),
('ROLE_MANAGER', 'DELETE_REGISTRATION'),
('ROLE_MANAGER', 'VIEW_TIME_OFF_ALL'), -- Xem TẤT CẢ nghỉ phép
('ROLE_MANAGER', 'APPROVE_TIME_OFF'),
('ROLE_MANAGER', 'REJECT_TIME_OFF'),
('ROLE_MANAGER', 'CANCEL_TIME_OFF_PENDING')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Bệnh nhân (Patient)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_PATIENT', 'VIEW_PATIENT'), -- Chỉ xem hồ sơ của mình
('ROLE_PATIENT', 'VIEW_TREATMENT'), -- Xem phác đồ điều trị của mình
('ROLE_PATIENT', 'VIEW_APPOINTMENT'), -- Xem lịch hẹn của mình
('ROLE_PATIENT', 'CREATE_APPOINTMENT') -- Đặt lịch hẹn cho mình
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Gán quyền tăng ca (Overtime) cho nhân viên
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_OT_OWN'), ('ROLE_DOCTOR', 'CREATE_OT'), ('ROLE_DOCTOR', 'CANCEL_OT_OWN'),
('ROLE_NURSE', 'VIEW_OT_OWN'), ('ROLE_NURSE', 'CREATE_OT'), ('ROLE_NURSE', 'CANCEL_OT_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_OT_OWN'), ('ROLE_RECEPTIONIST', 'CREATE_OT'), ('ROLE_RECEPTIONIST', 'CANCEL_OT_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_OT_OWN'), ('ROLE_ACCOUNTANT', 'CREATE_OT'), ('ROLE_ACCOUNTANT', 'CANCEL_OT_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_OT_OWN'), ('ROLE_INVENTORY_MANAGER', 'CREATE_OT'), ('ROLE_INVENTORY_MANAGER', 'CANCEL_OT_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Gán quyền Quản lý Nghỉ phép + Tăng ca cho Manager (bổ sung cho ROLE_MANAGER)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_MANAGER', 'VIEW_TIMEOFF_TYPE_ALL'),
('ROLE_MANAGER', 'CREATE_TIMEOFF_TYPE'),
('ROLE_MANAGER', 'UPDATE_TIMEOFF_TYPE'),
('ROLE_MANAGER', 'DELETE_TIMEOFF_TYPE'),
('ROLE_MANAGER', 'VIEW_LEAVE_BALANCE_ALL'),
('ROLE_MANAGER', 'ADJUST_LEAVE_BALANCE'),
('ROLE_MANAGER', 'VIEW_OT_ALL'),
('ROLE_MANAGER', 'APPROVE_OT'),
('ROLE_MANAGER', 'REJECT_OT'),
('ROLE_MANAGER', 'CANCEL_OT_PENDING')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================
-- BƯỚC 5: TẠO CHUYÊN KHOA
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
-- BƯỚC 6: TẠO TÀI KHOẢN VÀ GÁN ROLE
-- ============================================
-- Lưu ý: Mật khẩu đều là "123456" (đã mã hóa BCrypt)
-- Mỗi tài khoản có role_id trỏ trực tiếp đến bảng roles
-- ============================================

INSERT INTO accounts (account_id, account_code, username, email, password, role_id, status, created_at)
VALUES
-- Admin
(1, 'ACC001', 'admin', 'admin@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_ADMIN', 'ACTIVE', NOW()),

-- Nhân viên (Employees)
(2, 'ACC002', 'nhasi1', 'nhasi1@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_DOCTOR', 'ACTIVE', NOW()),

(3, 'ACC003', 'nhasi2', 'nhasi2@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_DOCTOR', 'ACTIVE', NOW()),

(4, 'ACC004', 'letan', 'letan@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_RECEPTIONIST', 'ACTIVE', NOW()),

(5, 'ACC005', 'ketoan', 'ketoan@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_ACCOUNTANT', 'ACTIVE', NOW()),

(6, 'ACC006', 'yta', 'yta@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_NURSE', 'ACTIVE', NOW()),

(7, 'ACC007', 'manager', 'manager@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_MANAGER', 'ACTIVE', NOW()),

-- Bệnh nhân (Patients)
(8, 'ACC008', 'benhnhan1', 'benhnhan1@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', NOW()),

(9, 'ACC009', 'benhnhan2', 'benhnhan2@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', NOW()),

(10, 'ACC010', 'benhnhan3', 'benhnhan3@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', NOW())

ON CONFLICT (account_id) DO NOTHING;

-- ============================================
-- BƯỚC 7: TẠO THÔNG TIN NHÂN VIÊN
-- ============================================
-- Lưu ý: employee KHÔNG còn trường role_id (đã chuyển sang account.role_id)
-- ============================================

INSERT INTO employees (employee_id, account_id, employee_code, first_name, last_name, phone, date_of_birth, address, employment_type, is_active, created_at)
VALUES
(1, 1, 'EMP001', 'Admin', 'Hệ thống', '0900000001', '1985-01-01', 'Phòng quản trị', 'FULL_TIME', TRUE, NOW()),
(2, 2, 'EMP002', 'Minh', 'Nguyễn Văn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', 'FULL_TIME', TRUE, NOW()),
(3, 3, 'EMP003', 'Lan', 'Trần Thị', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', 'FULL_TIME', TRUE, NOW()),
(4, 4, 'EMP004', 'Mai', 'Lê Thị', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', 'FULL_TIME', TRUE, NOW()),
(5, 5, 'EMP005', 'Tuấn', 'Hoàng Văn', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', 'FULL_TIME', TRUE, NOW()),
(6, 6, 'EMP006', 'Hoa', 'Phạm Thị', '0906789012', '1992-06-15', '111 Lý Thường Kiệt, Q10, TPHCM', 'PART_TIME', TRUE, NOW()),
(7, 7, 'EMP007', 'Quân', 'Trần Minh', '0909999999', '1980-10-10', '77 Phạm Ngọc Thạch, Q3, TPHCM', 'FULL_TIME', TRUE, NOW())
ON CONFLICT (employee_id) DO NOTHING;

-- ============================================
-- BƯỚC 8: GÁN CHUYÊN KHOA CHO NHÂN VIÊN
-- ============================================
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(2, 1), (2, 7), -- Bác sĩ 1 (ID 2)
(3, 2), (3, 4), -- Bác sĩ 2 (ID 3)
(6, 6) -- Y tá (ID 6)
ON CONFLICT (employee_id, specialization_id) DO NOTHING;

-- ============================================
-- BƯỚC 9: TẠO THÔNG TIN BỆNH NHÂN
-- ============================================
INSERT INTO patients (patient_id, account_id, patient_code, first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES
(1, 8, 'PAT001', 'Khang', 'Nguyễn Văn', 'benhnhan1@email.com', '0911111111', '1990-01-15', '123 Lê Văn Việt, Q9, TPHCM', 'MALE', TRUE, NOW(), NOW()),
(2, 9, 'PAT002', 'Lan', 'Trần Thị', 'benhnhan2@email.com', '0922222222', '1985-05-20', '456 Võ Văn Ngân, Thủ Đức, TPHCM', 'FEMALE', TRUE, NOW(), NOW()),
(3, 10, 'PAT003', 'Đức', 'Lê Minh', 'benhnhan3@email.com', '0933333333', '1995-12-10', '789 Đường D2, Bình Thạnh, TPHCM', 'MALE', TRUE, NOW(), NOW())
ON CONFLICT (patient_id) DO NOTHING;

-- ============================================
-- BƯỚC 10: TẠO MẪU CA LÀM VIỆC
-- ============================================
INSERT INTO work_shifts (work_shift_id, shift_name, start_time, end_time, category, is_active)
VALUES
('WKS_MORNING_01', 'Ca Sáng (8h-16h)', '08:00:00', '16:00:00', 'NORMAL', TRUE),
('WKS_AFTERNOON_01', 'Ca Chiều (13h-20h)', '13:00:00', '20:00:00', 'NORMAL', TRUE),
('WKS_MORNING_02', 'Ca Part-time Sáng (8h-12h)', '08:00:00', '12:00:00', 'NORMAL', TRUE),
('WKS_AFTERNOON_02', 'Ca Part-time Chiều (13h-17h)', '13:00:00', '17:00:00', 'NORMAL', TRUE)
ON CONFLICT (work_shift_id) DO NOTHING;

-- ============================================
-- BƯỚC 11: TẠO LOẠI NGHỈ PHÉP (TIME OFF TYPES)
-- ============================================
-- Chỉ tạo 3 loại cơ bản, các loại khác sẽ thêm sau khi cần
INSERT INTO time_off_types (type_id, type_code, type_name, is_paid, requires_approval, is_active, created_at, updated_at)
VALUES
-- 1. Nghỉ phép năm - CẦN TRACKING số dư (12 ngày/năm theo luật)
('TOT001', 'ANNUAL_LEAVE', 'Nghi phep nam (12 ngay/nam)', TRUE, TRUE, TRUE, NOW(), NOW()),

-- 2. Nghỉ không lương - KHÔNG tracking (theo thỏa thuận, không giới hạn)
('TOT002', 'UNPAID_PERSONAL', 'Nghi viec rieng khong luong (theo thoa thuan)', FALSE, TRUE, TRUE, NOW(), NOW()),

-- 3. Nghỉ ốm - CÓ LƯƠNG nhưng KHÔNG tracking (BHXH quản lý)
('TOT003', 'SICK_LEAVE', 'Nghi om co bao hiem xa hoi', TRUE, TRUE, TRUE, NOW(), NOW())
ON CONFLICT (type_id) DO NOTHING;

-- Ghi chú: Các loại nghỉ khác (kết hôn, tang, thai sản...) sẽ được thêm sau khi cần
-- Xem chi tiết trong file QUY_DINH_NGAY_NGHI.md

-- ============================================
-- BƯỚC 12: TẠO SỐ DƯ NGHỈ PHÉP CHO NHÂN VIÊN
-- ============================================
-- Chỉ tạo số dư cho NGHỈ PHÉP NĂM (TOT001) - 12 ngày/năm theo luật
-- Các loại nghỉ khác không cần tracking số dư (là fixed days hoặc unpaid)
INSERT INTO employee_leave_balances (balance_id, employee_id, time_off_type_id, cycle_year, total_days_allowed, days_taken, created_at, updated_at)
VALUES
-- Bác sĩ 1 (ID 2) - Nghỉ phép năm
(1, 2, 'TOT001', 2024, 12.0, 3.0, NOW(), NOW()), -- 2024: 12 ngày, đã dùng 3
(2, 2, 'TOT001', 2025, 12.0, 0.0, NOW(), NOW()), -- 2025: 12 ngày, chưa dùng

-- Bác sĩ 2 (ID 3) - Nghỉ phép năm
(3, 3, 'TOT001', 2024, 12.0, 5.0, NOW(), NOW()),
(4, 3, 'TOT001', 2025, 12.0, 0.0, NOW(), NOW()),

-- Lễ tân (ID 4) - Nghỉ phép năm
(5, 4, 'TOT001', 2024, 12.0, 4.0, NOW(), NOW()),
(6, 4, 'TOT001', 2025, 12.0, 0.0, NOW(), NOW()),

-- Kế toán (ID 5) - Nghỉ phép năm
(7, 5, 'TOT001', 2024, 12.0, 2.0, NOW(), NOW()),
(8, 5, 'TOT001', 2025, 12.0, 0.0, NOW(), NOW()),

-- Y tá (ID 6) - Nghỉ phép năm
(9, 6, 'TOT001', 2024, 12.0, 1.0, NOW(), NOW()),
(10, 6, 'TOT001', 2025, 12.0, 0.0, NOW(), NOW()),

-- Quản lý (ID 7) - Nghỉ phép năm (quản lý được thưởng thêm)
(11, 7, 'TOT001', 2024, 15.0, 6.0, NOW(), NOW()), -- Quản lý: 15 ngày
(12, 7, 'TOT001', 2025, 15.0, 0.0, NOW(), NOW())
ON CONFLICT (balance_id) DO NOTHING;

-- ============================================
-- BƯỚC 13: TẠO LỊCH SỬ THAY ĐỔI SỐ DƯ
-- ============================================
-- Lịch sử khởi tạo số dư ban đầu + trừ số dư khi có yêu cầu được phê duyệt
INSERT INTO leave_balance_history (history_id, balance_id, changed_by, change_amount, reason, notes, created_at)
VALUES
-- Khởi tạo số dư năm 2024
(1, 1, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2024 - Bác sĩ 1', NOW()),
(2, 3, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2024 - Bác sĩ 2', NOW()),
(3, 5, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2024 - Lễ tân', NOW()),
(4, 7, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2024 - Kế toán', NOW()),
(5, 9, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2024 - Y tá', NOW()),
(6, 11, 1, 15.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2024 - Quản lý', NOW()),

-- Khởi tạo số dư năm 2025
(7, 2, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2025 - Bác sĩ 1', NOW()),
(8, 4, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2025 - Bác sĩ 2', NOW()),
(9, 6, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2025 - Lễ tân', NOW()),
(10, 8, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2025 - Kế toán', NOW()),
(11, 10, 1, 12.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2025 - Y tá', NOW()),
(12, 12, 1, 15.0, 'ANNUAL_RESET', 'Khởi tạo số dư nghỉ phép năm 2025 - Quản lý', NOW()),

-- Lịch sử trừ số dư khi nhân viên đã xin nghỉ (giả định đã có yêu cầu được phê duyệt)
(13, 1, 1, -3.0, 'APPROVED_REQUEST', 'Trừ 3 ngày nghỉ phép năm đã được phê duyệt - Bác sĩ 1', NOW()),
(14, 3, 1, -5.0, 'APPROVED_REQUEST', 'Trừ 5 ngày nghỉ phép năm đã được phê duyệt - Bác sĩ 2', NOW()),
(15, 5, 1, -4.0, 'APPROVED_REQUEST', 'Trừ 4 ngày nghỉ phép năm đã được phê duyệt - Lễ tân', NOW()),
(16, 7, 1, -2.0, 'APPROVED_REQUEST', 'Trừ 2 ngày nghỉ phép năm đã được phê duyệt - Kế toán', NOW()),
(17, 9, 1, -1.0, 'APPROVED_REQUEST', 'Trừ 1 ngày nghỉ phép năm đã được phê duyệt - Y tá', NOW()),
(18, 11, 1, -6.0, 'APPROVED_REQUEST', 'Trừ 6 ngày nghỉ phép năm đã được phê duyệt - Quản lý', NOW())
ON CONFLICT (history_id) DO NOTHING;

-- ============================================
-- BƯỚC 14: TẠO ĐỊNH NGHĨA NGÀY LỄ QUỐC GIA
-- ============================================
-- Chỉ lưu các ngày lễ QUỐC GIA theo Luật Lao động Việt Nam
-- Các ngày nghỉ việc riêng (kết hôn, tang...) sẽ thêm sau khi cần
INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, created_at, updated_at)
VALUES
(1, 'Tết Dương lịch - 01/01', 'NATIONAL', NOW(), NOW()),
(2, 'Tết Nguyên đán - 5 ngày theo lịch nhà nước', 'NATIONAL', NOW(), NOW()),
(3, 'Giỗ Tổ Hùng Vương - 10/3 Âm lịch', 'NATIONAL', NOW(), NOW()),
(4, 'Ngày Chiến thắng - 30/04', 'NATIONAL', NOW(), NOW()),
(5, 'Ngày Quốc tế Lao động - 01/05', 'NATIONAL', NOW(), NOW()),
(6, 'Quốc khánh - 02/09', 'NATIONAL', NOW(), NOW()),
(7, 'Quốc khánh - 01 ngày liền kề', 'NATIONAL', NOW(), NOW())
ON CONFLICT (definition_id) DO NOTHING;

-- Ghi chú: Các ngày lễ khác (công ty, kết hôn, tang...) xem file QUY_DINH_NGAY_NGHI.md

-- ============================================
-- BƯỚC 15: ĐỒNG BỘ CÁC SEQUENCE (AUTO INCREMENT)
-- ============================================

SELECT setval(pg_get_serial_sequence('base_roles', 'base_role_id'), COALESCE((SELECT MAX(base_role_id) FROM base_roles), 0)+1, false);
SELECT setval(pg_get_serial_sequence('accounts', 'account_id'), COALESCE((SELECT MAX(account_id) FROM accounts), 0)+1, false);
SELECT setval(pg_get_serial_sequence('employees', 'employee_id'), COALESCE((SELECT MAX(employee_id) FROM employees), 0)+1, false);
SELECT setval(pg_get_serial_sequence('patients', 'patient_id'), COALESCE((SELECT MAX(patient_id) FROM patients), 0)+1, false);
SELECT setval(pg_get_serial_sequence('specializations', 'specialization_id'), COALESCE((SELECT MAX(specialization_id) FROM specializations), 0)+1, false);
SELECT setval(pg_get_serial_sequence('employee_leave_balances', 'balance_id'), COALESCE((SELECT MAX(balance_id) FROM employee_leave_balances), 0)+1, false);
SELECT setval(pg_get_serial_sequence('leave_balance_history', 'history_id'), COALESCE((SELECT MAX(history_id) FROM leave_balance_history), 0)+1, false);
SELECT setval(pg_get_serial_sequence('holiday_definitions', 'definition_id'), COALESCE((SELECT MAX(definition_id) FROM holiday_definitions), 0)+1, false);

-- ============================================
-- BƯỚC 16: TẠO DỮ LIỆU NGÀY LỄ (HOLIDAY_DATES)
-- ============================================
-- Các ngày lễ Quốc gia năm 2025 (Theo Luật Lao động Việt Nam)
INSERT INTO holiday_dates (holiday_id, holiday_date, holiday_name, year, description, created_at, updated_at)
VALUES
(1, '2025-01-01', 'Tết Dương lịch', 2025, 'Ngày Tết Dương lịch 01/01/2025', NOW(), NOW()),
(2, '2025-01-28', 'Tết Nguyên đán (30 Tết)', 2025, 'Ngày 30 Tết Âm lịch năm 2025', NOW(), NOW()),
(3, '2025-01-29', 'Tết Nguyên đán (Mùng 1)', 2025, 'Mùng 1 Tết Nguyên đán năm 2025', NOW(), NOW()),
(4, '2025-01-30', 'Tết Nguyên đán (Mùng 2)', 2025, 'Mùng 2 Tết Nguyên đán năm 2025', NOW(), NOW()),
(5, '2025-01-31', 'Tết Nguyên đán (Mùng 3)', 2025, 'Mùng 3 Tết Nguyên đán năm 2025', NOW(), NOW()),
(6, '2025-02-01', 'Tết Nguyên đán (Mùng 4)', 2025, 'Mùng 4 Tết Nguyên đán năm 2025', NOW(), NOW()),
(7, '2025-04-07', 'Giỗ Tổ Hùng Vương', 2025, 'Giỗ Tổ Hùng Vương 10/3 Âm lịch năm 2025', NOW(), NOW()),
(8, '2025-04-30', 'Ngày Giải phóng miền Nam', 2025, 'Ngày Chiến thắng 30/04/1975', NOW(), NOW()),
(9, '2025-05-01', 'Ngày Quốc tế Lao động', 2025, 'Ngày Quốc tế Lao động 01/05', NOW(), NOW()),
(10, '2025-09-01', 'Ngày nghỉ liền kề Quốc khánh', 2025, 'Ngày nghỉ bù cho Quốc khánh', NOW(), NOW()),
(11, '2025-09-02', 'Ngày Quốc khánh', 2025, 'Quốc khánh nước Cộng hoà Xã hội chủ nghĩa Việt Nam', NOW(), NOW()),
(12, '2025-12-25', 'Lễ Giáng sinh', 2025, 'Ngày lễ Giáng sinh (Christmas)', NOW(), NOW())
ON CONFLICT (holiday_id) DO NOTHING;

-- ============================================
-- BƯỚC 17: THÊM PERMISSIONS MỚI CHO SHIFT RENEWAL
-- ============================================
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_RENEWAL_OWN', 'VIEW_RENEWAL_OWN', 'SHIFT_RENEWAL', 'Xem yêu cầu gia hạn ca của bản thân', '/app/shift-renewals', 91, NULL, TRUE, NOW()),
('RESPOND_RENEWAL_OWN', 'RESPOND_RENEWAL_OWN', 'SHIFT_RENEWAL', 'Phản hồi yêu cầu gia hạn ca của bản thân', NULL, 92, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- Gán quyền cho nhân viên PART_TIME (giả sử có trong role_permissions)
-- Lưu ý: Cần mapping role nào có quyền này, ví dụ ROLE_DOCTOR, ROLE_NURSE nếu họ là part-time
INSERT INTO role_permissions (role_id, permission_id, granted_at)
VALUES
('ROLE_DOCTOR', 'VIEW_RENEWAL_OWN', NOW()),
('ROLE_DOCTOR', 'RESPOND_RENEWAL_OWN', NOW()),
('ROLE_NURSE', 'VIEW_RENEWAL_OWN', NOW()),
('ROLE_NURSE', 'RESPOND_RENEWAL_OWN', NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================
-- BƯỚC 18: TẠO DỮ LIỆU MẪU SHIFT_RENEWAL_REQUESTS
-- ============================================
-- Giả sử có 2 đăng ký ca làm việc sắp hết hạn (expiring registrations)
-- Dữ liệu mẫu này giả định đã có registration_id trong working_schedule table

-- Renewal Request 1: Cho nhân viên ID 2 (Bác sĩ 2 - part-time), status PENDING_ACTION
INSERT INTO shift_renewal_requests (renewal_id, expiring_registration_id, employee_id, status, expires_at, confirmed_at, message, created_at, updated_at)
VALUES
('SRR251022001', 'ESR250101001', 2, 'PENDING_ACTION', NOW() + INTERVAL '5 days', NULL,
'Đăng ký ca làm việc của bạn sẽ hết hạn vào ngày 30/10/2025. Vui lòng xác nhận gia hạn thêm 3 tháng.',
NOW(), NOW())
ON CONFLICT (renewal_id) DO NOTHING;

-- Renewal Request 2: Cho nhân viên ID 5 (Y tá - part-time), status CONFIRMED
INSERT INTO shift_renewal_requests (renewal_id, expiring_registration_id, employee_id, status, expires_at, confirmed_at, message, created_at, updated_at)
VALUES
('SRR251022002', 'ESR250201001', 5, 'CONFIRMED', NOW() + INTERVAL '7 days', NOW(),
'Đăng ký ca làm việc đã được gia hạn thêm 3 tháng kể từ ngày hết hạn.',
NOW() - INTERVAL '2 days', NOW())
ON CONFLICT (renewal_id) DO NOTHING;

-- Renewal Request 3: Cho nhân viên ID 2, status DECLINED
INSERT INTO shift_renewal_requests (renewal_id, expiring_registration_id, employee_id, status, expires_at, confirmed_at, message, created_at, updated_at)
VALUES
('SRR251015001', 'ESR250101002', 2, 'DECLINED', NOW() + INTERVAL '1 day', NULL,
'Nhân viên đã từ chối gia hạn đăng ký ca làm việc này.',
NOW() - INTERVAL '7 days', NOW())
ON CONFLICT (renewal_id) DO NOTHING;

-- ============================================
-- BƯỚC 19: TẠO DỮ LIỆU MẪU EMPLOYEE_SHIFTS
-- ============================================
-- Tạo lịch ca làm việc cho các nhân viên trong tuần tới (giả định)
-- Lưu ý: work_shift_id cần khớp với dữ liệu trong bảng work_shifts

-- Ca làm việc cho Bác sĩ 1 (Full-time, employee_id=1) - Tuần tới
INSERT INTO employee_shifts (shift_id, employee_id, work_date, work_shift_id, source, registration_id, status, notes, created_at, updated_at)
VALUES
-- Thứ 2 (23/10/2025)
(1, 1, '2025-10-23', 'SLOT_MORNING', 'BATCH_JOB', NULL, 'SCHEDULED', 'Ca sáng thứ 2', NOW(), NOW()),
(2, 1, '2025-10-23', 'SLOT_AFTERNOON', 'BATCH_JOB', NULL, 'SCHEDULED', 'Ca chiều thứ 2', NOW(), NOW()),

-- Thứ 3 (24/10/2025)
(3, 1, '2025-10-24', 'SLOT_MORNING', 'BATCH_JOB', NULL, 'SCHEDULED', 'Ca sáng thứ 3', NOW(), NOW()),
(4, 1, '2025-10-24', 'SLOT_AFTERNOON', 'BATCH_JOB', NULL, 'SCHEDULED', 'Ca chiều thứ 3', NOW(), NOW()),

-- Thứ 4 (25/10/2025)
(5, 1, '2025-10-25', 'SLOT_MORNING', 'BATCH_JOB', NULL, 'SCHEDULED', 'Ca sáng thứ 4', NOW(), NOW()),
(6, 1, '2025-10-25', 'SLOT_AFTERNOON', 'BATCH_JOB', NULL, 'SCHEDULED', 'Ca chiều thứ 4', NOW(), NOW()),

-- Ca làm việc cho Y tá (Part-time, employee_id=5) - Dựa trên registration
(7, 5, '2025-10-23', 'SLOT_MORNING', 'REGISTRATION_JOB', 'ESR250201001', 'SCHEDULED', 'Ca sáng part-time', NOW(), NOW()),
(8, 5, '2025-10-25', 'SLOT_AFTERNOON', 'REGISTRATION_JOB', 'ESR250201001', 'SCHEDULED', 'Ca chiều part-time', NOW(), NOW()),

-- Ca làm việc cho Bác sĩ 2 (Part-time, employee_id=2)
(9, 2, '2025-10-24', 'SLOT_MORNING', 'REGISTRATION_JOB', 'ESR250101001', 'SCHEDULED', 'Ca sáng part-time', NOW(), NOW()),
(10, 2, '2025-10-24', 'SLOT_AFTERNOON', 'REGISTRATION_JOB', 'ESR250101001', 'SCHEDULED', 'Ca chiều part-time', NOW(), NOW()),

-- Ca overtime cho Lễ tân (employee_id=4)
(11, 4, '2025-10-26', 'SLOT_OVERTIME', 'OVERTIME', NULL, 'SCHEDULED', 'Ca tăng ca thứ 7', NOW(), NOW()),

-- Ca đã hoàn thành (COMPLETED)
(12, 1, '2025-10-21', 'SLOT_MORNING', 'BATCH_JOB', NULL, 'COMPLETED', 'Ca sáng đã hoàn thành', NOW() - INTERVAL '2 days', NOW()),
(13, 1, '2025-10-21', 'SLOT_AFTERNOON', 'BATCH_JOB', NULL, 'COMPLETED', 'Ca chiều đã hoàn thành', NOW() - INTERVAL '2 days', NOW()),

-- Ca bị hủy (CANCELLED)
(14, 2, '2025-10-22', 'SLOT_MORNING', 'REGISTRATION_JOB', 'ESR250101001', 'CANCELLED', 'Nhân viên báo nghỉ đột xuất', NOW() - INTERVAL '1 day', NOW()),

-- Ca vắng mặt (ABSENT)
(15, 5, '2025-10-20', 'SLOT_AFTERNOON', 'REGISTRATION_JOB', 'ESR250201001', 'ABSENT', 'Nhân viên không đến làm', NOW() - INTERVAL '3 days', NOW())
ON CONFLICT (shift_id) DO NOTHING;

-- ============================================
-- BƯỚC 20: ĐỒNG BỘ SEQUENCES CHO CÁC BẢNG MỚI
-- ============================================
SELECT setval(pg_get_serial_sequence('holiday_dates', 'holiday_id'), COALESCE((SELECT MAX(holiday_id) FROM holiday_dates), 0)+1, false);
SELECT setval(pg_get_serial_sequence('employee_shifts', 'shift_id'), COALESCE((SELECT MAX(shift_id) FROM employee_shifts), 0)+1, false);

-- ============================================
-- KẾT THÚC SEED DATA
-- ============================================
--
-- HƯỚNG DẪN SỬ DỤNG:
--
-- 1. LOGIN VÀO HỆ THỐNG:
--   - Admin: username="admin", password="123456"
--   - Manager: username="manager", password="123456"
--   - Bác sĩ: username="nhasi1" hoặc "nhasi2", password="123456"
--   - Lễ tân: username="letan", password="123456"
--   - Bệnh nhân: username="benhnhan1", password="123456"
--
-- 2. SAU KHI LOGIN, HỆ THỐNG SẼ:
--   - Trả về baseRole (string: "admin"/"employee"/"patient") để FE chọn layout
--   - Trả về homePath (effective path: override hoặc default) để redirect
--   - Trả về sidebar đã được filter theo quyền
--   - Trả về danh sách permissions để check quyền nút bấm
--
-- 3. BASE ROLE & HOME PATH LOGIC:
--   - Mỗi role thuộc 1 trong 3 base_roles (admin=1, employee=2, patient=3)
--   - Base role xác định: Layout FE + Default home path
--   - Role có thể override home_path (home_path_override column)
--   - Effective home path = home_path_override ?? base_role.default_home_path
--
--   VÍ DỤ:
--     ROLE_DOCTOR: base_role_id=2 (employee), home_path_override=NULL (đã sửa lại)
--       → baseRole='employee', homePath='/app/dashboard' (default từ base_roles)
--
--     ROLE_NURSE: base_role_id=2 (employee), home_path_override=NULL
--       → baseRole='employee', homePath='/app/dashboard' (default từ base_roles)
--
-- 4. VÍ DỤ PARENT-CHILD PERMISSION:
--   - Nếu user có 'VIEW_REGISTRATION_ALL', FE chỉ hiển thị 'VIEW_REGISTRATION_ALL'
--   - Nếu user CHỈ có 'VIEW_REGISTRATION_OWN', FE sẽ hiển thị 'VIEW_REGISTRATION_OWN'
--   - Quyền cha sẽ tự động ẩn quyền con trong sidebar
--
-- ============================================
