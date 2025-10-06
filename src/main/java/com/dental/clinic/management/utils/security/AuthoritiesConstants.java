package com.dental.clinic.management.utils.security;

public final class AuthoritiesConstants {

    private AuthoritiesConstants() {
    } // ngăn không cho new

    // Roles
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String USER = "ROLE_USER";

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
    public static final String DELETE_PATIENT = "DELETE_PATIENT";
}
