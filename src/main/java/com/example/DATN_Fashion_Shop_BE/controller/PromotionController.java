package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.*;
import com.example.DATN_Fashion_Shop_BE.dto.request.promotion.PromotionRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.promotion.UpdateProductsPromotionRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.promotion.PromotionResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.promotion.PromotionSimpleResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import com.example.DATN_Fashion_Shop_BE.service.ProductService;
import com.example.DATN_Fashion_Shop_BE.service.PromotionService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PromotionResponse>> getActivePromotion() throws DataNotFoundException {
        PromotionResponse promotion = promotionService.getActivePromotion();

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                promotion
        ));
    }

    // Endpoint lấy danh sách promotion đang active trong khoảng thời gian cho trước
    @GetMapping("/active-between")
    public ResponseEntity<ApiResponse<PageResponse<PromotionResponse>>> getActivePromotionsWithinDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {

        Page<PromotionResponse> promotions = promotionService.getActivePromotionsWithinDateRange(startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(promotions)
        ));
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.createPromotion(request);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY),
                response)
        );
    }

    @PutMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable Long promotionId,
            @Valid @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.updatePromotion(promotionId, request);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY),
                response));
    }

    // Xóa promotion theo ID
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long promotionId) {
        promotionService.deletePromotion(promotionId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.BANNER_DELETED_SUCCESSFULLY),
                null));
    }

    @PostMapping("/{promotionId}/update-products")
    public ResponseEntity<?> updateProductsPromotion(
            @PathVariable Long promotionId,
            @RequestBody UpdateProductsPromotionRequest request) {

        promotionService.updateProductsPromotion(promotionId, request.getProductIds(), request.isActivate());

        return ResponseEntity.ok("Products updated successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PromotionSimpleResponse>>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<PromotionSimpleResponse> promotions = promotionService.getAllPromotions(pageable);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(promotions)
        ));
    }

    @GetMapping("/{promotionId}/product-ids")
    public ResponseEntity<ApiResponse<List<Long>>> getProductIdsByPromotionId(
            @PathVariable Long promotionId) {

        List<Long> productIds = promotionService.getProductIdsByPromotionId(promotionId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Product IDs retrieved successfully",
                productIds
        ));
    }

    // API để xóa sản phẩm khỏi promotion
    @DeleteMapping("/{promotionId}/products/{productId}")
    public ResponseEntity<ApiResponse<String>> removeProductFromPromotion(
            @PathVariable Long promotionId,
            @PathVariable Long productId) {

        promotionService.removeProductFromPromotion(promotionId, productId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Product removed from promotion successfully",
                null
        ));
    }

    @DeleteMapping("/{promotionId}/remove-all-products")
    public ResponseEntity<ApiResponse<String>> removeAllProductsFromPromotion(@PathVariable Long promotionId) {
        promotionService.removeAllProductsFromPromotion(promotionId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "All products removed from promotion successfully",
                null
        ));
    }

    @GetMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<PromotionSimpleResponse>> getPromotionById(
            @PathVariable Long promotionId) {
        PromotionSimpleResponse response = promotionService.getPromotionSimpleResponse(promotionId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Promotion fetched successfully",
                response));
    }
}
