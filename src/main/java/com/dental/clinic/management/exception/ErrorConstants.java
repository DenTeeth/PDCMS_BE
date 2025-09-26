package com.dental.clinic.management.exception;

import java.net.URI;

/**
 * Application constants for error handling and problem detail types.
 */
public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://www.jhipster.tech/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
    public static final URI ACCOUNT_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/account-not-found");
    public static final URI BAD_CREDENTIALS_TYPE = URI.create(PROBLEM_BASE_URL + "/bad-credentials");

    private ErrorConstants() {
    }
}
