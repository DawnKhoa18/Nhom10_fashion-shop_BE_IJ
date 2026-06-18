package com.example.demo.controller;

import com.example.demo.dto.CartItemResponse;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.Product;
import com.example.demo.model.ProductVariant;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private static final Long DEFAULT_CART_ID = 1L;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @GetMapping
    public List<CartItemResponse> getDefaultCartItems() {
        return buildCartItems(DEFAULT_CART_ID);
    }

    @GetMapping("/all")
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    @GetMapping("/customer/{customerId}")
    public Cart getCartByCustomer(@PathVariable Long customerId) {
        return cartRepository.findByCustomerId(customerId).orElse(null);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload) {
        Long cartId = getLong(payload, "cartId", DEFAULT_CART_ID);
        Long productId = getLong(payload, "maSP", null);
        Integer quantity = Math.max(getInt(payload, "soLuong", 1), 1);

        if (productId == null || productRepository.findById(productId).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Khong tim thay san pham."));
        }

        ProductVariant variant = resolveVariant(productId, getString(payload, "mau"), getString(payload, "size"));
        Optional<CartItem> existing = variant != null
                ? cartItemRepository.findFirstByCartIdAndProductIdAndVariantId(cartId, productId, variant.getId())
                : cartItemRepository.findFirstByCartIdAndProductIdAndVariantIdIsNull(cartId, productId);

        CartItem item = existing.orElseGet(CartItem::new);
        if (item.getId() == null) {
            item.setCartId(cartId);
            item.setProductId(productId);
            item.setVariantId(variant != null ? variant.getId() : null);
            item.setQuantity(quantity);
        } else {
            item.setQuantity((item.getQuantity() == null ? 0 : item.getQuantity()) + quantity);
        }

        cartItemRepository.save(item);
        return ResponseEntity.ok(Map.of("success", true, "cartCount", getCartQuantity(cartId)));
    }

    @GetMapping("/count/{cartId}")
    public ResponseEntity<Integer> getCartCount(@PathVariable Long cartId) {
        return ResponseEntity.ok(getCartQuantity(cartId));
    }

    @PostMapping("/update-qty")
    public ResponseEntity<?> updateCartQty(@RequestBody Map<String, Object> payload) {
        CartItem item = findCartItem(payload).orElse(null);
        if (item == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Khong tim thay san pham trong gio."));
        }

        item.setQuantity(Math.max(getInt(payload, "soLuong", 1), 1));
        CartItem saved = cartItemRepository.save(item);
        CartItemResponse response = toCartItemResponse(saved);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "thanhTien", response.getThanhTien(),
                "cartCount", getCartQuantity(saved.getCartId())
        ));
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody Map<String, Object> payload) {
        CartItem item = findCartItem(payload).orElse(null);
        if (item == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Khong tim thay san pham trong gio."));
        }

        Long cartId = item.getCartId();
        cartItemRepository.delete(item);
        return ResponseEntity.ok(Map.of("success", true, "cartCount", getCartQuantity(cartId)));
    }

    private List<CartItemResponse> buildCartItems(Long cartId) {
        List<CartItemResponse> result = new ArrayList<>();
        for (CartItem item : cartItemRepository.findByCartId(cartId)) {
            CartItemResponse response = toCartItemResponse(item);
            if (response != null) {
                result.add(response);
            }
        }
        return result;
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        Product product = productRepository.findById(item.getProductId()).orElse(null);
        if (product == null) {
            return null;
        }

        ProductVariant variant = item.getVariantId() != null
                ? productVariantRepository.findById(item.getVariantId()).orElse(null)
                : null;

        BigDecimal donGia = variant != null && variant.getPrice() != null ? variant.getPrice() : product.getPrice();
        Integer soLuong = item.getQuantity() == null ? 0 : item.getQuantity();
        BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
        String imageUrl = "http://localhost:8080/Images/Products/" + product.getThumbnail();

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                imageUrl,
                variant != null ? variant.getColor() : "Default",
                variant != null ? variant.getSize() : "Free",
                donGia,
                soLuong,
                thanhTien
        );
    }

    private Optional<CartItem> findCartItem(Map<String, Object> payload) {
        Long id = getLong(payload, "id", null);
        if (id != null) {
            return cartItemRepository.findById(id);
        }

        Long cartId = getLong(payload, "cartId", DEFAULT_CART_ID);
        Long productId = getLong(payload, "maSP", null);
        if (productId == null) {
            return Optional.empty();
        }

        ProductVariant variant = resolveVariant(productId, getString(payload, "mau"), getString(payload, "size"));
        return variant != null
                ? cartItemRepository.findFirstByCartIdAndProductIdAndVariantId(cartId, productId, variant.getId())
                : cartItemRepository.findFirstByCartIdAndProductIdAndVariantIdIsNull(cartId, productId);
    }

    private ProductVariant resolveVariant(Long productId, String color, String size) {
        if (color != null && size != null) {
            Optional<ProductVariant> exact = productVariantRepository.findFirstByProductIdAndColorAndSize(productId, color, size);
            if (exact.isPresent()) {
                return exact.get();
            }
            if ("FreeSize".equalsIgnoreCase(size)) {
                exact = productVariantRepository.findFirstByProductIdAndColorAndSize(productId, color, "Free");
                if (exact.isPresent()) {
                    return exact.get();
                }
            }
        }

        if (color != null) {
            Optional<ProductVariant> byColor = productVariantRepository.findFirstByProductIdAndColor(productId, color);
            if (byColor.isPresent()) {
                return byColor.get();
            }
        }

        return productVariantRepository.findFirstByProductId(productId).orElse(null);
    }

    private int getCartQuantity(Long cartId) {
        Integer total = cartItemRepository.sumQuantityByCartId(cartId);
        return total == null ? 0 : total;
    }

    private Long getLong(Map<String, Object> payload, String key, Long defaultValue) {
        Object value = payload.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private Integer getInt(Map<String, Object> payload, String key, Integer defaultValue) {
        Object value = payload.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String getString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null || value.toString().isBlank() ? null : value.toString();
    }
}
