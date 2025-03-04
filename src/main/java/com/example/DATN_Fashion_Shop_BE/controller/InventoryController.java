package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory.InventoryAudResponse;
import com.example.DATN_Fashion_Shop_BE.service.InventoryService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/inventory")
@AllArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final LocalizationUtils localizationUtils;


//    @GetMapping("/store/{storeId}/history")
//    public ResponseEntity<ApiResponse<PageResponse<InventoryAudResponse>>> getInventoryHistoryByStore(
//            @PathVariable Long storeId,
//            @RequestParam(required = false) Long id,
//            @RequestParam(required = false) Long updatedBy,
//            @RequestParam(required = false) Integer rev,
//            @RequestParam(required = false) String revType,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAtFrom,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAtTo,
//            @RequestParam(required = false) String productName,
//            @RequestParam(required = false) Long categoryId,
//            @RequestParam(required = false, defaultValue = "vi") String languageCode,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<InventoryAudResponse> history = inventoryService.getInventoryHistoryByStore(
//                pageable, id, updatedBy, rev, revType, updatedAtFrom, updatedAtTo, productName, storeId, categoryId, languageCode);
//
//        return ResponseEntity.ok(ApiResponseUtils.successResponse(
//                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
//                PageResponse.fromPage(history)));
//    }
}
