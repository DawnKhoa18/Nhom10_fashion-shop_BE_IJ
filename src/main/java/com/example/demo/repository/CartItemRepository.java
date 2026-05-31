package com.example.demo.repository;

import com.example.demo.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm tất cả sản phẩm trong một giỏ hàng cụ thể
    List<CartItem> findByCartId(Long cartId);
}