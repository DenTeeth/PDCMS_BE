package com.dental.clinic.management.patient.domain;

/**
 * Tooth condition ENUM for odontogram (dental chart visualization)
 *
 * Maps to PostgreSQL ENUM: tooth_condition_enum
 *
 * Coverage:
 * - HEALTHY: Normal tooth (default, usually not recorded)
 * - CARIES: Tooth decay / cavity
 * - FILLED: Filled tooth (after treatment)
 * - CROWN: Crowned tooth (porcelain/metal crown)
 * - MISSING: Missing / extracted tooth
 * - IMPLANT: Dental implant
 * - ROOT_CANAL: Root canal treatment completed
 * - FRACTURED: Fractured / broken tooth
 * - IMPACTED: Impacted tooth (e.g., wisdom tooth)
 *
 * @author Dental Clinic System
 * @since API 8.9
 */
public enum ToothConditionEnum {
    HEALTHY,      // Rang khoe
    CARIES,       // Sau rang
    FILLED,       // Da tram
    CROWN,        // Boc su
    MISSING,      // Mat rang
    IMPLANT,      // Cay ghep
    ROOT_CANAL,   // Dieu tri tuy
    FRACTURED,    // Gay rang
    IMPACTED      // Moc ngam
}
