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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Transactional
    public TotalCartResponse getTotalCartItems(Long userId, String sessionId) {
        Cart cart = getOrCreateCart(userId, sessionId);
        if (userId != null) {
            return TotalCartResponse.builder()
                    .totalCart(cartItemRepository.sumQuantityByCart(cart))
                    .build();
        } else if (sessionId != null) {
//            Cart cart = cartRepository.findBySessionId(sessionId).orElse(null);
            return TotalCartResponse.builder()
                    .totalCart(cartItemRepository.sumQuantityByCart(cart))
                    .build();
        }
        return TotalCartResponse.builder()
                .totalCart(0)
                .build();
    }

    @Transactional
    public void mergeCart(String sessionId, Long userId) {
        if (sessionId == null || userId == null) {
            throw new IllegalArgumentException("SessionId and userId must not be null");
        }

        // Lấy cart theo sessionId (nếu có)
        Optional<Cart> sessionCartOpt = cartRepository.findBySessionId(sessionId);
        if (sessionCartOpt.isEmpty()) {
            return; // Không có gì để merge
        }
        Cart sessionCart = sessionCartOpt.get();

        // Lấy cart theo userId (hoặc tạo mới nếu chưa có)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart userCart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(user)
                        .sessionId(null)
                        .cartItems(new ArrayList<>())
                        .build())
                );

        // Chuyển tất cả cartItem từ sessionCart → userCart
        for (CartItem sessionCartItem : sessionCart.getCartItems()) {
            ProductVariant productVariant = sessionCartItem.getProductVariant();

            // Lấy số lượng tồn kho từ warehouse
            int availableStock = getAvailableStockFromWarehouse(productVariant.getId());
            int sessionQuantity = sessionCartItem.getQuantity();

            // Kiểm tra xem sản phẩm đã có trong giỏ hàng user chưa
            Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProductVariant(userCart, productVariant);

            if (existingItemOpt.isPresent()) {
                // Nếu đã có trong giỏ hàng, cộng số lượng nhưng không vượt quá tồn kho
                CartItem existingItem = existingItemOpt.get();
                int newQuantity = existingItem.getQuantity() + sessionQuantity;

                if (newQuantity > availableStock) {
                    newQuantity = availableStock; // Giới hạn số lượng theo tồn kho
                }

                existingItem.setQuantity(newQuantity);
                cartItemRepository.save(existingItem);
            } else {
                // Nếu chưa có, tạo mới nhưng không vượt quá tồn kho
                int quantityToAdd = Math.min(sessionQuantity, availableStock);

                if (quantityToAdd > 0) {
                    CartItem newItem = CartItem.builder()
                            .cart(userCart)
                            .productVariant(productVariant)
                            .quantity(quantityToAdd)
                            .build();

                    cartItemRepository.save(newItem);
                }
            }
        }
        // Xóa cart item của sessionId sau khi merge
        cartItemRepository.deleteAll(sessionCart.getCartItems());
    }


    @Transactional
    public CartItemResponse staffAddToCart(Long userId, Long storeId, CartRequest request) {
        Cart cart = getOrCreateCartForUser(userId); // Tạo giỏ hàng nếu chưa có
        ProductVariant productVariant = getProductVariant(request.getProductVariantId());

        // Kiểm tra tồn kho tại cửa hàng
        Integer availableStock = inventoryRepository
                .findQuantityInStockByStoreAndVariant(storeId, request.getProductVariantId());

        if (availableStock == null) {
            throw new IllegalStateException("The requested product variant does not exist in the selected store.");
        }

        if (request.getQuantity() > availableStock) {
            throw new IllegalStateException("Not enough stock available for variant " + request.getProductVariantId() + ". Only " + availableStock + " left.");
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
                    return cartItemRepository.save(newItem);
                });


        int newQuantity = cartItem.getQuantity() + request.getQuantity();
        if (newQuantity > availableStock) {
            throw new IllegalStateException("Not enough stock available for variant " + request.getProductVariantId() + ". Only " + availableStock + " left.");
        }

        cartItem.setQuantity(newQuantity);
        return CartItemResponse.fromCartItem(cartItemRepository.save(cartItem));
    }

    @Transactional
    public CartItemResponse staffUpdateCart(Long userId, Long storeId, CartRequest request) {
        Cart cart = getOrCreateCartForUser(userId);

        CartItem cartItem = cartItemRepository.findByCart(cart).stream()
                .filter(item -> item.getProductVariant().getId().equals(request.getProductVariantId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Product not found in cart"));

        int availableStock = inventoryRepository.findQuantityInStockByStoreAndVariant(storeId, request.getProductVariantId());

        if (request.getQuantity() > availableStock) {
            throw new IllegalStateException("Not enough stock available. Only " + availableStock + " items left.");
        }

        cartItem.setQuantity(request.getQuantity());
        return CartItemResponse.fromCartItem(cartItemRepository.save(cartItem));
    }


    private Cart getOrCreateCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(user)
                        .sessionId(null)  // User đã đăng nhập, không cần sessionId
                        .cartItems(new ArrayList<>())
                        .build())
                );
    }
}
