-- ============================================
-- HỆ THỐNG QUẢN LÝ PHÒNG KHÁM NHA KHOA
-- Dental Clinic Management System - Seed Data V2
-- ============================================
-- UPDATED: Path fields removed, Modules merged, Email verification ready
-- Seeded accounts: ACTIVE status (skip verification for demo data)
-- New accounts: PENDING_VERIFICATION (require email verification)
-- ============================================

-- ============================================
-- BƯỚC 1: TẠO BASE ROLES (3 loại cố định)
-- ============================================
-- Base roles xác định LAYOUT FE (AdminLayout/EmployeeLayout/PatientLayout)
-- FE tự xử lý routing, không cần path từ BE
-- ============================================

INSERT INTO base_roles (base_role_id, base_role_name, description, is_active, created_at)
VALUES
(1, 'admin', 'Admin Portal - Quản trị viên hệ thống', TRUE, NOW()),
(2, 'employee', 'Employee Portal - Nhân viên phòng khám', TRUE, NOW()),
(3, 'patient', 'Patient Portal - Bệnh nhân', TRUE, NOW())
ON CONFLICT (base_role_id) DO NOTHING;

-- ============================================
-- BƯỚC 2: TẠO CÁC VAI TRÒ (ROLES)
-- ============================================
-- Mỗi role có base_role_id xác định layout FE
-- FE tự xử lý routing dựa trên baseRole và permissions
-- ============================================

INSERT INTO roles (role_id, role_name, base_role_id, description, requires_specialization, is_active, created_at)
VALUES
-- Admin Portal (base_role_id = 1)
('ROLE_ADMIN', 'ROLE_ADMIN', 1, 'Quản trị viên hệ thống - Toàn quyền quản lý', FALSE, TRUE, NOW()),

-- Employee Portal (base_role_id = 2)
('ROLE_DOCTOR', 'ROLE_DOCTOR', 2, 'Bác sĩ nha khoa - Khám và điều trị bệnh nhân', TRUE, TRUE, NOW()),
('ROLE_NURSE', 'ROLE_NURSE', 2, 'Y tá - Hỗ trợ điều trị và chăm sóc bệnh nhân', TRUE, TRUE, NOW()),
('ROLE_RECEPTIONIST', 'ROLE_RECEPTIONIST', 2, 'Lễ tân - Tiếp đón và quản lý lịch hẹn', FALSE, TRUE, NOW()),
('ROLE_ACCOUNTANT', 'ROLE_ACCOUNTANT', 2, 'Kế toán - Quản lý tài chính và thanh toán', FALSE, TRUE, NOW()),
('ROLE_INVENTORY_MANAGER', 'ROLE_INVENTORY_MANAGER', 2, 'Quản lý kho - Quản lý vật tư và thuốc men', FALSE, TRUE, NOW()),
('ROLE_MANAGER', 'ROLE_MANAGER', 2, 'Quản lý - Quản lý vận hành và nhân sự', FALSE, TRUE, NOW()),

-- Patient Portal (base_role_id = 3)
('ROLE_PATIENT', 'ROLE_PATIENT', 3, 'Bệnh nhân - Xem hồ sơ và đặt lịch khám', FALSE, TRUE, NOW())
ON CONFLICT (role_id) DO NOTHING;

-- ============================================
-- BƯỚC 3: TẠO CÁC QUYỀN (PERMISSIONS) - MERGED MODULES
-- ============================================
-- 10 modules sau khi merge (giảm từ 12 modules):
-- 1. ACCOUNT (4 perms)
-- 2. EMPLOYEE (6 perms)
-- 3. PATIENT (4 perms)
-- 4. TREATMENT (3 perms)
-- 5. APPOINTMENT (5 perms)
-- 6. CUSTOMER_MANAGEMENT (8 perms) = CONTACT + CONTACT_HISTORY
-- 7. SCHEDULE_MANAGEMENT (27 perms) = WORK_SHIFTS + REGISTRATION + SHIFT_RENEWAL
-- 8. LEAVE_MANAGEMENT (29 perms) = TIME_OFF + OVERTIME + TIME_OFF_MANAGEMENT
-- 9. SYSTEM_CONFIGURATION (12 perms) = ROLE + PERMISSION + SPECIALIZATION
-- 10. HOLIDAY (4 perms) = Holiday Management (NEW)
--
-- PATH FIELD REMOVED - FE handles routing independently
-- ============================================

-- MODULE 1: ACCOUNT
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_ACCOUNT', 'VIEW_ACCOUNT', 'ACCOUNT', 'Xem danh sách tài khoản', 10, NULL, TRUE, NOW()),
('CREATE_ACCOUNT', 'CREATE_ACCOUNT', 'ACCOUNT', 'Tạo tài khoản mới', 11, NULL, TRUE, NOW()),
('UPDATE_ACCOUNT', 'UPDATE_ACCOUNT', 'ACCOUNT', 'Cập nhật tài khoản', 12, NULL, TRUE, NOW()),
('DELETE_ACCOUNT', 'DELETE_ACCOUNT', 'ACCOUNT', 'Xóa tài khoản', 13, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 2: EMPLOYEE
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_EMPLOYEE', 'VIEW_EMPLOYEE', 'EMPLOYEE', 'Xem danh sách nhân viên', 20, NULL, TRUE, NOW()),
('READ_ALL_EMPLOYEES', 'READ_ALL_EMPLOYEES', 'EMPLOYEE', 'Đọc tất cả thông tin nhân viên', 21, NULL, TRUE, NOW()),
('READ_EMPLOYEE_BY_CODE', 'READ_EMPLOYEE_BY_CODE', 'EMPLOYEE', 'Đọc thông tin nhân viên theo mã', 22, NULL, TRUE, NOW()),
('CREATE_EMPLOYEE', 'CREATE_EMPLOYEE', 'EMPLOYEE', 'Tạo nhân viên mới', 23, NULL, TRUE, NOW()),
('UPDATE_EMPLOYEE', 'UPDATE_EMPLOYEE', 'EMPLOYEE', 'Cập nhật thông tin nhân viên', 24, NULL, TRUE, NOW()),
('DELETE_EMPLOYEE', 'DELETE_EMPLOYEE', 'EMPLOYEE', 'Xóa nhân viên', 25, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 3: PATIENT
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_PATIENT', 'VIEW_PATIENT', 'PATIENT', 'Xem danh sách bệnh nhân', 30, NULL, TRUE, NOW()),
('CREATE_PATIENT', 'CREATE_PATIENT', 'PATIENT', 'Tạo hồ sơ bệnh nhân mới', 31, NULL, TRUE, NOW()),
('UPDATE_PATIENT', 'UPDATE_PATIENT', 'PATIENT', 'Cập nhật hồ sơ bệnh nhân', 32, NULL, TRUE, NOW()),
('DELETE_PATIENT', 'DELETE_PATIENT', 'PATIENT', 'Xóa hồ sơ bệnh nhân', 33, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 4: TREATMENT
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_TREATMENT', 'VIEW_TREATMENT', 'TREATMENT', 'Xem danh sách điều trị', 40, NULL, TRUE, NOW()),
('CREATE_TREATMENT', 'CREATE_TREATMENT', 'TREATMENT', 'Tạo phác đồ điều trị mới', 41, NULL, TRUE, NOW()),
('UPDATE_TREATMENT', 'UPDATE_TREATMENT', 'TREATMENT', 'Cập nhật phác đồ điều trị', 42, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 5: APPOINTMENT
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_APPOINTMENT', 'VIEW_APPOINTMENT', 'APPOINTMENT', 'Xem danh sách lịch hẹn', 50, NULL, TRUE, NOW()),
('CREATE_APPOINTMENT', 'CREATE_APPOINTMENT', 'APPOINTMENT', 'Đặt lịch hẹn mới', 51, NULL, TRUE, NOW()),
('UPDATE_APPOINTMENT', 'UPDATE_APPOINTMENT', 'APPOINTMENT', 'Cập nhật lịch hẹn', 52, NULL, TRUE, NOW()),
('CANCEL_APPOINTMENT', 'CANCEL_APPOINTMENT', 'APPOINTMENT', 'Hủy lịch hẹn', 53, NULL, TRUE, NOW()),
('DELETE_APPOINTMENT', 'DELETE_APPOINTMENT', 'APPOINTMENT', 'Xóa lịch hẹn', 54, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 6: CUSTOMER_MANAGEMENT (MERGED: CONTACT + CONTACT_HISTORY)
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Contact management
('VIEW_CONTACT', 'VIEW_CONTACT', 'CUSTOMER_MANAGEMENT', 'Xem danh sách liên hệ khách hàng', 60, NULL, TRUE, NOW()),
('CREATE_CONTACT', 'CREATE_CONTACT', 'CUSTOMER_MANAGEMENT', 'Tạo liên hệ khách hàng mới', 61, NULL, TRUE, NOW()),
('UPDATE_CONTACT', 'UPDATE_CONTACT', 'CUSTOMER_MANAGEMENT', 'Cập nhật liên hệ khách hàng', 62, NULL, TRUE, NOW()),
('DELETE_CONTACT', 'DELETE_CONTACT', 'CUSTOMER_MANAGEMENT', 'Xóa liên hệ khách hàng', 63, NULL, TRUE, NOW()),
-- Contact history
('VIEW_CONTACT_HISTORY', 'VIEW_CONTACT_HISTORY', 'CUSTOMER_MANAGEMENT', 'Xem lịch sử liên hệ', 64, NULL, TRUE, NOW()),
('CREATE_CONTACT_HISTORY', 'CREATE_CONTACT_HISTORY', 'CUSTOMER_MANAGEMENT', 'Tạo lịch sử liên hệ', 65, NULL, TRUE, NOW()),
('UPDATE_CONTACT_HISTORY', 'UPDATE_CONTACT_HISTORY', 'CUSTOMER_MANAGEMENT', 'Cập nhật lịch sử liên hệ', 66, NULL, TRUE, NOW()),
('DELETE_CONTACT_HISTORY', 'DELETE_CONTACT_HISTORY', 'CUSTOMER_MANAGEMENT', 'Xóa lịch sử liên hệ', 67, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 7: SCHEDULE_MANAGEMENT (MERGED: WORK_SHIFTS + REGISTRATION + SHIFT_RENEWAL)
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Work shifts
('VIEW_WORK_SHIFTS', 'VIEW_WORK_SHIFTS', 'SCHEDULE_MANAGEMENT', 'Xem danh sách mẫu ca làm việc', 80, NULL, TRUE, NOW()),
('CREATE_WORK_SHIFTS', 'CREATE_WORK_SHIFTS', 'SCHEDULE_MANAGEMENT', 'Tạo mẫu ca làm việc mới', 81, NULL, TRUE, NOW()),
('UPDATE_WORK_SHIFTS', 'UPDATE_WORK_SHIFTS', 'SCHEDULE_MANAGEMENT', 'Cập nhật mẫu ca làm việc', 82, NULL, TRUE, NOW()),
('DELETE_WORK_SHIFTS', 'DELETE_WORK_SHIFTS', 'SCHEDULE_MANAGEMENT', 'Xóa mẫu ca làm việc', 83, NULL, TRUE, NOW()),
-- Part-time slot management (V2 - BE-307)
('MANAGE_WORK_SLOTS', 'MANAGE_WORK_SLOTS', 'SCHEDULE_MANAGEMENT', 'Quản lý suất part-time (tạo/sửa/xóa)', 84, NULL, TRUE, NOW()),
('VIEW_AVAILABLE_SLOTS', 'VIEW_AVAILABLE_SLOTS', 'SCHEDULE_MANAGEMENT', 'Xem suất part-time khả dụng', 85, NULL, TRUE, NOW()),
-- Shift registration (parent-child pattern)
('VIEW_REGISTRATION_ALL', 'VIEW_REGISTRATION_ALL', 'SCHEDULE_MANAGEMENT', 'Xem tất cả đăng ký ca làm việc', 90, NULL, TRUE, NOW()),
('VIEW_REGISTRATION_OWN', 'VIEW_REGISTRATION_OWN', 'SCHEDULE_MANAGEMENT', 'Xem đăng ký ca làm việc của bản thân', 91, 'VIEW_REGISTRATION_ALL', TRUE, NOW()),
('CREATE_REGISTRATION', 'CREATE_REGISTRATION', 'SCHEDULE_MANAGEMENT', 'Tạo đăng ký ca làm việc', 92, NULL, TRUE, NOW()),
('UPDATE_REGISTRATION', 'UPDATE_REGISTRATION', 'SCHEDULE_MANAGEMENT', 'Cập nhật đăng ký ca', 93, NULL, TRUE, NOW()),
('UPDATE_REGISTRATIONS_ALL', 'UPDATE_REGISTRATIONS_ALL', 'SCHEDULE_MANAGEMENT', 'Cập nhật tất cả đăng ký ca', 93, NULL, TRUE, NOW()),
('UPDATE_REGISTRATION_OWN', 'UPDATE_REGISTRATION_OWN', 'SCHEDULE_MANAGEMENT', 'Cập nhật đăng ký ca của bản thân', 94, 'UPDATE_REGISTRATIONS_ALL', TRUE, NOW()),
('CANCEL_REGISTRATION_OWN', 'CANCEL_REGISTRATION_OWN', 'SCHEDULE_MANAGEMENT', 'Hủy đăng ký ca của bản thân', 95, NULL, TRUE, NOW()),
('DELETE_REGISTRATION', 'DELETE_REGISTRATION', 'SCHEDULE_MANAGEMENT', 'Xóa đăng ký ca', 96, NULL, TRUE, NOW()),
('DELETE_REGISTRATION_ALL', 'DELETE_REGISTRATION_ALL', 'SCHEDULE_MANAGEMENT', 'Xóa tất cả đăng ký ca', 97, NULL, TRUE, NOW()),
('DELETE_REGISTRATION_OWN', 'DELETE_REGISTRATION_OWN', 'SCHEDULE_MANAGEMENT', 'Xóa đăng ký ca của bản thân', 98, 'DELETE_REGISTRATION_ALL', TRUE, NOW()),
-- Shift renewal
('VIEW_RENEWAL_OWN', 'VIEW_RENEWAL_OWN', 'SCHEDULE_MANAGEMENT', 'Xem yêu cầu gia hạn ca của bản thân', 99, NULL, TRUE, NOW()),
('RESPOND_RENEWAL_OWN', 'RESPOND_RENEWAL_OWN', 'SCHEDULE_MANAGEMENT', 'Phản hồi yêu cầu gia hạn ca của bản thân', 100, NULL, TRUE, NOW()),
-- Employee shift management (BE-302)
('VIEW_SHIFTS_ALL', 'VIEW_SHIFTS_ALL', 'SCHEDULE_MANAGEMENT', 'Xem tất cả ca làm việc nhân viên', 101, NULL, TRUE, NOW()),
('VIEW_SHIFTS_OWN', 'VIEW_SHIFTS_OWN', 'SCHEDULE_MANAGEMENT', 'Xem ca làm việc của bản thân', 102, 'VIEW_SHIFTS_ALL', TRUE, NOW()),
('VIEW_SHIFTS_SUMMARY', 'VIEW_SHIFTS_SUMMARY', 'SCHEDULE_MANAGEMENT', 'Xem thống kê ca làm việc', 103, NULL, TRUE, NOW()),
('CREATE_SHIFTS', 'CREATE_SHIFTS', 'SCHEDULE_MANAGEMENT', 'Tạo ca làm việc thủ công', 104, NULL, TRUE, NOW()),
('UPDATE_SHIFTS', 'UPDATE_SHIFTS', 'SCHEDULE_MANAGEMENT', 'Cập nhật ca làm việc', 105, NULL, TRUE, NOW()),
('DELETE_SHIFTS', 'DELETE_SHIFTS', 'SCHEDULE_MANAGEMENT', 'Hủy ca làm việc', 106, NULL, TRUE, NOW()),
-- Fixed shift registration management (BE-307 V2)
('MANAGE_FIXED_REGISTRATIONS', 'MANAGE_FIXED_REGISTRATIONS', 'SCHEDULE_MANAGEMENT', 'Quản lý đăng ký ca cố định (tạo/sửa/xóa)', 107, NULL, TRUE, NOW()),
('VIEW_FIXED_REGISTRATIONS_ALL', 'VIEW_FIXED_REGISTRATIONS_ALL', 'SCHEDULE_MANAGEMENT', 'Xem tất cả đăng ký ca cố định', 108, NULL, TRUE, NOW()),
('VIEW_FIXED_REGISTRATIONS_OWN', 'VIEW_FIXED_REGISTRATIONS_OWN', 'SCHEDULE_MANAGEMENT', 'Xem đăng ký ca cố định của bản thân', 109, 'VIEW_FIXED_REGISTRATIONS_ALL', TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 8: LEAVE_MANAGEMENT (MERGED: TIME_OFF + OVERTIME + TIME_OFF_MANAGEMENT)
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
-- View permissions (parent-child)
('VIEW_LEAVE_ALL', 'VIEW_LEAVE_ALL', 'LEAVE_MANAGEMENT', 'Xem tất cả yêu cầu nghỉ phép & tăng ca', 110, NULL, TRUE, NOW()),
('VIEW_LEAVE_OWN', 'VIEW_LEAVE_OWN', 'LEAVE_MANAGEMENT', 'Xem yêu cầu nghỉ phép & tăng ca của bản thân', 111, 'VIEW_LEAVE_ALL', TRUE, NOW()),
-- Time-off view aliases (for AuthoritiesConstants compatibility)
('VIEW_TIMEOFF_ALL', 'VIEW_TIMEOFF_ALL', 'LEAVE_MANAGEMENT', 'Xem tất cả yêu cầu nghỉ phép (alias)', 112, NULL, TRUE, NOW()),
('VIEW_TIMEOFF_OWN', 'VIEW_TIMEOFF_OWN', 'LEAVE_MANAGEMENT', 'Xem yêu cầu nghỉ phép của bản thân (alias)', 113, 'VIEW_TIMEOFF_ALL', TRUE, NOW()),
-- Overtime view permissions (aliases for compatibility with AuthoritiesConstants)
('VIEW_OT_ALL', 'VIEW_OT_ALL', 'LEAVE_MANAGEMENT', 'Xem tất cả yêu cầu tăng ca', 114, NULL, TRUE, NOW()),
('VIEW_OT_OWN', 'VIEW_OT_OWN', 'LEAVE_MANAGEMENT', 'Xem yêu cầu tăng ca của bản thân', 115, 'VIEW_OT_ALL', TRUE, NOW()),
('CREATE_OT', 'CREATE_OT', 'LEAVE_MANAGEMENT', 'Tạo yêu cầu tăng ca (alias)', 116, NULL, TRUE, NOW()),
('APPROVE_OT', 'APPROVE_OT', 'LEAVE_MANAGEMENT', 'Phê duyệt yêu cầu tăng ca (alias)', 117, NULL, TRUE, NOW()),
('REJECT_OT', 'REJECT_OT', 'LEAVE_MANAGEMENT', 'Từ chối yêu cầu tăng ca (alias)', 118, NULL, TRUE, NOW()),
('CANCEL_OT_OWN', 'CANCEL_OT_OWN', 'LEAVE_MANAGEMENT', 'Hủy yêu cầu tăng ca của bản thân (alias)', 119, NULL, TRUE, NOW()),
('CANCEL_OT_PENDING', 'CANCEL_OT_PENDING', 'LEAVE_MANAGEMENT', 'Hủy yêu cầu tăng ca đang chờ (alias)', 120, NULL, TRUE, NOW()),
-- Time off actions
('CREATE_TIME_OFF', 'CREATE_TIME_OFF', 'LEAVE_MANAGEMENT', 'Tạo yêu cầu nghỉ phép', 125, NULL, TRUE, NOW()),
('CREATE_TIMEOFF', 'CREATE_TIMEOFF', 'LEAVE_MANAGEMENT', 'Tạo yêu cầu nghỉ phép (alias)', 126, NULL, TRUE, NOW()),
('APPROVE_TIME_OFF', 'APPROVE_TIME_OFF', 'LEAVE_MANAGEMENT', 'Phê duyệt yêu cầu nghỉ phép', 127, NULL, TRUE, NOW()),
('APPROVE_TIMEOFF', 'APPROVE_TIMEOFF', 'LEAVE_MANAGEMENT', 'Phê duyệt yêu cầu nghỉ phép (alias)', 128, NULL, TRUE, NOW()),
('REJECT_TIME_OFF', 'REJECT_TIME_OFF', 'LEAVE_MANAGEMENT', 'Từ chối yêu cầu nghỉ phép', 129, NULL, TRUE, NOW()),
('REJECT_TIMEOFF', 'REJECT_TIMEOFF', 'LEAVE_MANAGEMENT', 'Từ chối yêu cầu nghỉ phép (alias)', 130, NULL, TRUE, NOW()),
('CANCEL_TIME_OFF_OWN', 'CANCEL_TIME_OFF_OWN', 'LEAVE_MANAGEMENT', 'Hủy yêu cầu nghỉ phép của bản thân', 131, NULL, TRUE, NOW()),
('CANCEL_TIMEOFF_OWN', 'CANCEL_TIMEOFF_OWN', 'LEAVE_MANAGEMENT', 'Hủy yêu cầu nghỉ phép của bản thân (alias)', 132, NULL, TRUE, NOW()),
('CANCEL_TIME_OFF_PENDING', 'CANCEL_TIME_OFF_PENDING', 'LEAVE_MANAGEMENT', 'Hủy yêu cầu nghỉ phép đang chờ', 133, NULL, TRUE, NOW()),
('CANCEL_TIMEOFF_PENDING', 'CANCEL_TIMEOFF_PENDING', 'LEAVE_MANAGEMENT', 'Hủy yêu cầu nghỉ phép đang chờ (alias)', 134, NULL, TRUE, NOW()),
-- Overtime actions
('CREATE_OVERTIME', 'CREATE_OVERTIME', 'LEAVE_MANAGEMENT', 'Tạo yêu cầu tăng ca', 140, NULL, TRUE, NOW()),
('APPROVE_OVERTIME', 'APPROVE_OVERTIME', 'LEAVE_MANAGEMENT', 'Phê duyệt yêu cầu tăng ca', 141, NULL, TRUE, NOW()),
('REJECT_OVERTIME', 'REJECT_OVERTIME', 'LEAVE_MANAGEMENT', 'Từ chối yêu cầu tăng ca', 132, NULL, TRUE, NOW()),
('CANCEL_OVERTIME_OWN', 'CANCEL_OVERTIME_OWN', 'LEAVE_MANAGEMENT', 'Hủy yêu cầu tăng ca của bản thân', 133, NULL, TRUE, NOW()),
('CANCEL_OVERTIME_PENDING', 'CANCEL_OVERTIME_PENDING', 'LEAVE_MANAGEMENT', 'Hủy yêu cầu tăng ca đang chờ', 134, NULL, TRUE, NOW()),
-- Time off type management
('VIEW_TIMEOFF_TYPE', 'VIEW_TIMEOFF_TYPE', 'LEAVE_MANAGEMENT', 'Xem danh sách loại nghỉ phép', 140, NULL, TRUE, NOW()),
('VIEW_TIMEOFF_TYPE_ALL', 'VIEW_TIMEOFF_TYPE_ALL', 'LEAVE_MANAGEMENT', 'Xem/Quản lý tất cả loại nghỉ phép (alias)', 141, NULL, TRUE, NOW()),
('CREATE_TIMEOFF_TYPE', 'CREATE_TIMEOFF_TYPE', 'LEAVE_MANAGEMENT', 'Tạo loại nghỉ phép mới', 142, NULL, TRUE, NOW()),
('UPDATE_TIMEOFF_TYPE', 'UPDATE_TIMEOFF_TYPE', 'LEAVE_MANAGEMENT', 'Cập nhật loại nghỉ phép', 143, NULL, TRUE, NOW()),
('DELETE_TIMEOFF_TYPE', 'DELETE_TIMEOFF_TYPE', 'LEAVE_MANAGEMENT', 'Xóa loại nghỉ phép', 144, NULL, TRUE, NOW()),
-- Leave balance management
('VIEW_LEAVE_BALANCE_ALL', 'VIEW_LEAVE_BALANCE_ALL', 'LEAVE_MANAGEMENT', 'Xem số dư nghỉ phép của nhân viên', 150, NULL, TRUE, NOW()),
('ADJUST_LEAVE_BALANCE', 'ADJUST_LEAVE_BALANCE', 'LEAVE_MANAGEMENT', 'Điều chỉnh số dư nghỉ phép', 151, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 9: SYSTEM_CONFIGURATION (MERGED: ROLE + PERMISSION + SPECIALIZATION)
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Role management
('VIEW_ROLE', 'VIEW_ROLE', 'SYSTEM_CONFIGURATION', 'Xem danh sách vai trò', 200, NULL, TRUE, NOW()),
('CREATE_ROLE', 'CREATE_ROLE', 'SYSTEM_CONFIGURATION', 'Tạo vai trò mới', 201, NULL, TRUE, NOW()),
('UPDATE_ROLE', 'UPDATE_ROLE', 'SYSTEM_CONFIGURATION', 'Cập nhật vai trò', 202, NULL, TRUE, NOW()),
('DELETE_ROLE', 'DELETE_ROLE', 'SYSTEM_CONFIGURATION', 'Xóa vai trò', 203, NULL, TRUE, NOW()),
-- Permission management
('VIEW_PERMISSION', 'VIEW_PERMISSION', 'SYSTEM_CONFIGURATION', 'Xem danh sách quyền', 210, NULL, TRUE, NOW()),
('CREATE_PERMISSION', 'CREATE_PERMISSION', 'SYSTEM_CONFIGURATION', 'Tạo quyền mới', 211, NULL, TRUE, NOW()),
('UPDATE_PERMISSION', 'UPDATE_PERMISSION', 'SYSTEM_CONFIGURATION', 'Cập nhật quyền', 212, NULL, TRUE, NOW()),
('DELETE_PERMISSION', 'DELETE_PERMISSION', 'SYSTEM_CONFIGURATION', 'Xóa quyền', 213, NULL, TRUE, NOW()),
-- Specialization management
('VIEW_SPECIALIZATION', 'VIEW_SPECIALIZATION', 'SYSTEM_CONFIGURATION', 'Xem danh sách chuyên khoa', 220, NULL, TRUE, NOW()),
('CREATE_SPECIALIZATION', 'CREATE_SPECIALIZATION', 'SYSTEM_CONFIGURATION', 'Tạo chuyên khoa mới', 221, NULL, TRUE, NOW()),
('UPDATE_SPECIALIZATION', 'UPDATE_SPECIALIZATION', 'SYSTEM_CONFIGURATION', 'Cập nhật chuyên khoa', 222, NULL, TRUE, NOW()),
('DELETE_SPECIALIZATION', 'DELETE_SPECIALIZATION', 'SYSTEM_CONFIGURATION', 'Xóa chuyên khoa', 223, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 10: HOLIDAY (Holiday Management)
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_HOLIDAY', 'VIEW_HOLIDAY', 'HOLIDAY', 'Xem danh sách ngày nghỉ lễ', 230, NULL, TRUE, NOW()),
('CREATE_HOLIDAY', 'CREATE_HOLIDAY', 'HOLIDAY', 'Tạo ngày nghỉ lễ mới', 231, NULL, TRUE, NOW()),
('UPDATE_HOLIDAY', 'UPDATE_HOLIDAY', 'HOLIDAY', 'Cập nhật ngày nghỉ lễ', 232, NULL, TRUE, NOW()),
('DELETE_HOLIDAY', 'DELETE_HOLIDAY', 'HOLIDAY', 'Xóa ngày nghỉ lễ', 233, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 11: ROOM_MANAGEMENT (Quản lý phòng khám/ghế nha khoa)
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Room management
('VIEW_ROOM', 'VIEW_ROOM', 'ROOM_MANAGEMENT', 'Xem danh sách và chi tiết phòng', 240, NULL, TRUE, NOW()),
('CREATE_ROOM', 'CREATE_ROOM', 'ROOM_MANAGEMENT', 'Tạo phòng/ghế mới', 241, NULL, TRUE, NOW()),
('UPDATE_ROOM', 'UPDATE_ROOM', 'ROOM_MANAGEMENT', 'Cập nhật thông tin phòng', 242, NULL, TRUE, NOW()),
('DELETE_ROOM', 'DELETE_ROOM', 'ROOM_MANAGEMENT', 'Vô hiệu hóa phòng (soft delete)', 243, NULL, TRUE, NOW()),
-- V16: Room-Service compatibility management
('UPDATE_ROOM_SERVICES', 'UPDATE_ROOM_SERVICES', 'ROOM_MANAGEMENT', 'Gán/cập nhật dịch vụ cho phòng', 244, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- MODULE 12: SERVICE_MANAGEMENT (Quản lý danh mục dịch vụ nha khoa)
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Service management
('VIEW_SERVICE', 'VIEW_SERVICE', 'SERVICE_MANAGEMENT', 'Xem danh sách và chi tiết dịch vụ', 250, NULL, TRUE, NOW()),
('CREATE_SERVICE', 'CREATE_SERVICE', 'SERVICE_MANAGEMENT', 'Tạo dịch vụ mới', 251, NULL, TRUE, NOW()),
('UPDATE_SERVICE', 'UPDATE_SERVICE', 'SERVICE_MANAGEMENT', 'Cập nhật thông tin dịch vụ', 252, NULL, TRUE, NOW()),
('DELETE_SERVICE', 'DELETE_SERVICE', 'SERVICE_MANAGEMENT', 'Vô hiệu hóa dịch vụ (soft delete)', 253, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

-- ============================================
-- BƯỚC 4: PHÂN QUYỀN CHO CÁC VAI TRÒ
-- ============================================

-- Admin có TẤT CẢ quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_ADMIN', permission_id FROM permissions WHERE is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Doctor
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_PATIENT'), ('ROLE_DOCTOR', 'UPDATE_PATIENT'),
('ROLE_DOCTOR', 'VIEW_TREATMENT'), ('ROLE_DOCTOR', 'CREATE_TREATMENT'), ('ROLE_DOCTOR', 'UPDATE_TREATMENT'),
('ROLE_DOCTOR', 'VIEW_APPOINTMENT'),
('ROLE_DOCTOR', 'VIEW_REGISTRATION_OWN'), ('ROLE_DOCTOR', 'VIEW_RENEWAL_OWN'), ('ROLE_DOCTOR', 'RESPOND_RENEWAL_OWN'),
('ROLE_DOCTOR', 'CREATE_REGISTRATION'),
('ROLE_DOCTOR', 'VIEW_LEAVE_OWN'), ('ROLE_DOCTOR', 'CREATE_TIME_OFF'), ('ROLE_DOCTOR', 'CREATE_OVERTIME'),
('ROLE_DOCTOR', 'CANCEL_TIME_OFF_OWN'), ('ROLE_DOCTOR', 'CANCEL_OVERTIME_OWN'),
('ROLE_DOCTOR', 'VIEW_HOLIDAY')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Nurse
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_NURSE', 'VIEW_PATIENT'), ('ROLE_NURSE', 'VIEW_TREATMENT'), ('ROLE_NURSE', 'VIEW_APPOINTMENT'),
('ROLE_NURSE', 'VIEW_REGISTRATION_OWN'), ('ROLE_NURSE', 'VIEW_RENEWAL_OWN'), ('ROLE_NURSE', 'RESPOND_RENEWAL_OWN'),
('ROLE_NURSE', 'CREATE_REGISTRATION'),
('ROLE_NURSE', 'VIEW_LEAVE_OWN'), ('ROLE_NURSE', 'CREATE_TIME_OFF'), ('ROLE_NURSE', 'CREATE_OVERTIME'),
('ROLE_NURSE', 'CANCEL_TIME_OFF_OWN'), ('ROLE_NURSE', 'CANCEL_OVERTIME_OWN'),
('ROLE_NURSE', 'VIEW_HOLIDAY')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Receptionist
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_RECEPTIONIST', 'VIEW_PATIENT'), ('ROLE_RECEPTIONIST', 'CREATE_PATIENT'), ('ROLE_RECEPTIONIST', 'UPDATE_PATIENT'),
('ROLE_RECEPTIONIST', 'VIEW_APPOINTMENT'), ('ROLE_RECEPTIONIST', 'CREATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'UPDATE_APPOINTMENT'), ('ROLE_RECEPTIONIST', 'DELETE_APPOINTMENT'),
-- CUSTOMER_MANAGEMENT
('ROLE_RECEPTIONIST', 'VIEW_CONTACT'), ('ROLE_RECEPTIONIST', 'CREATE_CONTACT'),
('ROLE_RECEPTIONIST', 'UPDATE_CONTACT'), ('ROLE_RECEPTIONIST', 'DELETE_CONTACT'),
('ROLE_RECEPTIONIST', 'VIEW_CONTACT_HISTORY'), ('ROLE_RECEPTIONIST', 'CREATE_CONTACT_HISTORY'),
('ROLE_RECEPTIONIST', 'UPDATE_CONTACT_HISTORY'), ('ROLE_RECEPTIONIST', 'DELETE_CONTACT_HISTORY'),
-- SCHEDULE & LEAVE
('ROLE_RECEPTIONIST', 'VIEW_REGISTRATION_OWN'),
('ROLE_RECEPTIONIST', 'CREATE_REGISTRATION'),
('ROLE_RECEPTIONIST', 'VIEW_LEAVE_OWN'), ('ROLE_RECEPTIONIST', 'CREATE_TIME_OFF'), ('ROLE_RECEPTIONIST', 'CREATE_OVERTIME'),
('ROLE_RECEPTIONIST', 'CANCEL_TIME_OFF_OWN'), ('ROLE_RECEPTIONIST', 'CANCEL_OVERTIME_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_HOLIDAY')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Manager (Full management permissions)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_MANAGER', 'VIEW_EMPLOYEE'), ('ROLE_MANAGER', 'CREATE_EMPLOYEE'),
('ROLE_MANAGER', 'UPDATE_EMPLOYEE'), ('ROLE_MANAGER', 'DELETE_EMPLOYEE'),
('ROLE_MANAGER', 'VIEW_PATIENT'), ('ROLE_MANAGER', 'VIEW_APPOINTMENT'),
-- CUSTOMER_MANAGEMENT
('ROLE_MANAGER', 'VIEW_CONTACT'), ('ROLE_MANAGER', 'CREATE_CONTACT'),
('ROLE_MANAGER', 'UPDATE_CONTACT'), ('ROLE_MANAGER', 'DELETE_CONTACT'),
('ROLE_MANAGER', 'VIEW_CONTACT_HISTORY'), ('ROLE_MANAGER', 'CREATE_CONTACT_HISTORY'),
('ROLE_MANAGER', 'UPDATE_CONTACT_HISTORY'), ('ROLE_MANAGER', 'DELETE_CONTACT_HISTORY'),
-- SCHEDULE_MANAGEMENT (full)
('ROLE_MANAGER', 'VIEW_WORK_SHIFTS'), ('ROLE_MANAGER', 'CREATE_WORK_SHIFTS'),
('ROLE_MANAGER', 'UPDATE_WORK_SHIFTS'), ('ROLE_MANAGER', 'DELETE_WORK_SHIFTS'),
('ROLE_MANAGER', 'MANAGE_WORK_SLOTS'), ('ROLE_MANAGER', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_MANAGER', 'VIEW_REGISTRATION_ALL'), ('ROLE_MANAGER', 'CREATE_REGISTRATION'),
('ROLE_MANAGER', 'UPDATE_REGISTRATION'), ('ROLE_MANAGER', 'DELETE_REGISTRATION'),
('ROLE_MANAGER', 'UPDATE_REGISTRATIONS_ALL'), ('ROLE_MANAGER', 'CANCEL_REGISTRATION_OWN'),
-- Employee shift management (BE-302)
('ROLE_MANAGER', 'VIEW_SHIFTS_ALL'), ('ROLE_MANAGER', 'VIEW_SHIFTS_SUMMARY'),
('ROLE_MANAGER', 'CREATE_SHIFTS'), ('ROLE_MANAGER', 'UPDATE_SHIFTS'), ('ROLE_MANAGER', 'DELETE_SHIFTS'),
-- Fixed shift registration management (BE-307 V2)
('ROLE_MANAGER', 'MANAGE_FIXED_REGISTRATIONS'), ('ROLE_MANAGER', 'VIEW_FIXED_REGISTRATIONS_ALL'),
-- LEAVE_MANAGEMENT (full management)
('ROLE_MANAGER', 'VIEW_LEAVE_ALL'),
('ROLE_MANAGER', 'APPROVE_TIME_OFF'), ('ROLE_MANAGER', 'REJECT_TIME_OFF'), ('ROLE_MANAGER', 'CANCEL_TIME_OFF_PENDING'),
('ROLE_MANAGER', 'VIEW_OT_ALL'), ('ROLE_MANAGER', 'APPROVE_OT'), ('ROLE_MANAGER', 'REJECT_OT'), ('ROLE_MANAGER', 'CANCEL_OT_PENDING'),
('ROLE_MANAGER', 'APPROVE_OVERTIME'), ('ROLE_MANAGER', 'REJECT_OVERTIME'), ('ROLE_MANAGER', 'CANCEL_OVERTIME_PENDING'),
('ROLE_MANAGER', 'VIEW_TIMEOFF_TYPE'), ('ROLE_MANAGER', 'VIEW_TIMEOFF_TYPE_ALL'), ('ROLE_MANAGER', 'CREATE_TIMEOFF_TYPE'),
('ROLE_MANAGER', 'UPDATE_TIMEOFF_TYPE'), ('ROLE_MANAGER', 'DELETE_TIMEOFF_TYPE'),
('ROLE_MANAGER', 'VIEW_LEAVE_BALANCE_ALL'), ('ROLE_MANAGER', 'ADJUST_LEAVE_BALANCE'),
-- SYSTEM_CONFIGURATION (limited)
('ROLE_MANAGER', 'VIEW_ROLE'), ('ROLE_MANAGER', 'VIEW_SPECIALIZATION'),
('ROLE_MANAGER', 'CREATE_SPECIALIZATION'), ('ROLE_MANAGER', 'UPDATE_SPECIALIZATION'),
-- HOLIDAY
('ROLE_MANAGER', 'VIEW_HOLIDAY'),
-- ROOM_MANAGEMENT (V16: Full management of rooms and room-service compatibility)
('ROLE_MANAGER', 'VIEW_ROOM'), ('ROLE_MANAGER', 'CREATE_ROOM'),
('ROLE_MANAGER', 'UPDATE_ROOM'), ('ROLE_MANAGER', 'DELETE_ROOM'),
('ROLE_MANAGER', 'UPDATE_ROOM_SERVICES'),
-- SERVICE_MANAGEMENT (V16: Full management of services)
('ROLE_MANAGER', 'VIEW_SERVICE'), ('ROLE_MANAGER', 'CREATE_SERVICE'),
('ROLE_MANAGER', 'UPDATE_SERVICE'), ('ROLE_MANAGER', 'DELETE_SERVICE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Accountant & Inventory Manager (LEAVE only)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_ACCOUNTANT', 'VIEW_LEAVE_OWN'), ('ROLE_ACCOUNTANT', 'CREATE_TIME_OFF'), ('ROLE_ACCOUNTANT', 'CREATE_OVERTIME'),
('ROLE_ACCOUNTANT', 'CANCEL_TIME_OFF_OWN'), ('ROLE_ACCOUNTANT', 'CANCEL_OVERTIME_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_HOLIDAY'),
('ROLE_INVENTORY_MANAGER', 'VIEW_LEAVE_OWN'), ('ROLE_INVENTORY_MANAGER', 'CREATE_TIME_OFF'), ('ROLE_INVENTORY_MANAGER', 'CREATE_OVERTIME'),
('ROLE_INVENTORY_MANAGER', 'CANCEL_TIME_OFF_OWN'), ('ROLE_INVENTORY_MANAGER', 'CANCEL_OVERTIME_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_HOLIDAY')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Patient (basic view only)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_PATIENT', 'VIEW_PATIENT'), ('ROLE_PATIENT', 'VIEW_TREATMENT'),
('ROLE_PATIENT', 'VIEW_APPOINTMENT'), ('ROLE_PATIENT', 'CREATE_APPOINTMENT')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant basic Overtime permissions to all employee roles (idempotent)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_OT_OWN'), ('ROLE_DOCTOR', 'CREATE_OT'), ('ROLE_DOCTOR', 'CANCEL_OT_OWN'),
('ROLE_NURSE', 'VIEW_OT_OWN'), ('ROLE_NURSE', 'CREATE_OT'), ('ROLE_NURSE', 'CANCEL_OT_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_OT_OWN'), ('ROLE_RECEPTIONIST', 'CREATE_OT'), ('ROLE_RECEPTIONIST', 'CANCEL_OT_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_OT_OWN'), ('ROLE_ACCOUNTANT', 'CREATE_OT'), ('ROLE_ACCOUNTANT', 'CANCEL_OT_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_OT_OWN'), ('ROLE_INVENTORY_MANAGER', 'CREATE_OT'), ('ROLE_INVENTORY_MANAGER', 'CANCEL_OT_OWN'),
('ROLE_MANAGER', 'VIEW_OT_ALL'), ('ROLE_MANAGER', 'VIEW_OT_OWN'), ('ROLE_MANAGER', 'CREATE_OT'), ('ROLE_MANAGER', 'CANCEL_OT_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant VIEW_WORK_SHIFTS to all employee roles (idempotent)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_WORK_SHIFTS'),
('ROLE_NURSE', 'VIEW_WORK_SHIFTS'),
('ROLE_RECEPTIONIST', 'VIEW_WORK_SHIFTS'),
('ROLE_ACCOUNTANT', 'VIEW_WORK_SHIFTS'),
('ROLE_INVENTORY_MANAGER', 'VIEW_WORK_SHIFTS'),
('ROLE_MANAGER', 'VIEW_WORK_SHIFTS')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant VIEW_SHIFTS_OWN to all employee roles (BE-307)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_SHIFTS_OWN'),
('ROLE_NURSE', 'VIEW_SHIFTS_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_SHIFTS_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_SHIFTS_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_SHIFTS_OWN'),
('ROLE_MANAGER', 'VIEW_SHIFTS_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant CREATE_REGISTRATION to all employee roles (idempotent) - Allow self shift registration
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'CREATE_REGISTRATION'),
('ROLE_NURSE', 'CREATE_REGISTRATION'),
('ROLE_RECEPTIONIST', 'CREATE_REGISTRATION'),
('ROLE_ACCOUNTANT', 'CREATE_REGISTRATION'),
('ROLE_INVENTORY_MANAGER', 'CREATE_REGISTRATION'),
('ROLE_MANAGER', 'CREATE_REGISTRATION'),
('ROLE_MANAGER', 'VIEW_REGISTRATION_ALL')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant VIEW_AVAILABLE_SLOTS to all employee roles (BE-307 V2) - Allow viewing available part-time slots
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_NURSE', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_RECEPTIONIST', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_ACCOUNTANT', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_INVENTORY_MANAGER', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_MANAGER', 'VIEW_AVAILABLE_SLOTS')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant CANCEL_REGISTRATION_OWN to all employee roles (BE-307 V2) - Allow canceling own registrations
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'CANCEL_REGISTRATION_OWN'),
('ROLE_NURSE', 'CANCEL_REGISTRATION_OWN'),
('ROLE_RECEPTIONIST', 'CANCEL_REGISTRATION_OWN'),
('ROLE_ACCOUNTANT', 'CANCEL_REGISTRATION_OWN'),
('ROLE_INVENTORY_MANAGER', 'CANCEL_REGISTRATION_OWN'),
('ROLE_MANAGER', 'CANCEL_REGISTRATION_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant VIEW_TIMEOFF_OWN to all employee roles (idempotent)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_TIMEOFF_OWN'),
('ROLE_NURSE', 'VIEW_TIMEOFF_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_TIMEOFF_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_TIMEOFF_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_TIMEOFF_OWN'),
('ROLE_MANAGER', 'VIEW_TIMEOFF_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant UPDATE_REGISTRATION_OWN to all employee roles (idempotent) - Allow employees to edit their own shifts
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'UPDATE_REGISTRATION_OWN'),
('ROLE_NURSE', 'UPDATE_REGISTRATION_OWN'),
('ROLE_RECEPTIONIST', 'UPDATE_REGISTRATION_OWN'),
('ROLE_ACCOUNTANT', 'UPDATE_REGISTRATION_OWN'),
('ROLE_INVENTORY_MANAGER', 'UPDATE_REGISTRATION_OWN'),
('ROLE_MANAGER', 'UPDATE_REGISTRATION_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant DELETE_REGISTRATION_OWN to all employee roles (idempotent) - Allow employees to delete their own shifts
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'DELETE_REGISTRATION_OWN'),
('ROLE_NURSE', 'DELETE_REGISTRATION_OWN'),
('ROLE_RECEPTIONIST', 'DELETE_REGISTRATION_OWN'),
('ROLE_ACCOUNTANT', 'DELETE_REGISTRATION_OWN'),
('ROLE_INVENTORY_MANAGER', 'DELETE_REGISTRATION_OWN'),
('ROLE_MANAGER', 'DELETE_REGISTRATION_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant CREATE_TIMEOFF to all employee roles (idempotent) - Allow all employees to request time-off
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'CREATE_TIMEOFF'),
('ROLE_NURSE', 'CREATE_TIMEOFF'),
('ROLE_RECEPTIONIST', 'CREATE_TIMEOFF'),
('ROLE_ACCOUNTANT', 'CREATE_TIMEOFF'),
('ROLE_INVENTORY_MANAGER', 'CREATE_TIMEOFF'),
('ROLE_MANAGER', 'CREATE_TIMEOFF')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant CANCEL_TIMEOFF_OWN to all employee roles (idempotent) - Allow employees to cancel their own time-off requests
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'CANCEL_TIMEOFF_OWN'),
('ROLE_NURSE', 'CANCEL_TIMEOFF_OWN'),
('ROLE_RECEPTIONIST', 'CANCEL_TIMEOFF_OWN'),
('ROLE_ACCOUNTANT', 'CANCEL_TIMEOFF_OWN'),
('ROLE_INVENTORY_MANAGER', 'CANCEL_TIMEOFF_OWN'),
('ROLE_MANAGER', 'CANCEL_TIMEOFF_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant VIEW_FIXED_REGISTRATIONS_OWN to all employee roles (BE-307 V2) - Allow viewing own fixed registrations
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DOCTOR', 'VIEW_FIXED_REGISTRATIONS_OWN'),
('ROLE_NURSE', 'VIEW_FIXED_REGISTRATIONS_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_FIXED_REGISTRATIONS_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_FIXED_REGISTRATIONS_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_FIXED_REGISTRATIONS_OWN'),
('ROLE_MANAGER', 'VIEW_FIXED_REGISTRATIONS_OWN')
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
-- BƯỚC 6: TẠO TÀI KHOẢN - STATUS = ACTIVE (SKIP VERIFICATION)
-- ============================================
-- Seeded accounts = ACTIVE (demo data, skip email verification)
-- New accounts created via API = PENDING_VERIFICATION (require email)
-- Default password: "123456" (BCrypt encoded)
-- ============================================

INSERT INTO accounts (account_id, account_code, username, email, password, role_id, status, created_at)
VALUES
-- Admin
(1, 'ACC001', 'admin', 'admin@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_ADMIN', 'ACTIVE', NOW()),

-- Employees
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

-- Patients (demo emails - ACTIVE status)
(8, 'ACC008', 'benhnhan1', 'benhnhan1@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', NOW()),

(9, 'ACC009', 'benhnhan2', 'benhnhan2@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', NOW()),

(10, 'ACC010', 'benhnhan3', 'benhnhan3@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', NOW()),

(11, 'ACC011', 'yta2', 'yta2@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_NURSE', 'ACTIVE', NOW()),

(12, 'ACC012', 'yta3', 'yta3@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_NURSE', 'ACTIVE', NOW())

ON CONFLICT (account_id) DO NOTHING;

-- ============================================
-- BƯỚC 7: TẠO ROOMS (PHÒNG KHÁM/GHẾ NHA KHOA)
-- ============================================
-- Seed data cho các phòng khám/ghế nha khoa
-- ============================================

INSERT INTO rooms (room_code, room_name, room_type, is_active, created_at, updated_at)
VALUES
('P-01', 'Phòng thường 1', 'STANDARD', TRUE, NOW(), NOW()),
('P-02', 'Phòng thường 2', 'STANDARD', TRUE, NOW(), NOW()),
('P-03', 'Phòng thường 3', 'STANDARD', TRUE, NOW(), NOW()),
('P-04', 'Phòng Implant', 'IMPLANT', TRUE, NOW(), NOW())
ON CONFLICT (room_code) DO NOTHING;

-- ============================================
-- BƯỚC 8-14: EMPLOYEES, PATIENTS, WORK_SHIFTS, ETC
-- (Giữ nguyên như cũ)
-- ============================================

INSERT INTO employees (employee_id, account_id, employee_code, first_name, last_name, phone, date_of_birth, address, employment_type, is_active, created_at)
VALUES
(1, 1, 'EMP001', 'Admin', 'Hệ thống', '0900000001', '1985-01-01', 'Phòng quản trị', 'FULL_TIME', TRUE, NOW()),
(2, 2, 'EMP002', 'Minh', 'Nguyễn Văn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', 'FULL_TIME', TRUE, NOW()),
(3, 3, 'EMP003', 'Lan', 'Trần Thị', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', 'FULL_TIME', TRUE, NOW()),
(4, 4, 'EMP004', 'Mai', 'Lê Thị', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', 'FULL_TIME', TRUE, NOW()),
(5, 5, 'EMP005', 'Tuấn', 'Hoàng Văn', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', 'FULL_TIME', TRUE, NOW()),
-- Part-time employees (Schema V14)
(6, 6, 'EMP006', 'Hoa', 'Phạm Thị', '0906789012', '1992-06-15', '111 Lý Thường Kiệt, Q10, TPHCM', 'PART_TIME_FIXED', TRUE, NOW()), -- Part-time có lịch cố định
(7, 7, 'EMP007', 'Quân', 'Trần Minh', '0909999999', '1980-10-10', '77 Phạm Ngọc Thạch, Q3, TPHCM', 'FULL_TIME', TRUE, NOW()),
(8, 11, 'EMP008', 'Linh', 'Nguyễn Thị', '0907777777', '1998-03-22', '234 Võ Thị Sáu, Q3, TPHCM', 'PART_TIME_FLEX', TRUE, NOW()), -- Part-time linh hoạt (đăng ký theo suất)
(9, 12, 'EMP009', 'Trang', 'Võ Thị', '0908888888', '1999-07-18', '456 Điện Biên Phủ, Q3, TPHCM', 'PART_TIME_FIXED', TRUE, NOW()) -- Part-time có lịch cố định
ON CONFLICT (employee_id) DO NOTHING;

INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(2, 1), (2, 7), (3, 2), (3, 4), (6, 6)
ON CONFLICT (employee_id, specialization_id) DO NOTHING;

INSERT INTO patients (patient_id, account_id, patient_code, first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES
(1, 8, 'PAT001', 'Khang', 'Nguyễn Văn', 'benhnhan1@email.com', '0911111111', '1990-01-15', '123 Lê Văn Việt, Q9, TPHCM', 'MALE', TRUE, NOW(), NOW()),
(2, 9, 'PAT002', 'Lan', 'Trần Thị', 'benhnhan2@email.com', '0922222222', '1985-05-20', '456 Võ Văn Ngân, Thủ Đức, TPHCM', 'FEMALE', TRUE, NOW(), NOW()),
(3, 10, 'PAT003', 'Đức', 'Lê Minh', 'benhnhan3@email.com', '0933333333', '1995-12-10', '789 Đường D2, Bình Thạnh, TPHCM', 'MALE', TRUE, NOW(), NOW())
ON CONFLICT (patient_id) DO NOTHING;

INSERT INTO work_shifts (work_shift_id, shift_name, start_time, end_time, category, is_active)
VALUES
('WKS_MORNING_01', 'Ca Sáng (8h-16h)', '08:00:00', '16:00:00', 'NORMAL', TRUE),
('WKS_AFTERNOON_01', 'Ca Chiều (13h-20h)', '13:00:00', '20:00:00', 'NORMAL', TRUE),
('WKS_MORNING_02', 'Ca Part-time Sáng (8h-12h)', '08:00:00', '12:00:00', 'NORMAL', TRUE),
('WKS_AFTERNOON_02', 'Ca Part-time Chiều (13h-17h)', '13:00:00', '17:00:00', 'NORMAL', TRUE)
ON CONFLICT (work_shift_id) DO NOTHING;

-- Clean up old time_off_types with TOTxxx format
DELETE FROM leave_balance_history WHERE balance_id IN (
    SELECT balance_id FROM employee_leave_balances WHERE time_off_type_id LIKE 'TOT%'
);
DELETE FROM employee_leave_balances WHERE time_off_type_id LIKE 'TOT%';
DELETE FROM time_off_types WHERE type_id LIKE 'TOT%';

-- Insert time_off_types with type_id = type_code
INSERT INTO time_off_types (type_id, type_code, type_name, is_paid, requires_approval, requires_balance, default_days_per_year, is_active)
VALUES
-- type_id = type_code for easier reference
('ANNUAL_LEAVE', 'ANNUAL_LEAVE', 'Nghỉ phép năm', TRUE, TRUE, TRUE, 12.0, TRUE),
('UNPAID_PERSONAL', 'UNPAID_PERSONAL', 'Nghỉ việc riêng không lương', FALSE, TRUE, FALSE, NULL, TRUE),
('SICK_LEAVE', 'SICK_LEAVE', 'Nghỉ ốm có bảo hiểm xã hội', TRUE, TRUE, FALSE, NULL, TRUE),
('MATERNITY_LEAVE', 'MATERNITY_LEAVE', 'Nghỉ thai sản (6 tháng)', TRUE, TRUE, FALSE, NULL, TRUE),
('PATERNITY_LEAVE', 'PATERNITY_LEAVE', 'Nghỉ chăm con (5-14 ngày)', TRUE, TRUE, FALSE, NULL, TRUE),
('MARRIAGE_LEAVE', 'MARRIAGE_LEAVE', 'Nghỉ kết hôn (3 ngày)', TRUE, TRUE, FALSE, NULL, TRUE),
('BEREAVEMENT_LEAVE', 'BEREAVEMENT_LEAVE', 'Nghỉ tang lễ (1-3 ngày)', TRUE, TRUE, FALSE, NULL, TRUE),
('EMERGENCY_LEAVE', 'EMERGENCY_LEAVE', 'Nghỉ khẩn cấp', FALSE, TRUE, FALSE, NULL, TRUE),
('STUDY_LEAVE', 'STUDY_LEAVE', 'Nghỉ học tập/đào tạo', TRUE, TRUE, FALSE, NULL, TRUE),
('COMPENSATORY_LEAVE', 'COMPENSATORY_LEAVE', 'Nghỉ bù (sau làm thêm giờ)', TRUE, FALSE, FALSE, NULL, TRUE)
ON CONFLICT (type_id) DO UPDATE SET
    type_code = EXCLUDED.type_code,
    type_name = EXCLUDED.type_name,
    is_paid = EXCLUDED.is_paid,
    requires_approval = EXCLUDED.requires_approval,
    requires_balance = EXCLUDED.requires_balance,
    default_days_per_year = EXCLUDED.default_days_per_year,
    is_active = EXCLUDED.is_active;

-- Sequences sync
SELECT setval(pg_get_serial_sequence('base_roles', 'base_role_id'), COALESCE((SELECT MAX(base_role_id) FROM base_roles), 0)+1, false);
SELECT setval(pg_get_serial_sequence('accounts', 'account_id'), COALESCE((SELECT MAX(account_id) FROM accounts), 0)+1, false);
SELECT setval(pg_get_serial_sequence('employees', 'employee_id'), COALESCE((SELECT MAX(employee_id) FROM employees), 0)+1, false);
SELECT setval(pg_get_serial_sequence('patients', 'patient_id'), COALESCE((SELECT MAX(patient_id) FROM patients), 0)+1, false);
SELECT setval(pg_get_serial_sequence('specializations', 'specialization_id'), COALESCE((SELECT MAX(specialization_id) FROM specializations), 0)+1, false);

-- ============================================
-- BƯỚC 14: SAMPLE DATA FOR TIME-OFF, RENEWAL, HOLIDAYS
-- ============================================

-- Sample time-off requests
INSERT INTO time_off_requests (request_id, employee_id, time_off_type_id, work_shift_id, start_date, end_date, status, approved_by, approved_at, requested_at, requested_by)
VALUES
('TOR251025001', 2, 'ANNUAL_LEAVE', 'WKS_MORNING_01', '2025-10-28', '2025-10-29', 'PENDING', NULL, NULL, NOW(), 2),
('TOR251025002', 3, 'SICK_LEAVE', 'WKS_AFTERNOON_01', '2025-11-02', '2025-11-02', 'APPROVED', 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '2 days', 3),
('TOR251025003', 4, 'UNPAID_PERSONAL', 'WKS_MORNING_02', '2025-11-05', '2025-11-06', 'REJECTED', 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '2 days', 4)
ON CONFLICT (request_id) DO NOTHING;

-- ============================================
-- SAMPLE OVERTIME REQUESTS (BE-304)
-- ============================================
-- Sample OT requests covering all statuses for FE testing
-- PENDING, APPROVED, REJECTED, CANCELLED

INSERT INTO overtime_requests (
    request_id, employee_id, requested_by, work_date, work_shift_id,
    reason, status, approved_by, approved_at, rejected_reason, cancellation_reason, created_at
)
VALUES
-- PENDING overtime requests (for testing approval/rejection/cancellation)
('OTR251030005', 2, 2, '2025-11-18', 'WKS_AFTERNOON_02',
 'Hoàn thành báo cáo cuối tháng', 'PENDING', NULL, NULL, NULL, NULL, NOW()),

('OTR251030006', 3, 3, '2025-11-20', 'WKS_MORNING_01',
 'Hỗ trợ dự án khẩn cấp', 'PENDING', NULL, NULL, NULL, NULL, NOW()),

('OTR251030007', 4, 4, '2025-11-22', 'WKS_AFTERNOON_01',
 'Hỗ trợ tiếp đón bệnh nhân ca tối', 'PENDING', NULL, NULL, NULL, NULL, NOW()),

-- APPROVED overtime requests (with auto-created employee shifts)
('OTR251030008', 5, 5, '2025-11-25', 'WKS_MORNING_02',
 'Xử lý công việc kế toán tồn đọng', 'APPROVED', 7, NOW() - INTERVAL '2 days', NULL, NULL, NOW() - INTERVAL '3 days'),

('OTR251030009', 6, 6, '2025-11-27', 'WKS_AFTERNOON_02',
 'Chăm sóc bệnh nhân đặc biệt', 'APPROVED', 7, NOW() - INTERVAL '1 day', NULL, NULL, NOW() - INTERVAL '2 days'),

-- REJECTED overtime request
('OTR251030010', 2, 2, '2025-11-28', 'WKS_MORNING_01',
 'Yêu cầu tăng ca thêm', 'REJECTED', 7, NOW() - INTERVAL '1 day', 'Đã đủ nhân sự cho ngày này', NULL, NOW() - INTERVAL '2 days'),

-- CANCELLED overtime request (self-cancelled)
('OTR251030011', 3, 3, '2025-11-30', 'WKS_AFTERNOON_01',
 'Yêu cầu tăng ca cuối tháng', 'CANCELLED', NULL, NULL, NULL, 'Có việc đột xuất không thể tham gia', NOW() - INTERVAL '1 day')

ON CONFLICT (request_id) DO NOTHING;

-- Create corresponding employee shifts for APPROVED OT requests
INSERT INTO employee_shifts (
    employee_shift_id, created_at, created_by, is_overtime, notes,
    source, source_off_request_id, source_ot_request_id, status, updated_at,
    work_date, employee_id, work_shift_id
)
VALUES
-- Auto-created shift for OTR251030008 (Accountant Tuan)
('EMS251030003', NOW() - INTERVAL '2 days', 7, TRUE,
 'Tạo từ yêu cầu OT OTR251030008 - Xử lý công việc kế toán tồn đọng',
 'OT_APPROVAL', NULL, 'OTR251030008', 'SCHEDULED', NULL,
 '2025-11-25', 5, 'WKS_MORNING_02'),

-- Auto-created shift for OTR251030009 (Nurse Hoa)
('EMS251030004', NOW() - INTERVAL '1 day', 7, TRUE,
 'Tạo từ yêu cầu OT OTR251030009 - Chăm sóc bệnh nhân đặc biệt',
 'OT_APPROVAL', NULL, 'OTR251030009', 'SCHEDULED', NULL,
 '2025-11-27', 6, 'WKS_AFTERNOON_02')

ON CONFLICT (employee_shift_id) DO NOTHING;
-- ============================================
-- EMPLOYEE SHIFT SAMPLE DATA (BE-302)
-- ============================================
-- Sample employee shifts for testing Employee Shift Management API
-- Covers different statuses, shift types, and scenarios
-- employee_id mapping: 2=nhasi1, 3=nhasi2, 4=letan, 5=ketoan, 6=yta, 7=manager

INSERT INTO employee_shifts (
    employee_shift_id, created_at, created_by, is_overtime, notes,
    source, status, updated_at, work_date, employee_id, work_shift_id
)
VALUES
-- November 2025 shifts (Current month for testing)
-- Dr. Minh (employee_id=2) - SCHEDULED shifts
('EMS251101001', NOW(), NULL, FALSE, 'Ca sáng thứ 2', 'MANUAL_ENTRY', 'SCHEDULED', NOW(), '2025-11-03', 2, 'WKS_MORNING_01'),
('EMS251101002', NOW(), NULL, FALSE, 'Ca chiều thứ 3', 'MANUAL_ENTRY', 'SCHEDULED', NOW(), '2025-11-04', 2, 'WKS_AFTERNOON_01'),
('EMS251101003', NOW(), NULL, FALSE, 'Ca tự động từ batch job', 'BATCH_JOB', 'SCHEDULED', NOW(), '2025-11-05', 2, 'WKS_MORNING_01'),

-- Dr. Lan (employee_id=3) - COMPLETED shifts
('EMS251101004', NOW(), NULL, FALSE, 'Ca sáng đã hoàn thành', 'MANUAL_ENTRY', 'COMPLETED', NOW(), '2025-11-01', 3, 'WKS_MORNING_01'),
('EMS251101005', NOW(), NULL, FALSE, 'Ca chiều đã hoàn thành', 'BATCH_JOB', 'COMPLETED', NOW(), '2025-11-02', 3, 'WKS_AFTERNOON_01'),

-- Receptionist Mai (employee_id=4) - CANCELLED shifts
('EMS251101006', NOW(), NULL, FALSE, 'Ca bị hủy do bận việc', 'MANUAL_ENTRY', 'CANCELLED', NOW(), '2025-11-03', 4, 'WKS_MORNING_02'),
('EMS251101007', NOW(), NULL, FALSE, 'Ca part-time chiều', 'MANUAL_ENTRY', 'SCHEDULED', NOW(), '2025-11-04', 4, 'WKS_AFTERNOON_02'),

-- Accountant Tuan (employee_id=5) - Mixed statuses
('EMS251101008', NOW(), NULL, FALSE, 'Ca sáng thứ 4', 'MANUAL_ENTRY', 'SCHEDULED', NOW(), '2025-11-06', 5, 'WKS_MORNING_01'),
('EMS251101009', NOW(), NULL, FALSE, 'Ca chiều từ batch job', 'BATCH_JOB', 'SCHEDULED', NOW(), '2025-11-07', 5, 'WKS_AFTERNOON_01'),
('EMS251101010', NOW(), NULL, FALSE, 'Ca đã hoàn thành', 'MANUAL_ENTRY', 'COMPLETED', NOW(), '2025-11-01', 5, 'WKS_MORNING_01'),

-- Nurse Hoa (employee_id=6) - ON_LEAVE status
('EMS251101011', NOW(), NULL, FALSE, 'Nghỉ phép có đăng ký', 'BATCH_JOB', 'ON_LEAVE', NOW(), '2025-11-05', 6, 'WKS_MORNING_02'),
('EMS251101012', NOW(), NULL, FALSE, 'Ca part-time chiều', 'MANUAL_ENTRY', 'SCHEDULED', NOW(), '2025-11-06', 6, 'WKS_AFTERNOON_02'),

-- Manager Quan (employee_id=7) - All permissions
('EMS251101013', NOW(), NULL, FALSE, 'Ca quản lý', 'MANUAL_ENTRY', 'SCHEDULED', NOW(), '2025-11-08', 7, 'WKS_MORNING_01'),
('EMS251101014', NOW(), NULL, FALSE, 'Ca quản lý từ batch job', 'BATCH_JOB', 'SCHEDULED', NOW(), '2025-11-09', 7, 'WKS_AFTERNOON_01'),

-- December 2025 shifts (Future month)
('EMS251201001', NOW(), NULL, FALSE, 'Ca tháng 12', 'BATCH_JOB', 'SCHEDULED', NOW(), '2025-12-02', 2, 'WKS_MORNING_01'),
('EMS251201002', NOW(), NULL, FALSE, 'Ca tháng 12', 'MANUAL_ENTRY', 'SCHEDULED', NOW(), '2025-12-03', 3, 'WKS_AFTERNOON_01'),
('EMS251201003', NOW(), NULL, FALSE, 'Ca tháng 12', 'BATCH_JOB', 'SCHEDULED', NOW(), '2025-12-04', 5, 'WKS_MORNING_01'),

-- October 2025 shifts (Past month for historical data)
('EMS251001001', NOW(), NULL, FALSE, 'Ca tháng trước đã hoàn thành', 'MANUAL_ENTRY', 'COMPLETED', NOW(), '2025-10-15', 2, 'WKS_MORNING_01'),
('EMS251001002', NOW(), NULL, FALSE, 'Ca tháng trước đã hoàn thành', 'BATCH_JOB', 'COMPLETED', NOW(), '2025-10-16', 3, 'WKS_AFTERNOON_01'),
('EMS251001003', NOW(), NULL, FALSE, 'Ca tháng trước bị hủy', 'MANUAL_ENTRY', 'CANCELLED', NOW(), '2025-10-17', 5, 'WKS_MORNING_01')

ON CONFLICT (employee_shift_id) DO NOTHING;

-- ============================================
-- HOLIDAY DEFINITIONS (New Schema with 2 tables)
-- ============================================
-- Production holidays: TET_2025, LIBERATION_DAY, LABOR_DAY, NATIONAL_DAY, NEW_YEAR, HUNG_KINGS
-- Test holidays: MAINTENANCE_WEEK (for FE testing shift blocking)
-- ============================================

-- Step 1: Insert holiday definitions (one by one to avoid conflicts)
INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, description, created_at, updated_at)
VALUES ('TET_2025', 'Tết Nguyên Đán 2025', 'NATIONAL', 'Lunar New Year 2025 - Vietnamese traditional holiday', NOW(), NOW())
ON CONFLICT (definition_id) DO NOTHING;

INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, description, created_at, updated_at)
VALUES ('LIBERATION_DAY', 'Ngày Giải phóng miền Nam', 'NATIONAL', 'Reunification Day - April 30th', NOW(), NOW())
ON CONFLICT (definition_id) DO NOTHING;

INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, description, created_at, updated_at)
VALUES ('LABOR_DAY', 'Ngày Quốc tế Lao động', 'NATIONAL', 'International Labor Day - May 1st', NOW(), NOW())
ON CONFLICT (definition_id) DO NOTHING;

INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, description, created_at, updated_at)
VALUES ('NATIONAL_DAY', 'Ngày Quốc khánh', 'NATIONAL', 'Vietnam National Day - September 2nd', NOW(), NOW())
ON CONFLICT (definition_id) DO NOTHING;

INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, description, created_at, updated_at)
VALUES ('NEW_YEAR', 'Tết Dương lịch', 'NATIONAL', 'Gregorian New Year', NOW(), NOW())
ON CONFLICT (definition_id) DO NOTHING;

INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, description, created_at, updated_at)
VALUES ('HUNG_KINGS', 'Giỗ Tổ Hùng Vương', 'NATIONAL', 'Hung Kings Commemoration Day', NOW(), NOW())
ON CONFLICT (definition_id) DO NOTHING;

-- Step 2: Insert holiday dates (specific dates for each definition)
-- Tết Nguyên Đán 2025 (Jan 29 - Feb 4, 2025)
INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-01-29', 'TET_2025', 'Ngày Tết Nguyên Đán (30 Tết)', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-01-30', 'TET_2025', 'Mùng 1 Tết', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-01-31', 'TET_2025', 'Mùng 2 Tết', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-02-01', 'TET_2025', 'Mùng 3 Tết', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-02-02', 'TET_2025', 'Mùng 4 Tết', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-02-03', 'TET_2025', 'Mùng 5 Tết', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-02-04', 'TET_2025', 'Mùng 6 Tết', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

-- Liberation Day & Labor Day (April 30 - May 1, 2025)
INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-04-30', 'LIBERATION_DAY', 'Ngày Giải phóng miền Nam', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-05-01', 'LABOR_DAY', 'Ngày Quốc tế Lao động', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

-- Hung Kings Commemoration Day 2025 (April 18, 2025 - lunar March 10th)
INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-04-18', 'HUNG_KINGS', 'Giỗ Tổ Hùng Vương', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

-- National Day (September 2, 2025)
INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-09-02', 'NATIONAL_DAY', 'Quốc khánh Việt Nam', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

-- New Year (January 1, 2025)
INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-01-01', 'NEW_YEAR', 'Tết Dương lịch 2025', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

-- ============================================
-- TEST DATA: MAINTENANCE_WEEK (For FE Testing)
-- ============================================
-- Purpose: Test holiday blocking functionality for shifts
-- Use Case: FE can test shift creation blocking on holidays
-- Dates: Next week (Monday, Wednesday, Friday)
-- Note: These are example dates - update as needed for testing
-- ============================================

INSERT INTO holiday_definitions (definition_id, holiday_name, holiday_type, description, created_at, updated_at)
VALUES ('MAINTENANCE_WEEK', 'System Maintenance Week', 'COMPANY', 'Scheduled system maintenance - For testing holiday blocking', NOW(), NOW())
ON CONFLICT (definition_id) DO NOTHING;

-- Add 3 maintenance days (Monday, Wednesday, Friday of a test week)
-- Example: November 3, 5, 7, 2025
INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-11-03', 'MAINTENANCE_WEEK', 'Monday maintenance - Test holiday blocking', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-11-05', 'MAINTENANCE_WEEK', 'Wednesday maintenance - Test holiday blocking', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

INSERT INTO holiday_dates (holiday_date, definition_id, description, created_at, updated_at)
VALUES ('2025-11-07', 'MAINTENANCE_WEEK', 'Friday maintenance - Test holiday blocking', NOW(), NOW())
ON CONFLICT (holiday_date, definition_id) DO NOTHING;

-- Expected Behavior:
-- ✅ Creating shifts on 2025-11-04 (Tuesday) or 2025-11-06 (Thursday) should SUCCEED
-- ❌ Creating shifts on 2025-11-03 (Monday), 2025-11-05 (Wednesday), or 2025-11-07 (Friday) should return 409 HOLIDAY_CONFLICT
-- ✅ Time-off requests spanning these dates should SUCCEED (expected behavior)
-- ✅ Batch jobs should SKIP these dates when auto-creating shifts

-- ============================================
-- WORKING SCHEDULE SAMPLE DATA (Schema V14 Hybrid)
-- ============================================
-- Sample data for testing Hybrid Schedule System:
-- - Luồng 1 (Fixed): fixed_shift_registrations + fixed_registration_days
-- - Luồng 2 (Flex): part_time_slots + part_time_registrations (UPDATED V14)
-- ============================================

-- ============================================
-- LUỒNG 1: FIXED SHIFT REGISTRATIONS
-- For FULL_TIME and PART_TIME_FIXED employees
-- ============================================

-- Fixed registration for Dr. Minh (FULL_TIME) - Weekdays Morning Shift
INSERT INTO fixed_shift_registrations (
    registration_id, employee_id, work_shift_id,
    effective_from, effective_to, is_active, created_at
)
VALUES
(1, 2, 'WKS_MORNING_01', '2025-01-01', '2026-12-31', TRUE, NOW())
ON CONFLICT (registration_id) DO NOTHING;

INSERT INTO fixed_registration_days (registration_id, day_of_week)
VALUES
(1, 'MONDAY'),
(1, 'TUESDAY'),
(1, 'WEDNESDAY'),
(1, 'THURSDAY'),
(1, 'FRIDAY')
ON CONFLICT (registration_id, day_of_week) DO NOTHING;

-- Fixed registration for Dr. Lan (FULL_TIME) - Weekdays Afternoon Shift
INSERT INTO fixed_shift_registrations (
    registration_id, employee_id, work_shift_id,
    effective_from, effective_to, is_active, created_at
)
VALUES
(2, 3, 'WKS_AFTERNOON_01', '2025-01-01', '2026-12-31', TRUE, NOW())
ON CONFLICT (registration_id) DO NOTHING;

INSERT INTO fixed_registration_days (registration_id, day_of_week)
VALUES
(2, 'MONDAY'),
(2, 'TUESDAY'),
(2, 'WEDNESDAY'),
(2, 'THURSDAY'),
(2, 'FRIDAY')
ON CONFLICT (registration_id, day_of_week) DO NOTHING;

-- Fixed registration for Receptionist Mai (FULL_TIME) - Weekdays Morning Part-time
INSERT INTO fixed_shift_registrations (
    registration_id, employee_id, work_shift_id,
    effective_from, effective_to, is_active, created_at
)
VALUES
(3, 4, 'WKS_MORNING_02', '2025-11-01', '2026-10-31', TRUE, NOW())
ON CONFLICT (registration_id) DO NOTHING;

INSERT INTO fixed_registration_days (registration_id, day_of_week)
VALUES
(3, 'MONDAY'),
(3, 'TUESDAY'),
(3, 'WEDNESDAY'),
(3, 'THURSDAY'),
(3, 'FRIDAY')
ON CONFLICT (registration_id, day_of_week) DO NOTHING;

-- Fixed registration for Accountant Tuan (FULL_TIME) - Full week Morning
INSERT INTO fixed_shift_registrations (
    registration_id, employee_id, work_shift_id,
    effective_from, effective_to, is_active, created_at
)
VALUES
(4, 5, 'WKS_MORNING_01', '2025-01-01', NULL, TRUE, NOW())
ON CONFLICT (registration_id) DO NOTHING;

INSERT INTO fixed_registration_days (registration_id, day_of_week)
VALUES
(4, 'MONDAY'),
(4, 'TUESDAY'),
(4, 'WEDNESDAY'),
(4, 'THURSDAY'),
(4, 'FRIDAY'),
(4, 'SATURDAY')
ON CONFLICT (registration_id, day_of_week) DO NOTHING;

-- Fixed registration for Nurse Hoa (PART_TIME_FIXED) - Monday, Wednesday, Friday Morning
INSERT INTO fixed_shift_registrations (
    registration_id, employee_id, work_shift_id,
    effective_from, effective_to, is_active, created_at
)
VALUES
(5, 6, 'WKS_MORNING_02', '2025-11-01', '2026-04-30', TRUE, NOW())
ON CONFLICT (registration_id) DO NOTHING;

INSERT INTO fixed_registration_days (registration_id, day_of_week)
VALUES
(5, 'MONDAY'),
(5, 'WEDNESDAY'),
(5, 'FRIDAY')
ON CONFLICT (registration_id, day_of_week) DO NOTHING;

-- Fixed registration for Manager Quan (FULL_TIME) - Flexible schedule
INSERT INTO fixed_shift_registrations (
    registration_id, employee_id, work_shift_id,
    effective_from, effective_to, is_active, created_at
)
VALUES
(6, 7, 'WKS_MORNING_01', '2025-01-01', NULL, TRUE, NOW())
ON CONFLICT (registration_id) DO NOTHING;

INSERT INTO fixed_registration_days (registration_id, day_of_week)
VALUES
(6, 'TUESDAY'),
(6, 'THURSDAY')
ON CONFLICT (registration_id, day_of_week) DO NOTHING;

-- Fixed registration for Nurse Trang (PART_TIME_FIXED) - Tuesday, Thursday, Saturday Afternoon
INSERT INTO fixed_shift_registrations (
    registration_id, employee_id, work_shift_id,
    effective_from, effective_to, is_active, created_at
)
VALUES
(7, 9, 'WKS_AFTERNOON_02', '2025-11-01', '2026-10-31', TRUE, NOW())
ON CONFLICT (registration_id) DO NOTHING;

INSERT INTO fixed_registration_days (registration_id, day_of_week)
VALUES
(7, 'TUESDAY'),
(7, 'THURSDAY'),
(7, 'SATURDAY')
ON CONFLICT (registration_id, day_of_week) DO NOTHING;

-- Reset sequence for fixed_shift_registrations to prevent duplicate key errors
SELECT setval('fixed_shift_registrations_registration_id_seq',
    COALESCE((SELECT MAX(registration_id) FROM fixed_shift_registrations), 0) + 1,
    false);

-- ============================================
-- LUỒNG 2: PART-TIME FLEX REGISTRATIONS
-- For PART_TIME_FLEX employees
-- ============================================

-- STEP 1: Create Part-Time Slots (Admin creates available slots)
-- Week schedule with varied quotas

-- MONDAY Slots
INSERT INTO part_time_slots (
    slot_id, work_shift_id, day_of_week, quota, is_active, created_at
)
VALUES
(1, 'WKS_MORNING_02', 'MONDAY', 2, TRUE, NOW()),
(2, 'WKS_AFTERNOON_02', 'MONDAY', 3, TRUE, NOW())
ON CONFLICT (slot_id) DO NOTHING;

-- TUESDAY Slots
INSERT INTO part_time_slots (
    slot_id, work_shift_id, day_of_week, quota, is_active, created_at
)
VALUES
(3, 'WKS_MORNING_02', 'TUESDAY', 2, TRUE, NOW()),
(4, 'WKS_AFTERNOON_02', 'TUESDAY', 2, TRUE, NOW())
ON CONFLICT (slot_id) DO NOTHING;

-- WEDNESDAY Slots
INSERT INTO part_time_slots (
    slot_id, work_shift_id, day_of_week, quota, is_active, created_at
)
VALUES
(5, 'WKS_MORNING_02', 'WEDNESDAY', 3, TRUE, NOW()),
(6, 'WKS_AFTERNOON_02', 'WEDNESDAY', 2, TRUE, NOW())
ON CONFLICT (slot_id) DO NOTHING;

-- THURSDAY Slots
INSERT INTO part_time_slots (
    slot_id, work_shift_id, day_of_week, quota, is_active, created_at
)
VALUES
(7, 'WKS_MORNING_02', 'THURSDAY', 2, TRUE, NOW()),
(8, 'WKS_AFTERNOON_02', 'THURSDAY', 3, TRUE, NOW())
ON CONFLICT (slot_id) DO NOTHING;

-- FRIDAY Slots
INSERT INTO part_time_slots (
    slot_id, work_shift_id, day_of_week, quota, is_active, created_at
)
VALUES
(9, 'WKS_MORNING_02', 'FRIDAY', 2, TRUE, NOW()),
(10, 'WKS_AFTERNOON_02', 'FRIDAY', 2, TRUE, NOW())
ON CONFLICT (slot_id) DO NOTHING;

-- SATURDAY Slots (Higher quota for weekend)
INSERT INTO part_time_slots (
    slot_id, work_shift_id, day_of_week, quota, is_active, created_at
)
VALUES
(11, 'WKS_MORNING_02', 'SATURDAY', 3, TRUE, NOW()),
(12, 'WKS_AFTERNOON_02', 'SATURDAY', 3, TRUE, NOW())
ON CONFLICT (slot_id) DO NOTHING;

-- SUNDAY Slots (Limited availability)
INSERT INTO part_time_slots (
    slot_id, work_shift_id, day_of_week, quota, is_active, created_at
)
VALUES
(13, 'WKS_MORNING_02', 'SUNDAY', 1, TRUE, NOW()),
(14, 'WKS_AFTERNOON_02', 'SUNDAY', 1, FALSE, NOW()) -- Inactive slot for testing
ON CONFLICT (slot_id) DO NOTHING;

-- Reset sequence after manual inserts with explicit IDs
-- This prevents "duplicate key value violates unique constraint" errors
SELECT setval('part_time_slots_slot_id_seq',
              (SELECT COALESCE(MAX(slot_id), 0) + 1 FROM part_time_slots),
              false);

-- One FULL slot for testing SLOT_IS_FULL error
-- NOTE: Using slot_id=1 (MONDAY morning, quota=2) for "full slot" test scenario
-- It will have 2 registrations to make it full

-- STEP 2: Part-Time Registrations (Schema V14 - Luồng 2: part_time_registrations)
-- Employees claim slots in the NEW table

-- Nurse Linh (employee_id=8, PART_TIME_FLEX) claims multiple slots

-- Linh claims TUESDAY Morning (slot_id=3)
INSERT INTO part_time_registrations (
    employee_id, part_time_slot_id,
    effective_from, effective_to, is_active, created_at, updated_at
)
VALUES
(8, 3, '2025-11-01', '2026-02-01', TRUE, NOW(), NOW());

-- Linh claims THURSDAY Afternoon (slot_id=8)
INSERT INTO part_time_registrations (
    employee_id, part_time_slot_id,
    effective_from, effective_to, is_active, created_at, updated_at
)
VALUES
(8, 8, '2025-11-01', '2026-02-01', TRUE, NOW(), NOW());

-- Linh claims SATURDAY Morning (slot_id=11)
INSERT INTO part_time_registrations (
    employee_id, part_time_slot_id,
    effective_from, effective_to, is_active, created_at, updated_at
)
VALUES
(8, 11, '2025-11-01', '2026-02-01', TRUE, NOW(), NOW());

-- Additional PART_TIME_FLEX employees for testing (create temporary test employees)
-- Test Employee 10: PART_TIME_FLEX
INSERT INTO accounts (account_id, username, password, email, status, role_id, created_at)
VALUES
(20, 'yta10', '$2a$10$RI1iV7k4XJFBWpQUCr.5L.ufNjjXlqvP0z1XrTiT8bKvYpHEtUQ8O', 'yta10@test.com', 'ACTIVE', 'ROLE_NURSE', NOW())
ON CONFLICT (account_id) DO NOTHING;

INSERT INTO employees (employee_id, account_id, employee_code, first_name, last_name, phone, date_of_birth, address, employment_type, is_active, created_at)
VALUES
(10, 20, 'EMP010', 'Minh', 'Lê Thị', '0909999999', '2000-01-15', '789 Nguyễn Huệ, Q1, TPHCM', 'PART_TIME_FLEX', TRUE, NOW())
ON CONFLICT (employee_id) DO NOTHING;

-- Test Employee 11: PART_TIME_FLEX
INSERT INTO accounts (account_id, username, password, email, status, role_id, created_at)
VALUES
(21, 'yta11', '$2a$10$RI1iV7k4XJFBWpQUCr.5L.ufNjjXlqvP0z1XrTiT8bKvYpHEtUQ8O', 'yta11@test.com', 'ACTIVE', 'ROLE_NURSE', NOW())
ON CONFLICT (account_id) DO NOTHING;

INSERT INTO employees (employee_id, account_id, employee_code, first_name, last_name, phone, date_of_birth, address, employment_type, is_active, created_at)
VALUES
(11, 21, 'EMP011', 'Hương', 'Phạm Thị', '0901111111', '1999-05-20', '321 Lê Lợi, Q1, TPHCM', 'PART_TIME_FLEX', TRUE, NOW())
ON CONFLICT (employee_id) DO NOTHING;

-- Employee 10 (Minh) claims MONDAY Afternoon (slot_id=2)
INSERT INTO part_time_registrations (
    employee_id, part_time_slot_id,
    effective_from, effective_to, is_active, created_at, updated_at
)
VALUES
(10, 2, '2025-11-01', '2026-02-01', TRUE, NOW(), NOW());

-- Employee 10 (Minh) claims WEDNESDAY Morning (slot_id=5)
INSERT INTO part_time_registrations (
    employee_id, part_time_slot_id,
    effective_from, effective_to, is_active, created_at, updated_at
)
VALUES
(10, 5, '2025-11-01', '2026-02-01', TRUE, NOW(), NOW());

-- Employee 10 (Minh) claims MONDAY Morning (slot_id=1, quota=2) - First registration (1/2)
INSERT INTO part_time_registrations (
    employee_id, part_time_slot_id,
    effective_from, effective_to, is_active, created_at, updated_at
)
VALUES
(10, 1, '2025-11-01', '2026-02-01', TRUE, NOW(), NOW());

-- Employee 11 (Huong) claims MONDAY Morning slot (slot_id=1, quota=2) - Makes it FULL (2/2)
INSERT INTO part_time_registrations (
    employee_id, part_time_slot_id,
    effective_from, effective_to, is_active, created_at, updated_at
)
VALUES
(11, 1, '2025-11-01', '2026-02-01', TRUE, NOW(), NOW());

-- Employee 11 (Huong) also claims FRIDAY Morning (slot_id=9)
INSERT INTO part_time_registrations (
    employee_id, part_time_slot_id,
    effective_from, effective_to, is_active, created_at, updated_at
)
VALUES
(11, 9, '2025-11-01', '2026-02-01', TRUE, NOW(), NOW());

-- Cancelled registration example (Linh cancels SUNDAY)
-- First claim SUNDAY Morning (slot_id=13)
INSERT INTO part_time_registrations (
    employee_id, part_time_slot_id,
    effective_from, effective_to, is_active, created_at, updated_at
)
VALUES
(8, 13, '2025-11-01', '2025-11-15', FALSE, NOW(), NOW()); -- Cancelled (is_active=false)

-- ============================================
-- RESET ALL SEQUENCES AFTER MANUAL INSERTS
-- ============================================
-- This prevents "duplicate key value violates unique constraint" errors
-- when Hibernate tries to insert new records after database restart

-- Reset accounts sequence
SELECT setval('accounts_account_id_seq',
              (SELECT COALESCE(MAX(account_id), 0) + 1 FROM accounts),
              false);

-- Reset employees sequence
SELECT setval('employees_employee_id_seq',
              (SELECT COALESCE(MAX(employee_id), 0) + 1 FROM employees),
              false);

-- Reset part_time_registrations sequence (NEW - Schema V14)
SELECT setval('part_time_registrations_registration_id_seq',
              (SELECT COALESCE(MAX(registration_id), 0) + 1 FROM part_time_registrations),
              false);

-- Note: part_time_slots sequence is already reset after its inserts above
-- Note: fixed_shift_registrations sequence is already reset after its inserts above

-- ============================================
-- SAMPLE DATA SUMMARY
-- ============================================
-- LUỒNG 1 (FIXED) - 7 registrations:
--   - Dr. Minh (2): M-F Morning
--   - Dr. Lan (3): M-F Afternoon
--   - Receptionist Mai (4): M-F Morning Part-time
--   - Accountant Tuan (5): M-Sa Morning
--   - Nurse Hoa (6): M/W/F Morning Part-time (PART_TIME_FIXED)
--   - Manager Quan (7): Tu/Th Morning
--   - Nurse Trang (9): Tu/Th/Sa Afternoon Part-time (PART_TIME_FIXED)
--
-- LUỒNG 2 (FLEX) - 15 slots, 8 registrations:
--   - 15 part-time slots created (14 active, 1 inactive)
--   - Nurse Linh (8): 3 active registrations + 1 cancelled
--   - Test Employee 10 (Minh): 2 active registrations
--   - Test Employee 11 (Huong): 2 active registrations (1 makes slot FULL)
--
-- TESTING SCENARIOS:
--   ✅ Fixed registrations for FULL_TIME and PART_TIME_FIXED
--   ✅ Flex registrations for PART_TIME_FLEX
--   ✅ Available slots with various quotas (1-3)
--   ✅ Partially filled slots (remaining capacity > 0)
--   ✅ FULL slot (slot_id=15, quota=1, registered=1)
--   ✅ Inactive slot (slot_id=14, is_active=false)
--   ✅ Cancelled registration (registration_id=8, is_active=false)
--   ✅ Various effective dates for renewal testing
-- ============================================

-- ============================================
-- EMPLOYEE LEAVE BALANCES - ANNUAL LEAVE (P5.2)
-- ============================================
-- Seed initial annual leave balances for all employees
-- Each employee gets 12 days per year for 2025
-- ============================================

-- Delete existing annual leave balances for 2025 to avoid duplicates
DELETE FROM employee_leave_balances
WHERE time_off_type_id = 'ANNUAL_LEAVE' AND cycle_year = 2025;

INSERT INTO employee_leave_balances (
    employee_id, time_off_type_id, cycle_year,
    total_days_allowed, days_taken, notes
)
VALUES
-- Admin (employee_id=1) - 2025
(1, 'ANNUAL_LEAVE', 2025, 12.0, 0.0, 'Phép năm 2025 - Khởi tạo'),

-- Dr. Minh (employee_id=2) - 2025
(2, 'ANNUAL_LEAVE', 2025, 12.0, 0.0, 'Phép năm 2025 - Khởi tạo'),

-- Dr. Lan (employee_id=3) - 2025
(3, 'ANNUAL_LEAVE', 2025, 12.0, 0.0, 'Phép năm 2025 - Khởi tạo'),

-- Receptionist Mai (employee_id=4) - 2025
(4, 'ANNUAL_LEAVE', 2025, 12.0, 0.0, 'Phép năm 2025 - Khởi tạo'),

-- Accountant Tuan (employee_id=5) - 2025
(5, 'ANNUAL_LEAVE', 2025, 12.0, 0.0, 'Phép năm 2025 - Khởi tạo'),

-- Nurse Hoa (employee_id=6) - 2025
(6, 'ANNUAL_LEAVE', 2025, 12.0, 0.0, 'Phép năm 2025 - Khởi tạo'),

-- Manager Quan (employee_id=7) - 2025
(7, 'ANNUAL_LEAVE', 2025, 12.0, 0.0, 'Phép năm 2025 - Khởi tạo'),

-- Nurse Linh (employee_id=8) - 2025 (Part-time flex)
(8, 'ANNUAL_LEAVE', 2025, 12.0, 0.0, 'Phép năm 2025 - Khởi tạo'),

-- Nurse Trang (employee_id=9) - 2025 (Part-time fixed)
(9, 'ANNUAL_LEAVE', 2025, 12.0, 0.0, 'Phép năm 2025 - Khởi tạo');

-- =============================================
-- SEED DATA CHO SERVICES & TREATMENT PLANS
-- =============================================

-- =============================================
-- BƯỚC 1: INSERT DỊCH VỤ (SERVICES)
-- =============================================
-- Giả định ID chuyên khoa:
-- NULL: Tổng quát (không cần chuyên khoa cụ thể)
-- 1: Chỉnh nha
-- 2: Implant
-- 3: Phục hình/Thẩm mỹ
-- 4: Tiểu phẫu
-- 5: Nội nha

INSERT INTO services (service_code, service_name, description, default_duration_minutes, default_buffer_minutes, price, specialization_id, is_active, created_at) VALUES
-- Nha khoa tổng quát (A)
('GEN_EXAM', 'Khám tổng quát & Tư vấn', 'Khám tổng quát, chụp X-quang phim nhỏ nếu cần thiết để chẩn đoán.', 30, 15, 100000, NULL, true, NOW()),
('GEN_XRAY_PERI', 'Chụp X-Quang quanh chóp', 'Chụp phim X-quang nhỏ tại ghế.', 10, 5, 50000, NULL, true, NOW()),
('SCALING_L1', 'Cạo vôi răng & Đánh bóng - Mức 1', 'Làm sạch vôi răng và mảng bám mức độ ít/trung bình.', 45, 15, 300000, NULL, true, NOW()),
('SCALING_L2', 'Cạo vôi răng & Đánh bóng - Mức 2', 'Làm sạch vôi răng và mảng bám mức độ nhiều.', 60, 15, 400000, NULL, true, NOW()),
('SCALING_VIP', 'Cạo vôi VIP không đau', 'Sử dụng máy rung siêu âm ít ê buốt.', 60, 15, 500000, NULL, true, NOW()),
('FILLING_COMP', 'Trám răng Composite', 'Trám răng sâu, mẻ bằng vật liệu composite thẩm mỹ.', 45, 15, 400000, NULL, true, NOW()),
('FILLING_GAP', 'Đắp kẽ răng thưa Composite', 'Đóng kẽ răng thưa nhỏ bằng composite.', 60, 15, 500000, 3, true, NOW()),
('EXTRACT_MILK', 'Nhổ răng sữa', 'Nhổ răng sữa cho trẻ em.', 15, 15, 50000, NULL, true, NOW()),
('EXTRACT_NORM', 'Nhổ răng thường', 'Nhổ răng vĩnh viễn đơn giản (không phải răng khôn).', 45, 15, 500000, NULL, true, NOW()),
('EXTRACT_WISDOM_L1', 'Nhổ răng khôn mức 1 (Dễ)', 'Tiểu phẫu nhổ răng khôn mọc thẳng, ít phức tạp.', 60, 30, 1500000, 4, true, NOW()),
('EXTRACT_WISDOM_L2', 'Nhổ răng khôn mức 2 (Khó)', 'Tiểu phẫu nhổ răng khôn mọc lệch, ngầm.', 90, 30, 2500000, 4, true, NOW()),
('ENDO_TREAT_ANT', 'Điều trị tủy răng trước', 'Lấy tủy, làm sạch, trám bít ống tủy cho răng cửa/răng nanh.', 60, 15, 1500000, 5, true, NOW()),
('ENDO_TREAT_POST', 'Điều trị tủy răng sau', 'Lấy tủy, làm sạch, trám bít ống tủy cho răng tiền cối/răng cối.', 75, 15, 2000000, 5, true, NOW()),
('ENDO_POST_CORE', 'Đóng chốt tái tạo cùi răng', 'Đặt chốt vào ống tủy đã chữa để tăng cường lưu giữ cho mão sứ.', 45, 15, 500000, NULL, true, NOW()),

-- Thẩm mỹ & Phục hình (B)
('BLEACH_ATHOME', 'Tẩy trắng răng tại nhà', 'Cung cấp máng và thuốc tẩy trắng tại nhà.', 30, 15, 800000, 3, true, NOW()),
('BLEACH_INOFFICE', 'Tẩy trắng răng tại phòng (Laser)', 'Tẩy trắng bằng đèn chiếu hoặc laser.', 90, 15, 1200000, 3, true, NOW()),

-- Răng sứ (Chia nhỏ từ Bảng 3)
('CROWN_PFM', 'Mão răng sứ Kim loại thường', 'Mão sứ sườn kim loại Cr-Co hoặc Ni-Cr.', 60, 15, 1000000, 3, true, NOW()),
('CROWN_TITAN', 'Mão răng sứ Titan', 'Mão sứ sườn hợp kim Titan.', 60, 15, 2500000, 3, true, NOW()),
('CROWN_ZIR_KATANA', 'Mão răng toàn sứ Katana/Zir HT', 'Mão sứ 100% Zirconia phổ thông.', 60, 15, 3500000, 3, true, NOW()),
('CROWN_ZIR_CERCON', 'Mão răng toàn sứ Cercon HT', 'Mão sứ 100% Zirconia cao cấp (Đức).', 60, 15, 5000000, 3, true, NOW()),
('CROWN_EMAX', 'Mão răng sứ thủy tinh Emax', 'Mão sứ Lithium Disilicate thẩm mỹ cao.', 60, 15, 6000000, 3, true, NOW()),
('CROWN_ZIR_LAVA', 'Mão răng toàn sứ Lava Plus', 'Mão sứ Zirconia đa lớp (Mỹ).', 60, 15, 8000000, 3, true, NOW()),
('VENEER_EMAX', 'Mặt dán sứ Veneer Emax', 'Mặt dán sứ Lithium Disilicate mài răng tối thiểu.', 75, 15, 6000000, 3, true, NOW()),
('VENEER_LISI', 'Mặt dán sứ Veneer Lisi Ultra', 'Mặt dán sứ Lithium Disilicate (Mỹ).', 75, 15, 8000000, 3, true, NOW()),
('INLAY_ONLAY_ZIR', 'Trám sứ Inlay/Onlay Zirconia', 'Miếng trám gián tiếp bằng sứ Zirconia CAD/CAM.', 60, 15, 2000000, 3, true, NOW()),
('INLAY_ONLAY_EMAX', 'Trám sứ Inlay/Onlay Emax', 'Miếng trám gián tiếp bằng sứ Emax Press.', 60, 15, 3000000, 3, true, NOW()),

-- Cắm ghép Implant (C)
('IMPL_CONSULT', 'Khám & Tư vấn Implant', 'Khám, đánh giá tình trạng xương, tư vấn kế hoạch.', 45, 15, 0, 2, true, NOW()),
('IMPL_CT_SCAN', 'Chụp CT Cone Beam (Implant)', 'Chụp phim 3D phục vụ cắm ghép Implant.', 30, 15, 500000, 2, true, NOW()),
('IMPL_SURGERY_KR', 'Phẫu thuật đặt trụ Implant Hàn Quốc', 'Phẫu thuật cắm trụ Implant (VD: Osstem, Biotem).', 90, 30, 15000000, 2, true, NOW()),
('IMPL_SURGERY_EUUS', 'Phẫu thuật đặt trụ Implant Thụy Sĩ/Mỹ', 'Phẫu thuật cắm trụ Implant (VD: Straumann, Nobel).', 90, 30, 25000000, 2, true, NOW()),
('IMPL_BONE_GRAFT', 'Ghép xương ổ răng', 'Phẫu thuật bổ sung xương cho vị trí cắm Implant.', 60, 30, 5000000, 2, true, NOW()),
('IMPL_SINUS_LIFT', 'Nâng xoang hàm (Hở/Kín)', 'Phẫu thuật nâng xoang để cắm Implant hàm trên.', 75, 30, 8000000, 2, true, NOW()),
('IMPL_HEALING', 'Gắn trụ lành thương (Healing Abutment)', 'Gắn trụ giúp nướu lành thương đúng hình dạng.', 20, 10, 500000, 2, true, NOW()),
('IMPL_IMPRESSION', 'Lấy dấu Implant', 'Lấy dấu để làm răng sứ trên Implant.', 30, 15, 0, 2, true, NOW()),
('IMPL_CROWN_TITAN', 'Mão sứ Titan trên Implant', 'Làm và gắn mão sứ Titan trên Abutment.', 45, 15, 3000000, 2, true, NOW()),
('IMPL_CROWN_ZIR', 'Mão sứ Zirconia trên Implant', 'Làm và gắn mão sứ Zirconia trên Abutment.', 45, 15, 5000000, 2, true, NOW()),

-- Chỉnh nha (D)
('ORTHO_CONSULT', 'Khám & Tư vấn Chỉnh nha', 'Khám, phân tích phim, tư vấn kế hoạch niềng.', 45, 15, 0, 1, true, NOW()),
('ORTHO_FILMS', 'Chụp Phim Chỉnh nha (Pano, Ceph)', 'Chụp phim X-quang Toàn cảnh và Sọ nghiêng.', 30, 15, 500000, 1, true, NOW()),
('ORTHO_BRACES_ON', 'Gắn mắc cài kim loại/sứ', 'Gắn bộ mắc cài lên răng.', 90, 30, 5000000, 1, true, NOW()),
('ORTHO_ADJUST', 'Tái khám Chỉnh nha / Siết niềng', 'Điều chỉnh dây cung, thay thun định kỳ.', 30, 15, 500000, 1, true, NOW()),
('ORTHO_INVIS_SCAN', 'Scan mẫu hàm Invisalign', 'Scan 3D mẫu hàm để gửi làm khay Invisalign.', 45, 15, 1000000, 1, true, NOW()),
('ORTHO_INVIS_ATTACH', 'Gắn Attachment Invisalign', 'Gắn các điểm tạo lực trên răng cho Invisalign.', 60, 15, 2000000, 1, true, NOW()),
('ORTHO_MINIVIS', 'Cắm Mini-vis Chỉnh nha', 'Phẫu thuật nhỏ cắm vít hỗ trợ niềng răng.', 45, 15, 1500000, 1, true, NOW()),
('ORTHO_BRACES_OFF', 'Tháo mắc cài & Vệ sinh', 'Tháo bỏ mắc cài sau khi kết thúc niềng.', 60, 15, 1000000, 1, true, NOW()),
('ORTHO_RETAINER_FIXED', 'Gắn hàm duy trì cố định', 'Dán dây duy trì mặt trong răng.', 30, 15, 1000000, 1, true, NOW()),
('ORTHO_RETAINER_REMOV', 'Làm hàm duy trì tháo lắp', 'Lấy dấu và giao hàm duy trì (máng trong/Hawley).', 30, 15, 1000000, 1, true, NOW()),

-- Dịch vụ Phục hình bổ sung (E)
('PROS_CEMENT', 'Gắn sứ / Thử sứ (Lần 2)', 'Hẹn lần 2 để thử và gắn vĩnh viễn mão sứ, cầu răng, veneer.', 30, 15, 0, 3, true, NOW()),
('DENTURE_CONSULT', 'Khám & Lấy dấu Hàm Tháo Lắp', 'Lấy dấu lần đầu để làm hàm giả tháo lắp.', 45, 15, 1000000, 3, true, NOW()),
('DENTURE_TRYIN', 'Thử sườn/Thử răng Hàm Tháo Lắp', 'Hẹn thử khung kim loại hoặc thử răng sáp.', 30, 15, 0, 3, true, NOW()),
('DENTURE_DELIVERY', 'Giao hàm & Chỉnh khớp cắn', 'Giao hàm hoàn thiện, chỉnh sửa các điểm vướng cộm.', 30, 15, 0, 3, true, NOW()),

-- Dịch vụ khác (F)
('OTHER_DIAMOND', 'Đính đá/kim cương lên răng', 'Gắn đá thẩm mỹ lên răng.', 30, 15, 300000, 3, true, NOW()),
('OTHER_GINGIVECTOMY', 'Phẫu thuật cắt nướu (thẩm mỹ)', 'Làm dài thân răng, điều trị cười hở lợi.', 60, 30, 1000000, 4, true, NOW()),
('EMERG_PAIN', 'Khám cấp cứu / Giảm đau', 'Khám và xử lý khẩn cấp các trường hợp đau nhức, sưng, chấn thương.', 30, 15, 150000, NULL, true, NOW()),
('SURG_CHECKUP', 'Tái khám sau phẫu thuật / Cắt chỉ', 'Kiểm tra vết thương sau nhổ răng khôn, cắm Implant, cắt nướu.', 15, 10, 0, 4, true, NOW())
ON CONFLICT (service_code) DO NOTHING;

-- ============================================
-- HƯỚNG DẪN SỬ DỤNG
-- ============================================
--
-- 1. LOGIN (DEFAULT PASSWORD = "123456"):
--   admin / nhasi1 / nhasi2 / letan / manager / benhnhan1
--
-- 2. THAY ĐỔI QUAN TRỌNG:
--   - ĐÃ XÓA: path fields từ permissions, base_roles, roles
--   - FE tự xử lý routing dựa trên groupedPermissions
--   - BE chỉ trả về groupedPermissions (grouped by module)
--
-- 3. MODULES ĐÃ MERGE (12 → 9):
--   CUSTOMER_MANAGEMENT (8) = CONTACT + CONTACT_HISTORY
--   SCHEDULE_MANAGEMENT (11) = WORK_SHIFTS + REGISTRATION + SHIFT_RENEWAL
--   LEAVE_MANAGEMENT (18) = TIME_OFF + OVERTIME + TIME_OFF_MANAGEMENT
--   SYSTEM_CONFIGURATION (12) = ROLE + PERMISSION + SPECIALIZATION
--
-- 4. EMAIL VERIFICATION:
--   - Seeded accounts: ACTIVE (skip verification)
--   - New accounts via API: PENDING_VERIFICATION (require email)
--   - Default password: 123456 (must change on first login)
--
-- ============================================
