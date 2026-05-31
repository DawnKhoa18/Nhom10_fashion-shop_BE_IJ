package com.example.demo.model;

import java.io.Serializable;
import java.util.Objects;

public class EventRecommendationDetailId implements Serializable {
    private Long eventId;
    private Long recommendationId;

    public EventRecommendationDetailId() {}

    public EventRecommendationDetailId(Long eventId, Long recommendationId) {
        this.eventId = eventId;
        this.recommendationId = recommendationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventRecommendationDetailId that = (EventRecommendationDetailId) o;
        return Objects.equals(eventId, that.eventId) && Objects.equals(recommendationId, that.recommendationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, recommendationId);
    }
}