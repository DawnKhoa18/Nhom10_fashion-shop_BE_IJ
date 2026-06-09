package com.example.demo.controller;

import com.example.demo.model.Cart;
import com.example.demo.model.CartItem; // Import thêm model này
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CartItemRepository; // Import thêm repository này
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository; // Thêm injection này

    @GetMapping
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    @GetMapping("/customer/{customerId}")
    public Cart getCartByCustomer(@PathVariable Long customerId) {
        return cartRepository.findByCustomerId(customerId).orElse(null);
    }

    // CHỈNH SỬA: Thêm kiểm tra và gán cartId mặc định để tránh lỗi database
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItem item) {
        // Nếu cartId từ React gửi lên là null, gán mặc định bằng 1L
        if (item.getCartId() == null) {
            item.setCartId(1L);
        }

        CartItem savedItem = cartItemRepository.save(item);
        return ResponseEntity.ok(savedItem);
    }

    // THÊM: Hàm này giúp React lấy tổng số lượng sản phẩm
    @GetMapping("/count/{cartId}")
    public ResponseEntity<Integer> getCartCount(@PathVariable Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        int total = items.stream().mapToInt(CartItem::getQuantity).sum();
        return ResponseEntity.ok(total);
    }
}