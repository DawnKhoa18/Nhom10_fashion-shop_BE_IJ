package com.example.demo.dto;

import java.time.LocalDateTime;

public class AdminCustomerResponse {
    private Long maKH;
    private String hoTenKH;
    private String email;
    private String sdt;
    private String gioiTinh;
    private String diaChi;
    private String soThich;
    private Integer trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime lanDangNhapGanNhat;

    public AdminCustomerResponse(Long maKH, String hoTenKH, String email, String sdt,
                                 String gioiTinh, String diaChi, String soThich,
                                 Integer trangThai, LocalDateTime ngayTao,
                                 LocalDateTime lanDangNhapGanNhat) {
        this.maKH = maKH;
        this.hoTenKH = hoTenKH;
        this.email = email;
        this.sdt = sdt;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi;
        this.soThich = soThich;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.lanDangNhapGanNhat = lanDangNhapGanNhat;
    }

    public Long getMaKH() { return maKH; }
    public String getHoTenKH() { return hoTenKH; }
    public String getEmail() { return email; }
    public String getSdt() { return sdt; }
    public String getGioiTinh() { return gioiTinh; }
    public String getDiaChi() { return diaChi; }
    public String getSoThich() { return soThich; }
    public Integer getTrangThai() { return trangThai; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public LocalDateTime getLanDangNhapGanNhat() { return lanDangNhapGanNhat; }
}
