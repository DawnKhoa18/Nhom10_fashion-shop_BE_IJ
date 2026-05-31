package com.example.demo.controller;

import com.example.demo.model.ProductImage;
import com.example.demo.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

    @Autowired
    private ProductImageRepository productImageRepository;

    @GetMapping
    public List<ProductImage> getAllImages() {
        return productImageRepository.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<ProductImage> getImagesByProduct(@PathVariable Long productId) {
        return productImageRepository.findByProductId(productId);
    }
}