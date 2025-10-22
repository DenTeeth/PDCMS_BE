package com.dental.clinic.management.exception;

public class InsufficientLeaveBalanceException extends RuntimeException {
    public InsufficientLeaveBalanceException(double remaining, double requested) {
        super("Số dư nghỉ phép không đủ. Còn lại: " + remaining + " ngày, yêu cầu: " + requested + " ngày");
    }
}
