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

-- MODULE: EMPLOYEE_SHIFTS (Quản lý ca làm việc của nhân viên - VÍ DỤ Parent-Child Permission)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Parent permission: Xem TẤT CẢ ca làm việc (bao gồm của mình và người khác)
('VIEW_SHIFTS_ALL', 'VIEW_SHIFTS_ALL', 'EMPLOYEE_SHIFTS',
 'Xem tất cả ca làm việc của nhân viên', '/app/shifts', 85, NULL, TRUE, NOW()),

-- Child permission: Chỉ xem ca làm việc CỦA MÌNH (sẽ bị ẩn khỏi sidebar nếu có parent)
('VIEW_SHIFTS_OWN', 'VIEW_SHIFTS_OWN', 'EMPLOYEE_SHIFTS',
 'Chỉ xem ca làm việc của chính mình', '/app/my-shifts', 86, 'VIEW_SHIFTS_ALL', TRUE, NOW()),

-- Permission: Xem báo cáo tổng hợp ca làm việc
('VIEW_SHIFTS_SUMMARY', 'VIEW_SHIFTS_SUMMARY', 'EMPLOYEE_SHIFTS',
 'Xem báo cáo tổng hợp ca làm việc', NULL, 87, NULL, TRUE, NOW()),

-- Action permissions (không có path, không hiển thị sidebar)
('CREATE_SHIFTS', 'CREATE_SHIFTS', 'EMPLOYEE_SHIFTS', 'Tạo ca làm việc cho nhân viên', NULL, 88, NULL, TRUE, NOW()),
('UPDATE_SHIFTS', 'UPDATE_SHIFTS', 'EMPLOYEE_SHIFTS', 'Cập nhật ca làm việc (trạng thái, ghi chú)', NULL, 89, NULL, TRUE, NOW()),
('DELETE_SHIFTS', 'DELETE_SHIFTS', 'EMPLOYEE_SHIFTS', 'Hủy ca làm việc', NULL, 90, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: REGISTRATION (Đăng ký ca - VÍ DỤ Parent-Child Permission)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Parent permission: Xem TẤT CẢ đăng ký ca
('VIEW_REGISTRATION_ALL', 'VIEW_REGISTRATION_ALL', 'REGISTRATION',
 'Xem tất cả đăng ký ca (bao gồm của mình và người khác)', '/app/registrations', 95, NULL, TRUE, NOW()),

-- Child permission: Chỉ xem đăng ký ca CỦA MÌNH (sẽ bị ẩn khỏi sidebar nếu có parent)
('VIEW_REGISTRATION_OWN', 'VIEW_REGISTRATION_OWN', 'REGISTRATION',
 'Chỉ xem đăng ký ca của chính mình', '/app/my-registrations', 96, 'VIEW_REGISTRATION_ALL', TRUE, NOW()),

-- Action permissions (không có path, không hiển thị sidebar)
('CREATE_REGISTRATION', 'CREATE_REGISTRATION', 'REGISTRATION', 'Tạo đăng ký ca mới', NULL, 97, NULL, TRUE, NOW()),
('UPDATE_REGISTRATION', 'UPDATE_REGISTRATION', 'REGISTRATION', 'Cập nhật đăng ký ca', NULL, 98, NULL, TRUE, NOW()),
('DELETE_REGISTRATION', 'DELETE_REGISTRATION', 'REGISTRATION', 'Xóa đăng ký ca', NULL, 99, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE: TIME_OFF (Nghỉ phép - VÍ DỤ Parent-Child Permission)
INSERT INTO permissions (permission_id, permission_name, module, description, path, display_order, parent_permission_id, is_active, created_at)
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
('VIEW_OT_ALL', 'VIEW_OT_ALL', 'OVERTIME', 'Xem tất cả yêu cầu tăng ca', NOW()),
('VIEW_OT_OWN', 'VIEW_OT_OWN', 'OVERTIME', 'Xem yêu cầu tăng ca của bản thân', NOW()),
('CREATE_OT', 'CREATE_OT', 'OVERTIME', 'Tạo yêu cầu tăng ca', NOW()),
('APPROVE_OT', 'APPROVE_OT', 'OVERTIME', 'Phê duyệt yêu cầu tăng ca', NOW()),
('REJECT_OT', 'REJECT_OT', 'OVERTIME', 'Từ chối yêu cầu tăng ca', NOW()),
('CANCEL_OT_OWN', 'CANCEL_OT_OWN', 'OVERTIME', 'Hủy yêu cầu tăng ca của bản thân', NOW()),
('CANCEL_OT_PENDING', 'CANCEL_OT_PENDING', 'OVERTIME', 'Hủy yêu cầu tăng ca đang chờ', NOW()),
('CREATE_TIME_OFF', 'CREATE_TIME_OFF', 'TIME_OFF', 'Tạo yêu cầu nghỉ phép', NULL, 102, NULL, TRUE, NOW()),
('APPROVE_TIME_OFF', 'APPROVE_TIME_OFF', 'TIME_OFF', 'Phê duyệt yêu cầu nghỉ phép', NULL, 103, NULL, TRUE, NOW()),
('VIEW_REGISTRATION_ALL', 'VIEW_REGISTRATION_ALL', 'SHIFT_REGISTRATION', 'Xem tất cả đăng ký ca làm việc', NOW()),
('VIEW_REGISTRATION_OWN', 'VIEW_REGISTRATION_OWN', 'SHIFT_REGISTRATION', 'Xem đăng ký ca làm việc của bản thân', NOW()),
('CREATE_REGISTRATION', 'CREATE_REGISTRATION', 'SHIFT_REGISTRATION', 'Tạo đăng ký ca làm việc', NOW()),
('UPDATE_REGISTRATION_ALL', 'UPDATE_REGISTRATION_ALL', 'SHIFT_REGISTRATION', 'Cập nhật tất cả đăng ký ca làm việc', NOW()),
('UPDATE_REGISTRATION_OWN', 'UPDATE_REGISTRATION_OWN', 'SHIFT_REGISTRATION', 'Cập nhật đăng ký ca làm việc của bản thân', NOW()),
('DELETE_REGISTRATION_ALL', 'DELETE_REGISTRATION_ALL', 'SHIFT_REGISTRATION', 'Xóa tất cả đăng ký ca làm việc', NOW()),
('DELETE_REGISTRATION_OWN', 'DELETE_REGISTRATION_OWN', 'SHIFT_REGISTRATION', 'Xóa đăng ký ca làm việc của bản thân', NOW())
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
('ROLE_DOCTOR', 'VIEW_SHIFTS_OWN'),        -- Chỉ xem ca làm việc của mình
('ROLE_DOCTOR', 'VIEW_REGISTRATION_OWN'),  -- Chỉ xem đăng ký ca của mình
('ROLE_DOCTOR', 'VIEW_TIME_OFF_OWN'),      -- Chỉ xem nghỉ phép của mình
('ROLE_DOCTOR', 'CREATE_TIME_OFF')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Y tá (Nurse)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_NURSE', 'VIEW_PATIENT'),
('ROLE_NURSE', 'VIEW_TREATMENT'),
('ROLE_NURSE', 'VIEW_APPOINTMENT'),
('ROLE_NURSE', 'VIEW_SHIFTS_OWN'),         -- Chỉ xem ca làm việc của mình
('ROLE_NURSE', 'VIEW_REGISTRATION_OWN'),
('ROLE_NURSE', 'VIEW_TIME_OFF_OWN'),
('ROLE_NURSE', 'CREATE_TIME_OFF')
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
('ROLE_RECEPTIONIST', 'VIEW_SHIFTS_OWN'),      -- Chỉ xem ca làm việc của mình
('ROLE_RECEPTIONIST', 'VIEW_REGISTRATION_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_TIME_OFF_OWN'),
('ROLE_RECEPTIONIST', 'CREATE_TIME_OFF')
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
('ROLE_MANAGER', 'VIEW_SHIFTS_ALL'),        -- Xem TẤT CẢ ca làm việc của nhân viên
('ROLE_MANAGER', 'VIEW_SHIFTS_SUMMARY'),    -- Xem báo cáo tổng hợp ca làm việc
('ROLE_MANAGER', 'CREATE_SHIFTS'),          -- Tạo ca làm việc cho nhân viên
('ROLE_MANAGER', 'UPDATE_SHIFTS'),          -- Cập nhật ca làm việc
('ROLE_MANAGER', 'DELETE_SHIFTS'),          -- Hủy ca làm việc
('ROLE_MANAGER', 'VIEW_REGISTRATION_ALL'),  -- Xem TẤT CẢ đăng ký ca
('ROLE_MANAGER', 'CREATE_REGISTRATION'),
('ROLE_MANAGER', 'UPDATE_REGISTRATION'),
('ROLE_MANAGER', 'DELETE_REGISTRATION'),
('ROLE_MANAGER', 'VIEW_TIME_OFF_ALL'),      -- Xem TẤT CẢ nghỉ phép
('ROLE_MANAGER', 'APPROVE_TIME_OFF')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Bệnh nhân (Patient)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_PATIENT', 'VIEW_PATIENT'),          -- Chỉ xem hồ sơ của mình
('ROLE_PATIENT', 'VIEW_TREATMENT'),        -- Xem phác đồ điều trị của mình
('ROLE_PATIENT', 'VIEW_APPOINTMENT'),      -- Xem lịch hẹn của mình
('ROLE_PATIENT', 'CREATE_APPOINTMENT'),    -- Đặt lịch hẹn cho mình
('ROLE_MANAGER', 'CREATE_EMPLOYEE'), ('ROLE_MANAGER', 'VIEW_EMPLOYEE'), ('ROLE_MANAGER', 'UPDATE_EMPLOYEE'),
('ROLE_MANAGER', 'DELETE_EMPLOYEE'), ('ROLE_MANAGER', 'CREATE_WORK_SHIFTS'), ('ROLE_MANAGER', 'VIEW_WORK_SHIFTS'),
('ROLE_MANAGER', 'UPDATE_WORK_SHIFTS'), ('ROLE_MANAGER', 'DELETE_WORK_SHIFTS'), ('ROLE_MANAGER', 'VIEW_PATIENT'),
('ROLE_MANAGER', 'VIEW_APPOINTMENT'), ('ROLE_MANAGER', 'VIEW_OT_ALL'), ('ROLE_MANAGER', 'APPROVE_OT'),
('ROLE_MANAGER', 'REJECT_OT'), ('ROLE_MANAGER', 'CANCEL_OT_PENDING')
ON CONFLICT (role_id, permission_id) DO NOTHING;
-- Assign overtime permissions to employees (Doctor, Nurse, Receptionist, etc.)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_OT_OWN'), ('ROLE_DOCTOR', 'CREATE_OT'), ('ROLE_DOCTOR', 'CANCEL_OT_OWN'),
('ROLE_NURSE', 'VIEW_OT_OWN'), ('ROLE_NURSE', 'CREATE_OT'), ('ROLE_NURSE', 'CANCEL_OT_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_OT_OWN'), ('ROLE_RECEPTIONIST', 'CREATE_OT'), ('ROLE_RECEPTIONIST', 'CANCEL_OT_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_OT_OWN'), ('ROLE_ACCOUNTANT', 'CREATE_OT'), ('ROLE_ACCOUNTANT', 'CANCEL_OT_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_OT_OWN'), ('ROLE_INVENTORY_MANAGER', 'CREATE_OT'), ('ROLE_INVENTORY_MANAGER', 'CANCEL_OT_OWN')
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

ON CONFLICT (employee_id) DO NOTHING;

-- ============================================
-- BƯỚC 8: GÁN CHUYÊN KHOA CHO NHÂN VIÊN
-- ============================================
(2, 1), (2, 7), -- Bác sĩ 1 (ID 2)
(3, 2), (3, 4), -- Bác sĩ 2 (ID 3)
(6, 6)          -- Y tá (ID 6)
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
-- BƯỚC 11: ĐỒNG BỘ CÁC SEQUENCE (AUTO INCREMENT)
-- ============================================

SELECT setval(pg_get_serial_sequence('base_roles', 'base_role_id'), COALESCE((SELECT MAX(base_role_id) FROM base_roles), 0)+1, false);
SELECT setval(pg_get_serial_sequence('accounts', 'account_id'), COALESCE((SELECT MAX(account_id) FROM accounts), 0)+1, false);
SELECT setval(pg_get_serial_sequence('employees', 'employee_id'), COALESCE((SELECT MAX(employee_id) FROM employees), 0)+1, false);
SELECT setval(pg_get_serial_sequence('patients', 'patient_id'), COALESCE((SELECT MAX(patient_id) FROM patients), 0)+1, false);
SELECT setval(pg_get_serial_sequence('specializations', 'specialization_id'), COALESCE((SELECT MAX(specialization_id) FROM specializations), 0)+1, false);

-- ============================================
-- KẾT THÚC SEED DATA
-- ============================================
--
-- HƯỚNG DẪN SỬ DỤNG:
--
-- 1. LOGIN VÀO HỆ THỐNG:
--    - Admin: username="admin", password="123456"
--    - Manager: username="manager", password="123456"
--    - Bác sĩ: username="nhasi1" hoặc "nhasi2", password="123456"
--    - Lễ tân: username="letan", password="123456"
--    - Bệnh nhân: username="benhnhan1", password="123456"
--
-- 2. SAU KHI LOGIN, HỆ THỐNG SẼ:
--    - Trả về base_role (admin/employee/patient) để FE chọn layout
--    - Trả về home_path để redirect (VD: "/app/schedule" cho bác sĩ)
-- 2. SAU KHI LOGIN, HỆ THỐNG SẼ:
--    - Trả về baseRole (string: "admin"/"employee"/"patient") để FE chọn layout
--    - Trả về homePath (effective path: override hoặc default) để redirect
--    - Trả về sidebar đã được filter theo quyền
--    - Trả về danh sách permissions để check quyền nút bấm
--
-- 3. BASE ROLE & HOME PATH LOGIC:
--    - Mỗi role thuộc 1 trong 3 base_roles (admin=1, employee=2, patient=3)
--    - Base role xác định: Layout FE + Default home path
--    - Role có thể override home_path (home_path_override column)
--    - Effective home path = home_path_override ?? base_role.default_home_path
--
--    VÍ DỤ:
--      ROLE_DOCTOR: base_role_id=2 (employee), home_path_override='/app/schedule'
--        → baseRole='employee', homePath='/app/schedule' (override)
--
--      ROLE_NURSE: base_role_id=2 (employee), home_path_override=NULL
--        → baseRole='employee', homePath='/app/dashboard' (default từ base_roles)
--
-- 4. VÍ DỤ PARENT-CHILD PERMISSION:on tự động bị ẩn khỏi sidebar
--
-- ============================================
