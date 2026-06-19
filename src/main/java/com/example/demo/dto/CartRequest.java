package com.example.demo.dto;

public class CartRequest {
    private Long maSP;
    private int soLuong;
    private String mau;
    private String size;

    public CartRequest() {}

    public Long getMaSP() { return maSP; }
    public void setMaSP(Long maSP) { this.maSP = maSP; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getMau() { return mau; }
    public void setMau(String mau) { this.mau = mau; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
}