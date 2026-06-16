package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdminStatisticsResponse {
    private String start;
    private String end;
    private BigDecimal doanhThu;
    private Integer donHang;
    private Integer khachHang;
    private Integer sanPham;
    private List<AdminRevenueDetailResponse> listChiTiet;
    private List<AdminStatusSummaryResponse> statusData;

    public AdminStatisticsResponse(String start, String end, BigDecimal doanhThu, Integer donHang,
                                   Integer khachHang, Integer sanPham,
                                   List<AdminRevenueDetailResponse> listChiTiet,
                                   List<AdminStatusSummaryResponse> statusData) {
        this.start = start;
        this.end = end;
        this.doanhThu = doanhThu;
        this.donHang = donHang;
        this.khachHang = khachHang;
        this.sanPham = sanPham;
        this.listChiTiet = listChiTiet;
        this.statusData = statusData;
    }

    public String getStart() { return start; }
    public String getEnd() { return end; }
    public BigDecimal getDoanhThu() { return doanhThu; }
    public Integer getDonHang() { return donHang; }
    public Integer getKhachHang() { return khachHang; }
    public Integer getSanPham() { return sanPham; }
    public List<AdminRevenueDetailResponse> getListChiTiet() { return listChiTiet; }
    public List<AdminStatusSummaryResponse> getStatusData() { return statusData; }
}
