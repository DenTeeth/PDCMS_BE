package com.dental.clinic.management.utils.validation;

import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import com.dental.clinic.management.working_schedule.service.HolidayDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reusable validator for holiday validation across all modules.
 * Prevents operations on holidays (appointments, shifts, requests, etc.)
 * 
 * ISSUE #53: Holiday validation missing across all modules
 * This component provides centralized holiday validation to ensure
 * business rule consistency: "Phòng khám đóng cửa vào ngày lễ"
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayValidator {
    
    private final HolidayDateService holidayDateService;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Validate single date is NOT a holiday.
     * 
     * @param date Date to validate
     * @param entityName Entity name for error message (e.g., "lịch hẹn", "ca làm việc")
     * @throws BadRequestAlertException if date is a holiday
     */
    public void validateNotHoliday(LocalDate date, String entityName) {
        if (holidayDateService.isHoliday(date)) {
            String formattedDate = date.format(DATE_FORMATTER);
            String errorMessage = String.format(
                "Không thể tạo %s vào ngày lễ (%s). Phòng khám đóng cửa vào ngày này.",
                entityName,
                formattedDate
            );
            
            log.warn("Holiday validation failed: {} attempted on holiday: {}", entityName, formattedDate);
            
            throw new BadRequestAlertException(
                errorMessage,
                entityName,
                "DATE_IS_HOLIDAY"
            );
        }
    }
    
    /**
     * Validate date range does NOT contain any holidays.
     * Used for time-off requests, leave requests, etc.
     * 
     * @param startDate Start date of range (inclusive)
     * @param endDate End date of range (inclusive)
     * @param entityName Entity name for error message
     * @throws BadRequestAlertException if any date in range is a holiday
     */
    public void validateRangeNotIncludeHolidays(
        LocalDate startDate, 
        LocalDate endDate, 
        String entityName) {
        
        // Get all holidays in the range
        List<LocalDate> holidays = holidayDateService.getHolidaysInRange(startDate, endDate.plusDays(1));
        
        if (!holidays.isEmpty()) {
            String holidayList = holidays.stream()
                .map(h -> h.format(DATE_FORMATTER))
                .collect(Collectors.joining(", "));
            
            String errorMessage = String.format(
                "Không thể tạo %s trong khoảng thời gian có ngày lễ: %s. " +
                "Vui lòng chọn khoảng thời gian không bao gồm ngày lễ.",
                entityName,
                holidayList
            );
            
            log.warn("Holiday validation failed: {} range includes {} holidays: {}", 
                     entityName, holidays.size(), holidayList);
            
            throw new BadRequestAlertException(
                errorMessage,
                entityName,
                "RANGE_INCLUDES_HOLIDAYS"
            );
        }
    }
    
    /**
     * Check if date is holiday (non-throwing version).
     * Used when you need to check without exception.
     * 
     * @param date Date to check
     * @return true if date is a holiday
     */
    public boolean isHoliday(LocalDate date) {
        return holidayDateService.isHoliday(date);
    }
    
    /**
     * Filter out holidays from a list of dates.
     * Used in batch operations (e.g., monthly shift generation).
     * 
     * @param dates List of dates to filter
     * @return List of dates excluding holidays
     */
    public List<LocalDate> filterOutHolidays(List<LocalDate> dates) {
        List<LocalDate> workingDays = dates.stream()
            .filter(date -> !isHoliday(date))
            .collect(Collectors.toList());
        
        int holidayCount = dates.size() - workingDays.size();
        if (holidayCount > 0) {
            log.info("Filtered out {} holidays from {} total dates", holidayCount, dates.size());
        }
        
        return workingDays;
    }
    
    /**
     * Get the next working day (skip holidays).
     * Delegates to HolidayDateService.
     * 
     * @param date Starting date
     * @return Next working day (or same day if not holiday)
     */
    public LocalDate getNextWorkingDay(LocalDate date) {
        return holidayDateService.getNextWorkingDay(date);
    }
    
    /**
     * Count working days between two dates (excluding holidays).
     * Delegates to HolidayDateService.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (exclusive)
     * @return Number of working days
     */
    public long countWorkingDaysBetween(LocalDate startDate, LocalDate endDate) {
        return holidayDateService.countWorkingDaysBetween(startDate, endDate);
    }
}
