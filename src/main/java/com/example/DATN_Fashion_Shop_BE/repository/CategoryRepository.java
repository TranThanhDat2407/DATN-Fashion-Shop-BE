package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.parentCategory.id IS NULL")
    List<Category> findParentCategories();


    // Truy vấn danh mục con từ bảng Categories dựa trên parentId
    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId")
    List<Category> findChildCategoriesByParentId(@Param("parentId") Long parentId);
}
