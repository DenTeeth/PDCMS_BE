package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.request.CreateContactHistoryRequest;
import com.dental.clinic.management.dto.response.ContactHistoryResponse;
import com.dental.clinic.management.service.ContactHistoryService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts/{contactId}/history")
@Tag(name = "Contact History", description = "APIs for managing contact history entries")
public class ContactHistoryController {

    private final ContactHistoryService service;

    public ContactHistoryController(ContactHistoryService service) {
        this.service = service;
    }

    @GetMapping("")
    @Operation(summary = "List contact history", description = "Get history entries for a given contact")
    @ApiMessage("List contact history successfully")
    public ResponseEntity<List<ContactHistoryResponse>> listHistory(@Parameter(description = "Contact id", required = true) @PathVariable String contactId) {
        List<ContactHistoryResponse> resp = service.listHistoryForContact(contactId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("")
    @Operation(summary = "Add contact history", description = "Add a history record for a contact")
    @ApiMessage("Add contact history successfully")
    public ResponseEntity<ContactHistoryResponse> addHistory(@Valid @RequestBody CreateContactHistoryRequest request) throws URISyntaxException {
        ContactHistoryResponse resp = service.addHistory(request);
        return ResponseEntity.created(new URI("/api/v1/contacts/" + resp.getContactId() + "/history/" + resp.getHistoryId())).body(resp);
    }
}
