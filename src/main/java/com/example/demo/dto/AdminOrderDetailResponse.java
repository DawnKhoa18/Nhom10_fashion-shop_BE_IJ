package com.example.demo.dto;

import java.math.BigDecimal;

public class AdminOrderDetailResponse {
    private Long maCTDonHang;
    private Long maSP;
    private String tenSP;
    private BigDecimal gia;
    private Integer soLuong;
    private BigDecimal thanhTien;

    public AdminOrderDetailResponse(Long maCTDonHang, Long maSP, String tenSP, BigDecimal gia,
                                    Integer soLuong, BigDecimal thanhTien) {
        this.maCTDonHang = maCTDonHang;
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.gia = gia;
        this.soLuong = soLuong;
        this.thanhTien = thanhTien;
    }

    public Long getMaCTDonHang() { return maCTDonHang; }
    public Long getMaSP() { return maSP; }
    public String getTenSP() { return tenSP; }
    public BigDecimal getGia() { return gia; }
    public Integer getSoLuong() { return soLuong; }
    public BigDecimal getThanhTien() { return thanhTien; }
}
