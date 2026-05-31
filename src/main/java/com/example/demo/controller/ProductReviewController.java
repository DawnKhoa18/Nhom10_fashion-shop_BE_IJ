package com.example.demo.controller;

import com.example.demo.model.ProductReview;
import com.example.demo.repository.ProductReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewRepository reviewRepository;

    // Xem toàn bộ đánh giá
    @GetMapping
    public List<ProductReview> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Xem đánh giá đã duyệt của một sản phẩm cụ thể
    @GetMapping("/product/{productId}")
    public List<ProductReview> getApprovedReviewsByProduct(@PathVariable Long productId) {
        return reviewRepository.findByProductIdAndIsApprovedTrue(productId);
    }
}