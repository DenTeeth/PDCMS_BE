package com.dental.clinic.management.warehouse.enums;

/**
 * Enum for unit of measure for inventory items.
 */
public enum UnitOfMeasure {
    PIECE("CÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡i"),
    BOX("HÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢p"),
    BOTTLE("LÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â");

    private final String vietnameseName;

    UnitOfMeasure(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}
