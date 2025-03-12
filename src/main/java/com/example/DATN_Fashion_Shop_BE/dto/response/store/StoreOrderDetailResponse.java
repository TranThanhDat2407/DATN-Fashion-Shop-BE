package com.example.DATN_Fashion_Shop_BE.dto.response.store;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderStatus.OrderStatusResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.payment.PaymentMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.UserResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreOrderDetailResponse extends BaseResponse {
    private String productImage;
    private String productName;
    private String colorName;
    private String sizeName;
    private String colorImage;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;

    public static StoreOrderDetailResponse fromOrderDetail(OrderDetail orderDetail, String languageCode) {
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

        return StoreOrderDetailResponse.builder()
                .productImage(productImage)
                .productName(product.getTranslationByLanguage(languageCode).getName()) // Giả sử Product có thuộc tính name
                .colorName(color != null ? color.getValueName() : "N/A")
                .sizeName(size != null ? size.getValueName() : "N/A")
                .colorImage(color != null ? color.getValueImg() : null) // Nếu có ảnh màu, lấy nó
                .quantity(orderDetail.getQuantity())
                .unitPrice(orderDetail.getUnitPrice())
                .totalPrice(orderDetail.getTotalPrice())
                .build();
    }
}
