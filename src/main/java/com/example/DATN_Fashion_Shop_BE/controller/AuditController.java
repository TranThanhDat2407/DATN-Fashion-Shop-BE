package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.AuditRecord;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.audit.CategoryAudResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.audit.CategoryTranslationAudResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.category.CategoryEditResponseDTO;
import com.example.DATN_Fashion_Shop_BE.model.CategoriesTranslation;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.service.AttributeValuesService;
import com.example.DATN_Fashion_Shop_BE.service.AuditService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/audit")
@AllArgsConstructor
public class AuditController {
    private final AuditService auditService;
    private final LocalizationUtils localizationUtils;

    /**
     * Endpoint tìm kiếm lịch sử audit của Category theo:
     * - revId (revision id)
     * - revType (revision type: ADD, MOD, DEL)
     * - updatedAtFrom và updatedAtTo (khoảng thời gian của updatedAt)
     *
     * Ví dụ: GET /api/v1/audit/category/search?page=0&pageSize=10
     *          &revId=5&revType=MOD&updatedAtFrom=2025-01-01T00:00:00&updatedAtTo=2025-01-31T23:59:59
     *
     * @param revId         (Tùy chọn) revision id cần tìm
     * @param revType       (Tùy chọn) revision type cần tìm (ADD, MOD, DEL)
     * @param updatedAtFrom (Tùy chọn) thời gian cập nhật bắt đầu (ISO DateTime)
     * @param updatedAtTo   (Tùy chọn) thời gian cập nhật kết thúc (ISO DateTime)
     * @return Page chứa danh sách CategoryAudResponse thỏa điều kiện tìm kiếm
     */
    @GetMapping("/category/history")
    public ResponseEntity<ApiResponse<PageResponse<CategoryAudResponse>>> searchCategoryAuditHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "revId") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long updatedBy,
            @RequestParam(required = false) Integer revId,
            @RequestParam(required = false) String revType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAtFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAtTo) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));

        Page<CategoryAudResponse> result = auditService.searchCategoryAuditHistory(
                pageable, id, updatedBy,revId, revType, updatedAtFrom, updatedAtTo);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(result)));
    }

    @GetMapping("/category-trans/history")
    public ResponseEntity<Page<CategoryTranslationAudResponse>> getCategoryTranslationAuditHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long updatedBy,
            @RequestParam(required = false) Integer revId,
            @RequestParam(required = false) String revType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAtFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAtTo) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<CategoryTranslationAudResponse> auditHistory = auditService
                .searchCategoryTranslationAuditHistory(pageable, id, updatedBy,revId, revType, updatedAtFrom, updatedAtTo);

        return ResponseEntity.ok(auditHistory);
    }

    @PutMapping("category-trans/undo-update/{id}")
    public ResponseEntity<String> undoCategoryTranslation(@PathVariable("id") Long id) {
        auditService.undoLastCategoryTranslationRevision(id);
        return ResponseEntity.ok("Hoàn tác thành công!");
    }

    @PutMapping("category-trans/undo/{id}/{revId}")
    public ResponseEntity<String> undoCategoryTranslation(
            @PathVariable("id") Long id,
            @PathVariable("revId") Integer revId) {
        auditService.undoCategoryTranslationToRevision(id, revId);
        return ResponseEntity.ok("Hoàn tác thành công về revision " + revId);
    }

    @PutMapping("category/undo/{id}/{revId}")
    public ResponseEntity<ApiResponse<CategoryEditResponseDTO>> undoCategory(
            @PathVariable("id") Long id,
            @PathVariable("revId") Integer revId) {
        CategoryEditResponseDTO response = auditService.undoCategoryToRevision(id, revId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                response));
    }


}
