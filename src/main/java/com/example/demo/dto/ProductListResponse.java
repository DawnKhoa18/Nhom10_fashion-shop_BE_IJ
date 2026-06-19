package com.example.demo.dto;

import com.example.demo.model.Product;
import java.util.List;

public class ProductListResponse {
    private List<Product> products;
    private String titlePage;
    private String banner;
    private boolean hienXemThem;

    public ProductListResponse(List<Product> products, String titlePage, String banner, boolean hienXemThem) {
        this.products = products;
        this.titlePage = titlePage;
        this.banner = banner;
        this.hienXemThem = hienXemThem;
    }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getTitlePage() { return titlePage; }
    public void setTitlePage(String titlePage) { this.titlePage = titlePage; }

    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }

    public boolean isHienXemThem() { return hienXemThem; }
    public void setHienXemThem(boolean hienXemThem) { this.hienXemThem = hienXemThem; }
}