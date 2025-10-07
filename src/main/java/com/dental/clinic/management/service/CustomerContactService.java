package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.CustomerContact;
import com.dental.clinic.management.dto.request.CreateContactRequest;
import com.dental.clinic.management.dto.request.UpdateContactRequest;
import com.dental.clinic.management.dto.response.ContactInfoResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.mapper.CustomerContactMapper;
import com.dental.clinic.management.repository.CustomerContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

@Service
public class CustomerContactService {

    private final CustomerContactRepository repository;
    private final CustomerContactMapper mapper;

    public CustomerContactService(CustomerContactRepository repository, CustomerContactMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @PreAuthorize("hasAuthority('" + VIEW_CONTACT + "')")
    @Transactional(readOnly = true)
    public Page<ContactInfoResponse> listContacts(int page, int size, String sortBy, String sortDirection) {
        page = Math.max(0, page);
        size = (size <= 0 || size > 100) ? 10 : size;
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return repository.findAll(pageable).map(mapper::toContactInfoResponse);
    }

    @PreAuthorize("hasAuthority('" + VIEW_CONTACT + "')")
    @Transactional(readOnly = true)
    public ContactInfoResponse getContact(String contactId) {
        return repository.findById(contactId)
                .map(mapper::toContactInfoResponse)
                .orElseThrow(() -> new BadRequestAlertException("Contact not found: " + contactId, "customer_contact", "contactnotfound"));
    }

    @PreAuthorize("hasAuthority('" + CREATE_CONTACT + "')")
    @Transactional
    public ContactInfoResponse createContact(CreateContactRequest request) {
        CustomerContact entity = mapper.toContact(request);
        // generate id
        entity.setContactId(UUID.randomUUID().toString());
        if (entity.getStatus() == null) entity.setStatus(com.dental.clinic.management.domain.enums.CustomerContactStatus.NEW);
        CustomerContact saved = repository.save(entity);
        return mapper.toContactInfoResponse(saved);
    }

    @PreAuthorize("hasAuthority('" + UPDATE_CONTACT + "')")
    @Transactional
    public ContactInfoResponse updateContact(String contactId, UpdateContactRequest request) {
        CustomerContact contact = repository.findById(contactId)
                .orElseThrow(() -> new BadRequestAlertException("Contact not found: " + contactId, "customer_contact", "contactnotfound"));
        mapper.updateContactFromRequest(request, contact);
        CustomerContact saved = repository.save(contact);
        return mapper.toContactInfoResponse(saved);
    }

    @PreAuthorize("hasAuthority('" + DELETE_CONTACT + "')")
    @Transactional
    public void deleteContact(String contactId) {
        CustomerContact contact = repository.findById(contactId)
                .orElseThrow(() -> new BadRequestAlertException("Contact not found: " + contactId, "customer_contact", "contactnotfound"));
        // Soft delete by setting status to NOT_INTERESTED or a specific flag; here we'll set status to NOT_INTERESTED
        contact.setStatus(com.dental.clinic.management.domain.enums.CustomerContactStatus.NOT_INTERESTED);
        repository.save(contact);
    }
}
