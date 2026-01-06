package com.dental.clinic.management.warehouse.repository;

import com.dental.clinic.management.warehouse.domain.StorageTransaction;
// import com.dental.clinic.management.warehouse.dto.response.SuppliedItemResponse;
import com.dental.clinic.management.warehouse.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StorageTransactionRepository extends JpaRepository<StorageTransaction, Long>,
    JpaSpecificationExecutor<StorageTransaction> {

  Optional<StorageTransaction> findByTransactionCode(String transactionCode);

  List<StorageTransaction> findByTransactionType(TransactionType transactionType);

  // GET ALL with sorting
  List<StorageTransaction> findAllByOrderByTransactionDateDesc();

  // Filter by transaction type
  List<StorageTransaction> findByTransactionTypeOrderByTransactionDateDesc(TransactionType transactionType);

  @Query("SELECT st FROM StorageTransaction st " +
      "WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
      "ORDER BY st.transactionDate DESC")
  List<StorageTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  // Filter by month and year
  @Query("SELECT st FROM StorageTransaction st " +
      "WHERE FUNCTION('MONTH', st.transactionDate) = :month " +
      "AND FUNCTION('YEAR', st.transactionDate) = :year " +
      "ORDER BY st.transactionDate DESC")
  List<StorageTransaction> findByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

  // Filter by type + month + year
  @Query("SELECT st FROM StorageTransaction st " +
      "WHERE st.transactionType = :type " +
      "AND FUNCTION('MONTH', st.transactionDate) = :month " +
      "AND FUNCTION('YEAR', st.transactionDate) = :year " +
      "ORDER BY st.transactionDate DESC")
  List<StorageTransaction> findByTransactionTypeAndMonthAndYear(
      @Param("type") TransactionType transactionType,
      @Param("month") Integer month,
      @Param("year") Integer year);

  /**
   * Generate mã phiếu tự động: PN-YYYYMMDD-SEQ
   */
  @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(transaction_code FROM 13) AS INTEGER)), 0) + 1 " +
      "FROM storage_transactions " +
      "WHERE transaction_code LIKE :prefix || '%' " +
      "AND DATE(transaction_date) = CURRENT_DATE", nativeQuery = true)
  Integer getNextSequenceNumber(@Param("prefix") String prefix);

  // ==================== Dashboard Statistics Queries ====================

  /**
   * Calculate total export value for expenses (EXPORT transactions that are APPROVED)
   */
  @Query("SELECT COALESCE(SUM(st.totalValue), 0) FROM StorageTransaction st " +
         "WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
         "AND st.transactionType = 'EXPORT' " +
         "AND st.status = 'APPROVED'")
  java.math.BigDecimal calculateTotalExportValue(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate);

  /**
   * Calculate export value by export type
   */
  @Query("SELECT COALESCE(SUM(st.totalValue), 0) FROM StorageTransaction st " +
         "WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
         "AND st.transactionType = 'EXPORT' " +
         "AND st.status = 'APPROVED' " +
         "AND st.exportType = :exportType")
  java.math.BigDecimal calculateExportValueByType(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate,
          @Param("exportType") String exportType);

  /**
   * Get export value by day
   */
  @Query("SELECT DATE(st.transactionDate) as date, COALESCE(SUM(st.totalValue), 0) as amount " +
         "FROM StorageTransaction st " +
         "WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
         "AND st.transactionType = 'EXPORT' " +
         "AND st.status = 'APPROVED' " +
         "GROUP BY DATE(st.transactionDate) " +
         "ORDER BY DATE(st.transactionDate)")
  List<Object[]> getExportValueByDay(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate);

  /**
   * Count transactions by type in date range
   */
  @Query("SELECT COUNT(st) FROM StorageTransaction st " +
         "WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
         "AND st.transactionType = :type")
  Long countByTypeInRange(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate,
          @Param("type") com.dental.clinic.management.warehouse.enums.TransactionType type);

  /**
   * Calculate total value by transaction type
   */
  @Query("SELECT COALESCE(SUM(st.totalValue), 0) FROM StorageTransaction st " +
         "WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
         "AND st.transactionType = :type " +
         "AND st.status = 'APPROVED'")
  java.math.BigDecimal calculateTotalValueByType(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate,
          @Param("type") com.dental.clinic.management.warehouse.enums.TransactionType type);

  /**
   * Count transactions by status
   */
  @Query("SELECT COUNT(st) FROM StorageTransaction st " +
         "WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
         "AND st.status = :status")
  Long countByStatusInRange(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate,
          @Param("status") String status);

  /**
   * Get transaction counts by day (for charts)
   */
  @Query("SELECT DATE(st.transactionDate) as date, COUNT(st) as count, " +
         "COALESCE(SUM(CASE WHEN st.transactionType = 'IMPORT' THEN st.totalValue ELSE 0 END), 0) as importValue, " +
         "COALESCE(SUM(CASE WHEN st.transactionType = 'EXPORT' THEN st.totalValue ELSE 0 END), 0) as exportValue " +
         "FROM StorageTransaction st " +
         "WHERE st.transactionDate BETWEEN :startDate AND :endDate " +
         "AND st.status = 'APPROVED' " +
         "GROUP BY DATE(st.transactionDate) " +
         "ORDER BY DATE(st.transactionDate)")
  List<Object[]> getTransactionsByDay(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate);

  /**
   * Get top imported items by quantity and value
   */
  @Query(value = "SELECT im.item_master_id, im.item_code, im.item_name, " +
         "SUM(sti.quantity_change) as total_quantity, " +
         "COALESCE(SUM(sti.total_line_value), 0) as total_value " +
         "FROM storage_transactions st " +
         "JOIN storage_transaction_items sti ON st.transaction_id = sti.transaction_id " +
         "JOIN item_batches ib ON sti.batch_id = ib.batch_id " +
         "JOIN item_masters im ON ib.item_master_id = im.item_master_id " +
         "WHERE st.transaction_date BETWEEN :startDate AND :endDate " +
         "AND st.transaction_type = 'IMPORT' " +
         "AND st.status = 'APPROVED' " +
         "GROUP BY im.item_master_id, im.item_code, im.item_name " +
         "ORDER BY total_value DESC " +
         "LIMIT :limit", nativeQuery = true)
  List<Object[]> getTopImportedItems(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate,
          @Param("limit") int limit);

  /**
   * Get top exported items by quantity and value
   */
  @Query(value = "SELECT im.item_master_id, im.item_code, im.item_name, " +
         "SUM(ABS(sti.quantity_change)) as total_quantity, " +
         "COALESCE(SUM(ABS(sti.total_line_value)), 0) as total_value " +
         "FROM storage_transactions st " +
         "JOIN storage_transaction_items sti ON st.transaction_id = sti.transaction_id " +
         "JOIN item_batches ib ON sti.batch_id = ib.batch_id " +
         "JOIN item_masters im ON ib.item_master_id = im.item_master_id " +
         "WHERE st.transaction_date BETWEEN :startDate AND :endDate " +
         "AND st.transaction_type = 'EXPORT' " +
         "AND st.status = 'APPROVED' " +
         "GROUP BY im.item_master_id, im.item_code, im.item_name " +
         "ORDER BY total_value DESC " +
         "LIMIT :limit", nativeQuery = true)
  List<Object[]> getTopExportedItems(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate,
          @Param("limit") int limit);

  /**
   * WORLD-CLASS QUERY: Lấy lịch sử vật tư cung cấp từ NCC
   * - Chỉ lấy giao dịch IMPORT
   * - DISTINCT ON: Lấy 1 dòng mới nhất cho mỗi vật tư
   * - ORDER BY: Đảm bảo lấy giao dịch có ngày gần nhất
   */
  @Query(value = """
      SELECT DISTINCT ON (im.item_master_id)
          im.item_code AS itemCode,
          im.item_name AS itemName,
          sti.price AS lastImportPrice,
          st.transaction_date AS lastImportDate
      FROM storage_transactions st
      JOIN storage_transaction_items sti ON st.transaction_id = sti.transaction_id
      JOIN item_batches ib ON sti.batch_id = ib.batch_id
      JOIN item_masters im ON ib.item_master_id = im.item_master_id
      WHERE st.supplier_id = :supplierId
        AND st.transaction_type = 'IMPORT'
      ORDER BY im.item_master_id, st.transaction_date DESC
      """, nativeQuery = true)
  List<Object[]> findSuppliedItemsBySupplier(@Param("supplierId") Long supplierId);

  /**
   * Check if supplier has any transactions (for safe delete validation)
   */
  @Query("SELECT COUNT(st) > 0 FROM StorageTransaction st WHERE st.supplier.id = :supplierId")
  boolean existsBySupplier(@Param("supplierId") Long supplierId);

  /**
   * API 6.4: Check duplicate invoice number
   */
  boolean existsByInvoiceNumber(String invoiceNumber);

  /**
   * API 6.4: Count transactions by code prefix for sequence generation
   */
  Long countByTransactionCodeStartingWith(String prefix);

  /**
   * Get transaction by ID with all details eagerly loaded
   * Prevents lazy loading issues when accessing batch, itemMaster, and unit
   */
  @Query("SELECT DISTINCT st FROM StorageTransaction st " +
         "LEFT JOIN FETCH st.items i " +
         "LEFT JOIN FETCH i.batch b " +
         "LEFT JOIN FETCH b.itemMaster im " +
         "LEFT JOIN FETCH i.unit u " +
         "LEFT JOIN FETCH st.supplier s " +
         "LEFT JOIN FETCH st.createdBy e " +
         "WHERE st.transactionId = :id")
  Optional<StorageTransaction> findByIdWithDetails(@Param("id") Long id);

  /**
   * Get recent reference codes for auto-complete suggestions
   * Returns transactions with non-null reference codes, ordered by date DESC
   */
  @Query(value = "SELECT * FROM storage_transactions " +
         "WHERE reference_code IS NOT NULL " +
         "ORDER BY transaction_date DESC " +
         "LIMIT :limit", nativeQuery = true)
  List<StorageTransaction> findRecentReferenceCodesWithLimit(@Param("limit") int limit);

  /**
   * Calculate total export value for expired items (DISPOSAL type with expired batches)
   */
  @Query(value = "SELECT COALESCE(SUM(ABS(sti.quantity_change * sti.price)), 0) " +
         "FROM storage_transactions st " +
         "JOIN storage_transaction_items sti ON st.transaction_id = sti.transaction_id " +
         "JOIN item_batches ib ON sti.batch_id = ib.batch_id " +
         "WHERE st.transaction_type = 'EXPORT' " +
         "AND st.export_type = 'DISPOSAL' " +
         "AND st.status = 'APPROVED' " +
         "AND st.transaction_date BETWEEN :startDate AND :endDate " +
         "AND ib.expiry_date IS NOT NULL " +
         "AND ib.expiry_date <= CAST(st.transaction_date AS DATE)", nativeQuery = true)
  java.math.BigDecimal calculateExpiredItemsValue(
          @Param("startDate") LocalDateTime startDate,
          @Param("endDate") LocalDateTime endDate);
}
