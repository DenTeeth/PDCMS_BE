package com.dental.clinic.management.treatment_plans.enums;

/**
 * Status of a treatment plan item (task/checklist).
 */
public enum PlanItemStatus {
    /**
     * Item pending approval/activation (newly created from template).
     * Waiting for doctor to activate the treatment plan.
     */
    PENDING,

    /**
     * Item ready to be scheduled for appointment
     */
    READY_FOR_BOOKING,

    /**
     * Item scheduled in an appointment
     */
    SCHEDULED,

    /**
     * Item currently being performed
     */
    IN_PROGRESS,

    /**
     * Item completed successfully
     */
    COMPLETED,

    /**
     * Item skipped (not needed or cancelled by patient)
     */
    SKIPPED
}
