package com.dental.clinic.management.exception.time_off;

public class InsufficientLeaveBalanceException extends RuntimeException {
    private final double remaining;
    private final double requested;

    public InsufficientLeaveBalanceException(double remaining, double requested) {
        super("Số dư nghỉ phép không đủ. Còn lại: " + remaining + " ngày, yêu cầu: " + requested + " ngày");
        this.remaining = remaining;
        this.requested = requested;
    }

    public double getRemaining() {
        return remaining;
    }

    public double getRequested() {
        return requested;
    }
}
