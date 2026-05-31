package com.example.demo.service;

import com.example.demo.dto.ProductDetailResponse;
import java.util.List;

public interface ProductService {
    // CHỈNH SỬA: Đổi tham số từ String slug thành Long id để tìm kiếm theo ID
    ProductDetailResponse getProductDetail(Long id);

    // Hàm bổ trợ lấy danh sách size khi người dùng click chọn một màu sắc khác (GIỮ NGUYÊN)
    List<String> getSizesByColor(Long productId, String color);
}