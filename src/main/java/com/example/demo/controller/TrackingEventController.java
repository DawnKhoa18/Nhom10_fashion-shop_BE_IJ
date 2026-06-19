package com.example.demo.controller;

import com.example.demo.model.TrackingEvent;
import com.example.demo.repository.TrackingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/events")
public class TrackingEventController {

    @Autowired
    private TrackingEventRepository trackingEventRepository;

    @GetMapping
    public List<TrackingEvent> getAllEvents() {
        return trackingEventRepository.findAll();
    }

    @PostMapping
    public TrackingEvent createEvent(@RequestBody TrackingEvent event) {
        return trackingEventRepository.save(event);
    }
}