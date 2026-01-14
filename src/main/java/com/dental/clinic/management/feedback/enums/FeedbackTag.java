package com.dental.clinic.management.feedback.enums;

/**
 * Predefined feedback tags
 * Các tag được định nghĩa sẵn để người dùng chọn
 */
public enum FeedbackTag {
    CLEAN("Sạch sẽ"),
    FRIENDLY("Thân thiện"),
    PROFESSIONAL("Chuyên nghiệp"),
    ON_TIME("Đúng giờ"),
    DETAILED_CONSULTATION("Tư vấn kỹ"),
    GENTLE("Nhẹ nhàng"),
    REASONABLE_PRICE("Giá hợp lý"),
    GOOD_FACILITIES("Cơ sở vật chất tốt");

    private final String displayName;

    FeedbackTag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
