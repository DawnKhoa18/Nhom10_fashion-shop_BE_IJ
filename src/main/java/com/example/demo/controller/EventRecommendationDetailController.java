package com.example.demo.controller;

import com.example.demo.model.EventRecommendationDetail;
import com.example.demo.repository.EventRecommendationDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/event-recommendation-details")
public class EventRecommendationDetailController {

    @Autowired
    private EventRecommendationDetailRepository repository;

    @GetMapping
    public List<EventRecommendationDetail> getAll() {
        return repository.findAll();
    }

    @GetMapping("/event/{eventId}")
    public List<EventRecommendationDetail> getByEvent(@PathVariable Long eventId) {
        return repository.findByEventId(eventId);
    }
}