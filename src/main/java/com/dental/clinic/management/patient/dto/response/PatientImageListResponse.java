package com.dental.clinic.management.patient.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientImageListResponse {

    private List<PatientImageResponse> images;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;

    public static PatientImageListResponse fromPage(Page<PatientImageResponse> page) {
        return PatientImageListResponse.builder()
                .images(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
    }
}
