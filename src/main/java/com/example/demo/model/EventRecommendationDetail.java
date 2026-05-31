package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ChiTietSuKienGoiY")
@IdClass(EventRecommendationDetailId.class)
public class EventRecommendationDetail {

    @Id
    @Column(name = "MaSuKien")
    private Long eventId;

    @Id
    @Column(name = "MaGoiY")
    private Long recommendationId;

    @Column(name = "Diem")
    private Float score;

    @Column(name = "NgayTao", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public EventRecommendationDetail() {}

    // Getter và Setter
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getRecommendationId() { return recommendationId; }
    public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }

    public Float getScore() { return score; }
    public void setScore(Float score) { this.score = score; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}