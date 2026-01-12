package com.dental.clinic.management.feedback.service;

import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.enums.AppointmentStatus;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.feedback.domain.AppointmentFeedback;
import com.dental.clinic.management.feedback.dto.CreateFeedbackRequest;
import com.dental.clinic.management.feedback.dto.DoctorFeedbackStatisticsResponse;
import com.dental.clinic.management.feedback.dto.FeedbackResponse;
import com.dental.clinic.management.feedback.dto.FeedbackStatisticsResponse;
import com.dental.clinic.management.feedback.dto.FeedbackStatisticsResponse.TagCount;
import com.dental.clinic.management.feedback.repository.AppointmentFeedbackRepository;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.specialization.domain.Specialization;
import com.dental.clinic.management.utils.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing appointment feedbacks
 * 
 * Business Rules:
 * - BR-20: Đánh giá đã gửi KHÔNG thể chỉnh sửa hoặc xóa
 * - BR-21: Người được phép đánh giá: Bệnh nhân của lịch hẹn, Admin, Manager
 * - BR-22: Chỉ đánh giá được lịch hẹn có status = COMPLETED
 * - BR-23: Mỗi lịch hẹn chỉ được đánh giá 1 lần (UNIQUE constraint)
 * - BR-24: Rating bắt buộc (1-5 sao), comment và tags là tùy chọn
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentFeedbackService {

    private final AppointmentFeedbackRepository feedbackRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Tạo feedback cho lịch hẹn
     */
    @Transactional
    public FeedbackResponse createFeedback(CreateFeedbackRequest request) {
        log.info("Creating feedback for appointment: {}", request.getAppointmentCode());

        // 1. Kiểm tra lịch hẹn có tồn tại không
        Appointment appointment = appointmentRepository.findByAppointmentCode(request.getAppointmentCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "APPOINTMENT_NOT_FOUND",
                        new RuntimeException("Không tìm thấy lịch hẹn")));

        // 2. BR-22: Kiểm tra lịch hẹn đã COMPLETED chưa
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "APPOINTMENT_NOT_COMPLETED",
                    new RuntimeException("Chỉ có thể đánh giá lịch hẹn đã hoàn thành"));
        }

        // 3. BR-23: Kiểm tra đã có feedback chưa
        if (feedbackRepository.existsByAppointmentCode(request.getAppointmentCode())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "FEEDBACK_ALREADY_EXISTS",
                    new RuntimeException("Lịch hẹn này đã được đánh giá"));
        }

        // 4. BR-21: Kiểm tra quyền (Bệnh nhân của lịch hẹn, hoặc Admin/Manager)
        validateFeedbackPermission(appointment);

        // 5. Lấy thông tin người tạo
        Long createdBy = getCurrentUserId();

        // 6. Tạo feedback entity
        AppointmentFeedback feedback = AppointmentFeedback.builder()
                .appointmentCode(request.getAppointmentCode())
                .patientId(appointment.getPatientId())
                .rating(request.getRating())
                .comment(request.getComment())
                .tags(request.getTags())
                .createdBy(createdBy)
                .build();

        feedbackRepository.save(feedback);

        log.info("Feedback created successfully: feedbackId={}, appointmentCode={}",
                feedback.getFeedbackId(), feedback.getAppointmentCode());

        return buildFeedbackResponse(feedback, appointment);
    }

    /**
     * Lấy feedback theo appointment code
     */
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackByAppointmentCode(String appointmentCode) {
        log.debug("Getting feedback for appointment: {}", appointmentCode);

        AppointmentFeedback feedback = feedbackRepository.findByAppointmentCode(appointmentCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "FEEDBACK_NOT_FOUND",
                        new RuntimeException("Lịch hẹn này chưa có đánh giá")));

        Appointment appointment = appointmentRepository.findByAppointmentCode(appointmentCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "APPOINTMENT_NOT_FOUND",
                        new RuntimeException("Không tìm thấy lịch hẹn")));

        return buildFeedbackResponse(feedback, appointment);
    }

    /**
     * Lấy danh sách feedbacks với filter (Admin/Employee only)
     */
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getFeedbacksList(
            Integer rating,
            String employeeCode,
            String patientCode,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size,
            String sort) {
        
        log.debug("Getting feedbacks list with filters - rating: {}, employeeCode: {}, patientCode: {}, fromDate: {}, toDate: {}",
                rating, employeeCode, patientCode, fromDate, toDate);

        // Resolve patient code to patient ID
        Integer patientId = null;
        if (patientCode != null && !patientCode.isBlank()) {
            Patient patient = patientRepository.findOneByPatientCode(patientCode).orElse(null);
            if (patient != null) {
                patientId = patient.getPatientId();
            }
        }

        // Parse sort parameter (format: "field,direction")
        Sort.Direction direction = Sort.Direction.DESC;
        String sortField = "createdAt";
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            sortField = parts[0];
            if (parts.length > 1) {
                direction = Sort.Direction.fromString(parts[1]);
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<AppointmentFeedback> feedbacksPage = feedbackRepository.findWithFilters(
                rating, patientId, fromDate, toDate, pageable);

        return feedbacksPage.map(feedback -> {
            Appointment appointment = appointmentRepository.findByAppointmentCode(feedback.getAppointmentCode())
                    .orElse(null);
            return buildFeedbackResponse(feedback, appointment);
        });
    }

    /**
     * Lấy thống kê feedbacks (Admin/Employee only)
     */
    @Transactional(readOnly = true)
    public FeedbackStatisticsResponse getFeedbackStatistics(
            String employeeCode,
            LocalDate fromDate,
            LocalDate toDate) {
        
        log.debug("Getting feedback statistics - employeeCode: {}, fromDate: {}, toDate: {}",
                employeeCode, fromDate, toDate);

        // Count total feedbacks
        Long totalFeedbacks = feedbackRepository.countWithDateRange(fromDate, toDate);

        // Calculate average rating
        Double averageRating = feedbackRepository.calculateAverageRating(fromDate, toDate);
        if (averageRating == null) {
            averageRating = 0.0;
        }

        // Get rating distribution
        Object[][] distributionData = feedbackRepository.getRatingDistribution(fromDate, toDate);
        Map<String, Long> ratingDistribution = new LinkedHashMap<>();
        // Initialize all ratings to 0
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(String.valueOf(i), 0L);
        }
        // Fill in actual data
        for (Object[] row : distributionData) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(String.valueOf(rating), count);
        }

        // Calculate top tags
        List<TagCount> topTags = calculateTopTags(fromDate, toDate);

        return FeedbackStatisticsResponse.builder()
                .totalFeedbacks(totalFeedbacks)
                .averageRating(Math.round(averageRating * 10.0) / 10.0) // Round to 1 decimal
                .ratingDistribution(ratingDistribution)
                .topTags(topTags)
                .build();
    }

    /**
     * Tính toán top tags phổ biến nhất
     */
    private List<TagCount> calculateTopTags(LocalDate fromDate, LocalDate toDate) {
        // Get all feedbacks within date range
        List<AppointmentFeedback> feedbacks = feedbackRepository.findWithFilters(
                null, null, fromDate, toDate, Pageable.unpaged()).getContent();

        // Count all tags
        Map<String, Long> tagCounts = new HashMap<>();
        for (AppointmentFeedback feedback : feedbacks) {
            if (feedback.getTags() != null) {
                for (String tag : feedback.getTags()) {
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0L) + 1);
                }
            }
        }

        // Sort by count descending and take top 10
        return tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> TagCount.builder()
                        .tag(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Build feedback response DTO
     */
    private FeedbackResponse buildFeedbackResponse(AppointmentFeedback feedback, Appointment appointment) {
        String patientName = null;
        String employeeName = null;

        if (appointment != null) {
            // Get patient name
            Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            if (patient != null) {
                patientName = patient.getLastName() + " " + patient.getFirstName();
            }

            // Get employee name
            Employee employee = employeeRepository.findById(appointment.getEmployeeId()).orElse(null);
            if (employee != null) {
                employeeName = employee.getLastName() + " " + employee.getFirstName();
            }
        }

        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .appointmentCode(feedback.getAppointmentCode())
                .patientName(patientName)
                .employeeName(employeeName)
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .tags(feedback.getTags())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    /**
     * BR-21: Validate feedback permission
     * Cho phép: Bệnh nhân của lịch hẹn, Admin, Manager
     */
    private void validateFeedbackPermission(Appointment appointment) {
        String currentUsername = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy thông tin người dùng"));

        boolean isAdmin = SecurityUtil.hasCurrentUserRole("ADMIN");
        boolean isManager = SecurityUtil.hasCurrentUserRole("MANAGER");

        // Admin và Manager có thể tạo feedback cho bất kỳ lịch hẹn nào
        if (isAdmin || isManager) {
            return;
        }

        // Patient chỉ có thể tạo feedback cho lịch hẹn của mình
        Patient patient = patientRepository.findByAccount_Username(currentUsername).orElse(null);
        if (patient != null && patient.getPatientId().equals(appointment.getPatientId())) {
            return;
        }

        throw new AccessDeniedException("Bạn không có quyền đánh giá lịch hẹn này");
    }

    /**
     * Lấy thống kê feedback theo bác sĩ
     * Endpoint: GET /api/v1/feedbacks/statistics/by-doctor
     * 
     * @param startDate Ngày bắt đầu (optional)
     * @param endDate Ngày kết thúc (optional)
     * @param top Số lượng bác sĩ muốn lấy (default: 10)
     * @param sortBy Sắp xếp theo ("rating" hoặc "feedbackCount")
     * @return DoctorFeedbackStatisticsResponse
     */
    @Transactional(readOnly = true)
    public DoctorFeedbackStatisticsResponse getStatisticsByDoctor(
            LocalDate startDate,
            LocalDate endDate,
            int top,
            String sortBy) {
        
        log.debug("Getting doctor feedback statistics - startDate: {}, endDate: {}, top: {}, sortBy: {}",
                startDate, endDate, top, sortBy);

        // Get raw statistics grouped by doctor
        Object[][] rawStats = feedbackRepository.getDoctorStatisticsGrouped(startDate, endDate);

        // Map to temporary structure for sorting
        List<DoctorStatTemp> tempList = new ArrayList<>();
        for (Object[] row : rawStats) {
            Integer employeeId = (Integer) row[0];
            Double avgRating = (Double) row[1];
            Long feedbackCount = (Long) row[2];

            tempList.add(new DoctorStatTemp(employeeId, avgRating, feedbackCount));
        }

        // Sort based on sortBy parameter
        if ("feedbackCount".equalsIgnoreCase(sortBy)) {
            tempList.sort(Comparator.comparing(DoctorStatTemp::getFeedbackCount).reversed());
        } else {
            // Default sort by rating
            tempList.sort(Comparator.comparing(DoctorStatTemp::getAvgRating).reversed());
        }

        // Take top N
        List<DoctorStatTemp> topDoctors = tempList.stream()
                .limit(top)
                .collect(Collectors.toList());

        // Build detailed statistics for each doctor
        List<DoctorFeedbackStatisticsResponse.DoctorStatistics> doctorStatsList = new ArrayList<>();
        for (DoctorStatTemp temp : topDoctors) {
            DoctorFeedbackStatisticsResponse.DoctorStatistics doctorStats = 
                    buildDoctorStatistics(temp.getEmployeeId(), startDate, endDate, temp.getAvgRating(), temp.getFeedbackCount());
            doctorStatsList.add(doctorStats);
        }

        return DoctorFeedbackStatisticsResponse.builder()
                .doctors(doctorStatsList)
                .build();
    }

    /**
     * Build detailed statistics for a specific doctor
     */
    private DoctorFeedbackStatisticsResponse.DoctorStatistics buildDoctorStatistics(
            Integer employeeId,
            LocalDate startDate,
            LocalDate endDate,
            Double avgRating,
            Long totalFeedbacks) {
        
        // Get employee info
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return null;
        }

        // Get rating distribution
        Object[][] distributionData = feedbackRepository.getDoctorRatingDistribution(employeeId, startDate, endDate);
        Map<String, Long> ratingDistribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(String.valueOf(i), 0L);
        }
        for (Object[] row : distributionData) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(String.valueOf(rating), count);
        }

        // Get all feedbacks for this doctor
        List<AppointmentFeedback> feedbacks = feedbackRepository.findByEmployeeIdAndDateRange(
                employeeId, startDate, endDate);

        // Calculate top tags
        Map<String, Long> tagCounts = new HashMap<>();
        for (AppointmentFeedback feedback : feedbacks) {
            if (feedback.getTags() != null) {
                for (String tag : feedback.getTags()) {
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0L) + 1);
                }
            }
        }

        List<TagCount> topTags = tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> TagCount.builder()
                        .tag(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // Get recent comments (top 3 most recent)
        List<DoctorFeedbackStatisticsResponse.RecentComment> recentComments = feedbacks.stream()
                .sorted(Comparator.comparing(AppointmentFeedback::getCreatedAt).reversed())
                .limit(3)
                .map(feedback -> {
                    String patientName = null;
                    Patient patient = patientRepository.findById(feedback.getPatientId()).orElse(null);
                    if (patient != null) {
                        patientName = patient.getLastName() + " " + patient.getFirstName();
                    }

                    return DoctorFeedbackStatisticsResponse.RecentComment.builder()
                            .feedbackId(feedback.getFeedbackId())
                            .patientName(patientName)
                            .rating(feedback.getRating())
                            .comment(feedback.getComment())
                            .tags(feedback.getTags())
                            .createdAt(feedback.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        // Build statistics object
        DoctorFeedbackStatisticsResponse.Statistics statistics = 
                DoctorFeedbackStatisticsResponse.Statistics.builder()
                        .averageRating(Math.round(avgRating * 10.0) / 10.0)
                        .totalFeedbacks(totalFeedbacks)
                        .ratingDistribution(ratingDistribution)
                        .topTags(topTags)
                        .recentComments(recentComments)
                        .build();

        // Build specialization string (join all specializations)
        String specializationStr = null;
        if (employee.getSpecializations() != null && !employee.getSpecializations().isEmpty()) {
            specializationStr = employee.getSpecializations().stream()
                    .map(Specialization::getSpecializationName)
                    .collect(Collectors.joining(", "));
        }

        // Build doctor statistics
        return DoctorFeedbackStatisticsResponse.DoctorStatistics.builder()
                .employeeId(employeeId)
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employee.getLastName() + " " + employee.getFirstName())
                .specialization(specializationStr)
                .avatar(null) // Employee entity doesn't have avatar field
                .statistics(statistics)
                .build();
    }

    /**
     * Temporary class for sorting
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class DoctorStatTemp {
        private Integer employeeId;
        private Double avgRating;
        private Long feedbackCount;
    }

    /**
     * Get current user ID (patient_id or employee_id)
     */
    private Long getCurrentUserId() {
        String currentUsername = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy thông tin người dùng"));

        // Try to find patient
        Patient patient = patientRepository.findByAccount_Username(currentUsername).orElse(null);
        if (patient != null) {
            return patient.getPatientId().longValue();
        }

        // Try to find employee
        Employee employee = employeeRepository.findByAccount_Username(currentUsername).orElse(null);
        if (employee != null) {
            return employee.getEmployeeId().longValue();
        }

        throw new AccessDeniedException("Không tìm thấy thông tin người dùng");
    }
}
