package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "DanhMuc")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhMuc")
    private Integer id;

    @Column(name = "TenDanhMuc", nullable = false)
    private String name;

    @Column(name = "Slug", unique = true, length = 255)
    private String slug;

    @Column(name = "Banner", nullable = false)
    private String banner;

    @Column(name = "MaDanhMucCha")
    private Integer parentId;

    public Category() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
}