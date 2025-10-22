package com.dental.clinic.management.working_schedule.enums;

/**
 * Status for shift renewal requests.
 * Represents the lifecycle of a part-time employee's shift renewal invitation.
 */
public enum RenewalStatus {
    /**
     * Renewal request created, awaiting employee response.
     */
    PENDING_ACTION,

    /**
     * Employee confirmed the renewal.
     * The original shift registration has been extended.
     */
    CONFIRMED,

    /**
     * Employee declined the renewal.
     * The shift registration will expire as originally scheduled.
     */
    DECLINED,

    /**
     * Renewal request expired (employee did not respond in time).
     * The shift registration will expire as originally scheduled.
     */
    EXPIRED
}
