package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);

    List<CartItem> findByCart(Cart cart);

    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);

    void deleteByCartUserId(Long id);
}

