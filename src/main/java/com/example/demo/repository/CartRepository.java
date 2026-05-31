package com.example.demo.repository;

import com.example.demo.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Tìm giỏ hàng theo mã khách hàng
    Optional<Cart> findByCustomerId(Long customerId);

    // Tìm giỏ hàng theo mã phiên (cho khách chưa đăng nhập)
    Optional<Cart> findBySessionId(String sessionId);
}