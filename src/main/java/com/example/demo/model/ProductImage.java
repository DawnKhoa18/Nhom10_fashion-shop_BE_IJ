package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "HinhAnhSanPham")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHinh")
    private Long id;

    @Column(name = "MaSP", nullable = false)
    private Long productId;

    @Column(name = "MaBienThe")
    private Long variantId;

    @Column(name = "TenHinh", nullable = false, length = 1000)
    private String imageName;

    @Column(name = "ThuTuHinh")
    private Integer displayOrder;

    // Khởi tạo rỗng
    public ProductImage() {
    }

    // Getter và Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}