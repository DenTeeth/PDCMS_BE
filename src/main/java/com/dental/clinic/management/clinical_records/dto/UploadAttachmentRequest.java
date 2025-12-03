package com.dental.clinic.management.clinical_records.dto;

import com.dental.clinic.management.clinical_records.enums.AttachmentTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadAttachmentRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotNull(message = "Attachment type is required")
    private AttachmentTypeEnum attachmentType;

    private String description;
}
