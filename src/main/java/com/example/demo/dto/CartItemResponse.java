package com.example.demo.dto;

import java.math.BigDecimal;

public class CartItemResponse {
    private Long id;
    private Long maSP;
    private String tenSP;
    private String hinhAnh;
    private String mauSac;
    private String size;
    private BigDecimal donGia;
    private Integer soLuong;
    private BigDecimal thanhTien;

    public CartItemResponse(Long id, Long maSP, String tenSP, String hinhAnh,
                            String mauSac, String size, BigDecimal donGia,
                            Integer soLuong, BigDecimal thanhTien) {
        this.id = id;
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.hinhAnh = hinhAnh;
        this.mauSac = mauSac;
        this.size = size;
        this.donGia = donGia;
        this.soLuong = soLuong;
        this.thanhTien = thanhTien;
    }

    public Long getId() { return id; }
    public Long getMaSP() { return maSP; }
    public String getTenSP() { return tenSP; }
    public String getHinhAnh() { return hinhAnh; }
    public String getMauSac() { return mauSac; }
    public String getSize() { return size; }
    public BigDecimal getDonGia() { return donGia; }
    public Integer getSoLuong() { return soLuong; }
    public BigDecimal getThanhTien() { return thanhTien; }
}