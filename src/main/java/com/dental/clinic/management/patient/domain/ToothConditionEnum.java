package com.dental.clinic.management.patient.domain;

/**
 * Tooth condition ENUM for odontogram (dental chart visualization)
 *
 * Maps to PostgreSQL ENUM: tooth_condition_enum
 *
 * Coverage:
 * - HEALTHY: Normal tooth (default, usually not recorded)
 * - CARIES_MILD: Tooth decay level 1 (early/mild cavity)
 * - CARIES_MODERATE: Tooth decay level 2 (moderate cavity)
 * - CARIES_SEVERE: Tooth decay level 3 (severe/deep cavity)
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
    HEALTHY,          // Răng khỏe
    CARIES_MILD,      // Sâu răng nhẹ (mức 1)
    CARIES_MODERATE,  // Sâu răng trung bình (mức 2)
    CARIES_SEVERE,    // Sâu răng nặng (mức 3)
    FILLED,           // Răng trám
    CROWN,            // Bọc sứ
    MISSING,          // Mất răng
    IMPLANT,          // Cấy ghép
    ROOT_CANAL,       // Điều trị tủy
    FRACTURED,        // Gãy răng
    IMPACTED          // Mọc ngầm
}
