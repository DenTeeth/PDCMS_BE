
package com.dental.clinic.management.patient.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for patient information response
 */
public class PatientInfoResponse {

    private Integer patientId;
    private String patientCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private String gender;
    private String medicalHistory;
    private String allergies;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String guardianName;
    private String guardianPhone;
    private String guardianRelationship;
    private String guardianCitizenId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Account-related fields
    private Boolean hasAccount;
    private Integer accountId;
    private String accountStatus;
    private Boolean isEmailVerified;

    // Booking block fields (BR-043, BR-044, BR-005)
    private Boolean isBookingBlocked;
    private String bookingBlockReason;
    private String bookingBlockNotes;
    private String blockedBy;
    private LocalDateTime blockedAt;
    private Integer consecutiveNoShows;

    // Constructors
    public PatientInfoResponse() {
    }

    // Getters and Setters
    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getHasAccount() {
        return hasAccount;
    }

    public void setHasAccount(Boolean hasAccount) {
        this.hasAccount = hasAccount;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public String getGuardianRelationship() {
        return guardianRelationship;
    }

    public void setGuardianRelationship(String guardianRelationship) {
        this.guardianRelationship = guardianRelationship;
    }

    public String getGuardianCitizenId() {
        return guardianCitizenId;
    }

    public void setGuardianCitizenId(String guardianCitizenId) {
        this.guardianCitizenId = guardianCitizenId;
    }

    public Boolean getIsBookingBlocked() {
        return isBookingBlocked;
    }

    public void setIsBookingBlocked(Boolean isBookingBlocked) {
        this.isBookingBlocked = isBookingBlocked;
    }

    public String getBookingBlockReason() {
        return bookingBlockReason;
    }

    public void setBookingBlockReason(String bookingBlockReason) {
        this.bookingBlockReason = bookingBlockReason;
    }

    public String getBookingBlockNotes() {
        return bookingBlockNotes;
    }

    public void setBookingBlockNotes(String bookingBlockNotes) {
        this.bookingBlockNotes = bookingBlockNotes;
    }

    public String getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(String blockedBy) {
        this.blockedBy = blockedBy;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(LocalDateTime blockedAt) {
        this.blockedAt = blockedAt;
    }

    public Integer getConsecutiveNoShows() {
        return consecutiveNoShows;
    }

    public void setConsecutiveNoShows(Integer consecutiveNoShows) {
        this.consecutiveNoShows = consecutiveNoShows;
    }

    @Override
    public String toString() {
        return "PatientInfoResponse{" +
                "patientId='" + patientId + '\'' +
                ", patientCode='" + patientCode + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender=" + gender +
                ", isActive=" + isActive +
                ", isBookingBlocked=" + isBookingBlocked +
                ", consecutiveNoShows=" + consecutiveNoShows +
                '}';
    }
}
