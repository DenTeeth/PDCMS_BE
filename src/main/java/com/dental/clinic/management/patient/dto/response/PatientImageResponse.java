package com.dental.clinic.management.patient.dto.response;

import com.dental.clinic.management.patient.enums.ImageType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientImageResponse {

    private Long imageId;
    private Long patientId;
    private String patientName;
    private Long clinicalRecordId;
    private String imageUrl;
    private String cloudinaryPublicId;
    private ImageType imageType;
    private String description;
    private LocalDate capturedDate;
    private Long uploadedBy;
    private String uploaderName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
