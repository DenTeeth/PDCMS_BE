package com.dental.clinic.management.working_schedule;

import com.dental.clinic.management.working_schedule.domain.PartTimeRegistration;
import com.dental.clinic.management.working_schedule.domain.PartTimeSlot;
import com.dental.clinic.management.working_schedule.enums.RegistrationStatus;
import com.dental.clinic.management.working_schedule.exception.QuotaExceededException;
import com.dental.clinic.management.working_schedule.repository.PartTimeRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.PartTimeSlotRepository;
import com.dental.clinic.management.working_schedule.service.PartTimeRegistrationApprovalService;
import com.dental.clinic.management.working_schedule.service.PartTimeSlotAvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.main.banner-mode=off")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PartTimeFlexIntegrationTest {

    @Autowired
    private PartTimeSlotRepository slotRepository;

    @Autowired
    private PartTimeRegistrationRepository registrationRepository;

    @Autowired
    private PartTimeSlotAvailabilityService availabilityService;

    @Autowired
    private PartTimeRegistrationApprovalService approvalService;

    /**
     * Scenario from spec:
     * - Slot: FRIDAY,SATURDAY, 2025-11-09 -> 2025-11-30, quota=2
     * - Doctor A: APPROVED 2025-11-09 -> 2025-11-16 (covers 14,15)
     * - Doctor B: APPROVED 2025-11-09 -> 2025-11-30 (covers all 6 days)
     * Assertions: 14/11 -> 2, 21/11 -> 1
     */
    @Test
    @Transactional
    public void testPerDayCountsWithApprovedRegistrations() {
        PartTimeSlot slot = new PartTimeSlot();
        slot.setWorkShiftId("WKS_MORNING_01");
        slot.setDayOfWeek("FRIDAY,SATURDAY");
        slot.setEffectiveFrom(LocalDate.of(2025,11,9));
        slot.setEffectiveTo(LocalDate.of(2025,11,30));
        slot.setQuota(2);
        slot.setIsActive(true);
        slotRepository.save(slot);

        // Doctor A: covers 14/11 and 15/11
        PartTimeRegistration a = PartTimeRegistration.builder()
                .employeeId(1)
                .partTimeSlotId(slot.getSlotId())
                .effectiveFrom(LocalDate.of(2025,11,9))
                .effectiveTo(LocalDate.of(2025,11,16))
                .status(RegistrationStatus.APPROVED)
                .isActive(true)
                .requestedDates(Set.of(LocalDate.of(2025,11,14), LocalDate.of(2025,11,15)))
                .build();
        registrationRepository.save(a);

        // Doctor B: covers all FRIDAY/SATURDAY in range
        PartTimeRegistration b = PartTimeRegistration.builder()
                .employeeId(2)
                .partTimeSlotId(slot.getSlotId())
                .effectiveFrom(LocalDate.of(2025,11,9))
                .effectiveTo(LocalDate.of(2025,11,30))
                .status(RegistrationStatus.APPROVED)
                .isActive(true)
                .requestedDates(Set.of(
                        LocalDate.of(2025,11,14), LocalDate.of(2025,11,15),
                        LocalDate.of(2025,11,21), LocalDate.of(2025,11,22),
                        LocalDate.of(2025,11,28), LocalDate.of(2025,11,29)
                ))
                .build();
        registrationRepository.save(b);

        long count14 = availabilityService.getRegisteredCountForDate(slot.getSlotId(), LocalDate.of(2025,11,14));
        long count21 = availabilityService.getRegisteredCountForDate(slot.getSlotId(), LocalDate.of(2025,11,21));

        assertEquals(2L, count14, "14-Nov should have 2 approved registrations");
        assertEquals(1L, count21, "21-Nov should have 1 approved registration");
    }

    /**
     * Test that approval is blocked when any day in requestedDates is full.
     */
    @Test
    @Transactional
    public void testApproveBlockedWhenFull() {
        PartTimeSlot slot = new PartTimeSlot();
        slot.setWorkShiftId("WKS_MORNING_01");
        slot.setDayOfWeek("FRIDAY,SATURDAY");
        slot.setEffectiveFrom(LocalDate.of(2025,11,9));
        slot.setEffectiveTo(LocalDate.of(2025,11,30));
        slot.setQuota(2);
        slot.setIsActive(true);
        slotRepository.save(slot);

        // Fill slot for 14/11 and 15/11 by two approved registrations
        PartTimeRegistration a = PartTimeRegistration.builder()
                .employeeId(1)
                .partTimeSlotId(slot.getSlotId())
                .effectiveFrom(LocalDate.of(2025,11,9))
                .effectiveTo(LocalDate.of(2025,11,16))
                .status(RegistrationStatus.APPROVED)
                .isActive(true)
                .requestedDates(Set.of(LocalDate.of(2025,11,14), LocalDate.of(2025,11,15)))
                .build();
        registrationRepository.save(a);

        PartTimeRegistration b = PartTimeRegistration.builder()
                .employeeId(2)
                .partTimeSlotId(slot.getSlotId())
                .effectiveFrom(LocalDate.of(2025,11,9))
                .effectiveTo(LocalDate.of(2025,11,30))
                .status(RegistrationStatus.APPROVED)
                .isActive(true)
                .requestedDates(Set.of(
                        LocalDate.of(2025,11,14), LocalDate.of(2025,11,15),
                        LocalDate.of(2025,11,21), LocalDate.of(2025,11,22),
                        LocalDate.of(2025,11,28), LocalDate.of(2025,11,29)
                ))
                .build();
        registrationRepository.save(b);

        // New pending registration (employee 3) requesting 14 and 15 — should be blocked
        PartTimeRegistration pending = PartTimeRegistration.builder()
                .employeeId(3)
                .partTimeSlotId(slot.getSlotId())
                .effectiveFrom(LocalDate.of(2025,11,9))
                .effectiveTo(LocalDate.of(2025,11,16))
                .status(RegistrationStatus.PENDING)
                .isActive(true)
                .requestedDates(Set.of(LocalDate.of(2025,11,14), LocalDate.of(2025,11,15)))
                .build();
        PartTimeRegistration saved = registrationRepository.save(pending);

        assertFalse(approvalService.canApprove(saved.getRegistrationId()), "Should not be approvable because some days are full");

        assertThrows(QuotaExceededException.class, () -> {
            approvalService.approveRegistration(saved.getRegistrationId(), 999);
        });
    }

    /**
     * Test that approval is allowed for a pending registration that requests only later dates which have space.
     */
    @Test
    @Transactional
    public void testApproveAllowedForLaterRange() {
        PartTimeSlot slot = new PartTimeSlot();
        slot.setWorkShiftId("WKS_MORNING_01");
        slot.setDayOfWeek("FRIDAY,SATURDAY");
        slot.setEffectiveFrom(LocalDate.of(2025,11,9));
        slot.setEffectiveTo(LocalDate.of(2025,11,30));
        slot.setQuota(2);
        slot.setIsActive(true);
        slotRepository.save(slot);

        // Only Doctor B is approved for whole period
        PartTimeRegistration b = PartTimeRegistration.builder()
                .employeeId(2)
                .partTimeSlotId(slot.getSlotId())
                .effectiveFrom(LocalDate.of(2025,11,9))
                .effectiveTo(LocalDate.of(2025,11,30))
                .status(RegistrationStatus.APPROVED)
                .isActive(true)
                .requestedDates(Set.of(
                        LocalDate.of(2025,11,14), LocalDate.of(2025,11,15),
                        LocalDate.of(2025,11,21), LocalDate.of(2025,11,22),
                        LocalDate.of(2025,11,28), LocalDate.of(2025,11,29)
                ))
                .build();
        registrationRepository.save(b);

        // Pending registration for 21/11 & 22/11 (these should have only 1 registered -> space)
        PartTimeRegistration pending = PartTimeRegistration.builder()
                .employeeId(4)
                .partTimeSlotId(slot.getSlotId())
                .effectiveFrom(LocalDate.of(2025,11,17))
                .effectiveTo(LocalDate.of(2025,11,30))
                .status(RegistrationStatus.PENDING)
                .isActive(true)
                .requestedDates(Set.of(LocalDate.of(2025,11,21), LocalDate.of(2025,11,22)))
                .build();
        PartTimeRegistration saved = registrationRepository.save(pending);

        assertTrue(approvalService.canApprove(saved.getRegistrationId()), "Should be approvable because all requested days have space");

        // Approve should succeed (no exception)
        approvalService.approveRegistration(saved.getRegistrationId(), 1000);

        PartTimeRegistration after = registrationRepository.findById(saved.getRegistrationId()).orElseThrow();
        assertEquals(RegistrationStatus.APPROVED, after.getStatus());
    }
}
