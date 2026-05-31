package com.example.demo.repository;

import com.example.demo.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    // Lấy lịch sử sự kiện của một khách hàng
    List<TrackingEvent> findByCustomerId(Long customerId);

    // Lấy sự kiện theo loại
    List<TrackingEvent> findByEventType(String eventType);
}