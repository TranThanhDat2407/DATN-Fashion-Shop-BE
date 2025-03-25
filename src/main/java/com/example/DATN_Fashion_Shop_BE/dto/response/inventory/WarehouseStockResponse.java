package com.example.DATN_Fashion_Shop_BE.dto.response.inventory;
import com.example.DATN_Fashion_Shop_BE.dto.PromotionDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseStockResponse extends BaseResponse {
   private Long inventoryId;
   private Long ProductId;
   private Long productVariantId;
   private String productImage;
   private String productName;
   private String colorName;
   private String sizeName;
   private String colorImage;
   private Double basePrice;
   private Double salePrice;
   private PromotionDTO promotion;
   private Integer quantityInStock;
   @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
   private LocalDateTime variantUpdateDate;

   public static WarehouseStockResponse fromInventory(Inventory inventory, String languageCode) {
      Product product = inventory.getProductVariant().getProduct();
      ProductVariant variant = inventory.getProductVariant();
      AttributeValue color = variant.getColorValue();
      String productImage = null;
      if (product.getMedias() != null && !product.getMedias().isEmpty()) {
         productImage = product.getMedias().stream()
                 .filter(media -> media.getColorValue() != null && color != null && media.getColorValue().getId().equals(color.getId())) // So sánh bằng ID thay vì equals()
                 .map(ProductMedia::getMediaUrl)
                 .findFirst()
                 .orElse(product.getMedias().get(0).getMediaUrl()); // Nếu không có, lấy ảnh đầu tiên
      }

      WarehouseStockResponse response = WarehouseStockResponse.builder()
              .inventoryId(inventory.getId())
              .ProductId(inventory.getProductVariant().getProduct().getId())
              .productName(inventory.getProductVariant().getProduct().getTranslationByLanguage(languageCode).getName())
              .sizeName(inventory.getProductVariant().getSizeValue().getValueName())
              .productImage(productImage)
              .productVariantId(inventory.getProductVariant().getId())
              .colorName(inventory.getProductVariant().getColorValue().getValueName())
              .colorImage(inventory.getProductVariant().getColorValue().getValueImg())
              .basePrice(inventory.getProductVariant().getProduct().getBasePrice())
              .salePrice(inventory.getProductVariant().getAdjustedPrice())
              .promotion(product.getPromotion() != null ? PromotionDTO.fromPromotion(product.getPromotion()) : null)
              .quantityInStock(inventory.getQuantityInStock())
              .variantUpdateDate(inventory.getProductVariant().getUpdatedAt())
              .build();
      response.setCreatedAt(inventory.getCreatedAt());
      response.setUpdatedAt(inventory.getUpdatedAt());
      response.setCreatedBy(inventory.getCreatedBy());
      response.setUpdatedBy(inventory.getCreatedBy());
      return response;
   }
}
