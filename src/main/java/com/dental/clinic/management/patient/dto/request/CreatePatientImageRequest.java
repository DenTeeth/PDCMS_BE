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

    @NotNull(message = "Mã bệnh nhân là bắt buộc")
    private Long patientId;

    private Long clinicalRecordId;

    @NotBlank(message = "Đường dẫn hình ảnh là bắt buộc")
    private String imageUrl;

    @NotBlank(message = "Mã Cloudinary là bắt buộc")
    private String cloudinaryPublicId;

    @NotNull(message = "Loại hình ảnh là bắt buộc")
    private ImageType imageType;

    private String description;

    private LocalDate capturedDate;
}
