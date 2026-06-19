package com.example.demo.controller;

import com.example.demo.model.OrderDetail;
import com.example.demo.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/order-details")
public class OrderDetailController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping
    public List<OrderDetail> getAllOrderDetails() {
        return orderDetailRepository.findAll();
    }

    @GetMapping("/order/{orderId}")
    public List<OrderDetail> getDetailsByOrderId(@PathVariable Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}