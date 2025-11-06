package com.dental.clinic.management.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Storage transaction statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageStatsResponse {

    private Integer totalImports;
    private Integer totalExports;
    private Integer totalAdjustments;
    private BigDecimal totalImportValue;
    private BigDecimal totalExportValue;
    private BigDecimal totalLossValue;
}

