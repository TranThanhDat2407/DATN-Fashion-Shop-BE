package com.example.DATN_Fashion_Shop_BE.service.category;

import com.example.DATN_Fashion_Shop_BE.dto.CategoryDTO;
import com.example.DATN_Fashion_Shop_BE.model.CategoriesTranslation;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import com.example.DATN_Fashion_Shop_BE.repository.CategoryRepository;
import com.example.DATN_Fashion_Shop_BE.repository.CategoryTranslationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryService {

    private CategoryRepository categoryRepository;

    private CategoryTranslationRepository categoryTranslationRepository;

    public List<CategoryDTO> getParentCategoriesWithTranslations(String languageCode) {
        // Lấy tất cả danh mục cha
        List<Category> parentCategories = categoryRepository.findParentCategories();

        // Lấy danh sách ID của danh mục cha
        List<Long> categoryIds = parentCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        // Lấy bản dịch theo danh sách ID và languageCode
        List<CategoriesTranslation> translations = categoryTranslationRepository
                .findByCategoryIdInAndLanguageCode(categoryIds, languageCode);

        // Tạo map từ ID danh mục -> bản dịch
        Map<Long, String> translationMap = translations.stream()
                .collect(Collectors.toMap(
                        translation -> translation.getCategory().getId(), // Key: ID danh mục
                        CategoriesTranslation::getName                 // Value: Tên bản dịch
                ));

        // Ánh xạ Entity sang DTO bằng Builder
        return parentCategories.stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getId())
                        .imageUrl(category.getImageUrl())
                        .name(translationMap.getOrDefault(category.getId(), ""))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách danh mục con của danh mục cha với bản dịch theo ngôn ngữ.
     *
     * @param languageCode Mã ngôn ngữ (vd: en, vi, jp).
     * @param parentId     ID của danh mục cha.
     * @return Danh sách danh mục con với tên đã dịch.
     */
    public List<CategoryDTO> getChildCategoriesWithTranslations(String languageCode, Long parentId) {
        // Lấy tất cả danh mục con từ CategoryRepository theo parentId
        List<Category> childCategories = categoryRepository.findChildCategoriesByParentId(parentId);

        // Lấy danh sách ID của các danh mục con
        List<Long> categoryIds = childCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        // Lấy các bản dịch từ CategoryTranslationRepository theo danh sách ID và ngôn ngữ
        List<CategoriesTranslation> translations = categoryTranslationRepository
                .findByCategoryIdInAndLanguageCode(categoryIds, languageCode);

        // Tạo map từ ID danh mục -> bản dịch
        Map<Long, String> translationMap = translations.stream()
                .collect(Collectors.toMap(
                        translation -> translation.getCategory().getId(), // Key: ID danh mục
                        CategoriesTranslation::getName                 // Value: Tên bản dịch
                ));

        // Ánh xạ Entity sang DTO và thêm bản dịch tên vào
        return childCategories.stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getId())
                        .imageUrl(category.getImageUrl())
                        .name(translationMap.getOrDefault(category.getId(), "")) // Lấy tên từ map hoặc trả về chuỗi rỗng
                        .build())
                .collect(Collectors.toList());
    }
}
