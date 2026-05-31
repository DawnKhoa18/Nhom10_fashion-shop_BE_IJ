package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietDonHang")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTDonHang")
    private Long id;

    @Column(name = "MaDH", nullable = false)
    private Long orderId;

    @Column(name = "MaSP", nullable = false)
    private Long productId;

    @Column(name = "MaBienThe")
    private Long variantId;

    @Column(name = "SoLuong", nullable = false)
    private Integer quantity;

    @Column(name = "Gia", nullable = false)
    private BigDecimal price;

    // DB tự tính
    @Column(name = "ThanhTien", insertable = false, updatable = false)
    private BigDecimal subTotal;

    public OrderDetail() {
    }

    // Getter và Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getSubTotal() { return subTotal; }
}