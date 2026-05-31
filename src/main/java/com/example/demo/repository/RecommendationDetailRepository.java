package com.example.demo.repository;

import com.example.demo.model.RecommendationDetail;
import com.example.demo.model.RecommendationDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecommendationDetailRepository extends JpaRepository<RecommendationDetail, RecommendationDetailId> {
    // Lấy danh sách sản phẩm gợi ý của một chiến dịch cụ thể, sắp xếp theo điểm cao nhất
    List<RecommendationDetail> findByRecommendationIdOrderByScoreDesc(Long recommendationId);
}