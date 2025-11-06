package com.dental.clinic.management.working_schedule.enums;

/**
 * Enum representing the status of part-time registration requests.
 * 
 * NEW SPECIFICATION (Approval Workflow):
 * - PENDING: Registration submitted, waiting for manager approval
 * - APPROVED: Registration approved, employee can work
 * - REJECTED: Registration rejected by manager
 * 
 * Only APPROVED registrations count toward quota.
 */
public enum RegistrationStatus {
    /**
     * Registration is pending manager approval.
     * Ã„ÂÃ„Æ’ng kÃƒÂ½ Ã„â€˜ang chÃ¡Â»Â quÃ¡ÂºÂ£n lÃƒÂ½ duyÃ¡Â»â€¡t.
     */
    PENDING,

    /**
     * Registration has been approved by manager.
     * Employee can work during the registered period.
     * Ã„ÂÃ„Æ’ng kÃƒÂ½ Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c duyÃ¡Â»â€¡t, nhÃƒÂ¢n viÃƒÂªn cÃƒÂ³ thÃ¡Â»Æ’ lÃƒÂ m viÃ¡Â»â€¡c.
     */
    APPROVED,

    /**
     * Registration has been rejected by manager.
     * Reason must be provided.
     * Ã„ÂÃ„Æ’ng kÃƒÂ½ Ã„â€˜ÃƒÂ£ bÃ¡Â»â€¹ tÃ¡Â»Â« chÃ¡Â»â€˜i, phÃ¡ÂºÂ£i cÃƒÂ³ lÃƒÂ½ do.
     */
    REJECTED
}
