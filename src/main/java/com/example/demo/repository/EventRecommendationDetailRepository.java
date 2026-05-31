package com.example.demo.repository;

import com.example.demo.model.EventRecommendationDetail;
import com.example.demo.model.EventRecommendationDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRecommendationDetailRepository extends JpaRepository<EventRecommendationDetail, EventRecommendationDetailId> {
    // Tìm các chi tiết liên quan đến một sự kiện cụ thể
    List<EventRecommendationDetail> findByEventId(Long eventId);

    // Tìm các chi tiết liên quan đến một phương pháp gợi ý cụ thể
    List<EventRecommendationDetail> findByRecommendationId(Long recommendationId);
}