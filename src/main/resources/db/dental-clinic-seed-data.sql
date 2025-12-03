-- ============================================
-- HỆ THỐNG QUẢN LÝ PHÒNG KHÁM NHA KHOA
-- Dental Clinic Management System - Seed Data V6
-- ============================================
-- NOTE:
-- - This is the ONLY SQL file used in the project
-- - Contains: ENUM types + Initial seed data (INSERT statements)
-- - Tables are automatically created by Hibernate (ddl-auto: update)
-- - This file runs AFTER Hibernate creates schema (defer-datasource-initialization: true)
-- - ENUMs MUST be created in this file to survive database drops
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;


-- ============================================
-- END ENUM TYPE DEFINITIONS (38 types total)
-- ============================================

INSERT INTO roles (role_id, role_name, description, requires_specialization, is_active, created_at)
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
('ROLE_DENTIST', 'ROLE_DENTIST', 2, 'Bác sĩ nha khoa - Khám và điều trị bệnh nhân', TRUE, TRUE, NOW()),
('ROLE_NURSE', 'ROLE_NURSE', 2, 'Y tá - Hỗ trợ điều trị và chăm sóc bệnh nhân', TRUE, TRUE, NOW()),
('ROLE_RECEPTIONIST', 'ROLE_RECEPTIONIST', 2, 'Lễ tân - Tiếp đón và quản lý lịch hẹn', FALSE, TRUE, NOW()),
('ROLE_ACCOUNTANT', 'ROLE_ACCOUNTANT', 2, 'Kế toán - Quản lý tài chính và thanh toán', FALSE, TRUE, NOW()),
('ROLE_INVENTORY_MANAGER', 'ROLE_INVENTORY_MANAGER', 2, 'Quản lý kho - Quản lý vật tư và thuốc men', FALSE, TRUE, NOW()),
('ROLE_MANAGER', 'ROLE_MANAGER', 2, 'Quản lý - Quản lý vận hành và nhân sự', FALSE, TRUE, NOW()),
('ROLE_DENTIST_INTERN', 'ROLE_DENTIST_INTERN', 2, 'Thực tập sinh nha khoa', FALSE, TRUE, NOW()),

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
('VIEW_APPOINTMENT', 'VIEW_APPOINTMENT', 'APPOINTMENT', 'Xem danh sách lịch hẹn (deprecated - use VIEW_APPOINTMENT_ALL or VIEW_APPOINTMENT_OWN)', 50, NULL, TRUE, NOW()),
-- NEW: RBAC-compliant permissions (P3.3)
('VIEW_APPOINTMENT_ALL', 'VIEW_APPOINTMENT_ALL', 'APPOINTMENT', 'Xem TẤT CẢ lịch hẹn (Lễ tân/Quản lý)', 51, NULL, TRUE, NOW()),
('VIEW_APPOINTMENT_OWN', 'VIEW_APPOINTMENT_OWN', 'APPOINTMENT', 'Chỉ xem lịch hẹn LIÊN QUAN (Bác sĩ/Y tá/Observer/Bệnh nhân)', 52, 'VIEW_APPOINTMENT_ALL', TRUE, NOW()),
('CREATE_APPOINTMENT', 'CREATE_APPOINTMENT', 'APPOINTMENT', 'Đặt lịch hẹn mới', 53, NULL, TRUE, NOW()),
('UPDATE_APPOINTMENT', 'UPDATE_APPOINTMENT', 'APPOINTMENT', 'Cập nhật lịch hẹn', 54, NULL, TRUE, NOW()),
('UPDATE_APPOINTMENT_STATUS', 'UPDATE_APPOINTMENT_STATUS', 'APPOINTMENT', 'Cập nhật trạng thái lịch hẹn (Check-in, In-progress, Completed, Cancelled) - API 3.5', 55, NULL, TRUE, NOW()),
('DELAY_APPOINTMENT', 'DELAY_APPOINTMENT', 'APPOINTMENT', 'Hoãn lịch hẹn sang thời gian khác (chỉ SCHEDULED/CHECKED_IN) - API 3.6', 56, NULL, TRUE, NOW()),
('CANCEL_APPOINTMENT', 'CANCEL_APPOINTMENT', 'APPOINTMENT', 'Hủy lịch hẹn', 57, NULL, TRUE, NOW()),
('DELETE_APPOINTMENT', 'DELETE_APPOINTMENT', 'APPOINTMENT', 'Xóa lịch hẹn', 58, NULL, TRUE, NOW())
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
-- Part-time registration approval (BE-403)
('MANAGE_PART_TIME_REGISTRATIONS', 'MANAGE_PART_TIME_REGISTRATIONS', 'SCHEDULE_MANAGEMENT', 'Duyệt/từ chối đăng ký part-time', 86, NULL, TRUE, NOW()),
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


-- MODULE 13: TREATMENT_PLAN (Quản lý phác đồ điều trị bệnh nhân)
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
-- Treatment plan management (RBAC: ALL vs OWN pattern)
('VIEW_TREATMENT_PLAN_ALL', 'VIEW_TREATMENT_PLAN_ALL', 'TREATMENT_PLAN', 'Xem TẤT CẢ phác đồ điều trị (Bác sĩ/Lễ tân)', 260, NULL, TRUE, NOW()),
('VIEW_ALL_TREATMENT_PLANS', 'VIEW_ALL_TREATMENT_PLANS', 'TREATMENT_PLAN', 'Xem danh sách lộ trình toàn hệ thống (Manager)', 261, NULL, TRUE, NOW()),
('VIEW_TREATMENT_PLAN_OWN', 'VIEW_TREATMENT_PLAN_OWN', 'TREATMENT_PLAN', 'Chỉ xem phác đồ điều trị của bản thân (Bệnh nhân)', 262, 'VIEW_TREATMENT_PLAN_ALL', TRUE, NOW()),
('CREATE_TREATMENT_PLAN', 'CREATE_TREATMENT_PLAN', 'TREATMENT_PLAN', 'Tạo phác đồ điều trị mới', 263, NULL, TRUE, NOW()),
('UPDATE_TREATMENT_PLAN', 'UPDATE_TREATMENT_PLAN', 'TREATMENT_PLAN', 'Cập nhật phác đồ điều trị', 264, NULL, TRUE, NOW()),
('DELETE_TREATMENT_PLAN', 'DELETE_TREATMENT_PLAN', 'TREATMENT_PLAN', 'Vô hiệu hóa phác đồ (soft delete)', 265, NULL, TRUE, NOW()),
('APPROVE_TREATMENT_PLAN', 'APPROVE_TREATMENT_PLAN', 'TREATMENT_PLAN', 'Duyệt/Từ chối lộ trình điều trị', 266, NULL, TRUE, NOW()),
('MANAGE_PLAN_PRICING', 'MANAGE_PLAN_PRICING', 'TREATMENT_PLAN', 'Điều chỉnh giá/chiết khấu phác đồ điều trị', 267, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;


-- MODULE 14: WAREHOUSE (Quản lý kho vật tư API 6.6, 6.7, 6.9)
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES
('VIEW_ITEMS', 'VIEW_ITEMS', 'WAREHOUSE', 'Xem danh sách vật tư (cho Bác sĩ/Lễ tân)', 269, NULL, TRUE, NOW()),
('VIEW_MEDICINES', 'VIEW_MEDICINES', 'WAREHOUSE', 'Xem và tìm kiếm thuốc men (chỉ MEDICINE category) - Cho Bác sĩ kê đơn - API 6.1.1', 268, NULL, TRUE, NOW()),
('VIEW_WAREHOUSE', 'VIEW_WAREHOUSE', 'WAREHOUSE', 'Xem danh sách giao dịch kho', 270, NULL, TRUE, NOW()),
('CREATE_ITEMS', 'CREATE_ITEMS', 'WAREHOUSE', 'Tạo vật tư mới với hệ thống đơn vị', 271, NULL, TRUE, NOW()),
('UPDATE_ITEMS', 'UPDATE_ITEMS', 'WAREHOUSE', 'Cập nhật thông tin vật tư và đơn vị tính', 272, NULL, TRUE, NOW()),
('CREATE_WAREHOUSE', 'CREATE_WAREHOUSE', 'WAREHOUSE', 'Tạo danh mục, nhà cung cấp', 273, NULL, TRUE, NOW()),
('UPDATE_WAREHOUSE', 'UPDATE_WAREHOUSE', 'WAREHOUSE', 'Cập nhật danh mục, nhà cung cấp', 274, NULL, TRUE, NOW()),
('DELETE_WAREHOUSE', 'DELETE_WAREHOUSE', 'WAREHOUSE', 'Xóa vật tư, danh mục, nhà cung cấp', 275, NULL, TRUE, NOW()),
('VIEW_WAREHOUSE_COST', 'VIEW_WAREHOUSE_COST', 'WAREHOUSE', 'Xem giá tiền kho (unitCost, totalValue, totalCost) - Chỉ Admin/Kế toán', 276, NULL, TRUE, NOW()),
('IMPORT_ITEMS', 'IMPORT_ITEMS', 'WAREHOUSE', 'Tạo phiếu nhập kho', 277, NULL, TRUE, NOW()),
('EXPORT_ITEMS', 'EXPORT_ITEMS', 'WAREHOUSE', 'Tạo phiếu xuất kho', 278, NULL, TRUE, NOW()),
('DISPOSE_ITEMS', 'DISPOSE_ITEMS', 'WAREHOUSE', 'Tạo phiếu thanh lý', 279, NULL, TRUE, NOW()),
('APPROVE_TRANSACTION', 'APPROVE_TRANSACTION', 'WAREHOUSE', 'Duyệt/Từ chối phiếu nhập xuất kho', 280, NULL, TRUE, NOW()),
('CANCEL_WAREHOUSE', 'CANCEL_WAREHOUSE', 'WAREHOUSE', 'Hủy phiếu nhập xuất kho (API 6.6.3)', 281, NULL, TRUE, NOW()),
('MANAGE_SUPPLIERS', 'MANAGE_SUPPLIERS', 'WAREHOUSE', 'Quản lý nhà cung cấp (API 6.13, 6.14)', 282, NULL, TRUE, NOW()),
('MANAGE_CONSUMABLES', 'MANAGE_CONSUMABLES', 'WAREHOUSE', 'Quản lý định mức tiêu hao vật tư (BOM) - API 6.18, 6.19', 283, NULL, TRUE, NOW()),
('MANAGE_WAREHOUSE', 'MANAGE_WAREHOUSE', 'WAREHOUSE', 'Toàn quyền quản lý kho', 284, NULL, TRUE, NOW()),
('WRITE_CLINICAL_RECORD', 'WRITE_CLINICAL_RECORD', 'CLINICAL_RECORDS', 'Tạo và cập nhật bệnh án, thêm thủ thuật (API 8.5, 9.2, 9.3)', 285, NULL, TRUE, NOW()),
('UPLOAD_ATTACHMENT', 'UPLOAD_ATTACHMENT', 'CLINICAL_RECORDS', 'Upload file đính kèm vào bệnh án (X-quang, ảnh, PDF) - API 8.11', 286, NULL, TRUE, NOW()),
('VIEW_ATTACHMENT', 'VIEW_ATTACHMENT', 'CLINICAL_RECORDS', 'Xem danh sách file đính kèm của bệnh án - API 8.12', 287, NULL, TRUE, NOW()),
('DELETE_ATTACHMENT', 'DELETE_ATTACHMENT', 'CLINICAL_RECORDS', 'Xóa file đính kèm (chỉ Admin hoặc người upload) - API 8.13', 288, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;


-- ============================================
-- BƯỚC 4: PHÂN QUYỀN CHO CÁC VAI TRÒ
-- ============================================

-- Admin có TẤT CẢ quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'ROLE_ADMIN', permission_id FROM permissions WHERE is_active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Dentist (Fix: ROLE_DENTIST → ROLE_DENTIST)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'VIEW_PATIENT'), ('ROLE_DENTIST', 'UPDATE_PATIENT'),
('ROLE_DENTIST', 'VIEW_TREATMENT'), ('ROLE_DENTIST', 'CREATE_TREATMENT'), ('ROLE_DENTIST', 'UPDATE_TREATMENT'),
('ROLE_DENTIST', 'VIEW_APPOINTMENT'), -- Deprecated
('ROLE_DENTIST', 'VIEW_APPOINTMENT_OWN'), -- NEW: Only see own appointments
('ROLE_DENTIST', 'UPDATE_APPOINTMENT_STATUS'), -- NEW API 3.5: Start, Complete treatment
('ROLE_DENTIST', 'DELAY_APPOINTMENT'), -- NEW API 3.6: Delay appointment when needed
('ROLE_DENTIST', 'VIEW_REGISTRATION_OWN'), ('ROLE_DENTIST', 'VIEW_RENEWAL_OWN'), ('ROLE_DENTIST', 'RESPOND_RENEWAL_OWN'),
('ROLE_DENTIST', 'CREATE_REGISTRATION'),
('ROLE_DENTIST', 'VIEW_LEAVE_OWN'), ('ROLE_DENTIST', 'CREATE_TIME_OFF'), ('ROLE_DENTIST', 'CREATE_OVERTIME'),
('ROLE_DENTIST', 'CANCEL_TIME_OFF_OWN'), ('ROLE_DENTIST', 'CANCEL_OVERTIME_OWN'),
('ROLE_DENTIST', 'VIEW_HOLIDAY'),
-- Treatment Plan permissions
('ROLE_DENTIST', 'VIEW_TREATMENT_PLAN_OWN'),
('ROLE_DENTIST', 'CREATE_TREATMENT_PLAN'),
('ROLE_DENTIST', 'UPDATE_TREATMENT_PLAN'),
('ROLE_DENTIST', 'DELETE_TREATMENT_PLAN'),
('ROLE_DENTIST', 'VIEW_SERVICE'),
('ROLE_DENTIST', 'VIEW_ITEMS'),
('ROLE_DENTIST', 'VIEW_MEDICINES'),
('ROLE_DENTIST', 'WRITE_CLINICAL_RECORD'),
('ROLE_DENTIST', 'UPLOAD_ATTACHMENT'),
('ROLE_DENTIST', 'VIEW_ATTACHMENT'),
('ROLE_DENTIST', 'DELETE_ATTACHMENT')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Nurse
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_NURSE', 'VIEW_PATIENT'), ('ROLE_NURSE', 'VIEW_TREATMENT'),
('ROLE_NURSE', 'VIEW_APPOINTMENT'), -- Deprecated
('ROLE_NURSE', 'VIEW_APPOINTMENT_OWN'), -- NEW: Only see participating appointments
('ROLE_NURSE', 'UPDATE_APPOINTMENT_STATUS'), -- NEW API 3.5: Help check-in patients
('ROLE_NURSE', 'VIEW_REGISTRATION_OWN'), ('ROLE_NURSE', 'VIEW_RENEWAL_OWN'), ('ROLE_NURSE', 'RESPOND_RENEWAL_OWN'),
('ROLE_NURSE', 'CREATE_REGISTRATION'),
('ROLE_NURSE', 'VIEW_LEAVE_OWN'), ('ROLE_NURSE', 'CREATE_TIME_OFF'), ('ROLE_NURSE', 'CREATE_OVERTIME'),
('ROLE_NURSE', 'CANCEL_TIME_OFF_OWN'), ('ROLE_NURSE', 'CANCEL_OVERTIME_OWN'),
('ROLE_NURSE', 'VIEW_HOLIDAY'),
('ROLE_NURSE', 'VIEW_ATTACHMENT')
ON CONFLICT (role_id, permission_id) DO NOTHING;


INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST_INTERN', 'VIEW_APPOINTMENT_OWN'), -- Chỉ thấy appointments họ tham gia
('ROLE_DENTIST_INTERN', 'VIEW_PATIENT'), -- Chỉ xem thông tin cơ bản bệnh nhân (không có medical history)
('ROLE_DENTIST_INTERN', 'VIEW_REGISTRATION_OWN'), -- Xem ca làm của mình
('ROLE_DENTIST_INTERN', 'CREATE_REGISTRATION'), -- Đăng ký ca làm
('ROLE_DENTIST_INTERN', 'VIEW_LEAVE_OWN'), -- Xem nghỉ phép của mình
('ROLE_DENTIST_INTERN', 'CREATE_TIME_OFF'), -- Tạo đơn xin nghỉ
('ROLE_DENTIST_INTERN', 'CANCEL_TIME_OFF_OWN'), -- Hủy đơn nghỉ của mình
('ROLE_DENTIST_INTERN', 'VIEW_HOLIDAY') -- Xem lịch nghỉ lễ
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Receptionist
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_RECEPTIONIST', 'VIEW_PATIENT'), ('ROLE_RECEPTIONIST', 'CREATE_PATIENT'), ('ROLE_RECEPTIONIST', 'UPDATE_PATIENT'),
('ROLE_RECEPTIONIST', 'VIEW_APPOINTMENT'), -- Deprecated
('ROLE_RECEPTIONIST', 'VIEW_APPOINTMENT_ALL'), -- NEW: Xem TẤT CẢ lịch hẹn
('ROLE_RECEPTIONIST', 'CREATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'UPDATE_APPOINTMENT'),
('ROLE_RECEPTIONIST', 'UPDATE_APPOINTMENT_STATUS'), -- NEW API 3.5: Check-in, In-progress, Complete
('ROLE_RECEPTIONIST', 'DELAY_APPOINTMENT'), -- NEW API 3.6: Delay appointment for patients
('ROLE_RECEPTIONIST', 'DELETE_APPOINTMENT'),
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
('ROLE_RECEPTIONIST', 'VIEW_HOLIDAY'),
('ROLE_RECEPTIONIST', 'VIEW_TREATMENT_PLAN_ALL'),
('ROLE_RECEPTIONIST', 'VIEW_WAREHOUSE'),
('ROLE_RECEPTIONIST', 'VIEW_ITEMS')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Manager (Full management permissions)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_MANAGER', 'VIEW_EMPLOYEE'), ('ROLE_MANAGER', 'CREATE_EMPLOYEE'),
('ROLE_MANAGER', 'UPDATE_EMPLOYEE'), ('ROLE_MANAGER', 'DELETE_EMPLOYEE'),
('ROLE_MANAGER', 'VIEW_PATIENT'), ('ROLE_MANAGER', 'VIEW_APPOINTMENT'),
('ROLE_MANAGER', 'VIEW_APPOINTMENT_ALL'), -- See all appointments
('ROLE_MANAGER', 'UPDATE_APPOINTMENT_STATUS'), -- NEW API 3.5: Full appointment status control
('ROLE_MANAGER', 'DELAY_APPOINTMENT'), -- NEW API 3.6: Reschedule appointments
-- CUSTOMER_MANAGEMENT
('ROLE_MANAGER', 'VIEW_CONTACT'), ('ROLE_MANAGER', 'CREATE_CONTACT'),
('ROLE_MANAGER', 'UPDATE_CONTACT'), ('ROLE_MANAGER', 'DELETE_CONTACT'),
('ROLE_MANAGER', 'VIEW_CONTACT_HISTORY'), ('ROLE_MANAGER', 'CREATE_CONTACT_HISTORY'),
('ROLE_MANAGER', 'UPDATE_CONTACT_HISTORY'), ('ROLE_MANAGER', 'DELETE_CONTACT_HISTORY'),
-- SCHEDULE_MANAGEMENT (full)
('ROLE_MANAGER', 'VIEW_WORK_SHIFTS'), ('ROLE_MANAGER', 'CREATE_WORK_SHIFTS'),
('ROLE_MANAGER', 'UPDATE_WORK_SHIFTS'), ('ROLE_MANAGER', 'DELETE_WORK_SHIFTS'),
('ROLE_MANAGER', 'MANAGE_WORK_SLOTS'), ('ROLE_MANAGER', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_MANAGER', 'MANAGE_PART_TIME_REGISTRATIONS'),
('ROLE_MANAGER', 'VIEW_REGISTRATION_ALL'), ('ROLE_MANAGER', 'CREATE_REGISTRATION'),
('ROLE_MANAGER', 'UPDATE_REGISTRATION'), ('ROLE_MANAGER', 'DELETE_REGISTRATION'),
('ROLE_MANAGER', 'UPDATE_REGISTRATIONS_ALL'), ('ROLE_MANAGER', 'CANCEL_REGISTRATION_OWN'),
-- Employee shift management (BE-302)
('ROLE_MANAGER', 'VIEW_SHIFTS_ALL'), ('ROLE_MANAGER', 'VIEW_SHIFTS_SUMMARY'),
('ROLE_MANAGER', 'CREATE_SHIFTS'), ('ROLE_MANAGER', 'UPDATE_SHIFTS'), ('ROLE_MANAGER', 'DELETE_SHIFTS'),
-- Fixed shift registration management (BE-307 V2)
('ROLE_MANAGER', 'MANAGE_FIXED_REGISTRATIONS'), ('ROLE_MANAGER', 'VIEW_FIXED_REGISTRATIONS_ALL'),
-- Shift renewal management (P7)
('ROLE_MANAGER', 'VIEW_RENEWAL_OWN'), ('ROLE_MANAGER', 'RESPOND_RENEWAL_OWN'),
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
('ROLE_MANAGER', 'UPDATE_SERVICE'), ('ROLE_MANAGER', 'DELETE_SERVICE'),
-- TREATMENT_PLAN (V19/V20/V21: Full management of treatment plans)
('ROLE_MANAGER', 'VIEW_TREATMENT_PLAN_ALL'), -- Can view all patients' treatment plans
('ROLE_MANAGER', 'VIEW_ALL_TREATMENT_PLANS'), -- V21: Can view system-wide treatment plan list
('ROLE_MANAGER', 'CREATE_TREATMENT_PLAN'), -- Can create treatment plans
('ROLE_MANAGER', 'UPDATE_TREATMENT_PLAN'), -- Can update treatment plans
('ROLE_MANAGER', 'DELETE_TREATMENT_PLAN'), -- Can delete treatment plans
('ROLE_MANAGER', 'APPROVE_TREATMENT_PLAN'), -- V20: Can approve/reject treatment plans (API 5.9)
('ROLE_MANAGER', 'MANAGE_PLAN_PRICING'), -- V21: Can adjust pricing/discounts on treatment plans
('ROLE_MANAGER', 'VIEW_WAREHOUSE'),
('ROLE_MANAGER', 'VIEW_WAREHOUSE_COST'), -- Can view cost/price fields in warehouse APIs
('ROLE_MANAGER', 'VIEW_ITEMS'),
('ROLE_MANAGER', 'IMPORT_ITEMS'),
('ROLE_MANAGER', 'EXPORT_ITEMS'),
('ROLE_MANAGER', 'APPROVE_TRANSACTION'),
('ROLE_MANAGER', 'CANCEL_WAREHOUSE'), -- Can cancel import/export transactions (API 6.6.3)
('ROLE_MANAGER', 'MANAGE_SUPPLIERS'), -- V28: Can manage suppliers (API 6.13, 6.14)
('ROLE_MANAGER', 'MANAGE_CONSUMABLES'), -- V30: Can manage service consumables BOM (API 6.18, 6.19)
('ROLE_MANAGER', 'MANAGE_WAREHOUSE') -- V28: Full warehouse management authority
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Accountant & Inventory Manager (LEAVE only)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_ACCOUNTANT', 'VIEW_LEAVE_OWN'), ('ROLE_ACCOUNTANT', 'CREATE_TIME_OFF'), ('ROLE_ACCOUNTANT', 'CREATE_OVERTIME'),
('ROLE_ACCOUNTANT', 'CANCEL_TIME_OFF_OWN'), ('ROLE_ACCOUNTANT', 'CANCEL_OVERTIME_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_HOLIDAY'),
-- TREATMENT_PLAN (V21: Accountant can adjust pricing - API 5.x)
('ROLE_ACCOUNTANT', 'VIEW_TREATMENT_PLAN_ALL'), -- Can view all treatment plans
('ROLE_ACCOUNTANT', 'MANAGE_PLAN_PRICING'), -- Can adjust pricing/discounts
-- WAREHOUSE (V22: Accountant can view transactions and financial data - API 6.6)
('ROLE_ACCOUNTANT', 'VIEW_WAREHOUSE'), -- Can view transaction history
('ROLE_ACCOUNTANT', 'VIEW_WAREHOUSE_COST'), -- Can view financial data (cost, payment info)
('ROLE_INVENTORY_MANAGER', 'VIEW_LEAVE_OWN'), ('ROLE_INVENTORY_MANAGER', 'CREATE_TIME_OFF'), ('ROLE_INVENTORY_MANAGER', 'CREATE_OVERTIME'),
('ROLE_INVENTORY_MANAGER', 'CANCEL_TIME_OFF_OWN'), ('ROLE_INVENTORY_MANAGER', 'CANCEL_OVERTIME_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_HOLIDAY'),
-- WAREHOUSE (V28: Full warehouse management - API 6.6, 6.9, 6.10, 6.11, 6.13, 6.14)
('ROLE_INVENTORY_MANAGER', 'VIEW_ITEMS'), -- Can view item list and units (API 6.8, 6.11)
('ROLE_INVENTORY_MANAGER', 'VIEW_WAREHOUSE'), -- Can view transaction history
('ROLE_INVENTORY_MANAGER', 'CREATE_ITEMS'), -- Can create item masters (API 6.9)
('ROLE_INVENTORY_MANAGER', 'UPDATE_ITEMS'), -- Can update item masters (API 6.10)
('ROLE_INVENTORY_MANAGER', 'CREATE_WAREHOUSE'), -- Can create categories/suppliers
('ROLE_INVENTORY_MANAGER', 'UPDATE_WAREHOUSE'), -- Can update items/categories/suppliers
('ROLE_INVENTORY_MANAGER', 'DELETE_WAREHOUSE'), -- Can delete items/categories/suppliers
-- REMOVED VIEW_WAREHOUSE_COST: Inventory Manager only sees quantities, NOT prices
('ROLE_INVENTORY_MANAGER', 'IMPORT_ITEMS'), -- Can create import transactions
('ROLE_INVENTORY_MANAGER', 'EXPORT_ITEMS'), -- Can create export transactions
('ROLE_INVENTORY_MANAGER', 'DISPOSE_ITEMS'), -- Can create disposal transactions
('ROLE_INVENTORY_MANAGER', 'APPROVE_TRANSACTION'), -- Can approve transactions
('ROLE_INVENTORY_MANAGER', 'CANCEL_WAREHOUSE'), -- Can cancel import/export transactions (API 6.6.3)
('ROLE_INVENTORY_MANAGER', 'MANAGE_SUPPLIERS'), -- Can manage suppliers (API 6.13, 6.14)
('ROLE_INVENTORY_MANAGER', 'MANAGE_WAREHOUSE') -- Full warehouse management authority
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Patient (basic view only)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_PATIENT', 'VIEW_PATIENT'), ('ROLE_PATIENT', 'VIEW_TREATMENT'),
('ROLE_PATIENT', 'VIEW_APPOINTMENT'), -- Deprecated (use VIEW_APPOINTMENT_OWN)
('ROLE_PATIENT', 'VIEW_APPOINTMENT_OWN'), -- NEW: Patient can view their own appointments
('ROLE_PATIENT', 'CREATE_APPOINTMENT'),
-- NEW: Treatment Plan permissions
('ROLE_PATIENT', 'VIEW_TREATMENT_PLAN_OWN'), -- Can only view their own treatment plans
('ROLE_PATIENT', 'VIEW_ATTACHMENT') -- Can view attachments of own clinical records
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant basic Overtime permissions to all employee roles (idempotent)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'VIEW_OT_OWN'), ('ROLE_DENTIST', 'CREATE_OT'), ('ROLE_DENTIST', 'CANCEL_OT_OWN'),
('ROLE_NURSE', 'VIEW_OT_OWN'), ('ROLE_NURSE', 'CREATE_OT'), ('ROLE_NURSE', 'CANCEL_OT_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_OT_OWN'), ('ROLE_RECEPTIONIST', 'CREATE_OT'), ('ROLE_RECEPTIONIST', 'CANCEL_OT_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_OT_OWN'), ('ROLE_ACCOUNTANT', 'CREATE_OT'), ('ROLE_ACCOUNTANT', 'CANCEL_OT_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_OT_OWN'), ('ROLE_INVENTORY_MANAGER', 'CREATE_OT'), ('ROLE_INVENTORY_MANAGER', 'CANCEL_OT_OWN'),
('ROLE_MANAGER', 'VIEW_OT_OWN'), ('ROLE_MANAGER', 'CREATE_OT'), ('ROLE_MANAGER', 'CANCEL_OT_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant VIEW_WORK_SHIFTS to all employee roles (idempotent)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'VIEW_WORK_SHIFTS'),
('ROLE_NURSE', 'VIEW_WORK_SHIFTS'),
('ROLE_RECEPTIONIST', 'VIEW_WORK_SHIFTS'),
('ROLE_ACCOUNTANT', 'VIEW_WORK_SHIFTS'),
('ROLE_INVENTORY_MANAGER', 'VIEW_WORK_SHIFTS')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant VIEW_SHIFTS_OWN to all employee roles (BE-307)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'VIEW_SHIFTS_OWN'),
('ROLE_NURSE', 'VIEW_SHIFTS_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_SHIFTS_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_SHIFTS_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_SHIFTS_OWN'),
('ROLE_MANAGER', 'VIEW_SHIFTS_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant CREATE_REGISTRATION to all employee roles (idempotent) - Allow self shift registration
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'CREATE_REGISTRATION'),
('ROLE_NURSE', 'CREATE_REGISTRATION'),
('ROLE_RECEPTIONIST', 'CREATE_REGISTRATION'),
('ROLE_ACCOUNTANT', 'CREATE_REGISTRATION'),
('ROLE_INVENTORY_MANAGER', 'CREATE_REGISTRATION')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant VIEW_AVAILABLE_SLOTS to all employee roles (BE-307 V2) - Allow viewing available part-time slots
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_NURSE', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_RECEPTIONIST', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_ACCOUNTANT', 'VIEW_AVAILABLE_SLOTS'),
('ROLE_INVENTORY_MANAGER', 'VIEW_AVAILABLE_SLOTS')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant CANCEL_REGISTRATION_OWN to all employee roles (BE-307 V2) - Allow canceling own registrations
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'CANCEL_REGISTRATION_OWN'),
('ROLE_NURSE', 'CANCEL_REGISTRATION_OWN'),
('ROLE_RECEPTIONIST', 'CANCEL_REGISTRATION_OWN'),
('ROLE_ACCOUNTANT', 'CANCEL_REGISTRATION_OWN'),
('ROLE_INVENTORY_MANAGER', 'CANCEL_REGISTRATION_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant VIEW_TIMEOFF_OWN to all employee roles (idempotent)
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'VIEW_TIMEOFF_OWN'),
('ROLE_NURSE', 'VIEW_TIMEOFF_OWN'),
('ROLE_RECEPTIONIST', 'VIEW_TIMEOFF_OWN'),
('ROLE_ACCOUNTANT', 'VIEW_TIMEOFF_OWN'),
('ROLE_INVENTORY_MANAGER', 'VIEW_TIMEOFF_OWN'),
('ROLE_MANAGER', 'VIEW_TIMEOFF_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant UPDATE_REGISTRATION_OWN to all employee roles (idempotent) - Allow employees to edit their own shifts
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'UPDATE_REGISTRATION_OWN'),
('ROLE_NURSE', 'UPDATE_REGISTRATION_OWN'),
('ROLE_RECEPTIONIST', 'UPDATE_REGISTRATION_OWN'),
('ROLE_ACCOUNTANT', 'UPDATE_REGISTRATION_OWN'),
('ROLE_INVENTORY_MANAGER', 'UPDATE_REGISTRATION_OWN'),
('ROLE_MANAGER', 'UPDATE_REGISTRATION_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant DELETE_REGISTRATION_OWN to all employee roles (idempotent) - Allow employees to delete their own shifts
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'DELETE_REGISTRATION_OWN'),
('ROLE_NURSE', 'DELETE_REGISTRATION_OWN'),
('ROLE_RECEPTIONIST', 'DELETE_REGISTRATION_OWN'),
('ROLE_ACCOUNTANT', 'DELETE_REGISTRATION_OWN'),
('ROLE_INVENTORY_MANAGER', 'DELETE_REGISTRATION_OWN'),
('ROLE_MANAGER', 'DELETE_REGISTRATION_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant CREATE_TIMEOFF to all employee roles (idempotent) - Allow all employees to request time-off
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'CREATE_TIMEOFF'),
('ROLE_NURSE', 'CREATE_TIMEOFF'),
('ROLE_RECEPTIONIST', 'CREATE_TIMEOFF'),
('ROLE_ACCOUNTANT', 'CREATE_TIMEOFF'),
('ROLE_INVENTORY_MANAGER', 'CREATE_TIMEOFF'),
('ROLE_MANAGER', 'CREATE_TIMEOFF')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant CANCEL_TIMEOFF_OWN to all employee roles (idempotent) - Allow employees to cancel their own time-off requests
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'CANCEL_TIMEOFF_OWN'),
('ROLE_NURSE', 'CANCEL_TIMEOFF_OWN'),
('ROLE_RECEPTIONIST', 'CANCEL_TIMEOFF_OWN'),
('ROLE_ACCOUNTANT', 'CANCEL_TIMEOFF_OWN'),
('ROLE_INVENTORY_MANAGER', 'CANCEL_TIMEOFF_OWN'),
('ROLE_MANAGER', 'CANCEL_TIMEOFF_OWN')
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- Grant VIEW_FIXED_REGISTRATIONS_OWN to all employee roles (BE-307 V2) - Allow viewing own fixed registrations
INSERT INTO role_permissions (role_id, permission_id)
VALUES
('ROLE_DENTIST', 'VIEW_FIXED_REGISTRATIONS_OWN'),
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
(7, 'SPEC007', 'Răng thẩm mỹ', 'Cosmetic Dentistry - Tẩy trắng, bọc sứ', TRUE, NOW()),
(8, 'SPEC-STANDARD', 'STANDARD - Y tế cơ bản', 'Baseline medical qualification - Required for all doctors/nurses', TRUE, NOW()),
(9, 'SPEC-INTERN', 'Thực tập sinh', 'Intern/Trainee - Nhân viên đang đào tạo, học việc', TRUE, NOW())
ON CONFLICT (specialization_id) DO NOTHING;


-- ============================================
-- BƯỚC 6: TẠO TÀI KHOẢN - STATUS = ACTIVE (SKIP VERIFICATION)
-- ============================================
-- Seeded accounts = ACTIVE (demo data, skip email verification)
-- New accounts created via API = PENDING_VERIFICATION (require email)
-- Default password: "123456" (BCrypt encoded)
-- ============================================

INSERT INTO accounts (account_id, account_code, username, email, password, role_id, status, is_email_verified, created_at)
VALUES
-- Dentists (Nha sĩ)
-- EMP001 - Lê Anh Khoa - FULL_TIME (Cả sáng 08:00-12:00 và chiều 13:00-17:00)
(1, 'ACC001', 'bacsi1', 'khoa.la@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_DENTIST', 'ACTIVE', TRUE, NOW()),

-- EMP002 - Trịnh Công Thái - FULL_TIME (Cả sáng 08:00-12:00 và chiều 13:00-17:00)
(2, 'ACC002', 'bacsi2', 'thai.tc@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_DENTIST', 'ACTIVE', TRUE, NOW()),

-- EMP003 - Jimmy Donaldson - PART_TIME_FLEX (Chỉ sáng 08:00-12:00)
(3, 'ACC003', 'bacsi3', 'jimmy.d@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_DENTIST', 'ACTIVE', TRUE, NOW()),

-- EMP004 - Junya Ota - PART_TIME_FIXED (Chỉ chiều 13:00-17:00)
(4, 'ACC004', 'bacsi4', 'junya.o@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_DENTIST', 'ACTIVE', TRUE, NOW()),

-- Staff
-- EMP005 - Đỗ Khánh Thuận - Lễ tân - FULL_TIME
(5, 'ACC005', 'letan1', 'thuan.dkb@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_RECEPTIONIST', 'ACTIVE', TRUE, NOW()),

-- EMP006 - Chử Quốc Thành - Kế toán - FULL_TIME
(6, 'ACC006', 'ketoan1', 'thanh.cq@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_ACCOUNTANT', 'ACTIVE', TRUE, NOW()),

-- Nurses (Y tá)
-- EMP007 - Đoàn Nguyễn Khôi Nguyên - FULL_TIME (Cả sáng và chiều)
(7, 'ACC007', 'yta1', 'nguyen.dnkn@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_NURSE', 'ACTIVE', TRUE, NOW()),

-- EMP008 - Nguyễn Trần Tuấn Khang - FULL_TIME (Cả sáng và chiều)
(8, 'ACC008', 'yta2', 'khang.nttk@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_NURSE', 'ACTIVE', TRUE, NOW()),

-- EMP009 - Huỳnh Tấn Quang Nhật - PART_TIME_FIXED (Chỉ sáng 08:00-12:00)
(9, 'ACC009', 'yta3', 'nhat.htqn@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_NURSE', 'ACTIVE', TRUE, NOW()),

-- EMP010 - Ngô Đình Chính - PART_TIME_FLEX (Chỉ chiều 13:00-17:00)
(10, 'ACC010', 'yta4', 'chinh.nd@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_NURSE', 'ACTIVE', TRUE, NOW()),

-- Manager
-- EMP011 - Võ Ngọc Minh Quân - Quản lý - FULL_TIME
(11, 'ACC011', 'quanli1', 'quan.vnm@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_MANAGER', 'ACTIVE', TRUE, NOW()),

-- Patients (Bệnh nhân) - All with is_email_verified = TRUE for demo data
-- Patient BN-1001 - Đoàn Thanh Phong
(12, 'ACC012', 'benhnhan1', 'phong.dt@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', TRUE, NOW()),

-- Patient BN-1002 - Phạm Văn Phong
(13, 'ACC013', 'benhnhan2', 'phong.pv@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', TRUE, NOW()),

-- Patient BN-1003 - Nguyễn Thị Anh
(14, 'ACC014', 'benhnhan3', 'anh.nt@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', TRUE, NOW()),

-- Patient BN-1004 - Mít tơ bít
(15, 'ACC015', 'benhnhan4', 'mit.bit@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', TRUE, NOW()),

-- EMP012 - Nguyễn Khánh Linh - Thực tập sinh - PART_TIME_FLEX
(16, 'ACC016', 'thuctap1', 'linh.nk@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_DENTIST_INTERN', 'ACTIVE', TRUE, NOW()),

-- Admin account - Super user
(17, 'ACC017', 'admin', 'admin@dentalclinic.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_ADMIN', 'ACTIVE', TRUE, NOW()),

-- Patient BN-1005 - Trần Văn Nam (for Treatment Plan testing)
(18, 'ACC018', 'benhnhan5', 'nam.tv@email.com',
'$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ROLE_PATIENT', 'ACTIVE', TRUE, NOW())
ON CONFLICT (account_id) DO NOTHING;



-- ============================================
-- BƯỚC 7: TẠO ROOMS (PHÒNG KHÁM/GHẾ NHA KHOA)
-- ============================================
-- Seed data cho các phòng khám/ghế nha khoa
-- Note: room_id must be provided manually in SQL since @PrePersist only works with JPA save()
-- Format: GHE + YYMMDD + sequence (e.g., GHE251103001)
-- ============================================

INSERT INTO rooms (room_id, room_code, room_name, room_type, is_active, created_at)
VALUES
-- Admin
(@admin_account_id, 'ROLE_ADMIN'),

-- Doctors
(@doctor1_account_id, 'ROLE_DOCTOR'),
(@doctor2_account_id, 'ROLE_DOCTOR'),

-- Receptionist
(@receptionist_account_id, 'ROLE_RECEPTIONIST'),

-- Accountant
(@accountant_account_id, 'ROLE_ACCOUNTANT'),

-- Nurse
(@nurse_account_id, 'ROLE_NURSE')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);


-- ============================================
-- STEP 7: CREATE EMPLOYEES (Auto-increment IDs + Generated Codes)
-- ============================================

-- Admin Employee
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@admin_account_id, 'ROLE_ADMIN', 'Admin', 'Hệ thống', '0900000001', '1985-01-01', 'Phòng quản trị', TRUE, NOW());
SET @admin_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@admin_employee_id, 3, '0')) WHERE employee_id = @admin_employee_id;

-- Doctor 1: Chuyên Chỉnh nha + Răng thẩm mỹ
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@doctor1_account_id, 'ROLE_DOCTOR', 'Minh', 'Nguyễn Văn', '0901234567', '1985-05-15', '123 Nguyễn Huệ, Q1, TPHCM', TRUE, NOW());
SET @doctor1_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@doctor1_employee_id, 3, '0')) WHERE employee_id = @doctor1_employee_id;

-- Doctor 2: Chuyên Nội nha + Phục hồi răng
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@doctor2_account_id, 'ROLE_DOCTOR', 'Lan', 'Trần Thị', '0902345678', '1988-08-20', '456 Lê Lợi, Q3, TPHCM', TRUE, NOW());
SET @doctor2_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@doctor2_employee_id, 3, '0')) WHERE employee_id = @doctor2_employee_id;

-- Receptionist
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@receptionist_account_id, 'ROLE_RECEPTIONIST', 'Mai', 'Lê Thị', '0903456789', '1995-03-10', '789 Trần Hưng Đạo, Q5, TPHCM', TRUE, NOW());
SET @receptionist_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@receptionist_employee_id, 3, '0')) WHERE employee_id = @receptionist_employee_id;

-- Accountant
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@accountant_account_id, 'ROLE_ACCOUNTANT', 'Tuấn', 'Hoàng Văn', '0904567890', '1992-07-25', '321 Hai Bà Trưng, Q1, TPHCM', TRUE, NOW());
SET @accountant_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@accountant_employee_id, 3, '0')) WHERE employee_id = @accountant_employee_id;

-- Nurse
INSERT INTO employees (account_id, role_id, first_name, last_name, phone, date_of_birth, address, is_active, created_at)
VALUES (@nurse_account_id, 'ROLE_NURSE', 'Hoa', 'Phạm Thị', '0906789012', '1992-06-15', '111 Lý Thường Kiệt, Q10, TPHCM', TRUE, NOW());
SET @nurse_employee_id = LAST_INSERT_ID();
UPDATE employees SET employee_code = CONCAT('EMP', LPAD(@nurse_employee_id, 3, '0')) WHERE employee_id = @nurse_employee_id;


-- ============================================
-- STEP 8: ASSIGN SPECIALIZATIONS TO DOCTORS
-- ============================================

-- Doctor 1: Chỉnh nha + Răng thẩm mỹ
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(@doctor1_employee_id, 1),
(@doctor1_employee_id, 7)
ON DUPLICATE KEY UPDATE employee_id = VALUES(employee_id);

-- Doctor 2: Nội nha + Phục hồi răng
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(@doctor2_employee_id, 2),
(@doctor2_employee_id, 4)
ON DUPLICATE KEY UPDATE employee_id = VALUES(employee_id);

-- Nurse: Nha khoa trẻ em
INSERT INTO employee_specializations (employee_id, specialization_id)
VALUES
(@nurse_employee_id, 6)
ON DUPLICATE KEY UPDATE employee_id = VALUES(employee_id);


-- ============================================
-- STEP 9: CREATE PATIENT ACCOUNTS (Auto-increment IDs + Generated Codes)
-- ============================================

-- Patient Account 1
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('benhnhan1', 'benhnhan1@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @patient1_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@patient1_account_id, 3, '0')) WHERE account_id = @patient1_account_id;

-- Patient Account 2
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('benhnhan2', 'benhnhan2@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @patient2_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@patient2_account_id, 3, '0')) WHERE account_id = @patient2_account_id;

-- Patient Account 3
INSERT INTO accounts (username, email, password, status, created_at)
VALUES ('benhnhan3', 'benhnhan3@email.com', '$2a$10$XOePZT251MQ7sdsoqH/jsO.vAuDoFrdWu/pAJSCD49/iwyIHQubf2', 'ACTIVE', NOW());
SET @patient3_account_id = LAST_INSERT_ID();
UPDATE accounts SET account_code = CONCAT('ACC', LPAD(@patient3_account_id, 3, '0')) WHERE account_id = @patient3_account_id;

-- Assign PATIENT role
INSERT INTO account_roles (account_id, role_id)
VALUES
(@patient1_account_id, 'ROLE_PATIENT'),
(@patient2_account_id, 'ROLE_PATIENT'),
(@patient3_account_id, 'ROLE_PATIENT')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);


-- ============================================
-- STEP 10: CREATE PATIENTS (Auto-increment IDs + Generated Codes)
-- ============================================

-- Patient 1
INSERT INTO patients (first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES ('Khang', 'Nguyễn Văn', 'benhnhan1@email.com', '0911111111', '1990-01-15', '123 Lê Văn Việt, Q9, TPHCM', 'MALE', TRUE, NOW(), NOW());
SET @patient1_id = LAST_INSERT_ID();
UPDATE patients SET patient_code = CONCAT('PAT', LPAD(@patient1_id, 3, '0')) WHERE patient_id = @patient1_id;

-- Patient 2
INSERT INTO patients (first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES ('Lan', 'Trần Thị', 'benhnhan2@email.com', '0922222222', '1985-05-20', '456 Võ Văn Ngân, Thủ Đức, TPHCM', 'FEMALE', TRUE, NOW(), NOW());
SET @patient2_id = LAST_INSERT_ID();
UPDATE patients SET patient_code = CONCAT('PAT', LPAD(@patient2_id, 3, '0')) WHERE patient_id = @patient2_id;

-- Patient 3
INSERT INTO patients (first_name, last_name, email, phone, date_of_birth, address, gender, is_active, created_at, updated_at)
VALUES ('Đức', 'Lê Minh', 'benhnhan3@email.com', '0933333333', '1995-12-10', '789 Đường D2, Bình Thạnh, TPHCM', 'MALE', TRUE, NOW(), NOW());
SET @patient3_id = LAST_INSERT_ID();
UPDATE patients SET patient_code = CONCAT('PAT', LPAD(@patient3_id, 3, '0')) WHERE patient_id = @patient3_id;


-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- View all employees with roles and accounts
SELECT
    vals.service_code,
    vals.service_name,
    vals.description,
    vals.default_duration_minutes,
    vals.default_buffer_minutes,
    vals.price,
    vals.specialization_id,
    sc.category_id,
    vals.display_order,
    vals.is_active,
    vals.created_at
FROM (VALUES
-- A. Nha khoa tổng quát (category_code = 'A_GENERAL')
('GEN_EXAM', 'Khám tổng quát & Tư vấn', 'Khám tổng quát, chụp X-quang phim nhỏ nếu cần thiết để chẩn đoán.', 30, 15, 100000, 8, 'A_GENERAL', 1, true, NOW()),
('GEN_XRAY_PERI', 'Chụp X-Quang quanh chóp', 'Chụp phim X-quang nhỏ tại ghế.', 10, 5, 50000, 8, 'A_GENERAL', 2, true, NOW()),
('SCALING_L1', 'Cạo vôi răng & Đánh bóng - Mức 1', 'Làm sạch vôi răng và mảng bám mức độ ít/trung bình.', 45, 15, 300000, 3, 'A_GENERAL', 3, true, NOW()),
('SCALING_L2', 'Cạo vôi răng & Đánh bóng - Mức 2', 'Làm sạch vôi răng và mảng bám mức độ nhiều.', 60, 15, 400000, 3, 'A_GENERAL', 4, true, NOW()),
('SCALING_VIP', 'Cạo vôi VIP không đau', 'Sử dụng máy rung siêu âm ít ê buốt.', 60, 15, 500000, 3, 'A_GENERAL', 5, true, NOW()),
('FILLING_COMP', 'Trám răng Composite', 'Trám răng sâu, mẻ bằng vật liệu composite thẩm mỹ.', 45, 15, 400000, 2, 'A_GENERAL', 6, true, NOW()),
('FILLING_GAP', 'Đắp kẽ răng thưa Composite', 'Đóng kẽ răng thưa nhỏ bằng composite.', 60, 15, 500000, 7, 'A_GENERAL', 7, true, NOW()),
('EXTRACT_MILK', 'Nhổ răng sữa', 'Nhổ răng sữa cho trẻ em.', 15, 15, 50000, 6, 'A_GENERAL', 8, true, NOW()),
('EXTRACT_NORM', 'Nhổ răng thường', 'Nhổ răng vĩnh viễn đơn giản (không phải răng khôn).', 45, 15, 500000, 5, 'A_GENERAL', 9, true, NOW()),
('EXTRACT_WISDOM_L1', 'Nhổ răng khôn mức 1 (Dễ)', 'Tiểu phẫu nhổ răng khôn mọc thẳng, ít phức tạp.', 60, 30, 1500000, 5, 'A_GENERAL', 10, true, NOW()),
('EXTRACT_WISDOM_L2', 'Nhổ răng khôn mức 2 (Khó)', 'Tiểu phẫu nhổ răng khôn mọc lệch, ngầm.', 90, 30, 2500000, 5, 'A_GENERAL', 11, true, NOW()),
('ENDO_TREAT_ANT', 'Điều trị tủy răng trước', 'Lấy tủy, làm sạch, trám bít ống tủy cho răng cửa/răng nanh.', 60, 15, 1500000, 2, 'A_GENERAL', 12, true, NOW()),
('ENDO_TREAT_POST', 'Điều trị tủy răng sau', 'Lấy tủy, làm sạch, trám bít ống tủy cho răng tiền cối/răng cối.', 75, 15, 2000000, 2, 'A_GENERAL', 13, true, NOW()),
('ENDO_POST_CORE', 'Đóng chốt tái tạo cùi răng', 'Đặt chốt vào ống tủy đã chữa để tăng cường lưu giữ cho mão sứ.', 45, 15, 500000, 4, 'A_GENERAL', 14, true, NOW()),

-- B. Thẩm mỹ & Phục hình (category_code = 'B_COSMETIC')
('BLEACH_ATHOME', 'Tẩy trắng răng tại nhà', 'Cung cấp máng và thuốc tẩy trắng tại nhà.', 30, 15, 800000, 7, 'B_COSMETIC', 1, true, NOW()),
('BLEACH_INOFFICE', 'Tẩy trắng răng tại phòng (Laser)', 'Tẩy trắng bằng đèn chiếu hoặc laser.', 90, 15, 1200000, 7, 'B_COSMETIC', 2, true, NOW()),
('CROWN_PFM', 'Mão răng sứ Kim loại thường', 'Mão sứ sườn kim loại Cr-Co hoặc Ni-Cr.', 60, 15, 1000000, 4, 'B_COSMETIC', 3, true, NOW()),
('CROWN_TITAN', 'Mão răng sứ Titan', 'Mão sứ sườn hợp kim Titan.', 60, 15, 2500000, 4, 'B_COSMETIC', 4, true, NOW()),
('CROWN_ZIR_KATANA', 'Mão răng toàn sứ Katana/Zir HT', 'Mão sứ 100% Zirconia phổ thông.', 60, 15, 3500000, 4, 'B_COSMETIC', 5, true, NOW()),
('CROWN_ZIR_CERCON', 'Mão răng toàn sứ Cercon HT', 'Mão sứ 100% Zirconia cao cấp (Đức).', 60, 15, 5000000, 4, 'B_COSMETIC', 6, true, NOW()),
('CROWN_EMAX', 'Mão răng sứ thủy tinh Emax', 'Mão sứ Lithium Disilicate thẩm mỹ cao.', 60, 15, 6000000, 4, 'B_COSMETIC', 7, true, NOW()),
('CROWN_ZIR_LAVA', 'Mão răng toàn sứ Lava Plus', 'Mão sứ Zirconia đa lớp (Mỹ).', 60, 15, 8000000, 4, 'B_COSMETIC', 8, true, NOW()),
('VENEER_EMAX', 'Mặt dán sứ Veneer Emax', 'Mặt dán sứ Lithium Disilicate mài răng tối thiểu.', 75, 15, 6000000, 7, 'B_COSMETIC', 9, true, NOW()),
('VENEER_LISI', 'Mặt dán sứ Veneer Lisi Ultra', 'Mặt dán sứ Lithium Disilicate (Mỹ).', 75, 15, 8000000, 7, 'B_COSMETIC', 10, true, NOW()),
('INLAY_ONLAY_ZIR', 'Trám sứ Inlay/Onlay Zirconia', 'Miếng trám gián tiếp bằng sứ Zirconia CAD/CAM.', 60, 15, 2000000, 4, 'B_COSMETIC', 11, true, NOW()),
('INLAY_ONLAY_EMAX', 'Trám sứ Inlay/Onlay Emax', 'Miếng trám gián tiếp bằng sứ Emax Press.', 60, 15, 3000000, 4, 'B_COSMETIC', 12, true, NOW()),

-- C. Cắm ghép Implant (category_code = 'C_IMPLANT')
('IMPL_CONSULT', 'Khám & Tư vấn Implant', 'Khám, đánh giá tình trạng xương, tư vấn kế hoạch.', 45, 15, 0, 4, 'C_IMPLANT', 1, true, NOW()),
('IMPL_CT_SCAN', 'Chụp CT Cone Beam (Implant)', 'Chụp phim 3D phục vụ cắm ghép Implant.', 30, 15, 500000, 4, 'C_IMPLANT', 2, true, NOW()),
('IMPL_SURGERY_KR', 'Phẫu thuật đặt trụ Implant Hàn Quốc', 'Phẫu thuật cắm trụ Implant (VD: Osstem, Biotem).', 90, 30, 15000000, 4, 'C_IMPLANT', 3, true, NOW()),
('IMPL_SURGERY_EUUS', 'Phẫu thuật đặt trụ Implant Thụy Sĩ/Mỹ', 'Phẫu thuật cắm trụ Implant (VD: Straumann, Nobel).', 90, 30, 25000000, 4, 'C_IMPLANT', 4, true, NOW()),
('IMPL_BONE_GRAFT', 'Ghép xương ổ răng', 'Phẫu thuật bổ sung xương cho vị trí cắm Implant.', 60, 30, 5000000, 5, 'C_IMPLANT', 5, true, NOW()),
('IMPL_SINUS_LIFT', 'Nâng xoang hàm (Hở/Kín)', 'Phẫu thuật nâng xoang để cắm Implant hàm trên.', 75, 30, 8000000, 5, 'C_IMPLANT', 6, true, NOW()),
('IMPL_HEALING', 'Gắn trụ lành thương (Healing Abutment)', 'Gắn trụ giúp nướu lành thương đúng hình dạng.', 20, 10, 500000, 4, 'C_IMPLANT', 7, true, NOW()),
('IMPL_IMPRESSION', 'Lấy dấu Implant', 'Lấy dấu để làm răng sứ trên Implant.', 30, 15, 0, 4, 'C_IMPLANT', 8, true, NOW()),
('IMPL_CROWN_TITAN', 'Mão sứ Titan trên Implant', 'Làm và gắn mão sứ Titan trên Abutment.', 45, 15, 3000000, 4, 'C_IMPLANT', 9, true, NOW()),
('IMPL_CROWN_ZIR', 'Mão sứ Zirconia trên Implant', 'Làm và gắn mão sứ Zirconia trên Abutment.', 45, 15, 5000000, 4, 'C_IMPLANT', 10, true, NOW()),

-- D. Chỉnh nha (category_code = 'D_ORTHO')
('ORTHO_CONSULT', 'Khám & Tư vấn Chỉnh nha', 'Khám, phân tích phim, tư vấn kế hoạch niềng.', 45, 15, 0, 1, 'D_ORTHO', 1, true, NOW()),
('ORTHO_FILMS', 'Chụp Phim Chỉnh nha (Pano, Ceph)', 'Chụp phim X-quang Toàn cảnh và Sọ nghiêng.', 30, 15, 500000, 1, 'D_ORTHO', 2, true, NOW()),
('ORTHO_BRACES_ON', 'Gắn mắc cài kim loại/sứ', 'Gắn bộ mắc cài lên răng.', 90, 30, 5000000, 1, 'D_ORTHO', 3, true, NOW()),
('ORTHO_ADJUST', 'Tái khám Chỉnh nha / Siết niềng', 'Điều chỉnh dây cung, thay thun định kỳ.', 30, 15, 500000, 1, 'D_ORTHO', 4, true, NOW()),
('ORTHO_INVIS_SCAN', 'Scan mẫu hàm Invisalign', 'Scan 3D mẫu hàm để gửi làm khay Invisalign.', 45, 15, 1000000, 1, 'D_ORTHO', 5, true, NOW()),
('ORTHO_INVIS_ATTACH', 'Gắn Attachment Invisalign', 'Gắn các điểm tạo lực trên răng cho Invisalign.', 60, 15, 2000000, 1, 'D_ORTHO', 6, true, NOW()),
('ORTHO_MINIVIS', 'Cắm Mini-vis Chỉnh nha', 'Phẫu thuật nhỏ cắm vít hỗ trợ niềng răng.', 45, 15, 1500000, 1, 'D_ORTHO', 7, true, NOW()),
('ORTHO_BRACES_OFF', 'Tháo mắc cài & Vệ sinh', 'Tháo bỏ mắc cài sau khi kết thúc niềng.', 60, 15, 1000000, 1, 'D_ORTHO', 8, true, NOW()),
('ORTHO_RETAINER_FIXED', 'Gắn hàm duy trì cố định', 'Dán dây duy trì mặt trong răng.', 30, 15, 1000000, 1, 'D_ORTHO', 9, true, NOW()),
('ORTHO_RETAINER_REMOV', 'Làm hàm duy trì tháo lắp', 'Lấy dấu và giao hàm duy trì (máng trong/Hawley).', 30, 15, 1000000, 1, 'D_ORTHO', 10, true, NOW()),

-- E. Phục hình Tháo lắp (category_code = 'E_PROS_DENTURE')
('PROS_CEMENT', 'Gắn sứ / Thử sứ (Lần 2)', 'Hẹn lần 2 để thử và gắn vĩnh viễn mão sứ, cầu răng, veneer.', 30, 15, 0, 4, 'E_PROS_DENTURE', 1, true, NOW()),
('DENTURE_CONSULT', 'Khám & Lấy dấu Hàm Tháo Lắp', 'Lấy dấu lần đầu để làm hàm giả tháo lắp.', 45, 15, 1000000, 4, 'E_PROS_DENTURE', 2, true, NOW()),
('DENTURE_TRYIN', 'Thử sườn/Thử răng Hàm Tháo Lắp', 'Hẹn thử khung kim loại hoặc thử răng sáp.', 30, 15, 0, 4, 'E_PROS_DENTURE', 3, true, NOW()),
('DENTURE_DELIVERY', 'Giao hàm & Chỉnh khớp cắn', 'Giao hàm hoàn thiện, chỉnh sửa các điểm vướng cộm.', 30, 15, 0, 4, 'E_PROS_DENTURE', 4, true, NOW()),

-- F. Dịch vụ khác (category_code = 'F_OTHER')
('OTHER_DIAMOND', 'Đính đá/kim cương lên răng', 'Gắn đá thẩm mỹ lên răng.', 30, 15, 300000, 7, 'F_OTHER', 1, true, NOW()),
('OTHER_GINGIVECTOMY', 'Phẫu thuật cắt nướu (thẩm mỹ)', 'Làm dài thân răng, điều trị cười hở lợi.', 60, 30, 1000000, 5, 'F_OTHER', 2, true, NOW()),
('EMERG_PAIN', 'Khám cấp cứu / Giảm đau', 'Khám và xử lý khẩn cấp các trường hợp đau nhức, sưng, chấn thương.', 30, 15, 150000, 8, 'F_OTHER', 3, true, NOW()),
('SURG_CHECKUP', 'Tái khám sau phẫu thuật / Cắt chỉ', 'Kiểm tra vết thương sau nhổ răng khôn, cắm Implant, cắt nướu.', 15, 10, 0, 5, 'F_OTHER', 4, true, NOW())
) AS vals(service_code, service_name, description, default_duration_minutes, default_buffer_minutes, price, specialization_id, category_code_ref, display_order, is_active, created_at)
LEFT JOIN service_categories sc ON sc.category_code = vals.category_code_ref
ON CONFLICT (service_code) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    display_order = EXCLUDED.display_order;

-- ============================================
-- ROOM-SERVICES MAPPINGS (V16)
-- Map services to rooms based on room type compatibility
-- NOTE: This MUST be placed AFTER services are inserted!
-- LOGIC: IMPLANT room = ALL STANDARD services + IMPLANT-specific services
-- ============================================
INSERT INTO room_services (room_id, service_id, created_at)
SELECT r.room_id, s.service_id, NOW()
FROM rooms r
CROSS JOIN services s
WHERE
    -- STANDARD rooms (P-01, P-02, P-03) - General services only
    (r.room_type = 'STANDARD' AND s.service_code IN (
        'GEN_EXAM', 'GEN_XRAY_PERI', 'SCALING_L1', 'SCALING_L2', 'SCALING_VIP',
        'FILLING_COMP', 'FILLING_GAP', 'EXTRACT_MILK', 'EXTRACT_NORM',
        'ENDO_TREAT_ANT', 'ENDO_TREAT_POST', 'ENDO_POST_CORE',
        'BLEACH_ATHOME', 'BLEACH_INOFFICE',
        'CROWN_PFM', 'CROWN_TITAN', 'CROWN_ZIR_KATANA', 'CROWN_ZIR_CERCON',
        'CROWN_EMAX', 'CROWN_ZIR_LAVA', 'VENEER_EMAX', 'VENEER_LISI',
        'INLAY_ONLAY_ZIR', 'INLAY_ONLAY_EMAX',
        'PROS_CEMENT', 'DENTURE_CONSULT', 'DENTURE_TRYIN', 'DENTURE_DELIVERY',
        'OTHER_DIAMOND', 'EMERG_PAIN', 'SURG_CHECKUP',
        -- V17 FIX: Added ORTHO services (was missing and caused booking bug)
        'ORTHO_RETAINER_REMOV', 'ORTHO_RETAINER_FIXED', 'ORTHO_BRACES_OFF',
        'ORTHO_MINIVIS', 'ORTHO_INVIS_ATTACH', 'ORTHO_ADJUST', 'ORTHO_BRACES_METAL',
        'ORTHO_BRACES_CERAMIC', 'ORTHO_BRACES_SELF', 'ORTHO_INVISALIGN'
    ))
    OR
    -- IMPLANT room (P-04) - ALL STANDARD services + IMPLANT-specific services
    (r.room_type = 'IMPLANT' AND s.service_code IN (
        -- ALL services from STANDARD rooms
        'GEN_EXAM', 'GEN_XRAY_PERI', 'SCALING_L1', 'SCALING_L2', 'SCALING_VIP',
        'FILLING_COMP', 'FILLING_GAP', 'EXTRACT_MILK', 'EXTRACT_NORM',
        'ENDO_TREAT_ANT', 'ENDO_TREAT_POST', 'ENDO_POST_CORE',
        'BLEACH_ATHOME', 'BLEACH_INOFFICE',
        'CROWN_PFM', 'CROWN_TITAN', 'CROWN_ZIR_KATANA', 'CROWN_ZIR_CERCON',
        'CROWN_EMAX', 'CROWN_ZIR_LAVA', 'VENEER_EMAX', 'VENEER_LISI',
        'INLAY_ONLAY_ZIR', 'INLAY_ONLAY_EMAX',
        'PROS_CEMENT', 'DENTURE_CONSULT', 'DENTURE_TRYIN', 'DENTURE_DELIVERY',
        'OTHER_DIAMOND', 'EMERG_PAIN', 'SURG_CHECKUP',
        -- ORTHO services
        'ORTHO_RETAINER_REMOV', 'ORTHO_RETAINER_FIXED', 'ORTHO_BRACES_OFF',
        'ORTHO_MINIVIS', 'ORTHO_INVIS_ATTACH', 'ORTHO_ADJUST', 'ORTHO_BRACES_METAL',
        'ORTHO_BRACES_CERAMIC', 'ORTHO_BRACES_SELF', 'ORTHO_INVISALIGN',
        -- PLUS Implant-specific services
        'IMPL_CONSULT', 'IMPL_CT_SCAN', 'IMPL_SURGERY_KR', 'IMPL_SURGERY_EUUS',
        'IMPL_BONE_GRAFT', 'IMPL_SINUS_LIFT', 'IMPL_HEALING',
        'IMPL_IMPRESSION', 'IMPL_CROWN_TITAN', 'IMPL_CROWN_ZIR',
        'EXTRACT_WISDOM_L1', 'EXTRACT_WISDOM_L2', 'OTHER_GINGIVECTOMY'
    ))
ON CONFLICT (room_id, service_id) DO NOTHING;


-- =============================================
-- BƯỚC 2.5: INSERT SERVICE DEPENDENCIES (V21 - Clinical Rules Engine)
-- =============================================
-- Quy tắc lâm sàng để đảm bảo an toàn và hiệu quả điều trị
-- =============================================

-- ❌ REMOVED: Rule 1 - GEN_EXAM prerequisite for FILLING_COMP
-- (Removed per Issue #43 - Business requirement: No prerequisite services)
-- Reason: prerequisite rules cause items to be set to WAITING_FOR_PREREQUISITE status
-- which prevents users from booking appointments immediately after plan approval

-- Rule 2: EXTRACT_WISDOM_L2 (Nhổ răng khôn) -> SURG_CHECKUP (Cắt chỉ) phải cách nhau ÍT NHẤT 7 ngày
INSERT INTO service_dependencies (service_id, dependent_service_id, rule_type, min_days_apart, receptionist_note, created_at)
SELECT
    s1.service_id,
    s2.service_id,
    'REQUIRES_MIN_DAYS',
    7,
    'Cắt chỉ SAU nhổ răng khôn ít nhất 7 ngày (lý tưởng 7-10 ngày).',
    NOW()
FROM services s1, services s2
WHERE s1.service_code = 'EXTRACT_WISDOM_L2'
  AND s2.service_code = 'SURG_CHECKUP'
ON CONFLICT DO NOTHING;

-- Rule 3: EXTRACT_WISDOM_L2 (Nhổ răng khôn) và BLEACH_INOFFICE (Tẩy trắng) LOẠI TRỪ cùng ngày
INSERT INTO service_dependencies (service_id, dependent_service_id, rule_type, receptionist_note, created_at)
SELECT
    s1.service_id,
    s2.service_id,
    'EXCLUDES_SAME_DAY',
    'KHÔNG được đặt Nhổ răng khôn và Tẩy trắng cùng ngày (nguy hiểm).',
    NOW()
FROM services s1, services s2
WHERE s1.service_code = 'EXTRACT_WISDOM_L2'
  AND s2.service_code = 'BLEACH_INOFFICE'
ON CONFLICT DO NOTHING;

-- Rule 3b: Reverse rule - BLEACH_INOFFICE cũng loại trừ EXTRACT_WISDOM_L2
INSERT INTO service_dependencies (service_id, dependent_service_id, rule_type, receptionist_note, created_at)
SELECT
    s1.service_id,
    s2.service_id,
    'EXCLUDES_SAME_DAY',
    'KHÔNG được đặt Tẩy trắng và Nhổ răng khôn cùng ngày (nguy hiểm).',
    NOW()
FROM services s1, services s2
WHERE s1.service_code = 'BLEACH_INOFFICE'
  AND s2.service_code = 'EXTRACT_WISDOM_L2'
ON CONFLICT DO NOTHING;

-- Rule 4: GEN_EXAM (Khám) và SCALING_L1 (Cạo vôi) GỢI Ý đặt chung (Soft rule)
INSERT INTO service_dependencies (service_id, dependent_service_id, rule_type, receptionist_note, created_at)
SELECT
    s1.service_id,
    s2.service_id,
    'BUNDLES_WITH',
    'Gợi ý: Nên đặt Khám + Cạo vôi cùng lúc để tiết kiệm thời gian.',
    NOW()
FROM services s1, services s2
WHERE s1.service_code = 'GEN_EXAM'
  AND s2.service_code = 'SCALING_L1'
ON CONFLICT DO NOTHING;

-- Rule 4b: Reverse bundle - SCALING_L1 cũng gợi ý bundle với GEN_EXAM
INSERT INTO service_dependencies (service_id, dependent_service_id, rule_type, receptionist_note, created_at)
SELECT
    s1.service_id,
    s2.service_id,
    'BUNDLES_WITH',
    'Gợi ý: Nên đặt Cạo vôi + Khám cùng lúc để tiết kiệm thời gian.',
    NOW()
FROM services s1, services s2
WHERE s1.service_code = 'SCALING_L1'
  AND s2.service_code = 'GEN_EXAM'
ON CONFLICT DO NOTHING;

-- =============================================
-- BƯỚC 3: INSERT TREATMENT PLAN TEMPLATES
-- =============================================
-- Treatment Plan Templates for common dental procedures
-- Used by doctors to create structured treatment plans
-- =============================================

-- Template 1: Niềng răng mắc cài kim loại (2 năm - 24 tái khám)
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_ORTHO_METAL', 'Niềng răng mắc cài kim loại trọn gói 2 năm',
        'Gói điều trị chỉnh nha toàn diện với mắc cài kim loại, bao gồm 24 lần tái khám siết niềng định kỳ.',
        730, 30000000, 1, true, NOW())
ON CONFLICT (template_code) DO NOTHING;


-- Template 2: Implant Hàn Quốc (6 tháng) - Changed specialization_id from 5 to 4 (all services are spec 4)
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_IMPLANT_OSSTEM', 'Cấy ghép Implant Hàn Quốc (Osstem) - Trọn gói',
        'Gói cấy ghép Implant hoàn chỉnh từ phẫu thuật đến gắn răng sứ, sử dụng trụ Osstem Hàn Quốc.',
        180, 19000000, 4, true, NOW())
ON CONFLICT (template_code) DO NOTHING;


-- Template 3A: Bọc răng sứ Cercon HT đơn giản (4 ngày)
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_CROWN_CERCON_SIMPLE', 'Bọc răng sứ Cercon HT - 1 răng (đơn giản)',
        'Gói bọc răng sứ toàn sứ Cercon HT cho răng đã điều trị tủy hoặc răng còn tủy sống không cần điều trị.',
        4, 3500000, 4, true, NOW())
ON CONFLICT (template_code) DO NOTHING;

-- Template 3B: Disable old mixed-spec template
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_CROWN_CERCON_ENDO', 'Bọc răng sứ Cercon HT - 1 răng (kèm điều trị tủy) - DEPRECATED',
        'DEPRECATED: Template cũ có mixed specializations. Use TPL_ENDO_TREATMENT and TPL_CROWN_AFTER_ENDO instead.',
        7, 5000000, 4, false, NOW())
ON CONFLICT (template_code) DO UPDATE SET is_active = false;

-- ============================================
--  NEW TEMPLATES - One specialization per template
-- ============================================

-- Template 4: Điều trị tủy răng (spec 2: Nội nha) - Only endodontic treatment
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_ENDO_TREATMENT', 'Điều trị tủy răng sau',
        'Gói điều trị tủy răng tiền cối/răng cối, bao gồm lấy tủy, làm sạch và trám bít ống tủy.',
        3, 2000000, 2, true, NOW())
ON CONFLICT (template_code) DO NOTHING;

-- Template 5: Bọc sứ sau điều trị tủy (spec 4: Phục hồi răng) - Restorative work
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_CROWN_AFTER_ENDO', 'Bọc sứ sau điều trị tủy',
        'Gói bọc răng sứ Cercon HT cho răng đã điều trị tủy, bao gồm đóng chốt tái tạo cùi răng, mài răng, lấy dấu và gắn sứ.',
        4, 4500000, 4, true, NOW())
ON CONFLICT (template_code) DO NOTHING;

-- Template 6: Cạo vôi răng định kỳ (spec 3: Nha chu)
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_PERIO_SCALING', 'Cạo vôi răng toàn hàm',
        'Gói cạo vôi răng định kỳ cho cả 2 hàm, bao gồm cạo vôi cơ bản + đánh bóng răng.',
        1, 500000, 3, true, NOW())
ON CONFLICT (template_code) DO NOTHING;

-- Template 7: Nhổ răng khôn (spec 5: Phẫu thuật hàm mặt)
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_SURGERY_WISDOM', 'Nhổ răng khôn',
        'Gói nhổ răng khôn mọc lệch/ngầm, bao gồm chụp phim, phẫu thuật và tái khám.',
        7, 2000000, 5, true, NOW())
ON CONFLICT (template_code) DO NOTHING;

-- Template 8: Trám răng sữa trẻ em (spec 6: Nha khoa trẻ em)
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_PEDO_FILLING', 'Trám răng sữa',
        'Gói trám răng sữa bị sâu cho trẻ em, sử dụng vật liệu GIC an toàn.',
        1, 300000, 6, true, NOW())
ON CONFLICT (template_code) DO NOTHING;

-- Template 9: Tẩy trắng răng (spec 7: Răng thẩm mỹ)
INSERT INTO treatment_plan_templates (template_code, template_name, description, estimated_duration_days, total_price, specialization_id, is_active, created_at)
VALUES ('TPL_COSMETIC_BLEACHING', 'Tẩy trắng răng tại phòng khám',
        'Gói tẩy trắng răng bằng công nghệ Laser/Zoom, bao gồm kiểm tra và làm sạch răng trước tẩy trắng.',
        1, 3000000, 7, true, NOW())
ON CONFLICT (template_code) DO NOTHING;


-- =============================================
-- BƯỚC 4: INSERT TEMPLATE PHASES (Giai đoạn điều trị)
-- =============================================

-- TPL_ORTHO_METAL: 4 giai đoạn
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Giai đoạn 1: Khám & Chuẩn bị', 14, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_ORTHO_METAL'
ON CONFLICT (template_id, phase_number) DO NOTHING;


INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 2, 'Giai đoạn 2: Gắn mắc cài', 1, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_ORTHO_METAL'
ON CONFLICT (template_id, phase_number) DO NOTHING;


INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 3, 'Giai đoạn 3: Điều chỉnh định kỳ (8 tháng)', 715, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_ORTHO_METAL'
ON CONFLICT (template_id, phase_number) DO NOTHING;


INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 4, 'Giai đoạn 4: Tháo niềng & Duy trì', 0, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_ORTHO_METAL'
ON CONFLICT (template_id, phase_number) DO NOTHING;


-- TPL_IMPLANT_OSSTEM: 3 giai đoạn
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Giai đoạn 1: Khám & Chẩn đoán hình ảnh', 7, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_IMPLANT_OSSTEM'
ON CONFLICT (template_id, phase_number) DO NOTHING;


INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 2, 'Giai đoạn 2: Phẫu thuật cắm Implant', 120, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_IMPLANT_OSSTEM'
ON CONFLICT (template_id, phase_number) DO NOTHING;


INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 3, 'Giai đoạn 3: Làm & Gắn răng sứ', 14, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_IMPLANT_OSSTEM'
ON CONFLICT (template_id, phase_number) DO NOTHING;


-- TPL_CROWN_CERCON_SIMPLE: 1 giai đoạn (chỉ bọc sứ)
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Giai đoạn 1: Mài răng, Lấy dấu & Gắn sứ', 4, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_CROWN_CERCON_SIMPLE'
ON CONFLICT (template_id, phase_number) DO NOTHING;

-- TPL_CROWN_CERCON_ENDO: 2 giai đoạn (điều trị tủy + bọc sứ)
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Giai đoạn 1: Điều trị tủy & Trụ sợi', 3, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_CROWN_CERCON_ENDO'
ON CONFLICT (template_id, phase_number) DO NOTHING;


INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 2, 'Giai đoạn 2: Mài răng, Lấy dấu & Gắn sứ', 4, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_CROWN_CERCON_ENDO'
ON CONFLICT (template_id, phase_number) DO NOTHING;


-- =============================================
-- BƯỚC 5: INSERT TEMPLATE PHASE SERVICES (Dịch vụ trong từng giai đoạn)
-- V19: Added sequence_number for ordered item creation
-- =============================================

-- TPL_ORTHO_METAL - Phase 1: Khám & Chuẩn bị (3 services in order)
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 45, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ORTHO_CONSULT'
WHERE t.template_code = 'TPL_ORTHO_METAL' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 2, 1, 30, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ORTHO_FILMS'
WHERE t.template_code = 'TPL_ORTHO_METAL' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;

-- REMOVED SCALING_L1 - periodontics service doesn't belong in orthodontics template


-- TPL_ORTHO_METAL - Phase 2: Gắn mắc cài (1 service)
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 90, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ORTHO_BRACES_ON'
WHERE t.template_code = 'TPL_ORTHO_METAL' AND tp.phase_number = 2
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_ORTHO_METAL - Phase 3: Tái khám 8 lần (quantity = 8) - FIXED: Reduced from 24 to 8 for realistic seed data
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 8, 30, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ORTHO_ADJUST'
WHERE t.template_code = 'TPL_ORTHO_METAL' AND tp.phase_number = 3
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_ORTHO_METAL - Phase 4: Tháo niềng & Duy trì (2 services in order)
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 60, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ORTHO_BRACES_OFF'
WHERE t.template_code = 'TPL_ORTHO_METAL' AND tp.phase_number = 4
ON CONFLICT (phase_id, service_id) DO NOTHING;


INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 2, 1, 30, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ORTHO_RETAINER_REMOV'
WHERE t.template_code = 'TPL_ORTHO_METAL' AND tp.phase_number = 4
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_IMPLANT_OSSTEM - Phase 1: Khám & Chẩn đoán (2 services in order)
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 45, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'IMPL_CONSULT'
WHERE t.template_code = 'TPL_IMPLANT_OSSTEM' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 2, 1, 30, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'IMPL_CT_SCAN'
WHERE t.template_code = 'TPL_IMPLANT_OSSTEM' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_IMPLANT_OSSTEM - Phase 2: Phẫu thuật (2 services in order: surgery first, then healing cap)
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 90, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'IMPL_SURGERY_KR'
WHERE t.template_code = 'TPL_IMPLANT_OSSTEM' AND tp.phase_number = 2
ON CONFLICT (phase_id, service_id) DO NOTHING;


INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 2, 1, 20, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'IMPL_HEALING'
WHERE t.template_code = 'TPL_IMPLANT_OSSTEM' AND tp.phase_number = 2
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_IMPLANT_OSSTEM - Phase 3: Làm răng sứ (2 services in order: impression first, then crown)
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 30, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'IMPL_IMPRESSION'
WHERE t.template_code = 'TPL_IMPLANT_OSSTEM' AND tp.phase_number = 3
ON CONFLICT (phase_id, service_id) DO NOTHING;


INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 2, 1, 45, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'IMPL_CROWN_ZIR'
WHERE t.template_code = 'TPL_IMPLANT_OSSTEM' AND tp.phase_number = 3
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_CROWN_CERCON_SIMPLE - Phase 1: Bọc sứ đơn giản (2 services: crown prep + cementing)
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 60, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'CROWN_ZIR_CERCON'
WHERE t.template_code = 'TPL_CROWN_CERCON_SIMPLE' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 2, 1, 30, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'PROS_CEMENT'
WHERE t.template_code = 'TPL_CROWN_CERCON_SIMPLE' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_CROWN_CERCON_ENDO - Phase 1: Điều trị tủy (2 services: endo treatment + post & core)
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 75, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ENDO_TREAT_POST'
WHERE t.template_code = 'TPL_CROWN_CERCON_ENDO' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 2, 1, 45, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ENDO_POST_CORE'
WHERE t.template_code = 'TPL_CROWN_CERCON_ENDO' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_CROWN_CERCON_ENDO - Phase 2: Bọc sứ (2 services: crown prep + cementing)
INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 60, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'CROWN_ZIR_CERCON'
WHERE t.template_code = 'TPL_CROWN_CERCON_ENDO' AND tp.phase_number = 2
ON CONFLICT (phase_id, service_id) DO NOTHING;


INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 2, 1, 30, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'PROS_CEMENT'
WHERE t.template_code = 'TPL_CROWN_CERCON_ENDO' AND tp.phase_number = 2
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- =============================================
--  NEW TEMPLATE PHASES AND SERVICES
-- =============================================

-- TPL_ENDO_TREATMENT: 1 phase (spec 2: Nội nha) - ONLY spec 2 services
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Điều trị tủy răng sau', 3, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_ENDO_TREATMENT'
ON CONFLICT (template_id, phase_number) DO NOTHING;

INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 75, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ENDO_TREAT_POST'
WHERE t.template_code = 'TPL_ENDO_TREATMENT' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_CROWN_AFTER_ENDO: 1 phase (spec 4: Phục hồi răng) - Includes post/core for restored teeth
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Đóng chốt + Bọc răng sứ Cercon HT', 4, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_CROWN_AFTER_ENDO'
ON CONFLICT (template_id, phase_number) DO NOTHING;

INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 45, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'ENDO_POST_CORE'
WHERE t.template_code = 'TPL_CROWN_AFTER_ENDO' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;

INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 2, 1, 60, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'CROWN_ZIR_CERCON'
WHERE t.template_code = 'TPL_CROWN_AFTER_ENDO' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;

INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 3, 1, 30, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'PROS_CEMENT'
WHERE t.template_code = 'TPL_CROWN_AFTER_ENDO' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_PERIO_SCALING: 1 phase (spec 3: Nha chu)
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Cạo vôi răng + Đánh bóng', 1, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_PERIO_SCALING'
ON CONFLICT (template_id, phase_number) DO NOTHING;

INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 45, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'SCALING_L1'
WHERE t.template_code = 'TPL_PERIO_SCALING' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_SURGERY_WISDOM: 1 phase (spec 5: Phẫu thuật)
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Nhổ răng khôn + Tái khám', 7, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_SURGERY_WISDOM'
ON CONFLICT (template_id, phase_number) DO NOTHING;

INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 60, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'EXTRACT_WISDOM_L1'
WHERE t.template_code = 'TPL_SURGERY_WISDOM' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_PEDO_FILLING: 1 phase (spec 6: Nha khoa trẻ em)
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Trám răng sữa', 1, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_PEDO_FILLING'
ON CONFLICT (template_id, phase_number) DO NOTHING;

INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 45, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'FILLING_COMP'
WHERE t.template_code = 'TPL_PEDO_FILLING' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;


-- TPL_COSMETIC_BLEACHING: 1 phase (spec 7: Răng thẩm mỹ)
INSERT INTO template_phases (template_id, phase_number, phase_name, estimated_duration_days, created_at)
SELECT t.template_id, 1, 'Tẩy trắng răng Laser', 1, NOW()
FROM treatment_plan_templates t WHERE t.template_code = 'TPL_COSMETIC_BLEACHING'
ON CONFLICT (template_id, phase_number) DO NOTHING;

INSERT INTO template_phase_services (phase_id, service_id, sequence_number, quantity, estimated_time_minutes, created_at)
SELECT tp.phase_id, s.service_id, 1, 1, 90, NOW()
FROM template_phases tp
JOIN treatment_plan_templates t ON tp.template_id = t.template_id
JOIN services s ON s.service_code = 'BLEACH_INOFFICE'
WHERE t.template_code = 'TPL_COSMETIC_BLEACHING' AND tp.phase_number = 1
ON CONFLICT (phase_id, service_id) DO NOTHING;



-- 4. EMAIL VERIFICATION:
--   - Seeded accounts: ACTIVE (skip verification)
--   - New accounts via API: PENDING_VERIFICATION (require email)
--   - Default password: 123456 (must change on first login)
--
-- ============================================


-- =====================================================
-- =====================================================

-- Fix specialization_code length error
ALTER TABLE specializations ALTER COLUMN specialization_code TYPE varchar(20);

INSERT INTO specializations (specialization_id, specialization_code, specialization_name, description, is_active, created_at)
VALUES
    (901, 'TEST-IMPLANT', 'Test Implant Specialist', 'Chuyên khoa Cấy ghép Implant (Test)', true, CURRENT_TIMESTAMP),
    (902, 'TEST-ORTHO', 'Test Orthodontics', 'Chuyên khoa Chỉnh nha (Test)', true, CURRENT_TIMESTAMP),
    (903, 'TEST-GENERAL', 'Test General Dentistry', 'Nha khoa tổng quát (Test)', true, CURRENT_TIMESTAMP)
ON CONFLICT (specialization_id) DO NOTHING;

-- =====================================================
--  OLD DATA (November 2025) - 8. EMPLOYEE SHIFTS (Test date: 2025-11-15 - Thứ Bảy)
-- Phòng khám KHÔNG làm Chủ nhật - muốn làm phải overtime
-- Full-time: Ca Sáng (8h-12h) + Ca Chiều (13h-17h)
-- Part-time fixed: Ca Part-time Sáng (8h-12h) hoặc Ca Part-time Chiều (13h-17h)
-- Part-time flex: Đăng ký linh hoạt

--  OLD DATA - Dentist 1: Lê Anh Khoa (Full-time) - Ca Sáng
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115001', 1, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Sáng (8h-12h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Dentist 1: Lê Anh Khoa (Full-time) - Ca Chiều
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115001B', 1, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Chiều (13h-17h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Dentist 2: Trịnh Công Thái (Full-time) - Ca Sáng
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115002', 2, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Sáng (8h-12h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Dentist 2: Trịnh Công Thái (Full-time) - Ca Chiều
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115002B', 2, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Chiều (13h-17h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Dentist 3: Jimmy Donaldson (Part-time flex) - Ca Part-time Sáng
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115003', 3, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Part-time Sáng (8h-12h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Dentist 4: Junya Ota (Part-time fixed) - Ca Part-time Chiều
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115004', 4, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Part-time Chiều (13h-17h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Y tá 1: Đoàn Nguyễn Khôi Nguyên (Full-time) - Ca Sáng
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115007', 7, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Sáng (8h-12h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Y tá 1: Đoàn Nguyễn Khôi Nguyên (Full-time) - Ca Chiều
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115007B', 7, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Chiều (13h-17h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Y tá 2: Nguyễn Trần Tuấn Khang (Full-time) - Ca Sáng
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115008A', 8, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Sáng (8h-12h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Y tá 2: Nguyễn Trần Tuấn Khang (Full-time) - Ca Chiều
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115008', 8, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Chiều (13h-17h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Y tá 3: Huỳnh Tấn Quang Nhật (Part-time fixed) - Ca Part-time Sáng
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115009', 9, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Part-time Sáng (8h-12h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- Y tá 4: Ngô Đình Chính (Part-time flex) - Ca Part-time Chiều
INSERT INTO employee_shifts (employee_shift_id, employee_id, work_date, work_shift_id, source, is_overtime, status, created_at)
SELECT 'EMS251115010', 10, DATE '2025-11-15', work_shift_id, 'MANUAL_ENTRY', FALSE, 'SCHEDULED', CURRENT_TIMESTAMP
FROM work_shifts WHERE shift_name = 'Ca Part-time Chiều (13h-17h)' LIMIT 1
ON CONFLICT (employee_shift_id) DO NOTHING;

-- ============================================
--  OLD DATA (November 2025) - SHIFTS FOR 2025-11-21 (FOR TESTING TREATMENT PLAN BOOKING)
-- ============================================

-- Roles (legacy/test file)
INSERT IGNORE INTO roles (role_id, role_name, description, created_at)
VALUES
    (1, 1),  -- GEN_EXAM (service_id=1, first in services table)
    (1, 3)   -- SCALING_L1 (service_id=3, third in services table)
ON CONFLICT (appointment_id, service_id) DO NOTHING;


-- Participants cho APT-001: Y tá + OBSERVER
INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES
    (1, 7, 'ASSISTANT'),    -- EMP007 - Y tá Nguyên
    (1, 12, 'OBSERVER')    -- EMP012 - Thực tập sinh Linh (TEST DATA)
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


--  OLD DATA - APT-002: Lịch hẹn Ca Chiều - Bác sĩ Thái (KHÔNG có OBSERVER)
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    2, 'APT-20251104-002', 2, 2, 'GHE251103002',
    '2025-11-04 14:00:00', '2025-11-04 14:30:00', 30,
    'SCHEDULED', 'Khám tổng quát - NO OBSERVER', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

-- Services cho APT-002
INSERT INTO appointment_services (appointment_id, service_id)
VALUES (2, 1)  -- GEN_EXAM service_id=1
ON CONFLICT (appointment_id, service_id) DO NOTHING;


--  OLD DATA - APT-003: Lịch hẹn LATE (quá giờ 15 phút) - Test computedStatus
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    3, 'APT-20251104-003', 3, 1, 'GHE251103001',
    '2025-11-04 08:00:00', '2025-11-04 08:30:00', 30,
    'SCHEDULED', 'Test LATE status - Bệnh nhân chưa check-in', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

-- Services cho APT-003
INSERT INTO appointment_services (appointment_id, service_id)
VALUES (3, 1)  -- GEN_EXAM service_id=1
ON CONFLICT (appointment_id, service_id) DO NOTHING;


-- Participants cho APT-003: Thực tập sinh Linh làm OBSERVER
INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (3, 12, 'OBSERVER')  -- EMP012 - Thực tập sinh Linh
ON CONFLICT (appointment_id, employee_id) DO NOTHING;



-- ============================================
--  OLD DATA (November 2025) - NEW: FUTURE APPOINTMENTS (Nov 6-8, 2025) for current date testing
-- ============================================

--  OLD DATA - APT-004: Nov 6 Morning - BS Khoa (EMP001) - NOW HAS SHIFT!
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    4, 'APT-20251106-001', 1, 1, 'GHE251103001',
    '2025-11-06 09:00:00', '2025-11-06 09:30:00', 30,
    'IN_PROGRESS', 'Khám tổng quát - BS Khoa ca sáng - Benh nhan dang kham', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES (4, 1)  -- GEN_EXAM
ON CONFLICT (appointment_id, service_id) DO NOTHING;


INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (4, 7, 'ASSISTANT')  -- EMP007 - Y tá Nguyên
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- APT-005: Nov 6 Afternoon - BS Lê Anh Khoa (EMP001) - FIXED: EMP001 has PERIODONTICS specialization
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    5, 'APT-20251106-002', 2, 1, 'GHE251103002',
    '2025-11-06 14:00:00', '2025-11-06 14:45:00', 45,
    'SCHEDULED', 'Lấy cao răng + Khám - BS Khoa ca chiều', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES
    (5, 1),  -- GEN_EXAM
    (5, 3)   -- SCALING_L1 (requires specialization_id=3 PERIODONTICS, EMP001 has it)
ON CONFLICT (appointment_id, service_id) DO NOTHING;

INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (5, 8, 'ASSISTANT')  -- EMP008 - Y tá Khang
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- APT-006: Nov 7 Morning - BS Jimmy (EMP003)
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    6, 'APT-20251107-001', 3, 3, 'GHE251103003',
    '2025-11-07 10:00:00', '2025-11-07 10:30:00', 30,
    'SCHEDULED', 'Khám nha khoa trẻ em - BS Jimmy', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES (6, 1)  -- GEN_EXAM
ON CONFLICT (appointment_id, service_id) DO NOTHING;


INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (6, 7, 'ASSISTANT')  -- EMP007 - Y tá Nguyên
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- APT-007: Nov 7 Afternoon - BS Thái (EMP002) - Can be used for reschedule testing
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    7, 'APT-20251107-002', 4, 2, 'GHE251103002',
    '2025-11-07 15:00:00', '2025-11-07 15:30:00', 30,
    'SCHEDULED', 'Khám định kỳ - BN Mít tơ bít', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES (7, 1)  -- GEN_EXAM
ON CONFLICT (appointment_id, service_id) DO NOTHING;


INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (7, 8, 'ASSISTANT')  -- EMP008 - Y tá Khang
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- APT-008: Nov 8 Morning - BS Khoa (EMP001) - Multiple services
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    8, 'APT-20251108-001', 2, 1, 'GHE251103001',
    '2025-11-08 09:30:00', '2025-11-08 10:15:00', 45,
    'SCHEDULED', 'Lấy cao răng nâng cao - BS Khoa', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES
    (8, 1),  -- GEN_EXAM
    (8, 4)   -- SCALING_L2 (Advanced scaling)
ON CONFLICT (appointment_id, service_id) DO NOTHING;

INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (8, 7, 'ASSISTANT')  -- EMP007 - Y tá Nguyên
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- Reset appointments sequence after seed data
SELECT setval('appointments_appointment_id_seq',
              (SELECT COALESCE(MAX(appointment_id), 0) FROM appointments) + 1,
              false);

-- ============================================
--  NEW DATA (December 2025) - APPOINTMENTS
-- ============================================

-- APT-D001: Dec 4 Morning - BS Khoa + Y tá Nguyên + OBSERVER (EMP012)
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    101, 'APT-20251204-001', 1, 1, 'GHE251103001',
    '2025-12-04 09:00:00', '2025-12-04 09:45:00', 45,
    'SCHEDULED', 'Khám tổng quát + Lấy cao răng - Test OBSERVER', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES
    (101, 1),  -- GEN_EXAM
    (101, 3)   -- SCALING_L1
ON CONFLICT (appointment_id, service_id) DO NOTHING;

INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES
    (101, 7, 'ASSISTANT'),    -- EMP007 - Y tá Nguyên
    (101, 12, 'OBSERVER')    -- EMP012 - Thực tập sinh Linh
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- APT-D002: Dec 4 Afternoon - BS Thái (NO OBSERVER)
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    102, 'APT-20251204-002', 2, 2, 'GHE251103002',
    '2025-12-04 14:00:00', '2025-12-04 14:30:00', 30,
    'SCHEDULED', 'Khám tổng quát - NO OBSERVER', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES (102, 1)  -- GEN_EXAM
ON CONFLICT (appointment_id, service_id) DO NOTHING;


-- APT-D003: Dec 4 Early Morning - Test LATE status
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    103, 'APT-20251204-003', 3, 1, 'GHE251103001',
    '2025-12-04 08:00:00', '2025-12-04 08:30:00', 30,
    'SCHEDULED', 'Test LATE status - Bệnh nhân chưa check-in', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES (103, 1)  -- GEN_EXAM
ON CONFLICT (appointment_id, service_id) DO NOTHING;

INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (103, 12, 'OBSERVER')  -- EMP012 - Thực tập sinh Linh
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- APT-D004: Dec 6 Morning - BS Khoa
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    104, 'APT-20251206-001', 1, 1, 'GHE251103001',
    '2025-12-06 09:00:00', '2025-12-06 09:30:00', 30,
    'SCHEDULED', 'Khám tổng quát - BS Khoa ca sáng', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES (104, 1)  -- GEN_EXAM
ON CONFLICT (appointment_id, service_id) DO NOTHING;


-- APT-D005: Dec 6 Afternoon - BS Minh
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    105, 'APT-20251206-002', 3, 3, 'GHE251103003',
    '2025-12-06 14:30:00', '2025-12-06 15:00:00', 30,
    'SCHEDULED', 'Khám tổng quát - BS Minh ca chiều', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES (105, 1)  -- GEN_EXAM
ON CONFLICT (appointment_id, service_id) DO NOTHING;

INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (105, 8, 'ASSISTANT')  -- EMP008 - Y tá Khang
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- APT-D006: Dec 7 Morning - BS Lan + Multiple services
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    106, 'APT-20251207-001', 4, 4, 'GHE251103004',
    '2025-12-07 10:00:00', '2025-12-07 10:45:00', 45,
    'SCHEDULED', 'Khám tổng quát + Nhổ răng - BS Lan', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES
    (106, 1),  -- GEN_EXAM
    (106, 7)   -- EXTRACTION (Nhổ răng)
ON CONFLICT (appointment_id, service_id) DO NOTHING;

INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (106, 7, 'ASSISTANT')  -- EMP007 - Y tá Nguyên
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- APT-D007: Dec 7 Afternoon - BS Thái
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    107, 'APT-20251207-002', 4, 2, 'GHE251103002',
    '2025-12-07 15:00:00', '2025-12-07 15:30:00', 30,
    'SCHEDULED', 'Khám định kỳ - BN Mít tơ bít', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES (107, 1)  -- GEN_EXAM
ON CONFLICT (appointment_id, service_id) DO NOTHING;

INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (107, 8, 'ASSISTANT')  -- EMP008 - Y tá Khang
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- APT-D008: Dec 8 Morning - BS Khoa + Multiple services
INSERT INTO appointments (
    appointment_id, appointment_code, patient_id, employee_id, room_id,
    appointment_start_time, appointment_end_time, expected_duration_minutes,
    status, notes, created_by, created_at, updated_at
) VALUES (
    108, 'APT-20251208-001', 2, 1, 'GHE251103001',
    '2025-12-08 09:30:00', '2025-12-08 10:15:00', 45,
    'SCHEDULED', 'Lấy cao răng nâng cao - BS Khoa', 5, NOW(), NOW()
)
ON CONFLICT (appointment_id) DO NOTHING;

INSERT INTO appointment_services (appointment_id, service_id)
VALUES
    (108, 1),  -- GEN_EXAM
    (108, 4)   -- SCALING_L2 (Advanced scaling)
ON CONFLICT (appointment_id, service_id) DO NOTHING;

INSERT INTO appointment_participants (appointment_id, employee_id, participant_role)
VALUES (108, 7, 'ASSISTANT')  -- EMP007 - Y tá Nguyên
ON CONFLICT (appointment_id, employee_id) DO NOTHING;


-- ============================================
--  NEW DATA (January 2026) - APPOINTMENTS
-- ============================================
