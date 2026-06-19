package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DanhGia")
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhGia")
    private Long id;

    @Column(name = "MaSP", nullable = false)
    private Long productId;

    @Column(name = "MaKH")
    private Long customerId;

    @Column(name = "Rating", nullable = false)
    private Integer rating;

    @Column(name = "TieuDe")
    private String title;

    @Column(name = "NoiDung", columnDefinition = "TEXT")
    private String content;

    @Column(name = "NgayTao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "DaDuyet")
    private Boolean isApproved = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public ProductReview() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
}