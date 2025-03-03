package com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail;

import com.example.DATN_Fashion_Shop_BE.dto.response.payment.PaymentMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.CreateProductResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductTranslationResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductVariantResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

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
    private ProductVariantResponse productVariant;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String paymentMethod;
    private Double tax;
    private Double shippingFee;
    private Double grandTotal;
    private String imageUrl;

    public static OrderDetailResponse fromOrderDetail(OrderDetail orderDetail, List<UserAddressResponse> userAddressResponses) {
        Product product = orderDetail.getProductVariant().getProduct();
        Order order = orderDetail.getOrder(); // Lấy Order từ OrderDetail

        // Lấy ảnh đầu tiên của sản phẩm nếu có
        String imageUrl = (product.getMedias() != null && !product.getMedias().isEmpty())
                ? product.getMedias().get(0).getMediaUrl()
                : null;

        UserAddressResponse defaultAddress = (userAddressResponses != null && !userAddressResponses.isEmpty())
                ? userAddressResponses.stream()
                .filter(UserAddressResponse::getIsDefault)
                .findFirst()
                .orElse(userAddressResponses.get(0))
                : null;


        List<PaymentMethodResponse> paymentMethods = (order.getPayments() != null)
                ? order.getPayments().stream()
                .map(payment -> PaymentMethodResponse.fromPaymentMethod(payment.getPaymentMethod()))
                .collect(Collectors.toList())
                : List.of();


        String paymentMethodNames = paymentMethods.isEmpty() ? "N/A"
                : paymentMethods.stream().map(PaymentMethodResponse::getMethodName)
                .collect(Collectors.joining(", "));


        return OrderDetailResponse.builder()
                .orderDetailId(orderDetail.getId())
                .orderId(order.getId())
                .quantity(orderDetail.getQuantity())
                .unitPrice(orderDetail.getUnitPrice())
                .totalPrice(orderDetail.getTotalPrice())
                .imageUrl(imageUrl)
                .productVariant(ProductVariantResponse.fromProductVariant(orderDetail.getProductVariant()))
                .recipientName(defaultAddress != null ? defaultAddress.getFirstName() + " " + defaultAddress.getLastName() : null)
                .recipientPhone(defaultAddress != null ? defaultAddress.getPhone() : null)
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(paymentMethodNames)
                .tax(order.getTaxAmount())
                .shippingFee(order.getShippingFee())
                .grandTotal(order.getTotalPrice())
                .build();
    }

}
