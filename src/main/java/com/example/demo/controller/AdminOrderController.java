package com.example.demo.controller;

import com.example.demo.dto.AdminOrderDetailResponse;
import com.example.demo.dto.AdminOrderResponse;
import com.example.demo.dto.UpdateOrderStatusRequest;
import com.example.demo.model.Customer;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.model.Product;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<AdminOrderResponse> getOrders() {
        return orderRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toOrderResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderResponse> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(toOrderResponse(order)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        if ("Đã hủy".equals(order.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Đơn hàng đã bị khách hủy, không thể cập nhật."));
        }

        String status = request.getTrangThai();
        if (!"Đang xử lý".equals(status) && !"Đã xử lý".equals(status)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Trạng thái đơn hàng không hợp lệ."));
        }

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(toOrderResponse(savedOrder));
    }

    private AdminOrderResponse toOrderResponse(Order order) {
        Customer customer = order.getCustomerId() != null
                ? customerRepository.findById(order.getCustomerId()).orElse(null)
                : null;

        List<AdminOrderDetailResponse> details = orderDetailRepository.findByOrderId(order.getId())
                .stream()
                .map(this::toOrderDetailResponse)
                .toList();

        return new AdminOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                customer != null ? customer.getFullName() : "Không có khách",
                customer != null ? customer.getPhone() : null,
                customer != null ? customer.getAddress() : null,
                order.getShippingAddress(),
                order.getCreatedAt(),
                order.getTotalAmount(),
                order.getStatus(),
                details
        );
    }

    private AdminOrderDetailResponse toOrderDetailResponse(OrderDetail detail) {
        Product product = productRepository.findById(detail.getProductId()).orElse(null);
        BigDecimal thanhTien = detail.getSubTotal();
        if (thanhTien == null && detail.getPrice() != null && detail.getQuantity() != null) {
            thanhTien = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
        }

        return new AdminOrderDetailResponse(
                detail.getId(),
                detail.getProductId(),
                product != null ? product.getName() : "Sản phẩm không tồn tại",
                detail.getPrice(),
                detail.getQuantity(),
                thanhTien
        );
    }
}
