package com.dental.clinic.management.patient.service;

import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import com.dental.clinic.management.account.enums.AccountStatus;
import com.dental.clinic.management.account.domain.Account;
// import com.dental.clinic.management.account.domain.AccountVerificationToken;
import com.dental.clinic.management.account.domain.PasswordResetToken;
import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.account.repository.AccountVerificationTokenRepository;
import com.dental.clinic.management.account.repository.PasswordResetTokenRepository;
import com.dental.clinic.management.role.domain.Role;
import com.dental.clinic.management.role.repository.RoleRepository;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.clinical_records.domain.PatientToothStatus;
import com.dental.clinic.management.clinical_records.domain.PatientToothStatusHistory;
import com.dental.clinic.management.patient.domain.ToothConditionEnum;
import com.dental.clinic.management.patient.dto.request.CreatePatientRequest;
import com.dental.clinic.management.patient.dto.request.ReplacePatientRequest;
import com.dental.clinic.management.patient.dto.request.UpdatePatientRequest;
import com.dental.clinic.management.patient.dto.response.PatientInfoResponse;
import com.dental.clinic.management.patient.dto.ToothStatusResponse;
import com.dental.clinic.management.patient.dto.UpdateToothStatusRequest;
import com.dental.clinic.management.patient.dto.UpdateToothStatusResponse;
import com.dental.clinic.management.patient.mapper.PatientMapper;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.clinical_records.repository.PatientToothStatusRepository;
import com.dental.clinic.management.clinical_records.repository.PatientToothStatusHistoryRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @SuppressWarnings("unused")
    private final AccountVerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final PatientToothStatusRepository patientToothStatusRepository;
    private final PatientToothStatusHistoryRepository patientToothStatusHistoryRepository;
    private final DuplicatePatientDetectionService duplicateDetectionService;
    private final EmployeeRepository employeeRepository;

    public PatientService(
            PatientRepository patientRepository,
            PatientMapper patientMapper,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            SequentialCodeGenerator codeGenerator,
            AccountVerificationTokenRepository verificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService,
            RoleRepository roleRepository,
            PatientToothStatusRepository patientToothStatusRepository,
            PatientToothStatusHistoryRepository patientToothStatusHistoryRepository,
            DuplicatePatientDetectionService duplicateDetectionService,
            EmployeeRepository employeeRepository) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.codeGenerator = codeGenerator;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.patientToothStatusRepository = patientToothStatusRepository;
        this.patientToothStatusHistoryRepository = patientToothStatusHistoryRepository;
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
                        "Không tìm thấy bệnh nhân với mã: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        if (!patient.getIsActive()) {
            throw new BadRequestAlertException(
                    "Bệnh nhân không còn hoạt động",
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
                        "Không tìm thấy bệnh nhân với mã: " + patientCode,
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
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + MANAGE_PATIENT + "')")
    @Transactional
    public PatientInfoResponse createPatient(CreatePatientRequest request) {
        log.debug("Request to create patient: {}", request);

        // BR-043: Check for duplicate patients (by Name + DOB or Phone)
        com.dental.clinic.management.patient.dto.DuplicatePatientCheckResult duplicateCheck = duplicateDetectionService
                .checkForDuplicates(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getDateOfBirth(),
                        request.getPhone());

        if (duplicateCheck.isHasDuplicates()) {
            log.warn("Duplicate patients found: {} matches", duplicateCheck.getMatches().size());

            // Check if it's an exact match (should be blocked)
            boolean hasExactMatch = duplicateDetectionService.hasExactMatch(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getDateOfBirth(),
                    request.getPhone());

            if (hasExactMatch) {
                throw new BadRequestAlertException(
                        "Bệnh nhân với thông tin này đã tồn tại trong hệ thống. Vui lòng kiểm tra lại.",
                        "patient",
                        "duplicatepatient");
            }

            // If not exact match, log warning but allow creation
            // (Staff can decide if it's actually a duplicate or not)
            log.info("Creating patient despite duplicate warning (not exact match)");
        }

        Account account = null;

        // Check if patient needs account (email provided)
        // NOTE: We no longer require username/password - patient will set password via
        // email
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {

            log.debug("Creating account for patient with email: {}", request.getEmail());

            // Check email uniqueness
            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestAlertException(
                        "Email đã tồn tại",
                        "account",
                        "emailexists");
            }
        }

        // Check phone uniqueness across both Patient and Employee tables
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            log.debug("Checking phone uniqueness: {}", request.getPhone());
            
            if (patientRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestAlertException(
                        "Số điện thoại đã tồn tại",
                        "patient",
                        "phoneexists");
            }

            if (employeeRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestAlertException(
                        "Số điện thoại đã tồn tại",
                        "employee",
                        "phoneexists");
            }
        }

        // Continue with account creation if email provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {

            // V23/V24: Get username from request or auto-generate from email
            String username = request.getUsername();
            if (username == null || username.trim().isEmpty()) {
                // Fallback: Extract username from email (before @)
                username = request.getEmail().split("@")[0];

                // Make sure username is unique by adding counter if needed
                String baseUsername = username;
                int counter = 1;
                while (accountRepository.existsByUsername(username)) {
                    username = baseUsername + counter;
                    counter++;
                }
                log.debug("Auto-generated username from email: {}", username);
            } else {
                // Check username uniqueness if provided by staff
                if (accountRepository.existsByUsername(username)) {
                    throw new BadRequestAlertException(
                            "Tên đăng nhập đã tồn tại",
                            "account",
                            "usernameexists");
                }
                log.debug("Using staff-provided username: {}", username);
            }

            // Create account for patient with TEMPORARY PASSWORD (patient will set real
            // password via email)
            // We create a temporary random password so the account can be saved,
            // but patient MUST reset it via email link before they can login
            String temporaryPassword = UUID.randomUUID().toString(); // Random temp password

            // Get ROLE_PATIENT from database
            Role patientRole = roleRepository.findById("ROLE_PATIENT")
                    .orElseThrow(() -> new BadRequestAlertException(
                            "Không tìm thấy vai trò ROLE_PATIENT trong hệ thống",
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
                log.info(" Welcome email with password setup link sent to: {}", account.getEmail());

            } catch (Exception e) {
                // Log error but don't fail the entire patient creation
                log.error(" Failed to send welcome email to {}: {}", account.getEmail(), e.getMessage(), e);
                log.warn(
                        " Patient account created successfully, but email not sent. Manual password setup may be required.");
                log.warn(" Possible causes: SMTP server not configured, network error, invalid email address");
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
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + MANAGE_PATIENT + "')")
    @Transactional
    public PatientInfoResponse updatePatient(String patientCode, UpdatePatientRequest request) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy bệnh nhân với mã: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        // Check phone uniqueness if phone is being updated
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            // Only check if phone is actually changing
            if (!request.getPhone().equals(patient.getPhone())) {
                log.debug("Checking phone uniqueness for update: {}", request.getPhone());
                
                if (patientRepository.existsByPhone(request.getPhone())) {
                    throw new BadRequestAlertException(
                            "Số điện thoại đã tồn tại",
                            "patient",
                            "phoneexists");
                }

                if (employeeRepository.existsByPhone(request.getPhone())) {
                    throw new BadRequestAlertException(
                            "Số điện thoại đã tồn tại",
                            "employee",
                            "phoneexists");
                }
            }
        }

        // Track if booking block status is being changed
        boolean blockingStatusChanged = request.getIsBookingBlocked() != null &&
                !request.getIsBookingBlocked().equals(patient.getIsBookingBlocked());

        // Update only non-null fields
        patientMapper.updatePatientFromRequest(request, patient);

        // If admin is blocking/unblocking patient, track who and when
        if (blockingStatusChanged) {
            if (Boolean.TRUE.equals(request.getIsBookingBlocked())) {
                // Blocking patient - set blocked_by and blocked_at
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
                patient.setBlockedBy(auth.getName());
                patient.setBlockedAt(java.time.LocalDateTime.now());
            } else {
                // Unblocking patient - clear blocking fields
                patient.setBlockedBy(null);
                patient.setBlockedAt(null);
                patient.setBookingBlockReason(null);
                patient.setBookingBlockNotes(null);
            }
        }

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
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + MANAGE_PATIENT + "')")
    @Transactional
    public PatientInfoResponse replacePatient(String patientCode, ReplacePatientRequest request) {
        Patient patient = patientRepository.findOneByPatientCode(patientCode)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy bệnh nhân với mã: " + patientCode,
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
                        "Không tìm thấy bệnh nhân với mã: " + patientCode,
                        "Patient",
                        "patientnotfound"));

        // Soft delete
        patient.setIsActive(false);
        patientRepository.save(patient);
    }

    /**
     * Get all tooth statuses for a patient (API 8.9)
     * Only returns abnormal teeth - teeth not in response are considered HEALTHY
     *
     * @param patientId the patient ID
     * @return list of tooth status responses
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_PATIENT + "')")
    @Transactional(readOnly = true)
    public List<ToothStatusResponse> getToothStatus(Integer patientId) {
        // Verify patient exists
        patientRepository.findById(patientId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy bệnh nhân với ID: " + patientId,
                        "Patient",
                        "patientnotfound"));

        List<PatientToothStatus> statuses = patientToothStatusRepository.findByPatient_PatientId(patientId);

        return statuses.stream()
                .map(status -> ToothStatusResponse.builder()
                        .toothStatusId(status.getToothStatusId())
                        .patientId(status.getPatient().getPatientId())
                        .toothNumber(status.getToothNumber())
                        .status(status.getStatus())
                        .notes(status.getNotes())
                        .recordedAt(status.getRecordedAt())
                        .updatedAt(status.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Update tooth status for a patient (API 8.10)
     * Creates history record on every status change
     *
     * @param patientId   the patient ID
     * @param toothNumber the tooth number
     * @param request     the update request
     * @param changedBy   the employee making the change
     * @return updated tooth status response
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + VIEW_PATIENT + "') or hasAuthority('" + MANAGE_PATIENT
            + "')")
    @Transactional
    public UpdateToothStatusResponse updateToothStatus(
            Integer patientId,
            String toothNumber,
            UpdateToothStatusRequest request,
            Integer changedBy) {

        // Verify patient exists
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy bệnh nhân với ID: " + patientId,
                        "Patient",
                        "patientnotfound"));

        // Find existing tooth status or create new one
        Optional<PatientToothStatus> existingStatusOpt = patientToothStatusRepository
                .findByPatient_PatientIdAndToothNumber(patientId, toothNumber);

        PatientToothStatus toothStatus;
        ToothConditionEnum oldStatus = null;

        if (existingStatusOpt.isPresent()) {
            toothStatus = existingStatusOpt.get();
            oldStatus = toothStatus.getStatus();
            toothStatus.setStatus(request.getStatus());
            toothStatus.setNotes(request.getNotes());
        } else {
            toothStatus = new PatientToothStatus();
            toothStatus.setPatient(patient);
            toothStatus.setToothNumber(toothNumber);
            toothStatus.setStatus(request.getStatus());
            toothStatus.setNotes(request.getNotes());
        }

        PatientToothStatus savedStatus = patientToothStatusRepository.save(toothStatus);

        // Create history record
        PatientToothStatusHistory history = new PatientToothStatusHistory();
        history.setPatient(patient);
        history.setToothNumber(toothNumber);
        history.setOldStatus(oldStatus);
        history.setNewStatus(request.getStatus());

        // Set changedBy employee
        Employee employee = new Employee();
        employee.setEmployeeId(changedBy);
        history.setChangedBy(employee);

        history.setReason(request.getReason());

        patientToothStatusHistoryRepository.save(history);

        return UpdateToothStatusResponse.builder()
                .toothStatusId(savedStatus.getToothStatusId())
                .patientId(savedStatus.getPatient().getPatientId())
                .toothNumber(savedStatus.getToothNumber())
                .status(savedStatus.getStatus())
                .notes(savedStatus.getNotes())
                .recordedAt(savedStatus.getRecordedAt())
                .updatedAt(savedStatus.getUpdatedAt())
                .message("Cập nhật tình trạng răng thành công")
                .build();
    }

    /**
     * Get current patient profile (for mobile app - Patient Portal)
     * Patient can only access their own profile
     *
     * @param username the logged-in username from JWT
     * @return PatientDetailResponse with full information
     */
    @Transactional(readOnly = true)
    public com.dental.clinic.management.patient.dto.response.PatientDetailResponse getCurrentPatientProfile(
            String username) {
        log.info("Getting patient profile for username: {}", username);

        Account account = accountRepository.findOneByUsername(username)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy tài khoản",
                        "Account",
                        "accountnotfound"));

        Patient patient = patientRepository.findOneByAccountAccountId(account.getAccountId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Không tìm thấy bệnh nhân cho tài khoản này",
                        "Patient",
                        "patientnotfound"));

        return mapToPatientDetailResponse(patient, account);
    }

    /**
     * Map Patient entity to PatientDetailResponse with full details
     */
    private com.dental.clinic.management.patient.dto.response.PatientDetailResponse mapToPatientDetailResponse(
            Patient patient, Account account) {

        String fullName = patient.getFirstName() + " " + patient.getLastName();

        Integer age = null;
        if (patient.getDateOfBirth() != null) {
            age = java.time.Period.between(patient.getDateOfBirth(), java.time.LocalDate.now()).getYears();
        }

        return com.dental.clinic.management.patient.dto.response.PatientDetailResponse.builder()
                .patientId(patient.getPatientId())
                .patientCode(patient.getPatientCode())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .fullName(fullName)
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .dateOfBirth(patient.getDateOfBirth())
                .age(age)
                .address(patient.getAddress())
                .gender(patient.getGender() != null ? patient.getGender().name() : null)
                .medicalHistory(patient.getMedicalHistory())
                .allergies(patient.getAllergies())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .emergencyContactRelationship(patient.getEmergencyContactRelationship())
                .guardianName(patient.getGuardianName())
                .guardianPhone(patient.getGuardianPhone())
                .guardianRelationship(patient.getGuardianRelationship())
                .guardianCitizenId(patient.getGuardianCitizenId())
                .isActive(patient.getIsActive())
                .consecutiveNoShows(patient.getConsecutiveNoShows())
                .isBookingBlocked(patient.getIsBookingBlocked())
                .bookingBlockReason(patient.getBookingBlockReason() != null
                        ? patient.getBookingBlockReason().name()
                        : null)
                .bookingBlockNotes(patient.getBookingBlockNotes())
                .blockedBy(patient.getBlockedBy())
                .blockedAt(patient.getBlockedAt())
                .accountId(account != null ? account.getAccountId() : null)
                .username(account != null ? account.getUsername() : null)
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
