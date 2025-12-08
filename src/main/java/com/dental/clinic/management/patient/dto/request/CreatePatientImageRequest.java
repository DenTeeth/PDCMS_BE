package com.dental.clinic.management.patient.dto.request;

import com.dental.clinic.management.patient.enums.ImageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePatientImageRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long clinicalRecordId;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotBlank(message = "Cloudinary Public ID is required")
    private String cloudinaryPublicId;

    @NotNull(message = "Image type is required")
    private ImageType imageType;

    private String description;

    private LocalDate capturedDate;
}
