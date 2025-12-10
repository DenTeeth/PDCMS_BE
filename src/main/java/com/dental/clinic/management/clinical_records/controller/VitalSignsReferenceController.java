package com.dental.clinic.management.clinical_records.controller;

import com.dental.clinic.management.clinical_records.dto.VitalSignsReferenceResponse;
import com.dental.clinic.management.clinical_records.service.VitalSignsReferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vital-signs-reference")
@RequiredArgsConstructor
@Tag(name = "Vital Signs Reference", description = "Reference ranges for vital signs assessment by age")
public class VitalSignsReferenceController {

    private final VitalSignsReferenceService vitalSignsReferenceService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('VIEW_VITAL_SIGNS_REFERENCE') or hasAuthority('WRITE_CLINICAL_RECORD')")
    @Operation(summary = "Get all active reference ranges", description = "Returns all active vital signs reference ranges for clinical assessment")
    public ResponseEntity<List<VitalSignsReferenceResponse>> getAllReferences() {
        return ResponseEntity.ok(vitalSignsReferenceService.getAllActiveReferences());
    }

    @GetMapping("/by-age/{age}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('VIEW_VITAL_SIGNS_REFERENCE') or hasAuthority('WRITE_CLINICAL_RECORD')")
    @Operation(summary = "Get reference ranges by age", description = "Returns applicable vital signs reference ranges for a specific patient age")
    public ResponseEntity<List<VitalSignsReferenceResponse>> getReferencesByAge(
            @PathVariable Integer age) {
        return ResponseEntity.ok(vitalSignsReferenceService.getReferencesByAge(age));
    }
}
