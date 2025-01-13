package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductsTranslation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductTranslationRepository extends JpaRepository<ProductsTranslation, Long> {
    @Query("SELECT pt FROM ProductsTranslation pt WHERE pt.product.id " +
            "IN :productIds AND pt.language.code = :languageCode")
    List<ProductsTranslation> findByProductIdInAndLanguageCode(
            @Param("productIds") List<Long> productIds,
            @Param("languageCode") String languageCode
    );
}
