package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Category;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL")
    List<Category> findParentCategories();

    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL AND c.isActive = :isActive")
    List<Category> findParentCategoriesByIsActive(@Param("isActive") Boolean isActive);

    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId")
    List<Category> findChildCategoriesByParentId(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId AND c.isActive = :isActive")
    List<Category> findChildCategoriesByParentIdAndIsActive(
            @Param("parentId") Long parentId,
            @Param("isActive") Boolean isActive
    );

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.parentCategory = NULL WHERE c.parentCategory.id = :categoryId")
    void updateSubCategoriesParentToNull(@Param("categoryId") Long categoryId);

    @Query("SELECT DISTINCT c FROM Category c " +
            "JOIN CategoriesTranslation ct ON ct.category.id = c.id " +
            "WHERE " +
            "(COALESCE(:name, '') = '' OR LOWER(ct.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (" +
            "(:parentId IS NULL OR :parentId = 0 AND c.parentCategory IS NULL OR c.parentCategory.id = :parentId) " +
            ") " +
            "AND (:isActive IS NULL OR c.isActive = :isActive)")
    Page<Category> findCategoriesByFilters(
            @Param("name") String name,
            @Param("parentId") Long parentId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT c.id FROM Category c WHERE c.id = :categoryId OR c.parentCategory.id = :categoryId OR c.parentCategory.id IN " +
            "(SELECT sc.id FROM Category sc WHERE sc.parentCategory.id = :categoryId)")
    List<Long> findChildCategoryIds(@Param("categoryId") Long categoryId);
}
