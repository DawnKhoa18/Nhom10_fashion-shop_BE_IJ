package com.example.demo.service;

import com.example.demo.model.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MomoPaymentService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${momo.partner-code:}")
    private String partnerCode;

    @Value("${momo.access-key:}")
    private String accessKey;

    @Value("${momo.secret-key:}")
    private String secretKey;

    @Value("${momo.create-endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String createEndpoint;

    @Value("${momo.query-endpoint:https://test-payment.momo.vn/v2/gateway/api/query}")
    private String queryEndpoint;

    @Value("${momo.redirect-url:http://localhost:3000/checkout/momo-return}")
    private String redirectUrl;

    @Value("${momo.ipn-url:http://localhost:8080/api/orders/momo/ipn}")
    private String ipnUrl;

    public MomoPaymentService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> createPayment(Order order) {
        requireConfiguration();

        String requestId = order.getOrderNumber() + "-" + System.currentTimeMillis();
        String amount = order.getTotalAmount().toBigIntegerExact().toString();
        String orderId = order.getOrderNumber();
        String orderInfo = "Thanh toan don hang " + orderId;
        String requestType = "captureWallet";
        String extraData = "";

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", partnerCode);
        payload.put("partnerName", "Fashion 4Men");
        payload.put("storeId", "Fashion4Men");
        payload.put("requestId", requestId);
        payload.put("amount", amount);
        payload.put("orderId", orderId);
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", redirectUrl);
        payload.put("ipnUrl", ipnUrl);
        payload.put("lang", "vi");
        payload.put("requestType", requestType);
        payload.put("autoCapture", true);
        payload.put("extraData", extraData);
        payload.put("signature", hmacSha256(rawSignature));

        Map<String, Object> response = postJson(createEndpoint, payload);
        if (number(response.get("resultCode")) != 0 || response.get("payUrl") == null) {
            throw new IllegalStateException(String.valueOf(response.getOrDefault("message", "MoMo không tạo được giao dịch")));
        }
        return response;
    }

    public Map<String, Object> queryPayment(String orderId) {
        requireConfiguration();

        String requestId = orderId + "-query-" + System.currentTimeMillis();
        String rawSignature = "accessKey=" + accessKey
                + "&orderId=" + orderId
                + "&partnerCode=" + partnerCode
                + "&requestId=" + requestId;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", partnerCode);
        payload.put("requestId", requestId);
        payload.put("orderId", orderId);
        payload.put("lang", "vi");
        payload.put("signature", hmacSha256(rawSignature));
        return postJson(queryEndpoint, payload);
    }

    public boolean isSuccessful(Map<String, Object> response) {
        return number(response.get("resultCode")) == 0;
    }

    private Map<String, Object> postJson(String endpoint, Map<String, Object> payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("MoMo HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kết nối MoMo bị gián đoạn", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Không kết nối được MoMo: " + ex.getMessage(), ex);
        }
    }

    private String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) result.append(String.format("%02x", value));
            return result.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể ký yêu cầu MoMo", ex);
        }
    }

    private int number(Object value) {
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return -1;
        }
    }

    private void requireConfiguration() {
        if (partnerCode.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình MOMO_PARTNER_CODE, MOMO_ACCESS_KEY và MOMO_SECRET_KEY");
        }
    }
}
