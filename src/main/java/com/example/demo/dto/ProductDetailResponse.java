package com.example.demo.dto;

import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.fasterxml.jackson.annotation.JsonProperty; // THÊM IMPORT NÀY
import java.util.List;

public class ProductDetailResponse {

    @JsonProperty("product") // Đảm bảo map đúng key json cho React
    private Product product;

    @JsonProperty("productImages")
    private List<ProductImage> productImages;

    @JsonProperty("isCoSize") // QUAN TRỌNG: Ép Jackson giữ lại chữ "is" đúng logic React đang check
    private boolean isCoSize;

    @JsonProperty("listMauSac")
    private List<String> listMauSac;

    @JsonProperty("listSizeTheoMau")
    private List<String> listSizeTheoMau;

    @JsonProperty("listSPTuongTu")
    private List<Product> listSPTuongTu;

    // Constructor mặc định (bắt buộc phải có cho Jackson map dữ liệu)
    public ProductDetailResponse() {
    }

    // Constructor đầy đủ để gán nhanh dữ liệu ở tầng ServiceImpl
    public ProductDetailResponse(Product product, List<ProductImage> productImages, boolean isCoSize,
                                 List<String> listMauSac, List<String> listSizeTheoMau, List<Product> listSPTuongTu) {
        this.product = product;
        this.productImages = productImages;
        this.isCoSize = isCoSize;
        this.listMauSac = listMauSac;
        this.listSizeTheoMau = listSizeTheoMau;
        this.listSPTuongTu = listSPTuongTu;
    }

    // Getter và Setter cho các thuộc tính
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<ProductImage> getProductImages() {
        return productImages;
    }

    public void setProductImages(List<ProductImage> productImages) {
        this.productImages = productImages;
    }

    public boolean isCoSize() {
        return isCoSize;
    }

    public void setCoSize(boolean coSize) {
        this.isCoSize = coSize;
    }

    public List<String> getListMauSac() {
        return listMauSac;
    }

    public void setListMauSac(List<String> listMauSac) {
        this.listMauSac = listMauSac;
    }

    public List<String> getListSizeTheoMau() {
        return listSizeTheoMau;
    }

    public void setListSizeTheoMau(List<String> listSizeTheoMau) {
        this.listSizeTheoMau = listSizeTheoMau;
    }

    public List<Product> getListSPTuongTu() {
        return listSPTuongTu;
    }

    public void setListSPTuongTu(List<Product> listSPTuongTu) {
        this.listSPTuongTu = listSPTuongTu;
    }
}