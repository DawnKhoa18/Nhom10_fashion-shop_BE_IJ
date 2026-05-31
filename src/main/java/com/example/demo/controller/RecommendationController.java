package com.example.demo.controller;

import com.example.demo.model.Recommendation;
import com.example.demo.repository.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationRepository recommendationRepository;

    // Lấy danh sách tất cả các cấu hình gợi ý
    @GetMapping
    public List<Recommendation> getAllRecommendations() {
        return recommendationRepository.findAll();
    }

    // Thêm một phương pháp gợi ý mới
    @PostMapping
    public Recommendation createRecommendation(@RequestBody Recommendation recommendation) {
        return recommendationRepository.save(recommendation);
    }
}