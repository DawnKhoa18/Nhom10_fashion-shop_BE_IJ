package com.example.demo.repository;

import com.example.demo.model.EventRecommendationDetail;
import com.example.demo.model.EventRecommendationDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRecommendationDetailRepository extends JpaRepository<EventRecommendationDetail, EventRecommendationDetailId> {

    List<EventRecommendationDetail> findByEventId(Long eventId);

    List<EventRecommendationDetail> findByRecommendationId(Long recommendationId);
}