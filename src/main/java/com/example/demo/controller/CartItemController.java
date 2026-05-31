package com.example.demo.controller;

import com.example.demo.model.CartItem;
import com.example.demo.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    @Autowired
    private CartItemRepository cartItemRepository;

    // Xem tất cả các món hàng trong tất cả giỏ hàng (Để test dữ liệu tổng)
    @GetMapping
    public List<CartItem> getAllItems() {
        return cartItemRepository.findAll();
    }

    // Xem các sản phẩm của một giỏ hàng nhất định
    @GetMapping("/cart/{cartId}")
    public List<CartItem> getItemsByCart(@PathVariable Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }
}