package com.example.demo.controller;

import com.example.demo.dto.CartItemResponse;
import com.example.demo.model.CartItem;
import com.example.demo.model.Product;
import com.example.demo.model.ProductVariant;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @GetMapping
    public List<CartItem> getAllItems() {
        return cartItemRepository.findAll();
    }

    @GetMapping("/cart/{cartId}")
    public List<CartItemResponse> getItemsByCart(@PathVariable Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        List<CartItemResponse> result = new ArrayList<>();

        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            ProductVariant variant = item.getVariantId() != null
                    ? productVariantRepository.findById(item.getVariantId()).orElse(null)
                    : null;

            if (product != null) {
                BigDecimal donGia = variant != null ? variant.getPrice() : product.getPrice();
                Integer soLuong = item.getQuantity();
                BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));

                result.add(new CartItemResponse(
                        item.getId(),
                        product.getId(),
                        product.getName(),
                        product.getThumbnail(),
                        variant != null ? variant.getColor() : "Không có",
                        variant != null ? variant.getSize() : "FreeSize",
                        donGia,
                        soLuong,
                        thanhTien
                ));
            }
        }

        return result;

    }

}