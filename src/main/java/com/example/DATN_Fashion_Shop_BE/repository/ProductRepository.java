package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p FROM Product p WHERE (:isActive IS NULL OR p.isActive = :isActive)")
    Page<Product> findAllByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.categories WHERE p.id = :productId")
    Optional<Product> findByIdWithCategories(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p " +
            "JOIN ProductsTranslation pt ON p.id = pt.product.id " +
            "JOIN Language l ON pt.language.id = l.id " +
            "WHERE (:isActive IS NULL OR p.isActive = :isActive) " +
            "AND l.code = :languageCode " +
            "AND (:name IS NULL OR LOWER(pt.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Product> findAllByName(
            @Param("isActive") Boolean isActive,
            @Param("name") String name,
            @Param("languageCode") String languageCode,
            Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "JOIN ProductsTranslation pt ON p.id = pt.product.id " +
            "JOIN Language l ON pt.language.id = l.id " +
            "JOIN Promotion promo ON p.promotion.id = promo.id " +
            "WHERE l.code = :languageCode " +
            "AND (:name IS NULL OR LOWER(pt.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND promo.isActive = true")
    Page<Product> findAllWithPromotionsByName(
            @Param("name") String name,
            @Param("languageCode") String languageCode,
            Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "JOIN p.categories c " +
            "LEFT JOIN p.promotion pr " +
            "JOIN ProductVariant pv ON pv.product.id = p.id " +
            "WHERE (" +
            "(c.id = :categoryId OR :categoryId IS NULL) " +  // Category filter (direct category or NULL)
            "OR c.id IN :subCategoryIds) " + // Subcategories filtering
            "AND p.isActive = :isActive " +
            "AND (:promotionId IS NULL OR pr.id = :promotionId) " +
            "AND (pv.salePrice BETWEEN :minPrice AND :maxPrice OR :minPrice IS NULL OR :maxPrice IS NULL)")
    Page<Product> findAllByCategoryAndFilters(
            @Param("categoryId") Long categoryId,
            @Param("subCategoryIds") Set<Long> subCategoryIds,
            @Param("isActive") Boolean isActive,
            @Param("promotionId") Long promotionId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "LEFT JOIN p.categories c " +
            "JOIN p.translations t " +
            "LEFT JOIN p.promotion pr " +
            "WHERE (:categoryId IS NULL OR c.id = :categoryId " +
            "   OR c.parentCategory.id = :categoryId " +
            "   OR c.parentCategory.id IN (" +
            "       SELECT sc.id FROM Category sc WHERE sc.parentCategory.id = :categoryId" +
            "   )) " +
            "AND (:isActive IS NULL OR p.isActive = :isActive) " +
            "AND (:nameKeyword IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :nameKeyword, '%'))) " +
            "AND (:promotionId IS NULL OR pr.id = :promotionId)")
    Page<Product> findProductsByCategoryWithoutPrice(
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive,
            @Param("nameKeyword") String nameKeyword,
            @Param("promotionId") Long promotionId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "LEFT JOIN p.categories c " +
            "JOIN p.variants pv " +
            "JOIN p.translations t " +
            "LEFT JOIN p.promotion pr " +
            "WHERE (:categoryId IS NULL OR c.id = :categoryId " +
            "   OR c.parentCategory.id = :categoryId " +
            "   OR c.parentCategory.id IN (" +
            "       SELECT sc.id FROM Category sc WHERE sc.parentCategory.id = :categoryId" +
            "   )) " +
            "AND (:isActive IS NULL OR p.isActive = :isActive) " +
            "AND (:nameKeyword IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :nameKeyword, '%'))) " +
            "AND (SELECT MIN(v.salePrice) FROM p.variants v) BETWEEN :minPrice AND :maxPrice " +
            "AND (:promotionId IS NULL OR pr.id = :promotionId)")  // Chỉ lọc khi promotionId có giá trị

    Page<Product> findProductsByCategoryAndLowestPrice(
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive,
            @Param("nameKeyword") String nameKeyword,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("promotionId") Long promotionId,
            Pageable pageable
    );


    List<Product> findByPromotion(Promotion promotion);

    @Query("SELECT p.id FROM Product p WHERE p.promotion.id = :promotionId")
    List<Long> findProductIdsByPromotionId(Long promotionId);


    List<Product> findProducByPromotionId(Long promotionId);
}
