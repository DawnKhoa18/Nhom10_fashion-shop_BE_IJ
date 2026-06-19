package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GoiY")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGoiY")
    private Long id;

    @Column(name = "TenGoiY", nullable = false)
    private String name;

    @Column(name = "MoTa", columnDefinition = "TEXT")
    private String description;

    @Column(name = "PhuongPhap", nullable = false)
    private String method;

    @Column(name = "NgayTao", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Recommendation() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}