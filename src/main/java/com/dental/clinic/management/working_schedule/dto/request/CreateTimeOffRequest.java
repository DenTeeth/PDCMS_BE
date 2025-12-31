package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * Request DTO for creating new time-off request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTimeOffRequest {

    // Optional for employee self-requests (will be auto-filled from JWT)
    // Required for admin creating request for another employee
    private Integer employeeId;

    @NotNull(message = "Mã loại nghỉ phép là bắt buộc")
    private String timeOffTypeId;

    @NotNull(message = "Ngày bắt đầu là bắt buộc")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc là bắt buộc")
    private LocalDate endDate;

    private String workShiftId; // NULL for full-day off, value for half-day off

    @NotNull(message = "Lý do là bắt buộc")
    private String reason;

    @Override
    public String toString() {
        return "CreateTimeOffRequest{" +
                "employeeId=" + employeeId +
                ", timeOffTypeId='" + timeOffTypeId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", workShiftId='" + workShiftId + '\'' +
                '}';
    }
}
