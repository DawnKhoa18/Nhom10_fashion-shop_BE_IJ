package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminOrderResponse {
    private Long maDH;
    private String soDon;
    private Long maKH;
    private String tenKhachHang;
    private String sdt;
    private String diaChi;
    private String diaChiGiaoHang;
    private LocalDateTime ngayTao;
    private BigDecimal tongTien;
    private String trangThai;
    private List<AdminOrderDetailResponse> chiTietDonHangs;

    public AdminOrderResponse(Long maDH, String soDon, Long maKH, String tenKhachHang, String sdt,
                              String diaChi, String diaChiGiaoHang, LocalDateTime ngayTao,
                              BigDecimal tongTien, String trangThai,
                              List<AdminOrderDetailResponse> chiTietDonHangs) {
        this.maDH = maDH;
        this.soDon = soDon;
        this.maKH = maKH;
        this.tenKhachHang = tenKhachHang;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.diaChiGiaoHang = diaChiGiaoHang;
        this.ngayTao = ngayTao;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.chiTietDonHangs = chiTietDonHangs;
    }

    public Long getMaDH() { return maDH; }
    public String getSoDon() { return soDon; }
    public Long getMaKH() { return maKH; }
    public String getTenKhachHang() { return tenKhachHang; }
    public String getSdt() { return sdt; }
    public String getDiaChi() { return diaChi; }
    public String getDiaChiGiaoHang() { return diaChiGiaoHang; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public BigDecimal getTongTien() { return tongTien; }
    public String getTrangThai() { return trangThai; }
    public List<AdminOrderDetailResponse> getChiTietDonHangs() { return chiTietDonHangs; }
}
