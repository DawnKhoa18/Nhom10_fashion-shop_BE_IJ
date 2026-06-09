package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty; // Thêm import này

@Entity
@Table(name = "ChiTietGioHang")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTGioHang")
    private Long id;

    @Column(name = "MaGioHang", nullable = false)
    private Long cartId;

    @Column(name = "MaSP", nullable = false)
    @JsonProperty("maSP") // Ánh xạ từ "maSP" của React
    private Long productId;

    @Column(name = "MaBienThe")
    private Long variantId;

    @Column(name = "SoLuong")
    @JsonProperty("soLuong") // Ánh xạ từ "soLuong" của React
    private Integer quantity = 1;

    @Column(name = "NgayThem", updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    public CartItem() {
    }

    // Getter và Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDateTime getAddedAt() { return addedAt; }
}