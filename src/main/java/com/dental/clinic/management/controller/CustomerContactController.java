package com.dental.clinic.management.controller;

import com.dental.clinic.management.dto.request.CreateContactRequest;
import com.dental.clinic.management.dto.request.UpdateContactRequest;
import com.dental.clinic.management.dto.response.ContactInfoResponse;
import com.dental.clinic.management.service.CustomerContactService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.dental.clinic.management.utils.security.AuthoritiesConstants;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customer-contacts")
@Tag(name = "Customer Contacts", description = "APIs for managing customer contacts")
public class CustomerContactController {

    private final CustomerContactService contactService;

    public CustomerContactController(CustomerContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    @Operation(summary = "List customer contacts")
    @ApiMessage("List customer contacts successfully")
    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "','" + AuthoritiesConstants.RECEPTIONIST + "')")
    public ResponseEntity<Page<ContactInfoResponse>> listContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        return ResponseEntity.ok(contactService.listContacts(page, size, sortBy, sortDirection));
    }

    @GetMapping("/{contactId}")
    @Operation(summary = "Get contact by id")
    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "','" + AuthoritiesConstants.RECEPTIONIST + "')")
    public ResponseEntity<ContactInfoResponse> getContact(@PathVariable String contactId) {
        return ResponseEntity.ok(contactService.getContact(contactId));
    }

    @PostMapping
    @Operation(summary = "Create new customer contact")
    @ApiMessage("Create customer contact successfully")
    // create allowed from website (public) and by authenticated users (service
    // enforces rules)
    public ResponseEntity<ContactInfoResponse> createContact(
            @Valid @RequestBody CreateContactRequest request)
            throws URISyntaxException {
        ContactInfoResponse resp = contactService.createContact(request);
        return ResponseEntity.created(new URI("/api/v1/customer-contacts/" + resp.getContactId())).body(resp);
    }

    // PUT for full/controlled update per spec (not PATCH)
    @PutMapping("/{contactId}")
    @Operation(summary = "Update contact (full/controlled)")
    @ApiMessage("Update customer contact successfully")
    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "','" + AuthoritiesConstants.RECEPTIONIST + "')")
    public ResponseEntity<ContactInfoResponse> updateContact(
            @PathVariable String contactId,
            @Valid @RequestBody UpdateContactRequest request) {
        // note: service must enforce forbidden fields (phone, fullName, source) and
        // status-flow
        ContactInfoResponse resp = contactService.updateContact(contactId, request);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{contactId}")
    @Operation(summary = "Delete contact (soft)")
    @ApiMessage("Delete customer contact successfully")
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<Void> deleteContact(@PathVariable String contactId) {
        contactService.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{contactId}/assign")
    @Operation(summary = "Assign contact to receptionist", 
               description = "Manual mode: provide employeeId (must be Receptionist role). Auto mode: leave employeeId empty to auto-assign to receptionist with least NEW contacts")
    @ApiMessage("Assign contact successfully")
    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<ContactInfoResponse> assignContact(
            @PathVariable String contactId,
            @RequestParam(required = false) String employeeId) {
        ContactInfoResponse resp = contactService.assignContact(contactId, employeeId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{contactId}/convert")
    @Operation(summary = "Convert contact to patient", 
               description = "Creates new patient and sets contact status to CONVERTED. Returns 400 if already converted or not interested")
    @ApiMessage("Convert contact to patient successfully")
    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "','" + AuthoritiesConstants.RECEPTIONIST + "')")
    public ResponseEntity<ContactInfoResponse> convertContact(@PathVariable String contactId) {
        ContactInfoResponse resp = contactService.convertContact(contactId);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistics for customer contacts")
    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "','" + AuthoritiesConstants.RECEPTIONIST + "')")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(contactService.getStats());
    }

    @GetMapping("/conversion-rate")
    @Operation(summary = "Conversion rate (contacts -> converted patients)")
    @PreAuthorize("hasAnyRole('" + AuthoritiesConstants.ADMIN + "','" + AuthoritiesConstants.RECEPTIONIST + "')")
    public ResponseEntity<Map<String, Object>> conversionRate() {
        return ResponseEntity.ok(contactService.getConversionRate());
    }

    // Validation handlers (return 400 with details)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> Map.of("field", f.getField(), "message", f.getDefaultMessage()))
                .collect(Collectors.toList());
        var body = Map.<String, Object>of(
                "statusCode", HttpStatus.BAD_REQUEST.value(),
                "error", "validation.failed",
                "message", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex,
            HttpServletRequest request) {
        var errors = ex.getConstraintViolations()
                .stream()
                .map(v -> Map.of("path", v.getPropertyPath().toString(), "message", v.getMessage()))
                .collect(Collectors.toList());
        var body = Map.<String, Object>of(
                "statusCode", HttpStatus.BAD_REQUEST.value(),
                "error", "validation.failed",
                "message", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}