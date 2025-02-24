package com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail;

import com.example.DATN_Fashion_Shop_BE.dto.response.product.CreateProductResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductTranslationResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductVariantResponse;
import com.example.DATN_Fashion_Shop_BE.model.OrderDetail;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {
    private Long orderDetailId;
    private Long orderId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private ProductTranslationResponse productTranslationResponse;
    private ProductVariantResponse productVariant;
    private String imageUrl; // Hình ảnh sản phẩm

//    public static OrderDetailResponse fromOrderDetail(OrderDetail orderDetail) {
//        // Lấy Product từ OrderDetail
//        Product product = orderDetail.getProduct();
//
//        // Tìm đúng ProductVariant bằng một tiêu chí, ví dụ: SKU hoặc màu sắc, kích thước
//        ProductVariant variant = findMatchingVariant(product, orderDetail);
//
//        // Lấy hình ảnh từ ProductMedia
//        String imageUrl = (product.getMedias() != null && !product.getMedias().isEmpty())
//                ? product.getMedias().get(0).getMediaUrl()
//                : null;
//
//        return OrderDetailResponse.builder()
//                .orderDetailId(orderDetail.getId())
//                .orderId(orderDetail.getOrder().getId())
//                .quantity(orderDetail.getQuantity())
//                .unitPrice(orderDetail.getUnitPrice())
//                .totalPrice(orderDetail.getTotalPrice())
//                .productTranslationResponse(ProductTranslationResponse.fromProductsTranslation(product.getTranslationByLanguage("en")))
//                .productVariant(ProductVariantResponse.fromProductVariant(variant)) // Lấy biến thể chính xác
//                .imageUrl(imageUrl) // Ảnh sản phẩm
//                .build();
//    }
//
//    /**
//     * Tìm `ProductVariant` phù hợp với `OrderDetail`
//     */
//    private static ProductVariant findMatchingVariant(Product product, OrderDetail orderDetail) {
//        if (product.getVariants() == null || product.getVariants().isEmpty()) {
//            return null; // Không có biến thể nào
//        }
//
//        // Giả sử `OrderDetail` có `skuCode`, tìm biến thể phù hợp
//        return product.getVariants().stream()
//                .filter(variant -> variant.getSizeValue().equals(orderDetail.getProduct().getVariants()))
//                .findFirst()
//                .orElse(product.getVariants().get(0)); // Nếu không tìm thấy, lấy biến thể đầu tiên
//    }

    public static OrderDetailResponse fromOrderDetail(OrderDetail orderDetail) {
        Product product = orderDetail.getProduct();

        // Tìm một ProductVariant phù hợp nếu có
        ProductVariant variant = product.getVariants().isEmpty() ? null : product.getVariants().get(0);

        // Lấy hình ảnh từ ProductMedia (lấy ảnh đầu tiên nếu có)
        String imageUrl = (product.getMedias() != null && !product.getMedias().isEmpty())
                ? product.getMedias().get(0).getMediaUrl()
                : null;

        return OrderDetailResponse.builder()
                .orderDetailId(orderDetail.getId())
                .orderId(orderDetail.getOrder().getId())
                .quantity(orderDetail.getQuantity())
                .unitPrice(orderDetail.getUnitPrice())
                .totalPrice(orderDetail.getTotalPrice())
                .productTranslationResponse(ProductTranslationResponse.fromProductsTranslation(
                        product.getTranslationByLanguage("en")))
                .productVariant(ProductVariantResponse.fromProductVariant(variant)) // Biến thể lấy từ Product
                .imageUrl(imageUrl) // Ảnh sản phẩm
                .build();


    }

}
