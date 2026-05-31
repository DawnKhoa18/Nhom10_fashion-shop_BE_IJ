package com.example.demo.repository;

import com.example.demo.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    // Lấy danh sách đánh giá của một sản phẩm
    List<ProductReview> findByProductId(Long productId);

    // Chỉ lấy những đánh giá đã được duyệt để hiện lên Web
    List<ProductReview> findByProductIdAndIsApprovedTrue(Long productId);
}