package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductsTranslation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductTranslationRepository extends JpaRepository<ProductsTranslation, Long> {
    @Query("SELECT pt FROM ProductsTranslation pt " +
            "WHERE LOWER(pt.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND pt.language.code = :language " +
            "AND pt.product.isActive = true")
    List<ProductsTranslation> searchByNameAndLanguage(
            @Param("name") String name,
            @Param("language") String language
    );
}
