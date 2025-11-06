package com.dental.clinic.management.booking_appointment.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * DatePreset Enum - CÃƒÂ¡c tÃƒÂ¹y chÃ¡Â»Ân lÃ¡Â»Âc theo khoÃ¡ÂºÂ£ng thÃ¡Â»Âi gian Ã„â€˜Ã¡Â»â€¹nh sÃ¡ÂºÂµn
 * Backend tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng tÃƒÂ­nh dateFrom/dateTo, KHÃƒâ€NG cÃ¡ÂºÂ§n thay Ã„â€˜Ã¡Â»â€¢i DB Schema
 */
public enum DatePreset {
    TODAY, // HÃƒÂ´m nay
    THIS_WEEK, // TuÃ¡ÂºÂ§n nÃƒÂ y (Monday -> Sunday)
    NEXT_7_DAYS, // 7 ngÃƒÂ y tiÃ¡ÂºÂ¿p theo
    THIS_MONTH; // ThÃƒÂ¡ng nÃƒÂ y

    /**
     * TÃƒÂ­nh ngÃƒÂ y bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u dÃ¡Â»Â±a trÃƒÂªn preset
     */
    public LocalDate getDateFrom() {
        LocalDate now = LocalDate.now();
        return switch (this) {
            case TODAY -> now;
            case THIS_WEEK -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case NEXT_7_DAYS -> now;
            case THIS_MONTH -> now.withDayOfMonth(1);
        };
    }

    /**
     * TÃƒÂ­nh ngÃƒÂ y kÃ¡ÂºÂ¿t thÃƒÂºc dÃ¡Â»Â±a trÃƒÂªn preset
     */
    public LocalDate getDateTo() {
        LocalDate now = LocalDate.now();
        return switch (this) {
            case TODAY -> now;
            case THIS_WEEK -> now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            case NEXT_7_DAYS -> now.plusDays(6);
            case THIS_MONTH -> now.with(TemporalAdjusters.lastDayOfMonth());
        };
    }
}
