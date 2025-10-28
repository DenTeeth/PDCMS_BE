package com.dental.clinic.management.working_schedule.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Utility class for Employee Shift validation.
 * Provides helper methods for date and shift validation rules.
 */
public class EmployeeShiftValidator {

    /**
     * Check if a date is a holiday.
     * Simple implementation: treat Sundays as holidays.
     * 
     * @param date date to check
     * @return true if holiday, false otherwise
     */
    public static boolean isHoliday(LocalDate date) {
        // Simple implementation: Sunday = holiday
        return date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * Check if a date is in the past.
     * 
     * @param date date to check
     * @return true if date is before today
     */
    public static boolean isPastDate(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    /**
     * Check if a date is in the future.
     * 
     * @param date date to check
     * @return true if date is after today
     */
    public static boolean isFutureDate(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    /**
     * Check if a date is today.
     * 
     * @param date date to check
     * @return true if date is today
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }

    /**
     * Check if a date is a weekend (Saturday or Sunday).
     * 
     * @param date date to check
     * @return true if weekend, false otherwise
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Calculate the number of days between two dates.
     * 
     * @param startDate start date
     * @param endDate   end date
     * @return number of days (inclusive)
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
