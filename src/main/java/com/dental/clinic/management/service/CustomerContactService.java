package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.CustomerContact;
import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.domain.ContactHistory;
import com.dental.clinic.management.dto.request.CreateContactRequest;
import com.dental.clinic.management.dto.request.UpdateContactRequest;
import com.dental.clinic.management.dto.response.ContactInfoResponse;
import com.dental.clinic.management.dto.response.ContactHistoryResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.mapper.CustomerContactMapper;
import com.dental.clinic.management.mapper.ContactHistoryMapper;
import com.dental.clinic.management.repository.CustomerContactRepository;
import com.dental.clinic.management.repository.ContactHistoryRepository;
import com.dental.clinic.management.repository.EmployeeRepository;
import com.dental.clinic.management.domain.enums.CustomerContactStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

@Service
public class CustomerContactService {

    private final Logger log = LoggerFactory.getLogger(CustomerContactService.class);

    private final CustomerContactRepository repository;
    private final CustomerContactMapper mapper;
    private final ContactHistoryRepository historyRepository;
    private final ContactHistoryMapper historyMapper;
    private final EmployeeRepository employeeRepository;

    public CustomerContactService(CustomerContactRepository repository,
            CustomerContactMapper mapper,
            ContactHistoryRepository historyRepository,
            ContactHistoryMapper historyMapper,
            EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.historyRepository = historyRepository;
        this.historyMapper = historyMapper;
        this.employeeRepository = employeeRepository;
    }

    // List: VIEW_CONTACT permission or ROLE_ADMIN
    @PreAuthorize("hasAnyAuthority('" + VIEW_CONTACT + "','" + ADMIN + "')")
    @Transactional(readOnly = true)
    public Page<ContactInfoResponse> listContacts(int page, int size, String sortBy, String sortDirection) {
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;
        Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy == null ? "createdAt" : sortBy));
        return repository.findAll(pageable).map(mapper::toContactInfoResponse);
    }

    // Get by id: VIEW_CONTACT permission or ROLE_ADMIN
    @PreAuthorize("hasAnyAuthority('" + VIEW_CONTACT + "','" + ADMIN + "')")
    @Transactional(readOnly = true)
    public ContactInfoResponse getContact(String contactId) {
        CustomerContact contact = repository.findOneByContactId(contactId)
                .orElseThrow(() -> new BadRequestAlertException("Contact not found: " + contactId, "customer_contact",
                        "contactnotfound"));

        ContactInfoResponse resp = mapper.toContactInfoResponse(contact);

        // load history and attach
        List<ContactHistory> historyEntities = historyRepository.findByContactIdOrderByCreatedAtDesc(contactId);
        List<ContactHistoryResponse> history = historyMapper.toResponseList(historyEntities);
        resp.setHistory(history);

        return resp;
    }

    // Create: CREATE_CONTACT permission or ROLE_ADMIN
    @PreAuthorize("hasAnyAuthority('" + CREATE_CONTACT + "','" + ADMIN + "')")
    @Transactional
    public ContactInfoResponse createContact(CreateContactRequest request) {
        if (request == null) {
            throw new BadRequestAlertException("Request required", "customer_contact", "request.required");
        }

        if (request.getPhone() != null && repository.existsByPhone(request.getPhone())) {
            log.warn("Duplicate phone creating contact: {}", request.getPhone());
        }

        // validate assignedTo if provided -> do NOT throw, just ignore and log if
        // employee not found
        if (request.getAssignedTo() != null && !request.getAssignedTo().isBlank()) {
            if (!employeeRepository.existsById(request.getAssignedTo())) {
                log.warn("Assigned employee not found, ignoring assignedTo: {}", request.getAssignedTo());
                request.setAssignedTo(null);
            }
        }

        CustomerContact entity = mapper.toEntity(request);

        // generate contactId as CT + YYYYMMDD + SEQ (daily) if not provided; fallback
        // to UUID
        if (entity.getContactId() == null || entity.getContactId().isBlank()) {
            try {
                LocalDate today = LocalDate.now();
                LocalDateTime from = today.atStartOfDay();
                LocalDateTime to = today.plusDays(1).atStartOfDay().minusNanos(1);
                long seq = repository.countByCreatedAtBetween(from, to) + 1;
                String date = today.format(DateTimeFormatter.BASIC_ISO_DATE);
                String seqStr = String.format("%03d", seq);
                entity.setContactId("CT" + date + seqStr);
            } catch (Exception ex) {
                // fallback
                entity.setContactId(UUID.randomUUID().toString());
            }
        }

        if (entity.getStatus() == null) {
            entity.setStatus(com.dental.clinic.management.domain.enums.CustomerContactStatus.NEW);
        }

        if (entity.getSource() == null) {
            entity.setSource(com.dental.clinic.management.domain.enums.CustomerContactSource.WEBSITE);
        }

        CustomerContact saved = repository.save(entity);
        log.debug("Created contact {}", saved.getContactId());
        return mapper.toContactInfoResponse(saved);
    }

    // Update: UPDATE_CONTACT permission or ROLE_ADMIN
    @PreAuthorize("hasAnyAuthority('" + UPDATE_CONTACT + "','" + ADMIN + "')")
    @Transactional
    public ContactInfoResponse updateContact(String contactId, UpdateContactRequest request) {
        CustomerContact contact = repository.findOneByContactId(contactId)
                .orElseThrow(() -> new BadRequestAlertException("Contact not found: " + contactId, "customer_contact",
                        "contactnotfound"));

        // validate assignedTo if provided in update -> do NOT throw, ignore if employee
        // not found
        if (request != null && request.getAssignedTo() != null && !request.getAssignedTo().isBlank()) {
            if (!employeeRepository.existsById(request.getAssignedTo())) {
                log.warn("Assigned employee not found, ignoring assignedTo update: {}", request.getAssignedTo());
                request.setAssignedTo(null); // leave existing assignedTo unchanged
            }
        }

        mapper.updateContactFromRequest(request, contact);
        CustomerContact saved = repository.save(contact);
        log.debug("Updated contact {}", saved.getContactId());
        return mapper.toContactInfoResponse(saved);
    }

    // Delete (soft): DELETE_CONTACT permission or ROLE_ADMIN
    @PreAuthorize("hasAnyAuthority('" + DELETE_CONTACT + "','" + ADMIN + "')")
    @Transactional
    public void deleteContact(String contactId) {
        CustomerContact contact = repository.findOneByContactId(contactId)
                .orElseThrow(() -> new BadRequestAlertException("Contact not found: " + contactId, "customer_contact",
                        "contactnotfound"));
        contact.setStatus(com.dental.clinic.management.domain.enums.CustomerContactStatus.NOT_INTERESTED);
        repository.save(contact);
        log.debug("Soft-deleted contact {}", contactId);
    }

    /**
     * Assign contact to a specific employee (manual) or auto-assign to receptionist
     * with least NEW contacts.
     * Manual: validate employee exists.
     */
    @PreAuthorize("hasAnyAuthority('" + UPDATE_CONTACT + "','" + ADMIN + "')")
    @Transactional
    public ContactInfoResponse assignContact(String contactId, String employeeId /* nullable -> auto */) {
        CustomerContact contact = repository.findOneByContactId(contactId)
                .orElseThrow(() -> new BadRequestAlertException("Contact not found: " + contactId, "customer_contact",
                        "contactnotfound"));

        if (employeeId != null && !employeeId.isBlank()) {
            if (!employeeRepository.existsById(employeeId)) {
                throw new BadRequestAlertException("Assigned employee not found: " + employeeId, "customer_contact",
                        "employee_not_found");
            }
            contact.setAssignedTo(employeeId);
        } else {
            // auto assign: choose employee with smallest number of NEW contacts
            List<Employee> employees = employeeRepository.findAll();
            if (employees.isEmpty()) {
                log.warn("No employees found for auto-assign");
            } else {
                // find employee with min count of NEW
                Employee best = employees.get(0);
                long bestCount = repository.countByAssignedToAndStatus(best.getEmployeeId(), CustomerContactStatus.NEW);
                for (Employee e : employees) {
                    long cnt = repository.countByAssignedToAndStatus(e.getEmployeeId(), CustomerContactStatus.NEW);
                    if (cnt < bestCount) {
                        best = e;
                        bestCount = cnt;
                    }
                }
                contact.setAssignedTo(best.getEmployeeId());
            }
        }
        CustomerContact saved = repository.save(contact);
        return mapper.toContactInfoResponse(saved);
    }

    /**
     * Convert contact -> patient (simple implementation).
     * Validations: contact.status != CONVERTED && != NOT_INTERESTED
     * Sets status = CONVERTED and sets convertedPatientId to a generated UUID (hook
     * to real patient creation can be added).
     */
    @PreAuthorize("hasAnyAuthority('" + UPDATE_CONTACT + "','" + ADMIN + "')")
    @Transactional
    public ContactInfoResponse convertContact(String contactId) {
        CustomerContact contact = repository.findOneByContactId(contactId)
                .orElseThrow(() -> new BadRequestAlertException("Contact not found: " + contactId, "customer_contact",
                        "contactnotfound"));

        if (contact.getStatus() == CustomerContactStatus.CONVERTED) {
            throw new BadRequestAlertException("Contact already converted: " + contactId, "customer_contact",
                    "already_converted");
        }
        if (contact.getStatus() == CustomerContactStatus.NOT_INTERESTED) {
            throw new BadRequestAlertException("Contact not eligible to convert: " + contactId, "customer_contact",
                    "not_interested");
        }

        // NOTE: integrate PatientService.create(...) here if you want a real patient
        // record
        String patientId = UUID.randomUUID().toString();
        contact.setConvertedPatientId(patientId);
        contact.setStatus(CustomerContactStatus.CONVERTED);
        CustomerContact saved = repository.save(contact);
        return mapper.toContactInfoResponse(saved);
    }

    /**
     * Stats: counts by status, by source, by assignedTo (simple aggregated maps)
     */
    @PreAuthorize("hasAnyAuthority('" + VIEW_CONTACT + "','" + ADMIN + "')")
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        List<CustomerContact> all = repository.findAll();
        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(c -> (c.getStatus() == null ? "UNKNOWN" : c.getStatus().name()),
                        Collectors.counting()));
        Map<String, Long> bySource = all.stream()
                .collect(Collectors.groupingBy(c -> (c.getSource() == null ? "UNKNOWN" : c.getSource().name()),
                        Collectors.counting()));
        Map<String, Long> byAssigned = all.stream()
                .collect(Collectors.groupingBy(c -> (c.getAssignedTo() == null ? "UNASSIGNED" : c.getAssignedTo()),
                        Collectors.counting()));

        Map<String, Object> out = new HashMap<>();
        out.put("byStatus", byStatus);
        out.put("bySource", bySource);
        out.put("byAssigned", byAssigned);
        out.put("total", all.size());
        return out;
    }

    /**
     * Conversion rate = converted / total (safe divide)
     */
    @PreAuthorize("hasAnyAuthority('" + VIEW_CONTACT + "','" + ADMIN + "')")
    @Transactional(readOnly = true)
    public Map<String, Object> getConversionRate() {
        List<CustomerContact> all = repository.findAll();
        long total = all.size();
        long converted = all.stream().filter(c -> c.getStatus() == CustomerContactStatus.CONVERTED).count();
        double rate = total == 0 ? 0.0 : (100.0 * converted / total);
        Map<String, Object> out = new HashMap<>();
        out.put("total", total);
        out.put("converted", converted);
        out.put("ratePercent", rate);
        return out;
    }
}
