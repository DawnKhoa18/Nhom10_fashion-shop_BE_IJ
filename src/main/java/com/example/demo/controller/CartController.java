package com.example.demo.controller;

import com.example.demo.model.Cart;
import com.example.demo.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    // Xem toàn bộ danh sách giỏ hàng đang có
    @GetMapping
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    // Lấy giỏ hàng theo ID khách hàng
    @GetMapping("/customer/{customerId}")
    public Cart getCartByCustomer(@PathVariable Long customerId) {
        return cartRepository.findByCustomerId(customerId).orElse(null);
    }
}