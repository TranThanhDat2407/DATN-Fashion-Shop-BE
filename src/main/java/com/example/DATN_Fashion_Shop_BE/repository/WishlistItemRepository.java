package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.WishList;
import com.example.DATN_Fashion_Shop_BE.model.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface WishlistItemRepository extends JpaRepository<WishListItem, Long> {
    boolean existsByWishlistUserIdAndProductVariantProductIdAndProductVariantColorValueId(
            Long userId, Long productId, Long colorId
    );


    Optional<WishListItem> findByWishlistUserIdAndProductVariantProductIdAndProductVariantColorValueId(
            Long userId, Long productId, Long colorId
    );

    List<WishListItem> findByWishlistUserId(Long userId);

    Integer countByWishlistUserId(Long userId);
}
