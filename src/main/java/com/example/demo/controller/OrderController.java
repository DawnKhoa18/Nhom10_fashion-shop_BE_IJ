package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.model.Product;
import com.example.demo.model.ProductVariant;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.Customer;

import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import com.example.demo.repository.CustomerRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{orderNumber}")
    public Order getOrderByNumber(@PathVariable String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerOrders(@PathVariable Long customerId) {
        List<Map<String, Object>> result = orderRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toOrderSummary)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/customer/{customerId}/{orderId}")
    public ResponseEntity<?> getCustomerOrderDetail(
            @PathVariable Long customerId,
            @PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null || !customerId.equals(order.getCustomerId())) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = toOrderSummary(order);
        List<Map<String, Object>> items = new ArrayList<>();

        for (OrderDetail detail : orderDetailRepository.findByOrderId(orderId)) {
            Product product = productRepository.findById(detail.getProductId()).orElse(null);
            ProductVariant variant = detail.getVariantId() == null
                    ? null
                    : productVariantRepository.findById(detail.getVariantId()).orElse(null);

            Map<String, Object> item = new HashMap<>();
            item.put("id", detail.getId());
            item.put("productId", detail.getProductId());
            item.put("productName", product == null ? "Sản phẩm không còn tồn tại" : product.getName());
            item.put("thumbnail", product == null ? null : product.getThumbnail());
            item.put("color", variant == null ? null : variant.getColor());
            item.put("size", variant == null ? null : variant.getSize());
            item.put("quantity", detail.getQuantity());
            item.put("price", detail.getPrice());
            item.put("subTotal", detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
            items.add(item);
        }

        result.put("items", items);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/customer/{customerId}/{orderId}/cancel")
    @Transactional
    public ResponseEntity<?> cancelCustomerOrder(
            @PathVariable Long customerId,
            @PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null || !customerId.equals(order.getCustomerId())) {
            return ResponseEntity.notFound().build();
        }

        if (!"Đang xử lý".equalsIgnoreCase(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể hủy đơn hàng đang xử lý"
            ));
        }

        order.setStatus("Đã hủy");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hủy đơn hàng thành công"
        ));
    }

    private Map<String, Object> toOrderSummary(Order order) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("status", order.getStatus());
        result.put("totalAmount", order.getTotalAmount());
        result.put("createdAt", order.getCreatedAt());
        result.put("updatedAt", order.getUpdatedAt());
        result.put("shippingAddress", order.getShippingAddress());
        return result;
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

            if (orderData.get("customerId") == null) {
                response.put("success", false);
                response.put("message", "Thiếu customerId");
                return ResponseEntity.badRequest().body(response);
            }

            Long customerId = Long.valueOf(orderData.get("customerId").toString());

            Cart cart = cartRepository.findByCustomerId(customerId).orElse(null);

            if (cart == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy giỏ hàng của khách hàng");
                return ResponseEntity.badRequest().body(response);
            }

            Long cartId = cart.getId();

            List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);

            if (cartItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "Giỏ hàng đang trống");
                return ResponseEntity.badRequest().body(response);
            }

            BigDecimal totalAmount = BigDecimal.ZERO;

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

                int quantity = item.getQuantity() == null ? 1 : item.getQuantity();

                totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantity)));
            }

            Order order = new Order();
            order.setOrderNumber("DH" + System.currentTimeMillis());
            order.setCustomerId(customerId);
            order.setStatus("Đang xử lý");
            order.setTotalAmount(totalAmount);
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

            boolean emailSent = sendOrderConfirmationEmail(savedOrder, customerId, tenKH, phone);

            response.put("success", true);
            response.put("message", "Đặt hàng thành công");
            response.put("orderNumber", savedOrder.getOrderNumber());
            response.put("emailSent", emailSent);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi đặt hàng: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private boolean sendOrderConfirmationEmail(Order order, Long customerId, String customerName, String phone) {
        try {
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
                return false;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            if (mailUsername != null && !mailUsername.isBlank()) {
                message.setFrom(mailUsername);
            }
            message.setTo(customer.getEmail());
            message.setSubject("Fashion 4Men - Xác nhận đơn hàng " + order.getOrderNumber());
            message.setText(buildOrderEmailBody(order, customerName, phone, customer.getEmail()));
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Không gửi được email xác nhận đơn hàng: " + e.getMessage());
            return false;
        }
    }

    private String buildOrderEmailBody(Order order, String customerName, String phone, String email) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        StringBuilder body = new StringBuilder();

        body.append("Xin chào ").append(customerName).append(",\n\n");
        body.append("Fashion 4Men đã nhận được đơn hàng của bạn.\n\n");
        body.append("Mã đơn hàng: ").append(order.getOrderNumber()).append("\n");
        body.append("Trạng thái: ").append(order.getStatus()).append("\n");
        body.append("Ngày đặt: ").append(order.getCreatedAt()).append("\n");
        body.append("Số điện thoại: ").append(phone).append("\n");
        body.append("Email: ").append(email).append("\n");
        body.append("Địa chỉ giao hàng: ").append(order.getShippingAddress()).append("\n");
        body.append("Tổng tiền: ").append(currencyFormat.format(order.getTotalAmount())).append("\n\n");
        body.append("Chi tiết sản phẩm:\n");

        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : details) {
            Product product = productRepository.findById(detail.getProductId()).orElse(null);
            ProductVariant variant = detail.getVariantId() == null
                    ? null
                    : productVariantRepository.findById(detail.getVariantId()).orElse(null);

            String productName = product == null ? "Sản phẩm không còn tồn tại" : product.getName();
            BigDecimal lineTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));

            body.append("- ").append(productName);
            if (variant != null) {
                body.append(" (").append(variant.getColor()).append(" / ").append(variant.getSize()).append(")");
            }
            body.append(" x ").append(detail.getQuantity());
            body.append(" - ").append(currencyFormat.format(lineTotal)).append("\n");
        }

        body.append("\nCảm ơn bạn đã mua sắm tại Fashion 4Men!");
        return body.toString();
    }
}
