package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; // 1. Thêm import để dùng List hình ảnh

@Entity
@Table(name = "SanPham")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSP")
    private Long id;

    @JsonProperty("tenSp")
    @Column(name = "TenSP", nullable = false)
    private String name;

    @JsonProperty("hinhAnh")
    @Column(name = "HinhDaiDien", length = 1000, nullable = false)
    private String thumbnail;

    @Column(name = "ChatLieu")
    private String material;

    @Column(name = "Form")
    private String form;

    @Column(name = "MoTaChiTiet", columnDefinition = "TEXT")
    private String description;

    @JsonProperty("giaBan")
    @Column(name = "Gia", nullable = false)
    private BigDecimal price;

    @Column(name = "HienThi")
    private Boolean isVisible;

    @Column(name = "NgayTao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "NgayCapNhat")
    private LocalDateTime updatedAt;

    // CẬP NHẬT: Thêm insertable và updatable = false để tránh xung đột với @JoinColumn của category
    @Column(name = "MaDanhMuc", insertable = false, updatable = false)
    private Integer categoryId;

    // CHỈ THÊM: Thiết lập mối quan hệ ManyToOne liên kết sang thực thể Category dựa trên khóa ngoại MaDanhMuc
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaDanhMuc", insertable = false, updatable = false)
    private Category category;

    // 2. LIÊN QUAN HOVER: Kết nối với bảng HinhAnhSanPham
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSP", insertable = false, updatable = false)
    private List<ProductImage> images;

    // 3. LIÊN QUAN HOVER: Hàm trả về link ảnh hover cho React (ThuTuHinh = 2)
    @JsonProperty("hoverImage")
    public String getHoverImage() {
        if (images != null) {
            return images.stream()
                    .filter(img -> img.getDisplayOrder() != null && img.getDisplayOrder() == 2)
                    .map(ProductImage::getImageName)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public Product() {}

    // --- Getter và Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    // Getter/Setter cho images để Hibernate hoạt động
    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }

    // CHỈ THÊM: Getter và Setter cho đối tượng Category phục vụ việc render tên danh mục
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}