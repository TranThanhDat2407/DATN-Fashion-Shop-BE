package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.AttributeValuePattern;
import com.example.DATN_Fashion_Shop_BE.model.Cart;
import com.example.DATN_Fashion_Shop_BE.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cart> findByUser(User user);
    Optional<Cart> findBySessionId(String sessionId);

    Optional<Cart> findByUser_Id(Long userId);

    Integer countByUserId(Long userId);
    Integer countBySessionId(String sessionId);

    void deleteByUserId(Long id);

    Boolean existsByUser(User user);
}
