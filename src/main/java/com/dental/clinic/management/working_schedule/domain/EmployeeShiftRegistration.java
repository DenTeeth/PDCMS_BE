package com.dental.clinic.management.working_schedule.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "working_schedule")
public class EmployeeShiftRegistration {

    @Id
    @Column(name = "registration_id", length = 20)
    private String registrationId;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "slot_id", length = 20, nullable = false)
    private String slotId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive;

    @OneToMany(mappedBy = "registration", fetch = FetchType.LAZY)
    private List<RegistrationDays> registrationDays = new ArrayList<>();

    // Constructors
    public EmployeeShiftRegistration() {
    }

    public EmployeeShiftRegistration(String registrationId, Integer employeeId, String slotId,
            LocalDate effectiveFrom, LocalDate effectiveTo, Boolean isActive) {
        this.registrationId = registrationId;
        this.employeeId = employeeId;
        this.slotId = slotId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<RegistrationDays> getRegistrationDays() {
        return registrationDays;
    }

    public void setRegistrationDays(List<RegistrationDays> registrationDays) {
        this.registrationDays = registrationDays;
    }

    @Override
    public String toString() {
        return "EmployeeShiftRegistration{" +
                "registrationId='" + registrationId + '\'' +
                ", employeeId=" + employeeId +
                ", slotId='" + slotId + '\'' +
                ", effectiveFrom=" + effectiveFrom +
                ", effectiveTo=" + effectiveTo +
                ", isActive=" + isActive +
                '}';
    }
}
