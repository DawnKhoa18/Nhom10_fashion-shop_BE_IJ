package com.example.demo.repository;

import com.example.demo.model.Product;
import com.example.demo.model.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findTop5ByOrderByCreatedAtDesc();

    @Query(value = "SELECT p.* FROM SanPham p " +
            "JOIN (SELECT MaSP, SUM(SoLuong) as TongBan " +
            "      FROM ChiTietDonHang " +
            "      GROUP BY MaSP) ct ON p.MaSP = ct.MaSP " +
            "ORDER BY ct.TongBan DESC", nativeQuery = true)
    List<Product> findBestSellers(Pageable pageable);

    List<Product> findTop5ByCategoryIdInOrderByCreatedAtDesc(List<Integer> categoryIds);

    List<Product> findByCategoryIdIn(List<Integer> categoryIds);

    @Query(value = "SELECT p.* FROM SanPham p " +
            "JOIN (SELECT MaSP, SUM(SoLuong) as TongBan " +
            "      FROM ChiTietDonHang " +
            "      GROUP BY MaSP) ct ON p.MaSP = ct.MaSP " +
            "ORDER BY ct.TongBan DESC", nativeQuery = true)
    List<Product> findAllBestSellers();

    @Query(value = "SELECT p.* FROM SanPham p " +
            "WHERE p.MaDanhMuc = (SELECT sp.MaDanhMuc FROM SanPham sp WHERE sp.MaSP = :productId) " +
            "AND p.MaSP <> :productId " +
            "AND p.HienThi = 1 " +
            "ORDER BY p.NgayTao DESC " +
            "LIMIT :take", nativeQuery = true)
    List<Product> findRelatedProducts(@Param("productId") Long productId, @Param("take") int take);

    Optional<Product> findByName(String name);
}
