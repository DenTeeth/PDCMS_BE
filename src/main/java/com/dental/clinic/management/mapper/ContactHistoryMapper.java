package com.dental.clinic.management.mapper;

import org.springframework.stereotype.Component;

import com.dental.clinic.management.domain.ContactHistory;
import com.dental.clinic.management.dto.request.CreateContactHistoryRequest;
import com.dental.clinic.management.dto.response.ContactHistoryResponse;

@Component
public class ContactHistoryMapper {

    public ContactHistoryResponse toResponse(ContactHistory h) {
        if (h == null) return null;
        ContactHistoryResponse r = new ContactHistoryResponse();
        r.setHistoryId(h.getHistoryId());
        r.setContactId(h.getContactId());
        r.setEmployeeId(h.getEmployeeId());
        r.setAction(h.getAction());
        r.setContent(h.getContent());
        r.setCreatedAt(h.getCreatedAt());
        return r;
    }

    public ContactHistory toEntity(CreateContactHistoryRequest req) {
        if (req == null) return null;
        ContactHistory h = new ContactHistory();
        h.setContactId(req.getContactId());
        h.setEmployeeId(req.getEmployeeId());
        h.setAction(req.getAction());
        h.setContent(req.getContent());
        return h;
    }
}
