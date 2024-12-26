package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.dto.CategoryDTO;
import com.example.DATN_Fashion_Shop_BE.service.category.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/{languageCode}/categories")
@AllArgsConstructor
public class CategoryController {
    private CategoryService categoryService;

    /**
     * Lấy danh sách danh mục cha với bản dịch theo ngôn ngữ.
     *
     * @param languageCode Mã ngôn ngữ (vd: en, vi, jp).
     * @return Danh sách danh mục cha với tên đã dịch.
     */

    @GetMapping("/parent")
    public ResponseEntity<List<CategoryDTO>> getParentCategoriesWithTranslations(
            @PathVariable String languageCode) {
        try {
            List<CategoryDTO> parentCategories = categoryService.getParentCategoriesWithTranslations(languageCode);
            return ResponseEntity.ok(parentCategories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{parentId}")
    public ResponseEntity<List<CategoryDTO>> getChildCategoriesWithTranslations(
            @PathVariable String languageCode, @PathVariable Long parentId) {
        try {
            // Gọi service để lấy danh sách danh mục con với bản dịch
            List<CategoryDTO> childCategories = categoryService.getChildCategoriesWithTranslations(languageCode, parentId);
            return ResponseEntity.ok(childCategories);
        } catch (Exception e) {
            // Nếu có lỗi, trả về mã lỗi 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
