
package com.dental.clinic.management.patient.dto.request;

import com.dental.clinic.management.employee.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO for creating a new patient
 *
 * ACCOUNT CREATION FLOW (Hospital Standard):
 *
 * Step 1: Staff creates patient record
 * - Staff enters: username (e.g., "nguyenvana", "BN001")
 * - Staff enters: patient info (name, email, phone, DOB, etc.)
 * - Staff does NOT enter password (security best practice)
 *
 * Step 2: Backend auto-creates account (if email provided)
 * - Backend generates temporary password (random UUID)
 * - Account status: PENDING_VERIFICATION
 * - Backend sends welcome email with password setup link
 *
 * Step 3: Patient verifies & sets password
 * - Patient clicks link in email
 * - Patient verifies email address
 * - Patient sets their own password
 * - Patient can now login
 *
 * Security: Staff NEVER sees or knows patient's password
 *
 * ⚠️ BREAKING CHANGE (V23/V24):
 * - Removed `password` field from request (patient sets via email)
 * - Kept `username` field (staff must provide username)
 * - Username is REQUIRED if creating account
 * - If no email → patient created without account (record-only)
 */
public class CreatePatientRequest {

    // ===== ACCOUNT FIELDS (Required for login-enabled patients) =====

    /**
     * Username for patient account (required if email provided)
     * Staff enters this - examples: "nguyenvana", "patient001", "BN12345"
     * Must be unique in the system
     */
    @Size(max = 50)
    private String username;

    // ===== PATIENT FIELDS =====

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @Email
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$")
    private String phone;

    @Past
    private LocalDate dateOfBirth;

    @Size(max = 500)
    private String address;

    private Gender gender;

    @Size(max = 1000)
    private String medicalHistory;

    @Size(max = 500)
    private String allergies;

    @Size(max = 100)
    private String emergencyContactName;

    @Pattern(regexp = "^[0-9]{10,15}$")
    private String emergencyContactPhone;

    // Constructors
    public CreatePatientRequest() {
    }

    public CreatePatientRequest(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @Override
    public String toString() {
        return "CreatePatientRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
