
package com.dental.clinic.management.patient.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dental.clinic.management.account.domain.Account;
import com.dental.clinic.management.employee.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A Patient entity.
 */
@Entity
@Table(name = "patients")
public class Patient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "patient_id")
  private Integer patientId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  @Size(max = 20)
  @Column(name = "patient_code", unique = true, length = 20)
  private String patientCode;

  @NotBlank
  @Size(max = 50)
  @Column(name = "first_name", nullable = false, length = 50)
  private String firstName;

  @NotBlank
  @Size(max = 50)
  @Column(name = "last_name", nullable = false, length = 50)
  private String lastName;

  @Email
  @Size(max = 100)
  @Column(name = "email", length = 100)
  private String email;

  @Size(max = 15)
  @Column(name = "phone", length = 15)
  private String phone;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(name = "address", columnDefinition = "TEXT")
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender", length = 10)
  private Gender gender;

  @Column(name = "medical_history", columnDefinition = "TEXT")
  private String medicalHistory;

  @Column(name = "allergies", columnDefinition = "TEXT")
  private String allergies;

  @Column(name = "emergency_contact_name", length = 100)
  private String emergencyContactName;

  @Column(name = "emergency_contact_phone", length = 15)
  private String emergencyContactPhone;

  @Column(name = "is_active")
  private Boolean isActive = true;

  /**
   * Rule #5: No-show tracking for booking restriction
   * Consecutive no-shows counter (resets when patient shows up)
   */
  @Column(name = "consecutive_no_shows", nullable = false)
  private Integer consecutiveNoShows = 0;

  /**
   * Rule #5: Booking blocked flag
   * Set to true when consecutiveNoShows >= 3
   */
  @Column(name = "is_booking_blocked", nullable = false)
  private Boolean isBookingBlocked = false;

  /**
   * Rule #5: Reason for booking block
   */
  @Column(name = "booking_block_reason", length = 500)
  private String bookingBlockReason;

  /**
   * Rule #5: When booking was blocked
   */
  @Column(name = "blocked_at")
  private LocalDateTime blockedAt;

  /**
   * Rule #14: Guardian information for minors (<16 years old)
   * Required for patients under 16 years old
   */
  @Column(name = "guardian_name", length = 100)
  private String guardianName;

  @Column(name = "guardian_phone", length = 15)
  private String guardianPhone;

  @Column(name = "guardian_relationship", length = 50)
  private String guardianRelationship;

  @Column(name = "guardian_citizen_id", length = 20)
  private String guardianCitizenId;

  /**
   * BR-044: Blacklist flag
   * Set to true when patient is added to blacklist
   */
  @Column(name = "is_blacklisted", nullable = false)
  private Boolean isBlacklisted = false;

  /**
   * BR-044: Predefined blacklist reason
   * Required when isBlacklisted = true
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "blacklist_reason", length = 50)
  private com.dental.clinic.management.patient.enums.PatientBlacklistReason blacklistReason;

  /**
   * BR-044: Additional notes for blacklist
   * Optional explanation or details
   */
  @Column(name = "blacklist_notes", columnDefinition = "TEXT")
  private String blacklistNotes;

  /**
   * BR-044: Who blacklisted the patient
   */
  @Column(name = "blacklisted_by", length = 100)
  private String blacklistedBy;

  /**
   * BR-044: When patient was blacklisted
   */
  @Column(name = "blacklisted_at")
  private LocalDateTime blacklistedAt;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Constructors
  public Patient() {
  }

  public Patient(Integer patientId, String firstName, String lastName) {
    this.patientId = patientId;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Getters and Setters
  public Integer getPatientId() {
    return patientId;
  }

  public void setPatientId(Integer patientId) {
    this.patientId = patientId;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
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

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
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

  public Integer getConsecutiveNoShows() {
    return consecutiveNoShows;
  }

  public void setConsecutiveNoShows(Integer consecutiveNoShows) {
    this.consecutiveNoShows = consecutiveNoShows;
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

  public LocalDateTime getBlockedAt() {
    return blockedAt;
  }

  public void setBlockedAt(LocalDateTime blockedAt) {
    this.blockedAt = blockedAt;
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

  public Boolean getIsBlacklisted() {
    return isBlacklisted;
  }

  public void setIsBlacklisted(Boolean isBlacklisted) {
    this.isBlacklisted = isBlacklisted;
  }

  public com.dental.clinic.management.patient.enums.PatientBlacklistReason getBlacklistReason() {
    return blacklistReason;
  }

  public void setBlacklistReason(com.dental.clinic.management.patient.enums.PatientBlacklistReason blacklistReason) {
    this.blacklistReason = blacklistReason;
  }

  public String getBlacklistNotes() {
    return blacklistNotes;
  }

  public void setBlacklistNotes(String blacklistNotes) {
    this.blacklistNotes = blacklistNotes;
  }

  public String getBlacklistedBy() {
    return blacklistedBy;
  }

  public void setBlacklistedBy(String blacklistedBy) {
    this.blacklistedBy = blacklistedBy;
  }

  public LocalDateTime getBlacklistedAt() {
    return blacklistedAt;
  }

  public void setBlacklistedAt(LocalDateTime blacklistedAt) {
    this.blacklistedAt = blacklistedAt;
  }

  // Helper methods
  public String getFullName() {
    return firstName + " " + lastName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Patient))
      return false;
    Patient patient = (Patient) o;
    return patientId != null && patientId.equals(patient.getPatientId());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Patient{" +
        "patientId=" + patientId +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", email='" + email + '\'' +
        ", phone='" + phone + '\'' +
        ", isActive=" + isActive +
        '}';
  }
}
