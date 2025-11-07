package com.dental.clinic.management.warehouse.dto.response;

import java.math.BigDecimal;

/**
 * Response DTO for Storage transaction statistics.
 */
public class StorageStatsResponse {

    private Integer totalImports;
    private Integer totalExports;
    private Integer totalAdjustments;
    private BigDecimal totalImportValue;
    private BigDecimal totalExportValue;
    private BigDecimal totalLossValue;

    public StorageStatsResponse() {
    }

    public StorageStatsResponse(Integer totalImports, Integer totalExports, Integer totalAdjustments,
            BigDecimal totalImportValue, BigDecimal totalExportValue, BigDecimal totalLossValue) {
        this.totalImports = totalImports;
        this.totalExports = totalExports;
        this.totalAdjustments = totalAdjustments;
        this.totalImportValue = totalImportValue;
        this.totalExportValue = totalExportValue;
        this.totalLossValue = totalLossValue;
    }

    public Integer getTotalImports() {
        return totalImports;
    }

    public void setTotalImports(Integer totalImports) {
        this.totalImports = totalImports;
    }

    public Integer getTotalExports() {
        return totalExports;
    }

    public void setTotalExports(Integer totalExports) {
        this.totalExports = totalExports;
    }

    public Integer getTotalAdjustments() {
        return totalAdjustments;
    }

    public void setTotalAdjustments(Integer totalAdjustments) {
        this.totalAdjustments = totalAdjustments;
    }

    public BigDecimal getTotalImportValue() {
        return totalImportValue;
    }

    public void setTotalImportValue(BigDecimal totalImportValue) {
        this.totalImportValue = totalImportValue;
    }

    public BigDecimal getTotalExportValue() {
        return totalExportValue;
    }

    public void setTotalExportValue(BigDecimal totalExportValue) {
        this.totalExportValue = totalExportValue;
    }

    public BigDecimal getTotalLossValue() {
        return totalLossValue;
    }

    public void setTotalLossValue(BigDecimal totalLossValue) {
        this.totalLossValue = totalLossValue;
    }
}
