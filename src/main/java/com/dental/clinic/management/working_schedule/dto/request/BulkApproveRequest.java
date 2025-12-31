package com.dental.clinic.management.working_schedule.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk approval of registrations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkApproveRequest {
    
    /**
     * List of registration IDs to approve.
     * Each will be validated individually before approval.
     */
    @NotEmpty(message = "Danh sách mã đăng ký không được để trống")
    private List<Integer> registrationIds;
}
