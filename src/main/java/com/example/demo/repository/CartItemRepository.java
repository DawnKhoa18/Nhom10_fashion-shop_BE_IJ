package com.example.demo.repository;

import com.example.demo.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm tất cả sản phẩm trong một giỏ hàng cụ thể
    List<CartItem> findByCartId(Long cartId);
    Optional<CartItem> findFirstByCartIdAndProductIdAndVariantId(Long cartId, Long productId, Long variantId);
    Optional<CartItem> findFirstByCartIdAndProductIdAndVariantIdIsNull(Long cartId, Long productId);

    // THÊM: Truy vấn tính tổng số lượng sản phẩm của một giỏ hàng
    @Query("SELECT SUM(c.quantity) FROM CartItem c WHERE c.cartId = :cartId")
    Integer sumQuantityByCartId(@Param("cartId") Long cartId);
}
