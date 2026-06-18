package com.example.demo.controller;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.model.Product;
import com.example.demo.model.ProductVariant;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.CartItem;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Long DEFAULT_CART_ID = 1L;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{orderNumber}")
    public Order getOrderByNumber(@PathVariable String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    @PostMapping("/place")
    @Transactional
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> orderData) {
        Map<String, Object> response = new HashMap<>();

        try {
            String tenKH = (String) orderData.get("tenKH");
            String phone = (String) orderData.get("phone");
            String diaChi = (String) orderData.get("diaChi");

            if (tenKH == null || tenKH.trim().isEmpty()
                    || phone == null || phone.trim().isEmpty()
                    || diaChi == null || diaChi.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập đầy đủ thông tin giao hàng");
                return ResponseEntity.badRequest().body(response);
            }

            List<CartItem> cartItems = cartItemRepository.findByCartId(DEFAULT_CART_ID);
            if (cartItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "Gio hang dang trong");
                return ResponseEntity.badRequest().body(response);
            }

            Order order = new Order();
            order.setOrderNumber("DH" + System.currentTimeMillis());
            order.setCustomerId(DEFAULT_CART_ID);
            order.setStatus("Đang xử lý");
            order.setTotalAmount(new BigDecimal(orderData.get("tongTien").toString()));
            order.setShippingAddress(diaChi);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);

            for (CartItem item : cartItems) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                ProductVariant variant = item.getVariantId() != null
                        ? productVariantRepository.findById(item.getVariantId()).orElse(null)
                        : null;

                if (product == null) {
                    continue;
                }

                BigDecimal price = variant != null && variant.getPrice() != null
                        ? variant.getPrice()
                        : product.getPrice();

                OrderDetail detail = new OrderDetail();
                detail.setOrderId(savedOrder.getId());
                detail.setProductId(product.getId());
                detail.setVariantId(variant != null ? variant.getId() : null);
                detail.setQuantity(item.getQuantity());
                detail.setPrice(price);
                orderDetailRepository.save(detail);
            }

            cartItemRepository.deleteAll(cartItems);

            response.put("success", true);
            response.put("message", "Đặt hàng thành công");
            response.put("orderNumber", savedOrder.getOrderNumber());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi đặt hàng: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    private String hmacSHA256(String data, String secretKey) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        byte[] hash = hmacSHA256.doFinal(data.getBytes("UTF-8"));

        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    @PostMapping("/momo")
    public ResponseEntity<?> createMomoPayment(@RequestBody Map<String, Object> orderData) {
        Map<String, Object> response = new HashMap<>();

        try {
            String tenKH = (String) orderData.get("tenKH");
            String phone = (String) orderData.get("phone");
            String diaChi = (String) orderData.get("diaChi");

            if (tenKH == null || tenKH.trim().isEmpty()
                    || phone == null || phone.trim().isEmpty()
                    || diaChi == null || diaChi.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập đầy đủ thông tin giao hàng");
                return ResponseEntity.badRequest().body(response);
            }

            String partnerCode = "MOMO";
            String accessKey = "F8B...";     // thay bằng accessKey sandbox của bạn
            String secretKey = "0F9...";     // thay bằng secretKey sandbox của bạn
            String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";

            String orderId = "DH" + System.currentTimeMillis();
            String requestId = orderId;
            long amount = new BigDecimal(orderData.get("tongTien").toString()).longValue();

            String orderInfo = "Thanh toan don hang " + orderId;
            String redirectUrl = "http://localhost:3000/checkout/success";
            String ipnUrl = "http://localhost:8080/api/orders/momo-ipn";
            String requestType = "captureWallet";
            String extraData = "";

            String rawSignature =
                    "accessKey=" + accessKey +
                            "&amount=" + amount +
                            "&extraData=" + extraData +
                            "&ipnUrl=" + ipnUrl +
                            "&orderId=" + orderId +
                            "&orderInfo=" + orderInfo +
                            "&partnerCode=" + partnerCode +
                            "&redirectUrl=" + redirectUrl +
                            "&requestId=" + requestId +
                            "&requestType=" + requestType;

            String signature = hmacSHA256(rawSignature, secretKey);

            String jsonBody = "{"
                    + "\"partnerCode\":\"" + partnerCode + "\","
                    + "\"accessKey\":\"" + accessKey + "\","
                    + "\"requestId\":\"" + requestId + "\","
                    + "\"amount\":" + amount + ","
                    + "\"orderId\":\"" + orderId + "\","
                    + "\"orderInfo\":\"" + orderInfo + "\","
                    + "\"redirectUrl\":\"" + redirectUrl + "\","
                    + "\"ipnUrl\":\"" + ipnUrl + "\","
                    + "\"extraData\":\"" + extraData + "\","
                    + "\"requestType\":\"" + requestType + "\","
                    + "\"signature\":\"" + signature + "\","
                    + "\"lang\":\"vi\""
                    + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> momoResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            response.put("success", true);
            response.put("message", "Tạo thanh toán MoMo thành công");
            response.put("momoResponse", momoResponse.body());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi tạo thanh toán MoMo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
