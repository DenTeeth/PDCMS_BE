package com.dental.clinic.management.mapper;

import org.springframework.stereotype.Component;

import com.dental.clinic.management.domain.CustomerContact;
import com.dental.clinic.management.dto.request.CreateContactRequest;
import com.dental.clinic.management.dto.request.UpdateContactRequest;
import com.dental.clinic.management.dto.response.ContactInfoResponse;

@Component
public class CustomerContactMapper {

    public ContactInfoResponse toContactInfoResponse(CustomerContact c) {
        if (c == null) return null;
        ContactInfoResponse r = new ContactInfoResponse();
        r.setContactId(c.getContactId());
        r.setFullName(c.getFullName());
        r.setPhone(c.getPhone());
        r.setEmail(c.getEmail());
        r.setSource(c.getSource());
        r.setStatus(c.getStatus());
        r.setAssignedTo(c.getAssignedTo());
        r.setConvertedPatientId(c.getConvertedPatientId());
        r.setNotes(c.getNotes());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }

    public CustomerContact toContact(CreateContactRequest req) {
        if (req == null) return null;
        CustomerContact c = new CustomerContact();
        c.setFullName(req.getFullName());
        c.setPhone(req.getPhone());
        c.setEmail(req.getEmail());
        c.setSource(req.getSource());
        c.setStatus(req.getStatus());
        c.setAssignedTo(req.getAssignedTo());
        c.setNotes(req.getNotes());
        return c;
    }

    public void updateContactFromRequest(UpdateContactRequest req, CustomerContact c) {
        if (req == null || c == null) return;
        if (req.getFullName() != null) c.setFullName(req.getFullName());
        if (req.getPhone() != null) c.setPhone(req.getPhone());
        if (req.getEmail() != null) c.setEmail(req.getEmail());
        if (req.getSource() != null) c.setSource(req.getSource());
        if (req.getStatus() != null) c.setStatus(req.getStatus());
        if (req.getAssignedTo() != null) c.setAssignedTo(req.getAssignedTo());
        if (req.getNotes() != null) c.setNotes(req.getNotes());
    }
}
