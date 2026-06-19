package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "KhachHang")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKH")
    private Long id;

    @Column(name = "HoTenKH", nullable = false)
    private String fullName;

    @Column(name = "MatKhau", nullable = false)
    private String password;

    @Column(name = "GioiTinh", nullable = false)
    private String gender;

    @Column(name = "SDT", nullable = false)
    private String phone;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "DiaChi")
    private String address;

    @Column(name = "NgayTao")
    private LocalDateTime createdAt;

    @Column(name = "LanDangNhapGanNhat")
    private LocalDateTime lastLogin;

    @Column(name = "TrangThai")
    private Integer status;

    @Column(name = "SoThich")
    private String hobby;

    public Customer() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getHobby() { return hobby; }
    public void setHobby(String hobby) { this.hobby = hobby; }
}