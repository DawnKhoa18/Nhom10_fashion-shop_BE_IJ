package com.example.demo.service;

import com.example.demo.model.Order;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
public class VnpayPaymentService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${vnpay.tmn-code:}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:}")
    private String hashSecret;

    @Value("${vnpay.payment-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String paymentUrl;

    @Value("${vnpay.return-url:http://localhost:3000/checkout/vnpay-return}")
    private String returnUrl;

    public String createPaymentUrl(Order order, HttpServletRequest request) {
        requireConfiguration();

        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).toBigIntegerExact().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", order.getOrderNumber());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderNumber());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", getClientIp(request));
        params.put("vnp_CreateDate", now.format(DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(DATE_FORMAT));

        String query = buildQuery(params);
        return paymentUrl + "?" + query + "&vnp_SecureHash=" + hmacSha512(query);
    }

    public boolean validateSignature(Map<String, String> receivedParams) {
        requireConfiguration();
        String receivedHash = receivedParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) return false;

        Map<String, String> signedParams = new TreeMap<>();
        receivedParams.forEach((key, value) -> {
            if (key.startsWith("vnp_")
                    && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key)
                    && value != null
                    && !value.isBlank()) {
                signedParams.put(key, value);
            }
        });

        String calculatedHash = hmacSha512(buildQuery(signedParams));
        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    public boolean isSuccessful(Map<String, String> params) {
        return "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));
    }

    public boolean amountMatches(Order order, Map<String, String> params) {
        String expected = order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).toBigIntegerExact().toString();
        return expected.equals(params.get("vnp_Amount"));
    }

    private String buildQuery(Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        params.forEach((key, value) -> {
            if (value == null || value.isBlank()) return;
            if (query.length() > 0) query.append('&');
            query.append(encode(key)).append('=').append(encode(value));
        });
        return query.toString();
    }

    private String hmacSha512(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(result.length * 2);
            for (byte value : result) hex.append(String.format("%02x", value));
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể ký yêu cầu VNPay", ex);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String address = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(address) ? "127.0.0.1" : address;
    }

    private void requireConfiguration() {
        if (tmnCode.isBlank() || hashSecret.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình VNPAY_TMN_CODE và VNPAY_HASH_SECRET");
        }
    }
}
