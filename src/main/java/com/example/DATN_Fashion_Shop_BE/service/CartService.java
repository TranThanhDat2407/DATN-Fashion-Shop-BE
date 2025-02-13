package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.cart.CartRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.cart.CartItemResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.cart.CartResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.cart.TotalCartResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;
    private final SessionService sessionService;
    private final FileStorageService fileStorageService;
    private final LocalizationUtils localizationUtils;


    @Transactional
    public CartItemResponse addToCart(Long userId, String sessionId, CartRequest request) {
        Cart cart = getOrCreateCart(userId, sessionId);
        ProductVariant productVariant = getProductVariant(request.getProductVariantId());

        int availableStock = getAvailableStockFromWarehouse(request.getProductVariantId());
        if (request.getQuantity() > availableStock) {
            throw new IllegalStateException("Not enough stock available. Only " + availableStock + " items left.");
        }

        CartItem cartItem = cartItemRepository.findByCart(cart).stream()
                .filter(item -> item.getProductVariant().getId().equals(request.getProductVariantId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .productVariant(productVariant)
                            .quantity(0)
                            .build();
                    cartItemRepository.save(newItem);
                    return newItem;
                });

        int newQuantity = cartItem.getQuantity() + request.getQuantity();
        if (newQuantity > availableStock) {
            throw new IllegalStateException("Not enough stock available. Only " + availableStock + " items left.");
        }

        cartItem.setQuantity(newQuantity);
        return CartItemResponse.fromCartItem(cartItemRepository.save(cartItem));
    }


    @Transactional
    public CartItemResponse updateCart(Long userId, String sessionId, Long cartItemId, int newQuantity) {
        Cart cart = getOrCreateCart(userId, sessionId);
        CartItem cartItem = getCartItem(cart, cartItemId);

        int availableStock = getAvailableStockFromWarehouse(cartItem.getProductVariant().getId());
        if (newQuantity > availableStock) {
            throw new IllegalStateException("Not enough stock available. Only " + availableStock + " items left.");
        }

        cartItem.setQuantity(newQuantity);
        return CartItemResponse.fromCartItem(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeFromCart(Long userId, String sessionId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId, sessionId);
        CartItem cartItem = getCartItem(cart, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(Long userId, String sessionId) {
        Cart cart = getOrCreateCart(userId, sessionId);
        cartItemRepository.deleteAll(cartItemRepository.findByCart(cart));
    }

    @Transactional
    public Cart getOrCreateCart(Long userId, String sessionId) {
        if (userId != null) {
            // Nếu có userId, tìm cart theo user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            return cartRepository.findByUser(user)
                    .orElseGet(() -> cartRepository.save(Cart.builder()
                            .user(user)
                            .sessionId(null)  // User đã đăng nhập, không cần sessionId
                            .cartItems(new ArrayList<>())
                            .build())
            );
        } else if (sessionId != null) {
            // Nếu không có userId, tìm cart theo sessionId
            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> cartRepository.save(Cart.builder()
                            .user(null)  // Không có user
                            .sessionId(sessionId)
                            .cartItems(new ArrayList<>())
                            .build())
                    );
        } else {
            throw new IllegalArgumentException("Both userId and sessionId are null");
        }
    }

    // Lấy ProductVariant theo ID
    private ProductVariant getProductVariant(Long productVariantId) {
        return productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found"));
    }

    // Lấy CartItem từ giỏ hàng
    private CartItem getCartItem(Cart cart, Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .filter(item -> item.getCart().equals(cart))
                .orElseThrow(() -> new ResourceNotFoundException("CartItem not found or does not belong to the user's cart"));
    }

    // Lấy tổng tồn kho từ warehouse
    private int getAvailableStockFromWarehouse(Long productVariantId) {
        return inventoryRepository.findByProductVariantIdAndWarehouseNotNull(productVariantId)
                .stream()
                .mapToInt(Inventory::getQuantityInStock)
                .sum();
    }

    public TotalCartResponse getTotalCartItems(Long userId, String sessionId) {
        if (userId != null) {
            return TotalCartResponse.builder()
                    .totalCart(cartRepository.countByUserId(userId))
                    .build();
        } else if (sessionId != null) {
            return TotalCartResponse.builder()
                    .totalCart(cartRepository.countBySessionId(sessionId))
                    .build();
        }
        return TotalCartResponse.builder()
                .totalCart(0)
                .build();
    }
}
