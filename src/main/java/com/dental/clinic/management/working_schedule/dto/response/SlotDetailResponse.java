package com.dental.clinic.management.working_schedule.dto.response;

import java.time.LocalDate;
import java.util.List;

public class SlotDetailResponse {

    private Long slotId;
    private String shiftName;
    private String dayOfWeek;
    private Integer quota;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer overallRemaining; // Minimum remaining across all dates

    private List<MonthlyAvailability> availabilityByMonth;

    // Constructors
    public SlotDetailResponse() {
    }

    public SlotDetailResponse(Long slotId, String shiftName, String dayOfWeek, Integer quota,
            LocalDate effectiveFrom, LocalDate effectiveTo, Integer overallRemaining,
            List<MonthlyAvailability> availabilityByMonth) {
        this.slotId = slotId;
        this.shiftName = shiftName;
        this.dayOfWeek = dayOfWeek;
        this.quota = quota;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.overallRemaining = overallRemaining;
        this.availabilityByMonth = availabilityByMonth;
    }

    // Getters and Setters
    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Integer getOverallRemaining() {
        return overallRemaining;
    }

    public void setOverallRemaining(Integer overallRemaining) {
        this.overallRemaining = overallRemaining;
    }

    public List<MonthlyAvailability> getAvailabilityByMonth() {
        return availabilityByMonth;
    }

    public void setAvailabilityByMonth(List<MonthlyAvailability> availabilityByMonth) {
        this.availabilityByMonth = availabilityByMonth;
    }

    public static class MonthlyAvailability {
        private String month; // Format: "YYYY-MM"
        private String monthName; // Format: "December 2025"
        private Integer totalDatesAvailable; // Count of dates where registered < quota
        private Integer totalDatesPartial; // Count of dates where 0 < registered < quota
        private Integer totalDatesFull; // Count of dates where registered == quota
        private String status; // "AVAILABLE", "PARTIAL", "FULL"
        private Integer totalWorkingDays; // Total working days in this month

        // Constructors
        public MonthlyAvailability() {
        }

        public MonthlyAvailability(String month, String monthName, Integer totalDatesAvailable,
                Integer totalDatesPartial, Integer totalDatesFull, String status, Integer totalWorkingDays) {
            this.month = month;
            this.monthName = monthName;
            this.totalDatesAvailable = totalDatesAvailable;
            this.totalDatesPartial = totalDatesPartial;
            this.totalDatesFull = totalDatesFull;
            this.status = status;
            this.totalWorkingDays = totalWorkingDays;
        }

        // Getters and Setters
        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getMonthName() {
            return monthName;
        }

        public void setMonthName(String monthName) {
            this.monthName = monthName;
        }

        public Integer getTotalDatesAvailable() {
            return totalDatesAvailable;
        }

        public void setTotalDatesAvailable(Integer totalDatesAvailable) {
            this.totalDatesAvailable = totalDatesAvailable;
        }

        public Integer getTotalDatesPartial() {
            return totalDatesPartial;
        }

        public void setTotalDatesPartial(Integer totalDatesPartial) {
            this.totalDatesPartial = totalDatesPartial;
        }

        public Integer getTotalDatesFull() {
            return totalDatesFull;
        }

        public void setTotalDatesFull(Integer totalDatesFull) {
            this.totalDatesFull = totalDatesFull;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getTotalWorkingDays() {
            return totalWorkingDays;
        }

        public void setTotalWorkingDays(Integer totalWorkingDays) {
            this.totalWorkingDays = totalWorkingDays;
        }
    }
}
