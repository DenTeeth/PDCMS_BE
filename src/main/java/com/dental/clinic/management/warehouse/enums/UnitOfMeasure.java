package com.dental.clinic.management.warehouse.enums;

/**
 * Enum for unit of measure for inventory items.
 */
public enum UnitOfMeasure {
    PIECE("Cái"),
    BOX("Hộp"),
    BOTTLE("Lọ");

    private final String vietnameseName;

    UnitOfMeasure(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}
