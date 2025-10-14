package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.ContactHistory;
import com.dental.clinic.management.dto.request.CreateContactHistoryRequest;
import com.dental.clinic.management.dto.response.ContactHistoryResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.mapper.ContactHistoryMapper;
import com.dental.clinic.management.repository.ContactHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

@Service
public class ContactHistoryService {

    private final Logger log = LoggerFactory.getLogger(ContactHistoryService.class);

    private final ContactHistoryRepository repository;
    private final ContactHistoryMapper mapper;

    public ContactHistoryService(ContactHistoryRepository repository, ContactHistoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * List history records for a contact (ordered desc).
     * Permissions: VIEW_CONTACT_HISTORY or ROLE_ADMIN
     */
    @PreAuthorize("hasAnyAuthority('" + VIEW_CONTACT_HISTORY + "','" + ADMIN + "')")
    @Transactional(readOnly = true)
    public List<ContactHistoryResponse> listHistoryForContact(String contactId) {
        if (contactId == null || contactId.trim().isEmpty()) {
            throw new BadRequestAlertException("contactId is required", "contact_history", "contactid.required");
        }
        var list = repository.findByContactIdOrderByCreatedAtDesc(contactId);
        return mapper.toResponseList(list);
    }

    /**
     * Add a history record. employeeId is taken from current authentication.
     * Permissions: CREATE_CONTACT_HISTORY or ROLE_ADMIN
     */
    @PreAuthorize("hasAnyAuthority('" + CREATE_CONTACT_HISTORY + "','" + ADMIN + "')")
    @Transactional
    public ContactHistoryResponse addHistory(CreateContactHistoryRequest req) {
        if (req == null) {
            throw new BadRequestAlertException("Request required", "contact_history", "request.required");
        }
        if (req.getContactId() == null || req.getContactId().trim().isEmpty()) {
            throw new BadRequestAlertException("contactId is required", "contact_history", "contactid.required");
        }
        if (req.getAction() == null) {
            throw new BadRequestAlertException("action is required", "contact_history", "action.required");
        }
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new BadRequestAlertException("content is required", "contact_history", "content.required");
        }

        ContactHistory h = new ContactHistory();
        h.setContactId(req.getContactId());
        h.setAction(req.getAction());
        h.setContent(req.getContent());

        // set employeeId from current authentication (principal name)
        // Note: if FK constraint fails, either remove FK or ensure employee exists
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            // Optionally lookup employee by username/account mapping if needed
            // For now, set directly (may fail FK if employee not exists)
            h.setEmployeeId(auth.getName());
        } else {
            // fallback for testing: set to null or a known employee_id
            h.setEmployeeId(null);
        }

        // generate historyId HIST + YYYYMMDD + SEQ (daily). Note: not race-proof.
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay().minusNanos(1);
        long seq = repository.countByCreatedAtBetween(from, to) + 1;
        String date = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String seqStr = String.format("%03d", seq);
        h.setHistoryId("HIST" + date + seqStr);

        ContactHistory saved = repository.save(h);
        log.debug("Created contact history {} for contact {}", saved.getHistoryId(), saved.getContactId());
        return mapper.toResponse(saved);
    }
}
