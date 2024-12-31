package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.CategoriesTranslation;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryTranslationRepository extends JpaRepository<CategoriesTranslation, Long> {
    List<CategoriesTranslation> findByCategoryIdInAndLanguageCode(List<Long> categoryIds, String languageCode);
    // Tìm bản dịch dựa vào ID danh mục và mã ngôn ngữ
    Optional<CategoriesTranslation> findByCategoryIdAndLanguageCode(Long categoryId, String languageCode);

    List<CategoriesTranslation> findByCategoryId(Long categoryId);
}
