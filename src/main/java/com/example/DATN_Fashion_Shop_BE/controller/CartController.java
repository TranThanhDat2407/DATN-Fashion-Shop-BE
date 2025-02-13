package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.cart.CartRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.promotion.PromotionRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.cart.CartItemResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.cart.CartResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.cart.TotalCartResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.promotion.PromotionResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.service.CartService;
import com.example.DATN_Fashion_Shop_BE.service.PromotionService;
import com.example.DATN_Fashion_Shop_BE.service.SessionService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("${api.prefix}/cart")
@RequiredArgsConstructor
public class CartController {

    private final LocalizationUtils localizationUtils;
    private final CartService cartService;
    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart2(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (sessionId == null) {
            sessionId = sessionService.getSessionIdFromRequest(request);
        }

        if (sessionId == null && userId == null) {
            sessionId = sessionService.generateNewSessionId();
            sessionService.setSessionIdInCookie(response, sessionId);
        }

        CartResponse cartResponse = cartService.getCart(userId, sessionId);
        return ResponseEntity.ok(cartResponse);
    }

    // Lấy giỏ hàng của người dùng
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        CartResponse response =  cartService.getCartForUser(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        response));
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/{userId}/add")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(@PathVariable Long userId,
                                                                   @RequestBody CartRequest request) {
        CartItemResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        response));
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @PutMapping("/{userId}/update/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCart(@PathVariable Long userId,
                                                       @PathVariable Long cartItemId,
                                                       @RequestParam int quantity) {
        CartItemResponse response = cartService.updateCart(userId, cartItemId, quantity);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        response));
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/{userId}/remove/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(@PathVariable Long userId, @PathVariable Long cartItemId) {
        cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        null));
    }

    // Xóa toàn bộ giỏ hàng
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        null));
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse<TotalCartResponse>> getTotalCartItems(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId) {
        
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        cartService.getTotalCartItems(userId, sessionId)
                )
        );
    }
}
