package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminProductResponse {
    private Long maSP;
    private String tenSP;
    private String danhMuc;
    private BigDecimal gia;
    private Boolean hienThi;
    private LocalDateTime ngayTao;
    private String mauSac;
    private String size;
    private Integer tongTon;
    private String hinhDaiDien;
    private Integer maDanhMuc;
    private String chatLieu;
    private String form;
    private String moTaChiTiet;

    public AdminProductResponse(Long maSP, String tenSP, String danhMuc, BigDecimal gia, Boolean hienThi,
                                LocalDateTime ngayTao, String mauSac, String size, Integer tongTon,
                                String hinhDaiDien, Integer maDanhMuc, String chatLieu, String form,
                                String moTaChiTiet) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.danhMuc = danhMuc;
        this.gia = gia;
        this.hienThi = hienThi;
        this.ngayTao = ngayTao;
        this.mauSac = mauSac;
        this.size = size;
        this.tongTon = tongTon;
        this.hinhDaiDien = hinhDaiDien;
        this.maDanhMuc = maDanhMuc;
        this.chatLieu = chatLieu;
        this.form = form;
        this.moTaChiTiet = moTaChiTiet;
    }

    public Long getMaSP() { return maSP; }
    public String getTenSP() { return tenSP; }
    public String getDanhMuc() { return danhMuc; }
    public BigDecimal getGia() { return gia; }
    public Boolean getHienThi() { return hienThi; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public String getMauSac() { return mauSac; }
    public String getSize() { return size; }
    public Integer getTongTon() { return tongTon; }
    public String getHinhDaiDien() { return hinhDaiDien; }
    public Integer getMaDanhMuc() { return maDanhMuc; }
    public String getChatLieu() { return chatLieu; }
    public String getForm() { return form; }
    public String getMoTaChiTiet() { return moTaChiTiet; }
}
