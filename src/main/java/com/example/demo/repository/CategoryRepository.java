package com.example.demo.repository;

import com.example.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional; // <--- Thêm import này

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Dùng Optional để xử lý an toàn khi không tìm thấy slug
    Optional<Category> findBySlug(String slug);

    // Tìm các danh mục con (như Áo thun, Áo sơ mi...)
    List<Category> findByParentId(Integer parentId);
}