package com.dental.clinic.management.utils.security;

public final class AuthoritiesConstants {

    private AuthoritiesConstants() {
    } // ngăn không cho new

    // Roles
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String USER = "ROLE_USER";
    public static final String ACCOUNTANT = "ROLE_ACCOUNTANT";
    public static final String DOCTOR = "ROLE_DOCTOR";
    public static final String RECEPTIONIST = "ROLE_RECEPTIONIST";
    public static final String WAREHOUSE_MANAGER = "ROLE_WAREHOUSE_MANAGER";

    // Permissions
    public static final String READ_ALL_EMPLOYEES = "READ_ALL_EMPLOYEES";
    public static final String READ_EMPLOYEE_BY_CODE = "READ_EMPLOYEE_BY_CODE";
    public static final String CREATE_EMPLOYEE = "CREATE_EMPLOYEE";
    public static final String UPDATE_EMPLOYEE = "UPDATE_EMPLOYEE";
    public static final String DELETE_EMPLOYEE = "DELETE_EMPLOYEE";

    // Patient Permissions
    public static final String VIEW_PATIENT = "VIEW_PATIENT";
    public static final String CREATE_PATIENT = "CREATE_PATIENT";
    public static final String UPDATE_PATIENT = "UPDATE_PATIENT";
    public static final String DELETE_PATIENT = "DELETE_PATIENT";// soft delete

    // Appointment Permissions
    public static final String VIEW_APPOINTMENT = "VIEW_APPOINTMENT";
    public static final String CREATE_APPOINTMENT = "CREATE_APPOINTMENT";
    public static final String UPDATE_APPOINTMENT = "UPDATE_APPOINTMENT";
    public static final String CANCEL_APPOINTMENT = "CANCEL_APPOINTMENT";
    public static final String DELETE_APPOINTMENT = "DELETE_APPOINTMENT";

    // Contact (Customer Contacts) Permissions
    public static final String VIEW_CONTACT = "VIEW_CONTACT";
    public static final String CREATE_CONTACT = "CREATE_CONTACT";
    public static final String UPDATE_CONTACT = "UPDATE_CONTACT";
    public static final String DELETE_CONTACT = "DELETE_CONTACT"; // soft delete if implemented

    // Contact History Permissions
    public static final String VIEW_CONTACT_HISTORY = "VIEW_CONTACT_HISTORY";
    public static final String CREATE_CONTACT_HISTORY = "CREATE_CONTACT_HISTORY";
    public static final String UPDATE_CONTACT_HISTORY = "UPDATE_CONTACT_HISTORY";
    public static final String DELETE_CONTACT_HISTORY = "DELETE_CONTACT_HISTORY"; // soft delete if implemented

    // Specialization Permissions
    public static final String VIEW_SPECIALIZATION = "VIEW_SPECIALIZATION";
    public static final String CREATE_SPECIALIZATION = "CREATE_SPECIALIZATION";

    // Employee Shift Registration Permissions
    public static final String VIEW_REGISTRATION_ALL = "VIEW_REGISTRATION_ALL";
    public static final String VIEW_REGISTRATION_OWN = "VIEW_REGISTRATION_OWN";
    public static final String CREATE_REGISTRATION = "CREATE_REGISTRATION";
    public static final String UPDATE_REGISTRATION_ALL = "UPDATE_REGISTRATION_ALL";
    public static final String UPDATE_REGISTRATION_OWN = "UPDATE_REGISTRATION_OWN";
    public static final String DELETE_REGISTRATION_ALL = "DELETE_REGISTRATION_ALL";
    public static final String DELETE_REGISTRATION_OWN = "DELETE_REGISTRATION_OWN";
}
