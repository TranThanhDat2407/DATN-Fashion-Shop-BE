package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.WishList;
import com.example.DATN_Fashion_Shop_BE.model.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface WishlistItemRepository extends JpaRepository<WishListItem, Long> {
    boolean existsByWishlistUserIdAndProductVariantProductIdAndProductVariantColorValueId(
            Long userId, Long productId, Long colorId
    );
}
