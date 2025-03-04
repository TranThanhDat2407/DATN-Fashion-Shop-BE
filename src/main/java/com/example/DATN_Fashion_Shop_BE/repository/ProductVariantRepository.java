package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
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

    @Query("SELECT DISTINCT pv.id FROM ProductVariant pv " +
            "JOIN pv.product p " +
            "JOIN p.categories c " +
            "WHERE c.id IN :categoryIds")
    List<Long> findProductVariantIdsByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT pv.id FROM ProductVariant pv " +
            "JOIN pv.product p " +
            "JOIN p.translations t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :productName, '%')) " +
            "AND t.language.code = :languageCode")
    List<Long> findProductVariantIdsByProductName(@Param("productName") String productName,
                                                  @Param("languageCode") String languageCode);
}
