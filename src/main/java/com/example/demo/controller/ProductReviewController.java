package com.example.demo.controller;

import com.example.demo.model.ProductReview;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public List<ProductReview> getAllReviews() {
        return reviewRepository.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<Map<String, Object>> getApprovedReviewsByProduct(@PathVariable Long productId) {
        return reviewRepository.findByProductIdAndIsApprovedTrue(productId)
                .stream()
                .sorted(Comparator.comparing(
                        ProductReview::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Map<String, Object> request) {
        try {
            Long customerId = toLong(request.get("customerId"));
            Long orderId = toLong(request.get("orderId"));
            Long productId = toLong(request.get("productId"));
            Integer rating = request.get("rating") == null
                    ? null
                    : Integer.valueOf(request.get("rating").toString());
            String title = String.valueOf(request.getOrDefault("title", "")).trim();
            String content = String.valueOf(request.getOrDefault("content", "")).trim();

            if (customerId == null || orderId == null || productId == null || rating == null) {
                return badRequest("Thiếu thông tin đánh giá.");
            }
            if (rating < 1 || rating > 5) {
                return badRequest("Số sao phải từ 1 đến 5.");
            }
            if (content.isBlank()) {
                return badRequest("Vui lòng nhập nội dung đánh giá.");
            }
            if (!productRepository.existsById(productId)) {
                return badRequest("Sản phẩm không tồn tại.");
            }

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null || !customerId.equals(order.getCustomerId())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Đơn hàng không thuộc tài khoản của bạn."
                ));
            }
            if (!isReviewableStatus(order.getStatus())) {
                return badRequest("Chỉ có thể đánh giá sản phẩm khi đơn hàng đã giao.");
            }

            boolean productBelongsToOrder = orderDetailRepository.findByOrderId(orderId)
                    .stream()
                    .map(OrderDetail::getProductId)
                    .anyMatch(productId::equals);
            if (!productBelongsToOrder) {
                return badRequest("Sản phẩm không có trong đơn hàng này.");
            }
            if (reviewRepository.existsByProductIdAndCustomerId(productId, customerId)) {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", "Bạn đã đánh giá sản phẩm này rồi."
                ));
            }

            ProductReview review = new ProductReview();
            review.setProductId(productId);
            review.setCustomerId(customerId);
            review.setRating(rating);
            review.setTitle(title);
            review.setContent(content);
            review.setIsApproved(true);
            ProductReview savedReview = reviewRepository.save(review);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Gửi đánh giá thành công.",
                    "review", toResponse(savedReview)
            ));
        } catch (NumberFormatException exception) {
            return badRequest("Thông tin đánh giá không hợp lệ.");
        }
    }

    private boolean isReviewableStatus(String status) {
        if (status == null) return false;
        String normalized = status.trim();
        return "Đã giao".equalsIgnoreCase(normalized)
                || "Đã xử lý".equalsIgnoreCase(normalized);
    }

    private Long toLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", message
        ));
    }

    private Map<String, Object> toResponse(ProductReview review) {
        Customer customer = review.getCustomerId() == null
                ? null
                : customerRepository.findById(review.getCustomerId()).orElse(null);
        Map<String, Object> response = new HashMap<>();
        response.put("id", review.getId());
        response.put("productId", review.getProductId());
        response.put("customerId", review.getCustomerId());
        response.put("customerName", customer == null ? "Khách hàng" : customer.getFullName());
        response.put("rating", review.getRating());
        response.put("title", review.getTitle());
        response.put("content", review.getContent());
        response.put("createdAt", review.getCreatedAt());
        return response;
    }
}
