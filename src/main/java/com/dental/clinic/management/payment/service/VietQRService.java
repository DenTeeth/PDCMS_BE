package com.dental.clinic.management.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service để generate VietQR code URL
 * Sử dụng https://img.vietqr.io để tạo QR code thanh toán ngân hàng
 *
 * Format:
 * https://img.vietqr.io/image/<BANK_ID>-<ACCOUNT_NO>-<TEMPLATE>.png?amount=<AMOUNT>&addInfo=<DESCRIPTION>&accountName=<ACCOUNT_NAME>
 * Example:
 * https://img.vietqr.io/image/acb-24131687-compact2.jpg?amount=50000&addInfo=test&accountName=TRINH%20CONG%20THAI
 */
@Service
@Slf4j
public class VietQRService {

    @Value("${vietqr.bank-id:ACB}")
    private String bankId;

    @Value("${vietqr.account-no:24131687}")
    private String accountNo;

    @Value("${vietqr.account-name:TRINH CONG THAI}")
    private String accountName;

    @Value("${vietqr.template:compact2}")
    private String template;

    /**
     * Generate VietQR URL
     *
     * @param amount      Số tiền thanh toán
     * @param paymentCode Mã thanh toán (PDCMS123456)
     * @return URL của QR code image
     */
    public String generateQRUrl(Long amount, String paymentCode) {
        try {
            String encodedAccountName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString());
            String encodedPaymentCode = URLEncoder.encode(paymentCode, StandardCharsets.UTF_8.toString());

            String qrUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                    bankId.toLowerCase(),
                    accountNo,
                    template,
                    amount,
                    encodedPaymentCode,
                    encodedAccountName);

            log.info("Generated VietQR URL for payment code {}: {}", paymentCode, qrUrl);
            return qrUrl;

        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding VietQR URL parameters", e);
            throw new RuntimeException("Failed to generate QR code URL", e);
        }
    }

    /**
     * Generate VietQR URL with custom description
     *
     * @param amount      Số tiền thanh toán
     * @param description Nội dung chuyển khoản tùy chỉnh
     * @return URL của QR code image
     */
    public String generateQRUrlWithDescription(Long amount, String description) {
        try {
            String encodedAccountName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString());
            String encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString());

            String qrUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                    bankId.toLowerCase(),
                    accountNo,
                    template,
                    amount,
                    encodedDescription,
                    encodedAccountName);

            log.info("Generated VietQR URL with description '{}': {}", description, qrUrl);
            return qrUrl;

        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding VietQR URL parameters", e);
            throw new RuntimeException("Đã xảy ra lỗi khi tạo URL mã QR", e);
        }
    }
}
