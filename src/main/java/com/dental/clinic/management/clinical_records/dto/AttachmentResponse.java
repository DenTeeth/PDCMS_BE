package com.dental.clinic.management.clinical_records.dto;

import com.dental.clinic.management.clinical_records.enums.AttachmentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {
    private Integer attachmentId;
    private Integer clinicalRecordId;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private AttachmentTypeEnum attachmentType;
    private String description;
    private Integer uploadedBy;
    private String uploadedByName;
    private String uploadedAt;
}
