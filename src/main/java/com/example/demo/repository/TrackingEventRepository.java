package com.example.demo.repository;

import com.example.demo.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByCustomerId(Long customerId);

    List<TrackingEvent> findByEventType(String eventType);
}