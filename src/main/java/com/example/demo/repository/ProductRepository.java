package com.example.demo.repository;

import com.example.demo.model.Product;
import com.example.demo.model.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional; // THÊM IMPORT ĐỂ DÙNG OPTIONAL

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. HÀNG MỚI: Cột NgayTao trong SQL ứng với biến createdAt trong Java
    List<Product> findTop5ByOrderByCreatedAtDesc();

    // 2. HÀNG BÁN CHẠY: Dùng Native Query với đúng tên bảng SanPham và ChiTietDonHang
    @Query(value = "SELECT p.* FROM SanPham p " +
            "JOIN (SELECT MaSP, SUM(SoLuong) as TongBan " +
            "      FROM ChiTietDonHang " +
            "      GROUP BY MaSP) ct ON p.MaSP = ct.MaSP " +
            "ORDER BY ct.TongBan DESC", nativeQuery = true)
    List<Product> findBestSellers(Pageable pageable);

    // 3. THEO DANH MỤC: Lấy sản phẩm mới nhất dựa trên List ID danh mục
    // (Xử lý được cả trường hợp lấy theo danh mục cha bao gồm các con)
    List<Product> findTop5ByCategoryIdInOrderByCreatedAtDesc(List<Integer> categoryIds);

    // ================= CHỈ THÊM 2 HÀM DƯỚI ĐÂY ĐỂ PHỤC VỤ TRANG DANH SÁCH DÙNG CHUNG =================

    // Hàm 4: Lấy toàn bộ sản phẩm thuộc danh sách ID danh mục (Dùng khi click vào menu Áo, Quần, Phụ kiện...)
    // Kế thừa từ hàm số 3 của ông nhưng bỏ "Top5" và bỏ "OrderBy" cứng để tí nữa Controller tự sắp xếp động theo bộ lọc
    List<Product> findByCategoryIdIn(List<Integer> categoryIds);

    // Hàm 5: Lấy toàn bộ hàng bán chạy không giới hạn số lượng để phục vụ tính năng xem thêm và bộ lọc
    // Kế thừa từ Native Query hàm số 2 của ông nhưng ném Pageable đi để lấy toàn bộ danh sách
    @Query(value = "SELECT p.* FROM SanPham p " +
            "JOIN (SELECT MaSP, SUM(SoLuong) as TongBan " +
            "      FROM ChiTietDonHang " +
            "      GROUP BY MaSP) ct ON p.MaSP = ct.MaSP " +
            "ORDER BY ct.TongBan DESC", nativeQuery = true)
    List<Product> findAllBestSellers();

    // Thay đổi dòng Query thành cấu trúc {call ...}
    @Query(value = "{call sp_LayGoiY_SanPhamTuongTu(:productId, :take)}", nativeQuery = true)
    List<Product> findRelatedProducts(@Param("productId") Long productId, @Param("take") int take);

    // ================= CHỈ SỬA ĐÚNG DÒNG NÀY: DÒ THEO BIẾN name TRONG ENTITY PRODUCT =================
    Optional<Product> findByName(String name);
}