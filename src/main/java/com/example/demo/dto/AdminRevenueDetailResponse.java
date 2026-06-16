package com.example.demo.dto;

import java.math.BigDecimal;

public class AdminRevenueDetailResponse {
    private String ngay;
    private BigDecimal tien;

    public AdminRevenueDetailResponse(String ngay, BigDecimal tien) {
        this.ngay = ngay;
        this.tien = tien;
    }

    public String getNgay() { return ngay; }
    public BigDecimal getTien() { return tien; }
}
