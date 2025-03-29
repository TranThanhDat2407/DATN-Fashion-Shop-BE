package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.CountWishList;
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.CountWishList;
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.Top10Products;
import com.example.DATN_Fashion_Shop_BE.model.WishList;
import com.example.DATN_Fashion_Shop_BE.model.WishListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    @Query("""
    SELECT new com.example.DATN_Fashion_Shop_BE.dto.response.revenue.CountWishList(
        MIN(pv.id),
        pt.name,
        pv.colorValue.valueName,
        pv.colorValue.valueImg,
        pm.mediaUrl,
        pv.salePrice,
        COUNT(DISTINCT w.id)
    ) 
    FROM ProductVariant pv
    JOIN pv.product p
    JOIN ProductsTranslation pt ON pt.product = p AND pt.language.code = :languageCode
    LEFT JOIN ProductMedia pm ON pm.product = p AND pm.colorValue.id = pv.colorValue.id
    LEFT JOIN WishListItem w ON w.productVariant = pv 
    WHERE (:productId IS NULL OR p.id = :productId)
    AND (:productName IS NULL OR LOWER(pt.name) LIKE LOWER(CONCAT('%', :productName, '%')))
    GROUP BY p.id, pt.name, pv.colorValue.valueName, pv.colorValue.valueImg, pm.mediaUrl, pv.salePrice
    HAVING MIN(pv.id) IS NOT NULL
    ORDER BY COUNT(DISTINCT w.id) DESC
""")
    Page<CountWishList> getProductStats(
            @Param("languageCode") String languageCode,
            @Param("productId") Long productId,
            @Param("productName") String productName,
            Pageable pageable);
}




