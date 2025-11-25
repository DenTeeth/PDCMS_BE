package com.dental.clinic.management.patient.service;

import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import com.dental.clinic.management.account.enums.AccountStatus;
import com.dental.clinic.management.account.domain.Account;
import com.dental.clinic.management.account.domain.AccountVerificationToken;
import com.dental.clinic.management.account.domain.PasswordResetToken;
import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.account.repository.AccountVerificationTokenRepository;
import com.dental.clinic.management.account.repository.PasswordResetTokenRepository;
import com.dental.clinic.management.role.domain.Role;
import com.dental.clinic.management.role.repository.RoleRepository;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.dto.request.CreatePatientRequest;
import com.dental.clinic.management.patient.dto.request.ReplacePatientRequest;
import com.dental.clinic.management.patient.dto.request.UpdatePatientRequest;
import com.dental.clinic.management.patient.dto.response.PatientInfoResponse;
import com.dental.clinic.management.patient.mapper.PatientMapper;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.utils.EmailService;
import com.dental.clinic.management.utils.SequentialCodeGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

/**
 * Service for managing patients
 */
@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SequentialCodeGenerator codeGenerator;
    private final AccountVerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final RoleRepository roleRepository;

    public PatientService(
            PatientRepository patientRepository,
            PatientMapper patientMapper,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            SequentialCodeGenerator codeGenerator,
            AccountVerificationTokenRepository verificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService,
            RoleRepository roleRepository) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.codeGenerator = codeGenerator;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
    }

    /**
     * Get all ACTIVE patients only (isActive = true) with pagination and sorting
     *
     * @param page          page number (zero-based)
     * @param size          number of items per page
     * @param sortBy        field name to sort by
     * @param sortDirection ASC or DESC
     * @return Page of PatientInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_PATIENT + "')")
    public Page<PatientInfoResponse> getAllActivePatients(
            int page, int size, String sortBy, String sortDirection) {

        // Validate inputs
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Filter only active patients using Specification
        Specification<Patient> spec = (root, query, cb) -> cb.equal(root.get("isActive"), true);

        Page<Patient> patientPage = patientRepository.findAll(spec, pageable);

        return patientPage.map(patientMapper::toPatientInfoResponse);
    }

    /**
     * Get ALL patients including deleted ones (Admin only)
     *
     * @param page          page number (zero-based)
     * @param size          number of items per pageThere is no data provider
     *                      registered that can provide view data.
     * @param sortBy        field name to sort by
     * @param sortDirection ASC or DESC
     * @return Page of PatientInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_PATIENT + "')")
    public Page<PatientInfoResponse> getAllPatientsIncludingDeleted(
            int page, int size, String sortBy, String sortDirection) {

        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Patient> patientPage = patientRepository.findAll(pageable);

        return patientPage.map(patientMapper::toPatientInfoResponse);
    }

    /**
     * Get active patient by patient code
     *
     * @param patientCode the patient code
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_PATIENT + "')")
    public PatientInfoResponse getActivePatientByCode(String patientCode) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        if (!patient.getIsActive()) {
            throw new BadRequestAlertException(
                    "Patient is inactive",
                    "Patient",
                    "patientinactive");
        }

        return patientMapper.toPatientInfoResponse(patient);
    }

    /**
     * Get patient by code including deleted ones (Admin only)
     *
     * @param patientCode the patient code
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_PATIENT + "')")
    public PatientInfoResponse getPatientByCodeIncludingDeleted(String patientCode) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        return patientMapper.toPatientInfoResponse(patient);
    }

    /**
     * Create new patient with account
     *
     * FLOW: Tạo Patient → Tự động tạo Account → Gửi email cho patient để đặt mật
     * khẩu
     * - Admin/Receptionist tạo patient với email
     * - System tự động tạo account KHÔNG CÓ PASSWORD (patient sẽ tự đặt)
     * - Gửi welcome email với link để patient đặt mật khẩu lần đầu
     * - Patient nhận email → Bấm link → Đặt mật khẩu → Đăng nhập
     *
     * @param request patient information (email required if creating account)
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + CREATE_PATIENT + "')")
    @Transactional
    public PatientInfoResponse createPatient(CreatePatientRequest request) {
        log.debug("Request to create patient: {}", request);

        Account account = null;

        // Check if patient needs account (email provided)
        // NOTE: We no longer require username/password - patient will set password via
        // email
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {

            log.debug("Creating account for patient with email: {}", request.getEmail());

            // Check uniqueness
            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestAlertException(
                        "Email already exists",
                        "account",
                        "emailexists");
            }

            // Generate username from email if not provided
            String username = request.getUsername();
            if (username == null || username.trim().isEmpty()) {
                // Extract username from email (before @)
                username = request.getEmail().split("@")[0];

                // Make sure username is unique
                String baseUsername = username;
                int counter = 1;
                while (accountRepository.existsByUsername(username)) {
                    username = baseUsername + counter;
                    counter++;
                }
                log.debug("Generated username from email: {}", username);
            } else {
                // Check username uniqueness if provided
                if (accountRepository.existsByUsername(username)) {
                    throw new BadRequestAlertException(
                            "Username already exists",
                            "account",
                            "usernameexists");
                }
            }

            // Create account for patient with TEMPORARY PASSWORD (patient will set real
            // password via email)
            // We create a temporary random password so the account can be saved,
            // but patient MUST reset it via email link before they can login
            String temporaryPassword = UUID.randomUUID().toString(); // Random temp password

            // Get ROLE_PATIENT from database
            Role patientRole = roleRepository.findById("ROLE_PATIENT")
                    .orElseThrow(() -> new BadRequestAlertException(
                            "ROLE_PATIENT not found in database",
                            "role",
                            "rolenotfound"));

            account = new Account();
            account.setUsername(username);
            account.setEmail(request.getEmail());
            account.setPassword(passwordEncoder.encode(temporaryPassword)); // Temporary password (unusable)
            account.setStatus(AccountStatus.PENDING_VERIFICATION); // Waiting for password setup
            account.setMustChangePassword(true); // MUST change password via email link
            account.setRole(patientRole); // Set ROLE_PATIENT
            account.setCreatedAt(java.time.LocalDateTime.now());

            account = accountRepository.save(account);
            account.setAccountCode(codeGenerator.generateAccountCode(account.getAccountId()));
            account = accountRepository.save(account);
            log.info("Created account with ID: {} and code: {} for patient (TEMP PASSWORD - waiting for setup)",
                    account.getAccountId(), account.getAccountCode());

            // Create and send password setup token using PasswordResetToken
            // (We use password reset flow for new patient password setup)
            try {
                PasswordResetToken setupToken = new PasswordResetToken(account);
                passwordResetTokenRepository.save(setupToken);

                // Send welcome email with password setup link
                // Build patient full name from firstName and lastName
                String patientName = username; // Default to username
                if (request.getFirstName() != null && request.getLastName() != null) {
                    patientName = request.getFirstName() + " " + request.getLastName();
                } else if (request.getFirstName() != null) {
                    patientName = request.getFirstName();
                }

                emailService.sendWelcomeEmailWithPasswordSetup(
                        account.getEmail(),
                        patientName,
                        setupToken.getToken());
                log.info("✅ Welcome email with password setup link sent to: {}", account.getEmail());

            } catch (Exception e) {
                // Log error but don't fail the entire patient creation
                log.error("⚠️ Failed to send welcome email to {}: {}", account.getEmail(), e.getMessage(), e);
                log.warn(
                        "⚠️ Patient account created successfully, but email not sent. Manual password setup may be required.");
                log.warn("⚠️ Possible causes: SMTP server not configured, network error, invalid email address");
                // Don't throw exception - allow patient creation to succeed
            }
        } else {
            log.debug("Creating patient without account (no email provided)");
        }

        // Convert DTO to entity
        Patient patient = patientMapper.toPatient(request);

        // Set active status
        patient.setIsActive(true);

        // Link account if created
        if (account != null) {
            patient.setAccount(account);
        }

        // Save to get auto-generated ID
        Patient savedPatient = patientRepository.save(patient);

        // Generate and set code
        savedPatient.setPatientCode(codeGenerator.generatePatientCode(savedPatient.getPatientId()));
        savedPatient = patientRepository.save(savedPatient);
        log.info("Created patient with code: {} and ID: {}", savedPatient.getPatientCode(),
                savedPatient.getPatientId());

        return patientMapper.toPatientInfoResponse(savedPatient);
    }

    /**
     * Update patient (PATCH - partial update)
     *
     * @param patientCode the patient code
     * @param request     the update information
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + UPDATE_PATIENT + "')")
    @Transactional
    public PatientInfoResponse updatePatient(String patientCode, UpdatePatientRequest request) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        // Update only non-null fields
        patientMapper.updatePatientFromRequest(request, patient);

        Patient updatedPatient = patientRepository.save(patient);

        return patientMapper.toPatientInfoResponse(updatedPatient);
    }

    /**
     * Replace patient (PUT - full replacement)
     *
     * @param patientCode the patient code
     * @param request     the replacement information
     * @return PatientInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + UPDATE_PATIENT + "')")
    @Transactional
    public PatientInfoResponse replacePatient(String patientCode, ReplacePatientRequest request) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        // Replace all fields
        patientMapper.replacePatientFromRequest(request, patient);

        Patient replacedPatient = patientRepository.save(patient);

        return patientMapper.toPatientInfoResponse(replacedPatient);
    }

    /**
     * Soft delete patient
     *
     * @param patientCode the patient code
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + DELETE_PATIENT + "')")
    @Transactional
    public void deletePatient(String patientCode) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Patient not found with code: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        // Soft delete
        patient.setIsActive(false);
        patientRepository.save(patient);
    }
}
