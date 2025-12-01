package com.dental.clinic.management.clinical_records.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItemDTO {
    private Integer prescriptionItemId;
    private String itemCode;
    private String itemName;
    private Integer quantity;
    private String dosageInstructions;
    private String createdAt;
}
