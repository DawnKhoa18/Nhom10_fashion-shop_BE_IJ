package com.example.demo.repository;

import com.example.demo.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    Optional<ProductVariant> findFirstByProductIdAndColorAndSize(Long productId, String color, String size);
    Optional<ProductVariant> findFirstByProductIdAndColor(Long productId, String color);
    Optional<ProductVariant> findFirstByProductId(Long productId);
    void deleteByProductId(Long productId);

    // Lấy danh sách màu sắc duy nhất (Dùng tên biến color và productId)
    @Query("SELECT DISTINCT pv.color FROM ProductVariant pv WHERE pv.productId = :productId")
    List<String> findDistinctColorsByProductId(@Param("productId") Long productId);

    // Lấy danh sách size duy nhất theo màu (Dùng tên biến size, productId và color)
    @Query("SELECT DISTINCT pv.size FROM ProductVariant pv WHERE pv.productId = :productId AND pv.color = :color")
    List<String> findDistinctSizesByProductIdAndColor(@Param("productId") Long productId, @Param("color") String color);
}
