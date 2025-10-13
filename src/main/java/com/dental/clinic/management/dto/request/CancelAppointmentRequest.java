package com.dental.clinic.management.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CancelAppointmentRequest {

    @NotBlank
    private String cancellationReason;

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
