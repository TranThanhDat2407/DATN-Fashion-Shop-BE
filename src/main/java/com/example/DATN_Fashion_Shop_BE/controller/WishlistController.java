package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.cart.CartRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.cart.CartItemResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.cart.CartResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.wishlist.TotalWishlistResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.wishlist.WishlistItemResponse;
import com.example.DATN_Fashion_Shop_BE.service.CartService;
import com.example.DATN_Fashion_Shop_BE.service.WishlistService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;
    private final LocalizationUtils localizationUtils;
    private static final Logger log = LoggerFactory.getLogger(WishlistController.class);
    // Lấy danh sách wishlist của user
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getWishlist(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                wishlistService.getWishlistByUser(userId)
        ));
    }

    // Thêm/Xóa sản phẩm khỏi wishlist
    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> toggleWishlist(
            @RequestParam Long userId,
            @RequestParam Long variantId) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                wishlistService.toggleWishlist(userId, variantId)
        ));
    }

    @PostMapping("/toggle-by-product-color")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> toggleWishlistByProductAndColor(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Long colorId) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                wishlistService.toggleWishlistByProductAndColor(userId, productId, colorId)
        ));
    }

    // tổng so luong wishlist
    @GetMapping("/total/{userId}")
    public ResponseEntity<ApiResponse<TotalWishlistResponse>> getWishlistCount(@PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                wishlistService.getWishlistCount(userId)
        ));
    }
}
