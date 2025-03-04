package com.example.DATN_Fashion_Shop_BE.dto.response.inventory;

import com.example.DATN_Fashion_Shop_BE.dto.response.audit.AuditResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryAudResponse extends AuditResponse {
    private Long id;
    private Long storeId;
    private Long productVariantId;
    private String colorImage;
    private String colorName;
    private String size;
    private String productName;
    private String productImage;
    private Integer quantity;
    private Integer deltaQuantity;

    public static InventoryAudResponse fromInventory(Inventory inventory,
                                                     DefaultRevisionEntity revEntity,
                                                     RevisionType revType,
                                                     Integer delta,
                                                     String languageCode
                                                     ) {

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

        return InventoryAudResponse.builder()
                .id(inventory.getId())
                .storeId(inventory.getStore().getId())
                .productVariantId(inventory.getProductVariant().getId())
                .quantity(inventory.getQuantityInStock())
                .deltaQuantity(delta)
                .productName(inventory.getProductVariant().getProduct()
                        .getTranslationByLanguage(languageCode).getName())
                .productImage(productImage)
                .colorImage(inventory.getProductVariant().getColorValue().getValueImg())
                .colorName(inventory.getProductVariant().getColorValue().getValueName())
                .size(inventory.getProductVariant().getSizeValue().getValueName())
                .revision(revEntity.getId())
                .revisionType(revType.name())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .createdBy(inventory.getCreatedBy())
                .updatedBy(inventory.getUpdatedBy())
                .build();
    }
}
