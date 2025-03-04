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
import com.example.DATN_Fashion_Shop_BE.model.Cart;
import com.example.DATN_Fashion_Shop_BE.service.CartService;
import com.example.DATN_Fashion_Shop_BE.service.PromotionService;
import com.example.DATN_Fashion_Shop_BE.service.SessionService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            HttpServletRequest httpRequest) {

        if (sessionId == null) {
            sessionId = sessionService.getSessionIdFromRequest(httpRequest);
        }

        Cart cart = cartService.getOrCreateCart(userId, sessionId);
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                     CartResponse.fromCart(cart)
                )
        );
    }


    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId,
            @RequestBody CartRequest request,
            HttpServletRequest httpRequest) {

        if (sessionId == null) {
            sessionId = sessionService.getSessionIdFromRequest(httpRequest);
        }

        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        cartService.addToCart(userId, sessionId, request)
                )
        );
    }

    @PostMapping("/staff-add")
    public ResponseEntity<ApiResponse<CartItemResponse>> staffAddToCart(
            @RequestParam Long userId,
            @RequestParam Long storeId,
            @RequestBody CartRequest request) {
        CartItemResponse response = cartService.staffAddToCart(userId, storeId, request);
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        response
                )
        );
    }

    @PutMapping("/staff-update")
    public ResponseEntity<ApiResponse<CartItemResponse>> staffUpdateCart(
            @RequestParam Long userId,
            @RequestParam Long storeId,
            @RequestBody CartRequest request) {
        CartItemResponse response = cartService.staffUpdateCart(userId, storeId, request);
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        response
                )
        );
    }


    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId,
            @PathVariable Long cartItemId,
            HttpServletRequest request) {

        if (sessionId == null) {
            sessionId = sessionService.getSessionIdFromRequest(request);
        }

        cartService.removeFromCart(userId, sessionId, cartItemId);
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                      null
                )
        );
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId,
            @PathVariable Long cartItemId,
            @RequestParam int newQuantity,
            HttpServletRequest request) {

        if (sessionId == null) {
            sessionId = sessionService.getSessionIdFromRequest(request);
        }

        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        cartService.updateCart(userId, sessionId, cartItemId, newQuantity)
                )
        );
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request) {

        if (sessionId == null) {
            sessionId = sessionService.getSessionIdFromRequest(request);
        }

        cartService.clearCart(userId, sessionId);
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                        null));
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse<TotalCartResponse>> getTotalCartItems(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (userId != null && userId <= 0) {
            userId = null;
        }

        if ((sessionId == null || sessionId.isEmpty()) && userId == null) {
            sessionId = sessionService.getSessionIdFromRequest(request);
        }
//
//        if ((sessionId == null || sessionId.trim().isEmpty()) && userId == null) {
//            sessionId = sessionService.generateNewSessionId();
//            sessionService.setSessionIdInCookie(response, sessionId);
//        }

        log.info(" Request nhận được - userId: {}, sessionId: {}", userId, sessionId);
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        cartService.getTotalCartItems(userId, sessionId)
                )
        );
    }



    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<String>> mergeCart(
            @RequestParam String sessionId,
            @RequestParam Long userId) {

        cartService.mergeCart(sessionId, userId);
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        null
                )
        );
    }
}
