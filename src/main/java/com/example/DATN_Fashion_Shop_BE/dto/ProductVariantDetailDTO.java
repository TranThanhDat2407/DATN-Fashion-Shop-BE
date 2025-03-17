package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductMedia;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDetailDTO {
    private Long id;
    private Long productId;
    private String variantImage;
    private String name;
    private Long colorId;
    private String color;
    private Long sizeId;
    private String size;
    private Double basePrice;
    private Double salePrice;
    private boolean isInWishlist;

    public static ProductVariantDetailDTO fromProductVariant(ProductVariant productVariant, String langCode){
        Product product = productVariant.getProduct();
        Double salePrice = productVariant.getAdjustedPrice(); // Lấy giá salePrice từ ProductVariant

        // Tính toán khuyến mãi dựa trên salePrice
//        if (product.getPromotion() != null && product.getPromotion().getIsActive()) {
//            Promotion promotion = product.getPromotion();
//
//            if (promotion.getDiscountPercentage() != null) {
//                salePrice *= (1 - promotion.getDiscountPercentage() / 100); // Áp dụng giảm giá theo %
//            }
//            // Đảm bảo salePrice không âm
//            salePrice = Math.max(salePrice, 0);
//        }

        String variantImage = product.getMedias().stream()
                .filter(media -> "IMAGE".equals(media.getMediaType())
                        && media.getColorValue() != null
                        && media.getColorValue().getId().equals(productVariant.getColorValue().getId()))
                .map(ProductMedia::getMediaUrl)
                .findFirst()
                .orElse(null);

        return ProductVariantDetailDTO.builder()
                .id(productVariant.getId())
                .productId(productVariant.getProduct().getId())
                .name(product.getTranslationByLanguage(langCode).getName())
                .variantImage(variantImage)
                .colorId(productVariant.getColorValue().getId())
                .color(productVariant.getColorValue().getValueName())
                .sizeId(productVariant.getSizeValue().getId())
                .size(productVariant.getSizeValue().getValueName())
                .basePrice(product.getBasePrice())
                .salePrice(salePrice)
                .build();
    }

    public static ProductVariantDetailDTO fromProductVariantAndWishList(ProductVariant productVariant, String langCode, boolean isInWishlist) {
        Product product = productVariant.getProduct();
        Double salePrice = productVariant.getAdjustedPrice(); // Lấy giá salePrice từ ProductVariant

        // Tính toán khuyến mãi dựa trên salePrice
//        if (product.getPromotion() != null && product.getPromotion().getIsActive()) {
//            Promotion promotion = product.getPromotion();
//
//            if (promotion.getDiscountPercentage() != null) {
//                salePrice *= (1 - promotion.getDiscountPercentage() / 100); // Áp dụng giảm giá theo %
//            }
//            // Đảm bảo salePrice không âm
//            salePrice = Math.max(salePrice, 0);
//        }

        String variantImage = product.getMedias().stream()
                .filter(media -> "IMAGE".equals(media.getMediaType())
                        && media.getColorValue() != null
                        && media.getColorValue().getId().equals(productVariant.getColorValue().getId()))
                .map(ProductMedia::getMediaUrl)
                .findFirst()
                .orElse(null);

        return ProductVariantDetailDTO.builder()
                .id(productVariant.getId())
                .name(product.getTranslationByLanguage(langCode).getName())
                .variantImage(variantImage)
                .colorId(productVariant.getColorValue().getId())
                .color(productVariant.getColorValue().getValueName())
                .size(productVariant.getSizeValue().getValueName())
                .basePrice(product.getBasePrice())
                .salePrice(salePrice)
                .isInWishlist(isInWishlist) // Set giá trị
                .build();
    }

}
