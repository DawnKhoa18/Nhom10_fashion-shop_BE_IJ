package com.example.demo.controller;

import com.example.demo.model.ProductVariant;
import com.example.demo.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/variants")
public class ProductVariantController {

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @GetMapping
    public List<ProductVariant> getAllVariants() {
        return productVariantRepository.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<ProductVariant> getVariantsByProduct(@PathVariable Long productId) {
        return productVariantRepository.findByProductId(productId);
    }
}