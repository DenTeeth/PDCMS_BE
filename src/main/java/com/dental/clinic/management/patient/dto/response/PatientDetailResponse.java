package com.dental.clinic.management.patient.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDetailResponse {

    private Integer patientId;
    private String patientCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Integer age;
    private String address;
    private String gender;

    // Medical Information
    private String medicalHistory;
    private String allergies;

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;

    // Guardian Information (for minors <16 years old)
    private String guardianName;
    private String guardianPhone;
    private String guardianRelationship;
    private String guardianCitizenId;

    // Booking Status
    private Boolean isActive;
    private Integer consecutiveNoShows;
    private Boolean isBookingBlocked;
    private String bookingBlockReason;
    private String bookingBlockNotes;
    private String blockedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime blockedAt;

    // Account Information
    private Integer accountId;
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
