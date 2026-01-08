package com.dental.clinic.management.dashboard.util;

import com.dental.clinic.management.dashboard.enums.ComparisonMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Utility class for handling date range conversions in dashboard
 * Supports both month-based (YYYY-MM) and date range (startDate/endDate) filtering
 * with backward compatibility
 */
public class DateRangeUtil {

    /**
     * Parse date range from either month parameter or startDate/endDate parameters
     * Priority: startDate/endDate > month
     * 
     * @param month Month string in YYYY-MM format (optional)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @return DateRange object with LocalDateTime boundaries
     * @throws IllegalArgumentException if no valid parameters provided
     */
    public static DateRange parseDateRange(String month, LocalDate startDate, LocalDate endDate) {
        // Priority 1: If startDate and endDate are provided
        if (startDate != null && endDate != null) {
            return new DateRange(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                formatDateRangeLabel(startDate, endDate)
            );
        }
        
        // Priority 2: If only month is provided (backward compatibility)
        if (month != null && !month.isBlank()) {
            YearMonth yearMonth = YearMonth.parse(month);
            return new DateRange(
                yearMonth.atDay(1).atStartOfDay(),
                yearMonth.atEndOfMonth().atTime(23, 59, 59),
                month
            );
        }
        
        // No valid parameters
        throw new IllegalArgumentException("Either 'month' or both 'startDate' and 'endDate' must be provided");
    }

    /**
     * Format date range label for display
     */
    private static String formatDateRangeLabel(LocalDate startDate, LocalDate endDate) {
        // If same month, use month format
        if (startDate.getYear() == endDate.getYear() && startDate.getMonth() == endDate.getMonth()) {
            return YearMonth.of(startDate.getYear(), startDate.getMonth()).toString();
        }
        // Otherwise, use date range format
        return String.format("%s to %s", startDate, endDate);
    }

    /**
     * Calculate previous period for comparison (deprecated - use calculateComparisonPeriod instead)
     * If original is a full month, returns previous month
     * Otherwise, returns same duration shifted back
     * @deprecated Use {@link #calculateComparisonPeriod(DateRange, ComparisonMode)} instead
     */
    @Deprecated
    public static DateRange calculatePreviousPeriod(DateRange current) {
        return calculateComparisonPeriod(current, ComparisonMode.MONTH);
    }

    /**
     * Calculate comparison period based on comparison mode
     * 
     * @param current Current date range
     * @param mode Comparison mode (MONTH, QUARTER, YEAR)
     * @return DateRange for comparison period
     */
    public static DateRange calculateComparisonPeriod(DateRange current, ComparisonMode mode) {
        if (mode == null || mode == ComparisonMode.NONE) {
            return null;
        }
        
        LocalDateTime currentStart = current.getStartDate();
        LocalDateTime currentEnd = current.getEndDate();
        
        LocalDateTime comparisonStart;
        LocalDateTime comparisonEnd;
        String label;
        
        switch (mode) {
            case MONTH:
                // Go back by same number of days
                long daysDiff = java.time.Duration.between(currentStart, currentEnd).toDays();
                comparisonEnd = currentStart.minusDays(1).withHour(23).withMinute(59).withSecond(59);
                comparisonStart = comparisonEnd.minusDays(daysDiff).withHour(0).withMinute(0).withSecond(0);
                label = "Previous Month";
                break;
                
            case QUARTER:
                // Go back 3 months (quarter)
                comparisonStart = currentStart.minusMonths(3);
                comparisonEnd = currentEnd.minusMonths(3);
                label = "Previous Quarter";
                break;
                
            case YEAR:
                // Go back 1 year (same period last year)
                comparisonStart = currentStart.minusYears(1);
                comparisonEnd = currentEnd.minusYears(1);
                label = "Same Period Last Year";
                break;
                
            default:
                return null;
        }
        
        return new DateRange(comparisonStart, comparisonEnd, label);
    }

    /**
     * DateRange container class
     */
    public static class DateRange {
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final String label;

        public DateRange(LocalDateTime startDate, LocalDateTime endDate, String label) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.label = label;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public String getLabel() {
            return label;
        }
    }
}
