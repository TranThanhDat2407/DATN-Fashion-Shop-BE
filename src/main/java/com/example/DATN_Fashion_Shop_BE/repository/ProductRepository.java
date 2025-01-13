package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE (:isActive IS NULL OR p.isActive = :isActive)")
    List<Product> findAllByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN ProductsTranslation pt ON p.id = pt.product.id " +
            "WHERE p.isActive = :isActive AND pt.language.code = :languageCode " +
            "AND pt.name LIKE %:name%")
    List<Product> findAllByIsActiveAndName(
            Boolean isActive, String name, Pageable pageable);
}
