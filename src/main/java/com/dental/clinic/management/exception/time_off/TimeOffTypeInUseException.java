package com.dental.clinic.management.exception.time_off;

public class TimeOffTypeInUseException extends RuntimeException {
    public TimeOffTypeInUseException(String typeId) {
        super("Không thể xóa loại nghỉ phép " + typeId + ". Có các yêu cầu đang hoạt động sử dụng loại này.");
    }
}
