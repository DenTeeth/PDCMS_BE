package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.ContactHistory;
import com.dental.clinic.management.dto.request.CreateContactHistoryRequest;
import com.dental.clinic.management.dto.response.ContactHistoryResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.mapper.ContactHistoryMapper;
import com.dental.clinic.management.repository.ContactHistoryRepository;
import com.dental.clinic.management.repository.CustomerContactRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

@Service
public class ContactHistoryService {

    private final ContactHistoryRepository repository;
    private final ContactHistoryMapper mapper;
    private final CustomerContactRepository contactRepository;

    public ContactHistoryService(ContactHistoryRepository repository, ContactHistoryMapper mapper, CustomerContactRepository contactRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.contactRepository = contactRepository;
    }

    @PreAuthorize("hasAuthority('" + VIEW_CONTACT_HISTORY + "')")
    @Transactional(readOnly = true)
    public List<ContactHistoryResponse> listHistoryForContact(String contactId) {
        // ensure contact exists
        contactRepository.findById(contactId).orElseThrow(() -> new BadRequestAlertException("Contact not found: " + contactId, "contact_history", "contactnotfound"));
        return repository.findByContactIdOrderByCreatedAtDesc(contactId).stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('" + CREATE_CONTACT_HISTORY + "')")
    @Transactional
    public ContactHistoryResponse addHistory(CreateContactHistoryRequest request) {
        // ensure contact exists
        contactRepository.findById(request.getContactId()).orElseThrow(() -> new BadRequestAlertException("Contact not found: " + request.getContactId(), "contact_history", "contactnotfound"));
        ContactHistory h = mapper.toEntity(request);
        h.setHistoryId(UUID.randomUUID().toString());
        ContactHistory saved = repository.save(h);
        return mapper.toResponse(saved);
    }
}
