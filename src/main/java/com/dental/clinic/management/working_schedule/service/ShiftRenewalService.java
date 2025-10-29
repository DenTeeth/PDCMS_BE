package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.InvalidRequestException;
import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.working_schedule.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.working_schedule.domain.PartTimeSlot;
import com.dental.clinic.management.working_schedule.domain.ShiftRenewalRequest;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.RenewalResponseRequest;
import com.dental.clinic.management.working_schedule.dto.response.ShiftRenewalResponse;
import com.dental.clinic.management.working_schedule.enums.RenewalStatus;
import com.dental.clinic.management.working_schedule.mapper.ShiftRenewalMapper;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeSlotRepository;
import com.dental.clinic.management.working_schedule.repository.ShiftRenewalRequestRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling shift renewal requests for part-time employees.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShiftRenewalService {

    private final ShiftRenewalRequestRepository renewalRepository;
    private final EmployeeShiftRegistrationRepository registrationRepository;
    private final PartTimeSlotRepository partTimeSlotRepository;
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRenewalMapper mapper;

    /**
     * Get all pending renewal requests for the current employee.
     * Only returns non-expired requests in PENDING_ACTION status.
     *
     * @param employeeId the employee ID from token
     * @return list of pending renewals
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.VIEW_RENEWAL_OWN + "')")
    public List<ShiftRenewalResponse> getPendingRenewals(Integer employeeId) {
        log.info("Getting pending renewals for employee ID: {}", employeeId);

        List<ShiftRenewalRequest> renewals = renewalRepository.findPendingByEmployeeId(
                employeeId,
                LocalDateTime.now());

        log.info("Found {} pending renewals for employee ID: {}", renewals.size(), employeeId);

        return renewals.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Respond to a renewal request (CONFIRMED or DECLINED).
     * If CONFIRMED, automatically extend the original shift registration by 3
     * months.
     *
     * @param renewalId  the renewal ID
     * @param employeeId the employee ID from token
     * @param request    the response (CONFIRMED or DECLINED)
     * @return updated renewal response
     * @throws NotFoundException       if renewal not found
     * @throws InvalidRequestException if invalid state or not owned by employee
     */
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.RESPOND_RENEWAL_OWN + "')")
    @Transactional
    public ShiftRenewalResponse respondToRenewal(
            String renewalId,
            Integer employeeId,
            RenewalResponseRequest request) {
        log.info("Employee {} responding to renewal {}: {}", employeeId, renewalId, request.getAction());

        // 1. Find renewal and verify ownership
        ShiftRenewalRequest renewal = renewalRepository.findByIdAndEmployeeId(renewalId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "NOT_FOUND",
                        String.format("Renewal request %s not found or not owned by employee %d", renewalId,
                                employeeId)));

        // 2. Validate state
        if (!renewal.isPending()) {
            throw new InvalidRequestException(
                    "INVALID_STATE",
                    String.format("Renewal %s is not in PENDING_ACTION status (current: %s)",
                            renewalId, renewal.getStatus()));
        }

        if (renewal.isExpired()) {
            throw new InvalidRequestException(
                    "EXPIRED",
                    String.format("Renewal %s has expired at %s", renewalId, renewal.getExpiresAt()));
        }

        // 3. Update renewal status
        RenewalStatus newStatus = "CONFIRMED".equals(request.getAction())
                ? RenewalStatus.CONFIRMED
                : RenewalStatus.DECLINED;

        renewal.setStatus(newStatus);
        renewal.setConfirmedAt(LocalDateTime.now());

        // 4. If CONFIRMED, extend the original registration by 3 months
        if (newStatus == RenewalStatus.CONFIRMED) {
            extendShiftRegistration(renewal.getExpiringRegistration());
        }

        ShiftRenewalRequest saved = renewalRepository.save(renewal);
        log.info("Renewal {} status updated to {}", renewalId, newStatus);

        return mapper.toResponse(saved);
    }

    /**
     * Extend shift registration by 3 months.
     * Updates the effective_to date of the registration.
     *
     * @param registration the registration to extend
     */
    private void extendShiftRegistration(EmployeeShiftRegistration registration) {
        if (registration.getEffectiveTo() == null) {
            log.warn("Registration {} has no effective_to date, cannot extend",
                    registration.getRegistrationId());
            return;
        }

        LocalDate newEffectiveTo = registration.getEffectiveTo().plusMonths(3);
        registration.setEffectiveTo(newEffectiveTo);
        registrationRepository.save(registration);

        log.info("Extended registration {} from {} to {}",
                registration.getRegistrationId(),
                registration.getEffectiveTo().minusMonths(3),
                newEffectiveTo);
    }

    /**
     * Create a renewal request for an expiring registration.
     * This method is called by the scheduled job to auto-create renewals.
     *
     * @param registrationId the expiring registration ID (String format
     *                       ESRyymmddSSS)
     * @return created renewal response
     * @throws NotFoundException if registration or employee not found
     */
    @Transactional
    public ShiftRenewalResponse createRenewalRequest(String registrationId) {
        log.info("Creating renewal request for registration ID: {}", registrationId);

        // 1. Find registration
        EmployeeShiftRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "NOT_FOUND",
                        String.format("Registration %s not found", registrationId)));

        // 2. Verify employee exists
        Employee employee = employeeRepository.findById(registration.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "NOT_FOUND",
                        String.format("Employee %d not found", registration.getEmployeeId())));

        // 3. Check if renewal already exists
        boolean exists = renewalRepository.existsByRegistrationIdAndStatus(
                registrationId,
                RenewalStatus.PENDING_ACTION);

        if (exists) {
            log.warn("Renewal request already exists for registration {}", registrationId);
            throw new InvalidRequestException(
                    "ALREADY_EXISTS",
                    String.format("Renewal request already exists for registration %s", registrationId));
        }

        // 4. Create renewal request
        ShiftRenewalRequest renewal = new ShiftRenewalRequest();
        renewal.setExpiringRegistration(registration);
        renewal.setEmployee(employee);
        renewal.setStatus(RenewalStatus.PENDING_ACTION);
        renewal.setExpiresAt(registration.getEffectiveTo().atTime(23, 59, 59)); // Expires on the registration end date
        renewal.setMessage(buildRenewalMessage(registration));

        ShiftRenewalRequest saved = renewalRepository.save(renewal);
        log.info("Created renewal request {} for registration {}", saved.getRenewalId(), registrationId);

        return mapper.toResponse(saved);
    }

    /**
     * Build renewal message with shift details.
     * 
     * @param registration the expiring registration
     * @return formatted message
     */
    private String buildRenewalMessage(EmployeeShiftRegistration registration) {
        // V2: Get shift name from part_time_slot
        String shiftName = "N/A";
        if (registration.getPartTimeSlotId() != null) {
            PartTimeSlot slot = partTimeSlotRepository.findById(registration.getPartTimeSlotId()).orElse(null);
            if (slot != null && slot.getWorkShiftId() != null) {
                WorkShift workShift = workShiftRepository.findById(slot.getWorkShiftId()).orElse(null);
                if (workShift != null) {
                    shiftName = workShift.getShiftName() + " - " + slot.getDayOfWeek();
                }
            }
        }
        
        String expiryDate = registration.getEffectiveTo() != null
                ? registration.getEffectiveTo().toString()
                : "N/A";

        return String.format(
                "Lich dang ky ca [%s] cua ban se het han vao ngay %s. Ban co muon gia han khong?",
                shiftName,
                expiryDate);
    }

    /**
     * Mark expired renewals as EXPIRED.
     * This method is called by a scheduled job.
     *
     * @return number of renewals marked as expired
     */
    @Transactional
    public int markExpiredRenewals() {
        List<ShiftRenewalRequest> expiredRenewals = renewalRepository
                .findExpiredPendingRenewals(LocalDateTime.now());

        expiredRenewals.forEach(renewal -> {
            renewal.setStatus(RenewalStatus.EXPIRED);
            log.info("Marked renewal {} as EXPIRED", renewal.getRenewalId());
        });

        renewalRepository.saveAll(expiredRenewals);

        log.info("Marked {} renewals as EXPIRED", expiredRenewals.size());
        return expiredRenewals.size();
    }
}
