package com.example.demo.dto;

import com.example.demo.model.Product;
import java.util.List;

public class ProductListResponse {
    private List<Product> products;
    private String titlePage;
    private String banner;
    private boolean hienXemThem; // true: hiện nút, false: ẩn nút

    // Constructor đầy đủ tham số
    public ProductListResponse(List<Product> products, String titlePage, String banner, boolean hienXemThem) {
        this.products = products;
        this.titlePage = titlePage;
        this.banner = banner;
        this.hienXemThem = hienXemThem;
    }

    // --- GETTER VÀ SETTER (Để Spring Boot tự convert sang JSON) ---
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getTitlePage() { return titlePage; }
    public void setTitlePage(String titlePage) { this.titlePage = titlePage; }

    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }

    public boolean isHienXemThem() { return hienXemThem; }
    public void setHienXemThem(boolean hienXemThem) { this.hienXemThem = hienXemThem; }
}