package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ChiTietGoiY")
@IdClass(RecommendationDetailId.class)
public class RecommendationDetail {

    @Id
    @Column(name = "MaGoiY")
    private Long recommendationId;

    @Id
    @Column(name = "MaSP")
    private Long productId;

    @Column(name = "Diem", nullable = false)
    private Float score;

    public RecommendationDetail() {}

    // Getter và Setter
    public Long getRecommendationId() { return recommendationId; }
    public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Float getScore() { return score; }
    public void setScore(Float score) { this.score = score; }
}