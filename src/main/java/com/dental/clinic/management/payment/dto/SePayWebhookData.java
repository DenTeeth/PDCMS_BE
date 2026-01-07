package com.dental.clinic.management.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * SePay Webhook Data
 * Reference: https://docs.sepay.vn/tich-hop-webhooks.html
 *
 * Example payload:
 * {
 * "id": 92704,
 * "gateway": "Vietcombank",
 * "transactionDate": "2023-03-25 14:02:37",
 * "accountNumber": "0123499999",
 * "code": null,
 * "content": "chuyen tien PDCMS123456",
 * "transferType": "in",
 * "transferAmount": 2277000,
 * "accumulated": 19077000,
 * "subAccount": null,
 * "referenceCode": "MBVCB.3278907687",
 * "description": "Full SMS content"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SePayWebhookData {

    /**
     * ID giao dịch trên SePay (unique for duplicate detection)
     */
    private Long id;

    /**
     * Brand name của ngân hàng (e.g., "Vietcombank")
     */
    private String gateway;

    /**
     * Thời gian xảy ra giao dịch phía ngân hàng
     */
    @JsonProperty("transactionDate")
    private String transactionDate;

    /**
     * Số tài khoản ngân hàng
     */
    @JsonProperty("accountNumber")
    private String accountNumber;

    /**
     * Mã code thanh toán (sepay tự nhận diện dựa vào cấu hình)
     * This is where PDCMS payment code will be extracted
     */
    private String code;

    /**
     * Nội dung chuyển khoản (contains payment code like "PDCMS123456")
     */
    private String content;

    /**
     * Loại giao dịch: "in" (tiền vào), "out" (tiền ra)
     */
    @JsonProperty("transferType")
    private String transferType;

    /**
     * Số tiền giao dịch
     */
    @JsonProperty("transferAmount")
    private BigDecimal transferAmount;

    /**
     * Số dư tài khoản (lũy kế)
     */
    private BigDecimal accumulated;

    /**
     * Tài khoản ngân hàng phụ (tài khoản định danh)
     */
    @JsonProperty("subAccount")
    private String subAccount;

    /**
     * Mã tham chiếu của tin nhắn SMS
     */
    @JsonProperty("referenceCode")
    private String referenceCode;

    /**
     * Toàn bộ nội dung tin nhắn SMS
     */
    private String description;
}
