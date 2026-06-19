package com.example.demo.controller;

import com.example.demo.model.RecommendationDetail;
import com.example.demo.repository.RecommendationDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/recommendation-details")
public class RecommendationDetailController {

    @Autowired
    private RecommendationDetailRepository detailRepository;

    @GetMapping
    public List<RecommendationDetail> getAllDetails() {
        return detailRepository.findAll();
    }

    @GetMapping("/{recommendationId}")
    public List<RecommendationDetail> getDetailsByRecId(@PathVariable Long recommendationId) {
        return detailRepository.findByRecommendationIdOrderByScoreDesc(recommendationId);
    }
}