package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.request.CreateContactRequest;
import com.dental.clinic.management.dto.request.UpdateContactRequest;
import com.dental.clinic.management.dto.response.ContactInfoResponse;
import com.dental.clinic.management.service.CustomerContactService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/v1/contacts")
@Tag(name = "Customer Contact Management", description = "APIs for managing customer contacts")
public class CustomerContactController {

    private final CustomerContactService service;

    public CustomerContactController(CustomerContactService service) {
        this.service = service;
    }

    @GetMapping("")
    @Operation(summary = "List contacts", description = "List customer contacts with pagination")
    @ApiMessage("List contacts successfully")
    public ResponseEntity<Page<ContactInfoResponse>> listContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Page<ContactInfoResponse> resp = service.listContacts(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{contactId}")
    @Operation(summary = "Get contact", description = "Get contact details by id")
    @ApiMessage("Get contact successfully")
    public ResponseEntity<ContactInfoResponse> getContact(@PathVariable String contactId) {
        ContactInfoResponse resp = service.getContact(contactId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("")
    @Operation(summary = "Create contact", description = "Create a new customer contact")
    @ApiMessage("Create contact successfully")
    public ResponseEntity<ContactInfoResponse> createContact(@Valid @RequestBody CreateContactRequest request) throws URISyntaxException {
        ContactInfoResponse resp = service.createContact(request);
        return ResponseEntity.created(new URI("/api/v1/contacts/" + resp.getContactId())).body(resp);
    }

    @PatchMapping("/{contactId}")
    @Operation(summary = "Update contact", description = "Update contact fields (partial)")
    @ApiMessage("Update contact successfully")
    public ResponseEntity<ContactInfoResponse> updateContact(@PathVariable String contactId, @Valid @RequestBody UpdateContactRequest request) {
        ContactInfoResponse resp = service.updateContact(contactId, request);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{contactId}")
    @Operation(summary = "Delete contact", description = "Soft delete contact")
    @ApiMessage("Delete contact successfully")
    public ResponseEntity<Void> deleteContact(@PathVariable String contactId) {
        service.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }
}
