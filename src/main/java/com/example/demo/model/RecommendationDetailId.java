package com.example.demo.model;

import java.io.Serializable;
import java.util.Objects;

public class RecommendationDetailId implements Serializable {
    private Long recommendationId;
    private Long productId;

    public RecommendationDetailId() {}

    public RecommendationDetailId(Long recommendationId, Long productId) {
        this.recommendationId = recommendationId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationDetailId that = (RecommendationDetailId) o;
        return Objects.equals(recommendationId, that.recommendationId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recommendationId, productId);
    }
}