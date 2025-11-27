package com.dental.clinic.management.warehouse.controller;

import com.dental.clinic.management.utils.annotation.ApiMessage;
import com.dental.clinic.management.warehouse.dto.request.TransactionHistoryRequest;
import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;
import com.dental.clinic.management.warehouse.dto.response.TransactionHistoryResponse;
import com.dental.clinic.management.warehouse.enums.PaymentStatus;
import com.dental.clinic.management.warehouse.enums.TransactionStatus;
import com.dental.clinic.management.warehouse.enums.TransactionType;
import com.dental.clinic.management.warehouse.service.TransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * ✨ API 6.6: Transaction History Controller
 *
 * Features:
 * - Comprehensive filtering (type, status, payment, date, supplier,
 * appointment)
 * - RBAC-aware data masking (VIEW_COST permission)
 * - Pagination & sorting
 * - Aggregated statistics
 */
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Warehouse Transaction History", description = "API 6.6 - Transaction History Management")
public class TransactionHistoryController {

        private final TransactionHistoryService transactionHistoryService;

        /**
         * API 6.6: Get Transaction History
         *
         * @param page          Page number (default: 0)
         * @param size          Page size (default: 20)
         * @param search        Search by transaction code or invoice number
         * @param type          Transaction type filter
         * @param status        Approval status filter
         * @param paymentStatus Payment status filter (for IMPORT)
         * @param fromDate      Date range start
         * @param toDate        Date range end
         * @param supplierId    Filter by supplier (for IMPORT)
         * @param appointmentId Filter by appointment (for EXPORT)
         * @param createdBy     Filter by creator
         * @param sortBy        Sort field (default: transactionDate)
         * @param sortDir       Sort direction (default: desc)
         * @return Paginated transaction history with stats
         */
        @GetMapping("/transactions")
        @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('VIEW_WAREHOUSE')")
        @Operation(summary = "Lấy Lịch sử Giao dịch Kho", description = """
                        ✨ API 6.6 - Lấy lịch sử các phiếu Nhập/Xuất/Điều chỉnh kho

                        **Tính năng chính:**
                        - Bộ lọc mạnh mẽ (loại, trạng thái, thanh toán, ngày, NCC, ca bệnh)
                        - Tìm kiếm theo mã phiếu hoặc số hóa đơn
                        - Thống kê tổng hợp (tổng tiền nhập/xuất, phiếu chờ duyệt)
                        - Phân quyền VIEW_COST để ẩn/hiện thông tin tài chính
                        - Liên kết phiếu xuất với ca điều trị
                        - Theo dõi công nợ nhà cung cấp
                        - Quy trình duyệt phiếu

                        **Use Cases:**
                        1. Kế toán đối soát cuối tháng: ?type=IMPORT&fromDate=2025-11-01&toDate=2025-11-30
                        2. Truy vết sự cố: ?search=PX-20251124-005
                        3. Kiểm tra công nợ: ?paymentStatus=PARTIAL
                        4. Duyệt phiếu: ?status=PENDING_APPROVAL

                        **Permissions:**
                        - VIEW_WAREHOUSE: Xem danh sách (bắt buộc)
                        - VIEW_COST: Xem thông tin tài chính (totalValue, paidAmount, remainingDebt)
                        """)
        @ApiMessage("Lấy lịch sử giao dịch thành công")
        public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
                        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") Integer page,

                        @Parameter(description = "Số bản ghi mỗi trang (1-100)") @RequestParam(defaultValue = "20") Integer size,

                        @Parameter(description = "Tìm kiếm theo mã phiếu (PN-xxx, PX-xxx) hoặc số hóa đơn") @RequestParam(required = false) String search,

                        @Parameter(description = "Loại phiếu: IMPORT, EXPORT, ADJUSTMENT") @RequestParam(required = false) TransactionType type,

                        @Parameter(description = "Trạng thái duyệt: DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, CANCELLED") @RequestParam(required = false) TransactionStatus status,

                        @Parameter(description = "Trạng thái thanh toán (chỉ IMPORT): UNPAID, PARTIAL, PAID") @RequestParam(required = false) PaymentStatus paymentStatus,

                        @Parameter(description = "Lấy giao dịch từ ngày (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

                        @Parameter(description = "Lấy giao dịch đến ngày (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

                        @Parameter(description = "Lọc theo nhà cung cấp (chỉ IMPORT)") @RequestParam(required = false) Long supplierId,

                        @Parameter(description = "Lọc theo ca điều trị (chỉ EXPORT)") @RequestParam(required = false) Long appointmentId,

                        @Parameter(description = "Lọc theo người tạo (employee_id)") @RequestParam(required = false) Long createdBy,

                        @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "transactionDate") String sortBy,

                        @Parameter(description = "Hướng sắp xếp: asc, desc") @RequestParam(defaultValue = "desc") String sortDir) {

                log.info("📋 GET /api/v1/warehouse/transactions - Page: {}, Size: {}, Type: {}, Status: {}",
                                page, size, type, status);

                TransactionHistoryRequest request = TransactionHistoryRequest.builder()
                                .page(page)
                                .size(size)
                                .search(search)
                                .type(type)
                                .status(status)
                                .paymentStatus(paymentStatus)
                                .fromDate(fromDate)
                                .toDate(toDate)
                                .supplierId(supplierId)
                                .appointmentId(appointmentId)
                                .createdBy(createdBy)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();

                TransactionHistoryResponse response = transactionHistoryService.getTransactionHistory(request);

                log.info("✅ Transaction history retrieved - Total: {}, Page: {}/{}",
                                response.getMeta().getTotalElements(),
                                response.getMeta().getPage() + 1,
                                response.getMeta().getTotalPages());

                return ResponseEntity.ok(response);
        }
}
