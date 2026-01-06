package com.dental.clinic.management.dashboard.service;

import com.dental.clinic.management.dashboard.dto.EmployeeStatisticsResponse;
import com.dental.clinic.management.booking_appointment.repository.AppointmentParticipantRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffRequestRepository;
import com.dental.clinic.management.working_schedule.repository.TimeOffTypeRepository;
// import com.dental.clinic.management.working_schedule.domain.TimeOffType;
import com.dental.clinic.management.working_schedule.enums.TimeOffStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
// import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardEmployeeService {

    private final AppointmentParticipantRepository appointmentParticipantRepository;
    private final TimeOffRequestRepository timeOffRequestRepository;
    @SuppressWarnings("unused")
    private final TimeOffTypeRepository timeOffTypeRepository;

    public EmployeeStatisticsResponse getEmployeeStatistics(String month, Integer topDoctors) {
        YearMonth currentMonth = YearMonth.parse(month);
        LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        LocalDate startLocalDate = currentMonth.atDay(1);
        LocalDate endLocalDate = currentMonth.atEndOfMonth();

        return EmployeeStatisticsResponse.builder()
                .month(month)
                .topDoctors(getTopDoctorPerformance(startDate, endDate, topDoctors))
                .timeOff(getTimeOffStatistics(startLocalDate, endLocalDate))
                .build();
    }

    private List<EmployeeStatisticsResponse.DoctorPerformance> getTopDoctorPerformance(
            LocalDateTime startDate, LocalDateTime endDate, Integer limit) {
        
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Object[]> results = appointmentParticipantRepository.getTopDoctorsByPerformance(
                startDate, endDate, pageRequest);
        
        return results.stream()
                .map(row -> {
                    Integer employeeId = (Integer) row[0];
                    String employeeCode = (String) row[1];
                    String firstName = (String) row[2];
                    String lastName = (String) row[3];
                    Long appointmentCount = ((Number) row[4]).longValue();
                    BigDecimal totalRevenue = (BigDecimal) row[5];
                    BigDecimal avgRevenue = (BigDecimal) row[6];
                    Long serviceCount = ((Number) row[7]).longValue();
                    
                    return EmployeeStatisticsResponse.DoctorPerformance.builder()
                            .employeeId(employeeId.longValue())
                            .employeeCode(employeeCode)
                            .fullName(firstName + " " + lastName)
                            .appointmentCount(appointmentCount)
                            .totalRevenue(totalRevenue)
                            .averageRevenuePerAppointment(avgRevenue)
                            .serviceCount(serviceCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private EmployeeStatisticsResponse.TimeOffStats getTimeOffStatistics(
            LocalDate startDate, LocalDate endDate) {
        
        // Total days and requests
        Long totalDays = timeOffRequestRepository.calculateTotalApprovedDays(startDate, endDate);
        Long totalRequests = timeOffRequestRepository.countApprovedRequests(startDate, endDate);
        
        // By type
        List<Object[]> typeResults = timeOffRequestRepository.getApprovedByTypeId(startDate, endDate);
        Map<String, EmployeeStatisticsResponse.TypeStats> typeStatsMap = new HashMap<>();
        
        for (Object[] row : typeResults) {
            String typeId = (String) row[0];
            Long requests = ((Number) row[1]).longValue();
            Long days = ((Number) row[2]).longValue();
            typeStatsMap.put(typeId, EmployeeStatisticsResponse.TypeStats.builder()
                    .requests(requests)
                    .days(days)
                    .build());
        }
        
        // Map type IDs to known types - you'll need to fetch TimeOffType to get proper mapping
        // For now, using hardcoded type checks
        EmployeeStatisticsResponse.TimeOffByType timeOffByType = EmployeeStatisticsResponse.TimeOffByType.builder()
                .paidLeave(typeStatsMap.getOrDefault("PAID_LEAVE", createEmptyTypeStats()))
                .unpaidLeave(typeStatsMap.getOrDefault("UNPAID_LEAVE", createEmptyTypeStats()))
                .emergencyLeave(typeStatsMap.getOrDefault("EMERGENCY_LEAVE", createEmptyTypeStats()))
                .sickLeave(typeStatsMap.getOrDefault("SICK_LEAVE", createEmptyTypeStats()))
                .other(createEmptyTypeStats())  // Sum up remaining types
                .build();
        
        // By status
        Long pending = timeOffRequestRepository.countByStatusInRange(startDate, endDate, TimeOffStatus.PENDING);
        Long approved = timeOffRequestRepository.countByStatusInRange(startDate, endDate, TimeOffStatus.APPROVED);
        Long rejected = timeOffRequestRepository.countByStatusInRange(startDate, endDate, TimeOffStatus.REJECTED);
        Long cancelled = timeOffRequestRepository.countByStatusInRange(startDate, endDate, TimeOffStatus.CANCELLED);
        
        EmployeeStatisticsResponse.TimeOffByStatus timeOffByStatus = EmployeeStatisticsResponse.TimeOffByStatus.builder()
                .pending(pending)
                .approved(approved)
                .rejected(rejected)
                .cancelled(cancelled)
                .build();
        
        // Top employees
        List<Object[]> topEmployeesResults = timeOffRequestRepository.getTopEmployeesByTimeOff(
                startDate, endDate, PageRequest.of(0, 10));
        
        List<EmployeeStatisticsResponse.TopEmployee> topEmployees = topEmployeesResults.stream()
                .map(row -> EmployeeStatisticsResponse.TopEmployee.builder()
                        .employeeId(((Number) row[0]).longValue())
                        .employeeCode((String) row[1])
                        .fullName((String) row[2] + " " + (String) row[3])
                        .totalDays(((Number) row[5]).longValue())
                        .requests(((Number) row[4]).longValue())
                        .build())
                .collect(Collectors.toList());
        
        return EmployeeStatisticsResponse.TimeOffStats.builder()
                .totalDays(totalDays)
                .totalRequests(totalRequests)
                .byType(timeOffByType)
                .byStatus(timeOffByStatus)
                .topEmployees(topEmployees)
                .build();
    }
    
    private EmployeeStatisticsResponse.TypeStats createEmptyTypeStats() {
        return EmployeeStatisticsResponse.TypeStats.builder()
                .requests(0L)
                .days(0L)
                .build();
    }
}
