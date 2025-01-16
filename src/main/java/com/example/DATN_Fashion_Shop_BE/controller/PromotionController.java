package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import com.example.DATN_Fashion_Shop_BE.service.ProductService;
import com.example.DATN_Fashion_Shop_BE.service.PromotionService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
    public ResponseEntity<ApiResponse<PromotionDTO>> getActivePromotion() throws DataNotFoundException {
        Promotion promotion = promotionService.getActivePromotion();

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                PromotionDTO.fromPromotion(promotion)
        ));
    }

    // Endpoint lấy danh sách promotion đang active trong khoảng thời gian cho trước
    @GetMapping("/active-between")
    public ResponseEntity<ApiResponse<PageResponse<Promotion>>> getActivePromotionsWithinDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {

        Page<Promotion> promotions = promotionService.getActivePromotionsWithinDateRange(startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(promotions)
        ));
    }
}
