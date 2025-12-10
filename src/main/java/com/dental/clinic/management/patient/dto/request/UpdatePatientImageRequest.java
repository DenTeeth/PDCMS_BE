package com.dental.clinic.management.patient.dto.request;

import com.dental.clinic.management.patient.enums.ImageType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePatientImageRequest {

    private ImageType imageType;

    private String description;

    private LocalDate capturedDate;

    private Long clinicalRecordId;
}
