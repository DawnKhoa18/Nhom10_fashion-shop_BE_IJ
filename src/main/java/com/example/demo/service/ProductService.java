package com.example.demo.service;

import com.example.demo.dto.ProductDetailResponse;
import java.util.List;

public interface ProductService {

    ProductDetailResponse getProductDetail(Long id);

    List<String> getSizesByColor(Long productId, String color);
}