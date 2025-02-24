package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.store.CreateStoreRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreInventoryResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreStockResponse;
import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import com.example.DATN_Fashion_Shop_BE.service.StoreService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/store")
@AllArgsConstructor
public class StoreController {
    private final StoreService storeService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> searchStores(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon
            ) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.searchStores(name, city, page, size, userLat, userLon)
        ));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long storeId) {

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.getStoreById(storeId)
        ));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<StoreInventoryResponse>> storeInventory(
            @RequestParam Long productId,
            @RequestParam Long colorId,
            @RequestParam Long sizeId,
            @RequestParam Long storeId) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.stockInStore(productId, colorId, sizeId, storeId)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@RequestBody CreateStoreRequest request) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.createStore(request)
        ));

    }

    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable Long storeId,
            @RequestBody CreateStoreRequest request) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.updateStore(storeId,request)
        ));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                null
        ));
    }


    @GetMapping("/product-inventory/{storeId}")
    public ResponseEntity<ApiResponse<PageResponse<StoreStockResponse>>> getInventoryByStore(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "vi") String languageCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(storeService
                        .getInventoryByStoreId(storeId, languageCode, productName, categoryId, page, size, sortBy, sortDir))
        ));
    }
}
