package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.wishlist.TotalWishlistResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.wishlist.WishlistItemResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final FileStorageService fileStorageService;
    private final LocalizationUtils localizationUtils;

    public List<WishlistItemResponse> getWishlistByUser(Long userId) {
        List<WishListItem> wishListItems = wishlistItemRepository.findByWishlistUserId(userId);

        return wishListItems.stream()
                .map(WishlistItemResponse::fromWishlistItem)
                .collect(Collectors.toList());
    }

    public WishlistItemResponse toggleWishlistByProductAndColor(Long userId, Long productId, Long colorId) {
        // Lấy danh sách product variants của product có colorId đó
        List<ProductVariant> variants = productVariantRepository
                .findByProductIdAndColorValueId(productId, colorId);

        if (variants.isEmpty()) {
            throw new ResourceNotFoundException("No ProductVariant found for the given productId and colorId");
        }

        // Chọn variant đầu tiên (hoặc có thể thay đổi logic chọn variant khác)
        ProductVariant variant = variants.get(0);

        // Kiểm tra xem sản phẩm đã tồn tại trong wishlist chưa
        Optional<WishListItem> existingItem = wishlistItemRepository
                .findByWishlistUserIdAndProductVariantProductIdAndProductVariantColorValueId(userId, productId, colorId);

        if (existingItem.isPresent()) {
            // Nếu đã tồn tại, xóa khỏi wishlist
            wishlistItemRepository.delete(existingItem.get());
            return WishlistItemResponse.fromWishlistItem(existingItem.get());
        }

        // Nếu chưa tồn tại, thêm mới
        WishList wishList = getOrCreateWishlist(userId);

        WishListItem wishListItem = WishListItem.builder()
                .wishlist(wishList)
                .productVariant(variant)
                .build();

        wishListItem = wishlistItemRepository.save(wishListItem);

        return WishlistItemResponse.fromWishlistItem(wishListItem);
    }

    public WishlistItemResponse toggleWishlist(Long userId, Long variantId) {
        // Lấy productVariant theo ID
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant not found"));

        Long productId = variant.getProduct().getId();
        Long colorId = variant.getColorValue().getId();

        // Tìm wishlist item theo user, productId và colorId
        Optional<WishListItem> existingItem = wishlistItemRepository
                .findByWishlistUserIdAndProductVariantProductIdAndProductVariantColorValueId(
                        userId, productId, colorId
                );

        if (existingItem.isPresent()) {
            // Nếu đã tồn tại, xóa khỏi wishlist
            wishlistItemRepository.delete(existingItem.get());

            return WishlistItemResponse.fromWishlistItem(existingItem.get());
        }


        WishList wishList = getOrCreateWishlist(userId);

        WishListItem wishListItem = WishListItem.builder()
                .wishlist(wishList)
                .productVariant(variant)
                .build();

        wishListItem = wishlistItemRepository.save(wishListItem);

        return WishlistItemResponse.fromWishlistItem(wishListItem);
    }

    public TotalWishlistResponse getWishlistCount(Long userId) {
        return TotalWishlistResponse.builder()
                .totalWishlist(wishlistItemRepository.countByWishlistUserId(userId))
                .build()
                ;
    }

    private WishList getOrCreateWishlist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return wishlistRepository.findByUser(user)
                .orElseGet(()
                        -> wishlistRepository.save(WishList.builder()
                        .user(user)
                        .wishListItems(new ArrayList<>())
                        .build()
                ));
    }

    public boolean isProductInWishlist(Long userId, Long productId, Long colorId) {
        return wishlistItemRepository.existsByWishlistUserIdAndProductVariantProductIdAndProductVariantColorValueId(userId, productId, colorId);
    }

}
