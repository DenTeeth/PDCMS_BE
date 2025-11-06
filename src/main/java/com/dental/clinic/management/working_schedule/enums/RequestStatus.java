package com.dental.clinic.management.working_schedule.enums;

/**
 * Enum representing the status of various requests (leave, overtime, etc.).
 */
public enum RequestStatus {
    /**
     * Request is pending approval.
     * YÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜ang chÃ¡Â»Â duyÃ¡Â»â€¡t.
     */
    PENDING,

    /**
     * Request has been approved.
     * YÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c duyÃ¡Â»â€¡t.
     */
    APPROVED,

    /**
     * Request has been rejected.
     * YÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜ÃƒÂ£ bÃ¡Â»â€¹ tÃ¡Â»Â« chÃ¡Â»â€˜i.
     */
    REJECTED,

    /**
     * Request was cancelled by the creator before approval.
     * YÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜ÃƒÂ£ bÃ¡Â»â€¹ ngÃ†Â°Ã¡Â»Âi tÃ¡ÂºÂ¡o hÃ¡Â»Â§y trÃ†Â°Ã¡Â»â€ºc khi Ã„â€˜Ã†Â°Ã¡Â»Â£c duyÃ¡Â»â€¡t.
     */
    CANCELLED
}
