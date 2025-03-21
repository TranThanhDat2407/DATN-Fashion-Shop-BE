package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);

    Optional<ProductVariant> findByProductIdAndColorValueIdAndSizeValueId(Long productId, Long colorId, Long sizeId);

    @Query("SELECT pv " +
            "FROM ProductMedia pm " +
            "JOIN pm.productVariants pv " +
            "WHERE pm.id = :mediaId")
    List<ProductVariant> findProductVariantsByMediaId(@Param("mediaId") Long mediaId);

    List<ProductVariant> findByProduct_IdAndColorValue_Id(Long productId, Long colorId);

    List<ProductVariant> findByProductIdAndColorValueId(Long productId, Long colorId);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId ORDER BY pv.salePrice ASC LIMIT 1")
    Optional<ProductVariant> findLowestPriceVariantByProductId(Long productId);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.colorValue.id = :colorId")
    List<ProductVariant> findByProductAndColor(@Param("productId") Long productId, @Param("colorId") Long colorId);


    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id IN " +
            "(SELECT p.id FROM Product p JOIN p.translations t " +
            "WHERE (:name IS NULL OR :name = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND t.language.code = :languageCode)")
    Page<ProductVariant> findByProductNameAndLanguage(@Param("name") String name,
                                                      @Param("languageCode") String languageCode,
                                                      Pageable pageable
                                                      );


    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.promotion.id = :promotionId")
    List<ProductVariant> findByPromotionId(@Param("promotionId") Long promotionId);
}
