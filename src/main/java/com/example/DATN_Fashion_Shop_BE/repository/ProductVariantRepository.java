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
}
