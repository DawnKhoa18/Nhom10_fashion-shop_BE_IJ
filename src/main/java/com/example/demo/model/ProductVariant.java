package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "BienTheSanPham")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaBienThe")
    private Long id;

    @Column(name = "MaSP", nullable = false)
    private Long productId;

    @Column(name = "Size")
    private String size;

    @Column(name = "MauSac", nullable = false)
    private String color;

    @Column(name = "Gia", nullable = false)
    private BigDecimal price;

    @Column(name = "SoLuongTon")
    private Integer stockQuantity;

    @Column(name = "NgayTao")
    private LocalDateTime createdAt;

    // Khởi tạo rỗng
    public ProductVariant() {
    }

    // Getter và Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}