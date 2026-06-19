package com.example.demo.repository;

import com.example.demo.model.RecommendationDetail;
import com.example.demo.model.RecommendationDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecommendationDetailRepository extends JpaRepository<RecommendationDetail, RecommendationDetailId> {

    List<RecommendationDetail> findByRecommendationIdOrderByScoreDesc(Long recommendationId);
}