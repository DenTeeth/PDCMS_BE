package com.dental.clinic.management.working_schedule.exception;

import java.time.LocalDate;

/**
 * Exception thrown when registration dates fall outside the slot's effective date range.
 */
public class DateOutsideSlotRangeException extends RuntimeException {
    private final LocalDate slotEffectiveFrom;
    private final LocalDate slotEffectiveTo;
    private final LocalDate requestedFrom;
    private final LocalDate requestedTo;

    public DateOutsideSlotRangeException(LocalDate slotEffectiveFrom, LocalDate slotEffectiveTo,
                                          LocalDate requestedFrom, LocalDate requestedTo) {
        super(String.format("Ngày đăng ký phải nằm trong khoảng hiệu lực của ca (%s đến %s). Ngày yêu cầu: %s đến %s",
                slotEffectiveFrom, slotEffectiveTo, requestedFrom, requestedTo));
        this.slotEffectiveFrom = slotEffectiveFrom;
        this.slotEffectiveTo = slotEffectiveTo;
        this.requestedFrom = requestedFrom;
        this.requestedTo = requestedTo;
    }

    public LocalDate getSlotEffectiveFrom() {
        return slotEffectiveFrom;
    }

    public LocalDate getSlotEffectiveTo() {
        return slotEffectiveTo;
    }

    public LocalDate getRequestedFrom() {
        return requestedFrom;
    }

    public LocalDate getRequestedTo() {
        return requestedTo;
    }
}
