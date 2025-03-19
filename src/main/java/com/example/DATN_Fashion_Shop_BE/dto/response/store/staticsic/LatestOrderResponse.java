package com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreOrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.UserResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LatestOrderResponse extends BaseResponse {
    private Long orderId;
    private UserResponse user;
    private String productImage;
    private String productName;
    private String colorName;
    private String sizeName;
    private String colorImage;
    private String status;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;

    public static LatestOrderResponse fromOrderDetail(OrderDetail orderDetail, String languageCode) {
        ProductVariant variant = orderDetail.getProductVariant();
        Product product = variant.getProduct();
        AttributeValue color = variant.getColorValue();
        AttributeValue size = variant.getSizeValue();

        // Lấy ảnh sản phẩm theo màu sắc
        String productImage = null;
        if (product.getMedias() != null && !product.getMedias().isEmpty()) {
            productImage = product.getMedias().stream()
                    .filter(media -> media.getColorValue() != null && color != null && media.getColorValue().getId().equals(color.getId()))
                    .map(ProductMedia::getMediaUrl)
                    .findFirst()
                    .orElse(product.getMedias().get(0).getMediaUrl()); // Nếu không có ảnh theo màu, lấy ảnh đầu tiên
        }

        LatestOrderResponse response = LatestOrderResponse.builder()
                .orderId(orderDetail.getOrder().getId())
                .user(orderDetail.getOrder().getUser() != null ?
                        UserResponse.fromUser(orderDetail.getOrder().getUser()) : null)
                .productImage(productImage)
                .productName(product.getTranslationByLanguage(languageCode).getName()) // Giả sử Product có thuộc tính name
                .colorName(color != null ? color.getValueName() : "N/A")
                .sizeName(size != null ? size.getValueName() : "N/A")
                .colorImage(color != null ? color.getValueImg() : null)
                .status(orderDetail.getOrder().getOrderStatus().getStatusName())
                .quantity(orderDetail.getQuantity())
                .unitPrice(orderDetail.getUnitPrice())
                .totalPrice(orderDetail.getTotalPrice())
                .build();
        response.setCreatedAt(orderDetail.getCreatedAt());
        response.setUpdatedAt(orderDetail.getUpdatedAt());
        response.setUpdatedBy(orderDetail.getUpdatedBy());
        response.setCreatedBy(orderDetail.getCreatedBy());
        return response;
    }
}
