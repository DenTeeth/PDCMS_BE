package com.dental.clinic.management.utils.security;

/**
 * Permission Constants - Optimized for Small-Medium Dental Clinic
 *
 * OPTIMIZATION RESULTS (2025-12-22):
 * - BEFORE: 169 permissions (74% unused in controllers!)
 * - AFTER: 66 permissions (synchronized with database)
 * - STRATEGY: CRUD → MANAGE_X consolidation + Keep RBAC patterns + Workflow separation
 *
 * CONSOLIDATION PATTERN:
 * - CREATE_X + UPDATE_X + DELETE_X → MANAGE_X
 * - RBAC: VIEW_ALL (Manager/Admin) vs VIEW_OWN (Employee/Patient)
 * - WORKFLOW: Keep APPROVE_X for business logic
 *
 * NOTE: All constants match the permissions table in database (66 total)
 */
public final class AuthoritiesConstants {

    private AuthoritiesConstants() {
    } // Prevent instantiation

    // ============================================
    // ROLES
    // ============================================
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String MANAGER = "ROLE_MANAGER";
    public static final String USER = "ROLE_USER";
    public static final String ACCOUNTANT = "ROLE_ACCOUNTANT";
    public static final String DOCTOR = "ROLE_DOCTOR";
    public static final String RECEPTIONIST = "ROLE_RECEPTIONIST";
    public static final String WAREHOUSE_MANAGER = "ROLE_WAREHOUSE_MANAGER";

    // ============================================
    // MODULE 1: ACCOUNT (2 permissions)
    // ============================================
    public static final String VIEW_ACCOUNT = "VIEW_ACCOUNT";
    public static final String MANAGE_ACCOUNT = "MANAGE_ACCOUNT"; // Create + Update + Delete

    // ============================================
    // MODULE 2: EMPLOYEE (3 permissions)
    // ============================================
    public static final String VIEW_EMPLOYEE = "VIEW_EMPLOYEE";
    public static final String MANAGE_EMPLOYEE = "MANAGE_EMPLOYEE"; // Create + Update
    public static final String DELETE_EMPLOYEE = "DELETE_EMPLOYEE"; // Separate (soft delete - critical operation)

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use VIEW_EMPLOYEE - Redundant read operation */
    // @Deprecated
    // public static final String READ_ALL_EMPLOYEES = "READ_ALL_EMPLOYEES";
    // /** @deprecated Use VIEW_EMPLOYEE - Redundant read operation */
    // @Deprecated
    // public static final String READ_EMPLOYEE_BY_CODE = "READ_EMPLOYEE_BY_CODE";
    // /** @deprecated Use MANAGE_EMPLOYEE */
    // @Deprecated
    // public static final String CREATE_EMPLOYEE = "CREATE_EMPLOYEE";
    // /** @deprecated Use MANAGE_EMPLOYEE */
    // @Deprecated
    // public static final String UPDATE_EMPLOYEE = "UPDATE_EMPLOYEE";

    // ============================================
    // MODULE 3: PATIENT (3 permissions)
    // ============================================
    public static final String VIEW_PATIENT = "VIEW_PATIENT";
    public static final String MANAGE_PATIENT = "MANAGE_PATIENT"; // Create + Update
    public static final String DELETE_PATIENT = "DELETE_PATIENT"; // Separate (soft delete - critical)

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_PATIENT */
    // @Deprecated
    // public static final String CREATE_PATIENT = "CREATE_PATIENT";
    // /** @deprecated Use MANAGE_PATIENT */
    // @Deprecated
    // public static final String UPDATE_PATIENT = "UPDATE_PATIENT";

    // ============================================
    // MODULE 4: APPOINTMENT (5 permissions) - Kept RBAC + Consolidated actions
    // ============================================
    public static final String VIEW_APPOINTMENT_ALL = "VIEW_APPOINTMENT_ALL"; // Manager/Admin
    public static final String VIEW_APPOINTMENT_OWN = "VIEW_APPOINTMENT_OWN"; // Employee/Patient (RBAC)
    public static final String CREATE_APPOINTMENT = "CREATE_APPOINTMENT";
    public static final String UPDATE_APPOINTMENT_STATUS = "UPDATE_APPOINTMENT_STATUS"; // Status transitions
    public static final String MANAGE_APPOINTMENT = "MANAGE_APPOINTMENT"; // Update + Delay + Cancel + Delete

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_APPOINTMENT */
    // @Deprecated
    // public static final String UPDATE_APPOINTMENT = "UPDATE_APPOINTMENT";
    // /** @deprecated Use MANAGE_APPOINTMENT */
    // @Deprecated
    // public static final String DELAY_APPOINTMENT = "DELAY_APPOINTMENT";
    // /** @deprecated Use MANAGE_APPOINTMENT */
    // @Deprecated
    // public static final String RESCHEDULE_APPOINTMENT = "RESCHEDULE_APPOINTMENT";
    // /** @deprecated Use MANAGE_APPOINTMENT */
    // @Deprecated
    // public static final String CANCEL_APPOINTMENT = "CANCEL_APPOINTMENT";
    // /** @deprecated Use MANAGE_APPOINTMENT */
    // @Deprecated
    // public static final String DELETE_APPOINTMENT = "DELETE_APPOINTMENT";

    // ============================================
    // MODULE 5: CLINICAL_RECORDS (4 permissions)
    // ============================================
    public static final String WRITE_CLINICAL_RECORD = "WRITE_CLINICAL_RECORD"; // Create + Update (9 usages!)
    public static final String VIEW_ATTACHMENT = "VIEW_ATTACHMENT";
    public static final String MANAGE_ATTACHMENTS = "MANAGE_ATTACHMENTS"; // Upload + Delete
    public static final String VIEW_VITAL_SIGNS_REFERENCE = "VIEW_VITAL_SIGNS_REFERENCE";

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_ATTACHMENTS */
    // @Deprecated
    // public static final String UPLOAD_ATTACHMENT = "UPLOAD_ATTACHMENT";
    // /** @deprecated Use MANAGE_ATTACHMENTS */
    // @Deprecated
    // public static final String DELETE_ATTACHMENT = "DELETE_ATTACHMENT";

    // ============================================
    // MODULE 6: PATIENT_IMAGES (3 permissions)
    // ============================================
    public static final String PATIENT_IMAGE_READ = "PATIENT_IMAGE_READ"; // View patient images
    public static final String MANAGE_PATIENT_IMAGES = "MANAGE_PATIENT_IMAGES"; // Create + Update + Delete
    public static final String DELETE_PATIENT_IMAGES = "DELETE_PATIENT_IMAGES"; // Permanent delete (Admin/Uploader)

    // ============================================
    // MODULE 7: NOTIFICATION (3 permissions) - Already optimal
    // ============================================
    public static final String VIEW_NOTIFICATION = "VIEW_NOTIFICATION";
    public static final String DELETE_NOTIFICATION = "DELETE_NOTIFICATION";
    public static final String MANAGE_NOTIFICATION = "MANAGE_NOTIFICATION"; // Admin only

    // ============================================
    // MODULE 8: HOLIDAY (2 permissions)
    // ============================================
    public static final String VIEW_HOLIDAY = "VIEW_HOLIDAY";
    public static final String MANAGE_HOLIDAY = "MANAGE_HOLIDAY"; // Create + Update + Delete

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_HOLIDAY */
    // @Deprecated
    // public static final String CREATE_HOLIDAY = "CREATE_HOLIDAY";
    // /** @deprecated Use MANAGE_HOLIDAY */
    // @Deprecated
    // public static final String UPDATE_HOLIDAY = "UPDATE_HOLIDAY";
    // /** @deprecated Use MANAGE_HOLIDAY */
    // @Deprecated
    // public static final String DELETE_HOLIDAY = "DELETE_HOLIDAY";

    // ============================================
    // MODULE 9: SERVICE (2 permissions)
    // ============================================
    public static final String VIEW_SERVICE = "VIEW_SERVICE";
    public static final String MANAGE_SERVICE = "MANAGE_SERVICE"; // Create + Update + Delete

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_SERVICE */
    // @Deprecated
    // public static final String CREATE_SERVICE = "CREATE_SERVICE";
    // /** @deprecated Use MANAGE_SERVICE */
    // @Deprecated
    // public static final String UPDATE_SERVICE = "UPDATE_SERVICE";
    // /** @deprecated Use MANAGE_SERVICE */
    // @Deprecated
    // public static final String DELETE_SERVICE = "DELETE_SERVICE";

    // ============================================
    // MODULE 10: ROOM (2 permissions)
    // ============================================
    public static final String VIEW_ROOM = "VIEW_ROOM";
    public static final String MANAGE_ROOM = "MANAGE_ROOM"; // Create + Update + Delete + Assign services

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_ROOM */
    // @Deprecated
    // public static final String CREATE_ROOM = "CREATE_ROOM";
    // /** @deprecated Use MANAGE_ROOM */
    // @Deprecated
    // public static final String UPDATE_ROOM = "UPDATE_ROOM";
    // /** @deprecated Use MANAGE_ROOM */
    // @Deprecated
    // public static final String UPDATE_ROOM_SERVICES = "UPDATE_ROOM_SERVICES";
    // /** @deprecated Use MANAGE_ROOM */
    // @Deprecated
    // public static final String DELETE_ROOM = "DELETE_ROOM";

    // ============================================
    // MODULE 11: WAREHOUSE (10 permissions) - Kept granular (high usage)
    // ============================================
    public static final String VIEW_WAREHOUSE = "VIEW_WAREHOUSE"; // 22 usages! Keep granular
    public static final String VIEW_ITEMS = "VIEW_ITEMS"; // For dentist/receptionist
    public static final String VIEW_MEDICINES = "VIEW_MEDICINES"; // For prescription
    public static final String VIEW_WAREHOUSE_COST = "VIEW_WAREHOUSE_COST"; // Admin/Accountant only
    public static final String MANAGE_WAREHOUSE = "MANAGE_WAREHOUSE"; // Categories, items, suppliers
    public static final String MANAGE_SUPPLIERS = "MANAGE_SUPPLIERS"; // Supplier management
    public static final String IMPORT_ITEMS = "IMPORT_ITEMS"; // Import transactions
    public static final String EXPORT_ITEMS = "EXPORT_ITEMS"; // Export transactions
    public static final String DISPOSE_ITEMS = "DISPOSE_ITEMS"; // Disposal transactions
    public static final String APPROVE_TRANSACTION = "APPROVE_TRANSACTION"; // Approve/reject workflow

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_WAREHOUSE */
    // @Deprecated
    // public static final String CREATE_WAREHOUSE = "CREATE_WAREHOUSE";
    // /** @deprecated Use MANAGE_WAREHOUSE */
    // @Deprecated
    // public static final String UPDATE_WAREHOUSE = "UPDATE_WAREHOUSE";
    // /** @deprecated Use MANAGE_WAREHOUSE */
    // @Deprecated
    // public static final String DELETE_WAREHOUSE = "DELETE_WAREHOUSE";

    // ============================================
    // MODULE 12: SCHEDULE_MANAGEMENT (9 permissions) - Optimized
    // ============================================
    public static final String VIEW_SCHEDULE_ALL = "VIEW_SCHEDULE_ALL"; // Manager view all
    public static final String VIEW_SCHEDULE_OWN = "VIEW_SCHEDULE_OWN"; // Employee view own (RBAC)
    public static final String VIEW_AVAILABLE_SLOTS = "VIEW_AVAILABLE_SLOTS"; // Part-time employees view available slots
    public static final String MANAGE_WORK_SHIFTS = "MANAGE_WORK_SHIFTS"; // Shift templates
    public static final String VIEW_REGISTRATION_OWN = "VIEW_REGISTRATION_OWN"; // Employee view own registrations (RBAC)
    public static final String MANAGE_WORK_SLOTS = "MANAGE_WORK_SLOTS"; // Part-time slots
    public static final String CREATE_REGISTRATION = "CREATE_REGISTRATION"; // Part-time employees register for slots
    public static final String MANAGE_PART_TIME_REGISTRATIONS = "MANAGE_PART_TIME_REGISTRATIONS"; // Part-time shift registrations
    public static final String MANAGE_FIXED_REGISTRATIONS = "MANAGE_FIXED_REGISTRATIONS"; // Fixed shift registrations

    // ============================================
    // MODULE 12A: SHIFT_RENEWAL (3 permissions) - Fixed Schedule Renewal (Luồng 1 only)
    // ============================================
    public static final String VIEW_RENEWAL_OWN = "VIEW_RENEWAL_OWN"; // View own renewal requests
    public static final String RESPOND_RENEWAL_OWN = "RESPOND_RENEWAL_OWN"; // Respond to own renewal requests
    public static final String VIEW_RENEWAL_ALL = "VIEW_RENEWAL_ALL"; // View all renewal requests (Admin/Manager)

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_WORK_SHIFTS */
    // @Deprecated
    // public static final String VIEW_WORK_SHIFTS = "VIEW_WORK_SHIFTS";
    // /** @deprecated Use MANAGE_WORK_SHIFTS */
    // @Deprecated
    // public static final String CREATE_WORK_SHIFTS = "CREATE_WORK_SHIFTS";
    // /** @deprecated Use MANAGE_WORK_SHIFTS */
    // @Deprecated
    // public static final String UPDATE_WORK_SHIFTS = "UPDATE_WORK_SHIFTS";
    // /** @deprecated Use MANAGE_WORK_SHIFTS */
    // @Deprecated
    // public static final String DELETE_WORK_SHIFTS = "DELETE_WORK_SHIFTS";

    // ============================================
    // MODULE 13: LEAVE_MANAGEMENT (8 permissions) - Kept workflow separation
    // ============================================
    public static final String VIEW_LEAVE_ALL = "VIEW_LEAVE_ALL"; // Manager view all
    public static final String VIEW_LEAVE_OWN = "VIEW_LEAVE_OWN"; // Employee view own (RBAC)
    public static final String VIEW_OT_ALL = "VIEW_OT_ALL"; // CRITICAL: Manager view overtime
    public static final String VIEW_OT_OWN = "VIEW_OT_OWN"; // CRITICAL: Employee view own overtime (RBAC)
    public static final String CREATE_TIME_OFF = "CREATE_TIME_OFF";
    public static final String APPROVE_TIME_OFF = "APPROVE_TIME_OFF"; // Workflow
    public static final String CREATE_OVERTIME = "CREATE_OVERTIME";
    public static final String APPROVE_OVERTIME = "APPROVE_OVERTIME"; // Workflow

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use APPROVE_TIME_OFF */
    // @Deprecated
    // public static final String REJECT_TIME_OFF = "REJECT_TIME_OFF";
    // /**
    //  * @deprecated Use CREATE_TIME_OFF (employee cancels own) or APPROVE_TIME_OFF
    //  *             (manager cancels)
    //  */
    // @Deprecated
    // public static final String CANCEL_TIME_OFF = "CANCEL_TIME_OFF";
    // /** @deprecated Use APPROVE_OVERTIME */
    // @Deprecated
    // public static final String REJECT_OVERTIME = "REJECT_OVERTIME";
    // /**
    //  * @deprecated Use CREATE_OVERTIME (employee cancels own) or APPROVE_OVERTIME
    //  *             (manager cancels)
    //  */
    // @Deprecated
    // public static final String CANCEL_OVERTIME = "CANCEL_OVERTIME";
    //
    // // Backwards compatibility aliases for old code
    // /** @deprecated Use VIEW_LEAVE_ALL */
    // @Deprecated
    // public static final String VIEW_TIMEOFF_ALL = VIEW_LEAVE_ALL;
    // /** @deprecated Use VIEW_LEAVE_OWN */
    // @Deprecated
    // public static final String VIEW_TIMEOFF_OWN = VIEW_LEAVE_OWN;
    // /** @deprecated Use CREATE_TIME_OFF */
    // @Deprecated
    // public static final String CREATE_TIMEOFF = CREATE_TIME_OFF;
    // /** @deprecated Use APPROVE_TIME_OFF */
    // @Deprecated
    // public static final String APPROVE_TIMEOFF = APPROVE_TIME_OFF;
    // /** @deprecated Use APPROVE_TIME_OFF */
    // @Deprecated
    // public static final String REJECT_TIMEOFF = REJECT_TIME_OFF;
    // /** @deprecated Use CREATE_TIME_OFF */
    // @Deprecated
    // public static final String CANCEL_TIMEOFF_OWN = CANCEL_TIME_OFF;
    // /** @deprecated Use APPROVE_TIME_OFF */
    // @Deprecated
    // public static final String CANCEL_TIMEOFF_PENDING = CANCEL_TIME_OFF;

    // ============================================
    // MODULE 14: TREATMENT_PLAN (5 permissions) - Kept RBAC
    // ============================================
    public static final String VIEW_TREATMENT_PLAN_ALL = "VIEW_TREATMENT_PLAN_ALL"; // Staff view all
    public static final String VIEW_TREATMENT_PLAN_OWN = "VIEW_TREATMENT_PLAN_OWN"; // Patient view own (RBAC)
    public static final String MANAGE_TREATMENT_PLAN = "MANAGE_TREATMENT_PLAN"; // Create + Update + Delete
    public static final String VIEW_TREATMENT = "VIEW_TREATMENT"; // View treatment items
    public static final String MANAGE_TREATMENT = "MANAGE_TREATMENT"; // Create + Update + Assign doctor

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_TREATMENT_PLAN */
    // @Deprecated
    // public static final String CREATE_TREATMENT_PLAN = "CREATE_TREATMENT_PLAN";
    // /** @deprecated Use MANAGE_TREATMENT_PLAN */
    // @Deprecated
    // public static final String UPDATE_TREATMENT_PLAN = "UPDATE_TREATMENT_PLAN";
    // /** @deprecated Use MANAGE_TREATMENT_PLAN */
    // @Deprecated
    // public static final String DELETE_TREATMENT_PLAN = "DELETE_TREATMENT_PLAN";
    // /** @deprecated Use MANAGE_TREATMENT_PLAN */
    // @Deprecated
    // public static final String APPROVE_TREATMENT_PLAN = "APPROVE_TREATMENT_PLAN";
    // /** @deprecated Use VIEW_TREATMENT_PLAN_ALL */
    // @Deprecated
    // public static final String VIEW_ALL_TREATMENT_PLANS = VIEW_TREATMENT_PLAN_ALL;
    // /** @deprecated Use MANAGE_TREATMENT_PLAN */
    // @Deprecated
    // public static final String MANAGE_PLAN_PRICING = "MANAGE_PLAN_PRICING";
    //
    // /** @deprecated Use MANAGE_TREATMENT */
    // @Deprecated
    // public static final String CREATE_TREATMENT = "CREATE_TREATMENT";
    // /** @deprecated Use MANAGE_TREATMENT */
    // @Deprecated
    // public static final String UPDATE_TREATMENT = "UPDATE_TREATMENT";
    // /** @deprecated Use MANAGE_TREATMENT */
    // @Deprecated
    // public static final String DELETE_TREATMENT = "DELETE_TREATMENT";
    // /** @deprecated Use MANAGE_TREATMENT */
    // @Deprecated
    // public static final String ASSIGN_DOCTOR_TO_ITEM = "ASSIGN_DOCTOR_TO_ITEM";

    // ============================================
    // MODULE 15: SYSTEM_CONFIGURATION (6 permissions)
    // ============================================
    public static final String VIEW_ROLE = "VIEW_ROLE";
    public static final String MANAGE_ROLE = "MANAGE_ROLE"; // Create + Update + Delete
    public static final String VIEW_PERMISSION = "VIEW_PERMISSION";
    public static final String MANAGE_PERMISSION = "MANAGE_PERMISSION"; // Create + Update + Delete
    public static final String VIEW_SPECIALIZATION = "VIEW_SPECIALIZATION";
    public static final String MANAGE_SPECIALIZATION = "MANAGE_SPECIALIZATION"; // Create + Update + Delete

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use MANAGE_SPECIALIZATION */
    // @Deprecated
    // public static final String CREATE_SPECIALIZATION = "CREATE_SPECIALIZATION";

    // ============================================
    // MODULE 16: CUSTOMER_CONTACT (2 permissions)
    // ============================================
    public static final String VIEW_CUSTOMER_CONTACT = "VIEW_CUSTOMER_CONTACT";
    public static final String MANAGE_CUSTOMER_CONTACT = "MANAGE_CUSTOMER_CONTACT"; // Contact + History CRUD

    // ===== NOT IN DATABASE - Commented out for reference =====
    // /** @deprecated Use VIEW_CUSTOMER_CONTACT */
    // @Deprecated
    // public static final String VIEW_CONTACT = "VIEW_CONTACT";
    // /** @deprecated Use MANAGE_CUSTOMER_CONTACT */
    // @Deprecated
    // public static final String CREATE_CONTACT = "CREATE_CONTACT";
    // /** @deprecated Use MANAGE_CUSTOMER_CONTACT */
    // @Deprecated
    // public static final String UPDATE_CONTACT = "UPDATE_CONTACT";
    // /** @deprecated Use MANAGE_CUSTOMER_CONTACT */
    // @Deprecated
    // public static final String DELETE_CONTACT = "DELETE_CONTACT";
    // /** @deprecated Use VIEW_CUSTOMER_CONTACT */
    // @Deprecated
    // public static final String VIEW_CONTACT_HISTORY = "VIEW_CONTACT_HISTORY";
    // /** @deprecated Use MANAGE_CUSTOMER_CONTACT */
    // @Deprecated
    // public static final String CREATE_CONTACT_HISTORY = "CREATE_CONTACT_HISTORY";
    // /** @deprecated Use MANAGE_CUSTOMER_CONTACT */
    // @Deprecated
    // public static final String UPDATE_CONTACT_HISTORY = "UPDATE_CONTACT_HISTORY";
    // /** @deprecated Use MANAGE_CUSTOMER_CONTACT */
    // @Deprecated
    // public static final String DELETE_CONTACT_HISTORY = "DELETE_CONTACT_HISTORY";
}
