package com.dental.clinic.management.working_schedule.dto.request;

import java.time.LocalDate;

public class UpdateEffectiveToRequest {

    private LocalDate effectiveTo; // Can be null for permanent

    // Constructors
    public UpdateEffectiveToRequest() {
    }

    public UpdateEffectiveToRequest(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    // Getters and Setters
    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
}
