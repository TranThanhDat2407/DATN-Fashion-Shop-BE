package com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail;

import com.example.DATN_Fashion_Shop_BE.dto.response.payment.PaymentMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductVariantResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailAdminResponse {
    private Long orderDetailId;
    private Long orderId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private ProductVariantResponse productVariant;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Double couponPrice;
    private Double tax;
    private Double shippingFee;
    private Double totalAmount;
    private String imageUrl;


    public static OrderDetailAdminResponse fromOrderDetailAdmin(OrderDetail orderDetail) {

        Order order = orderDetail.getOrder();
        ProductVariant variant = orderDetail.getProductVariant();
        Product product = variant.getProduct();

//        Product product = orderDetail.getProductVariant().getProduct();

        AttributeValue color = variant.getColorValue();
        String productImage = null;
        if (product.getMedias() != null && !product.getMedias().isEmpty()) {
            productImage = product.getMedias().stream()
                    .filter(media -> media.getColorValue() != null && color != null && media.getColorValue().getId().equals(color.getId())) // So sánh bằng ID thay vì equals()
                    .map(ProductMedia::getMediaUrl)
                    .findFirst()
                    .orElse(product.getMedias().get(0).getMediaUrl());
        }

        List<UserAddressResponse> userAddressResponses = order.getUser().getUserAddresses().stream()
                .map(UserAddressResponse::fromUserAddress)
                .collect(Collectors.toList());

        UserAddressResponse defaultAddress = (userAddressResponses != null && !userAddressResponses.isEmpty())
                ? userAddressResponses.stream()
                .filter(UserAddressResponse::getIsDefault)
                .findFirst()
                .orElse(userAddressResponses.get(0))
                : null;

        String customerName = (defaultAddress != null) ? defaultAddress.getFirstName() + " " + defaultAddress.getLastName() : null;
        String customerPhone = (defaultAddress != null) ? defaultAddress.getPhone() : null;


        // 🛠️ Xử lý phương thức thanh toán
        List<PaymentMethodResponse> paymentMethods = (order.getPayments() != null)
                ? order.getPayments().stream()
                .map(payment -> PaymentMethodResponse.fromPaymentMethod(payment.getPaymentMethod()))
                .collect(Collectors.toList())
                : List.of();

        String paymentMethodNames = (!paymentMethods.isEmpty())
                ? paymentMethods.stream().map(PaymentMethodResponse::getMethodName).collect(Collectors.joining(", "))
                : "Thanh toán khi nhận hàng";

        // 🛠️ Xử lý trạng thái thanh toán
        String paymentStatus = order.getPayments().stream()
                .map(Payment::getStatus)
                .findFirst()
                .orElse("Chưa thanh toán");

        return OrderDetailAdminResponse.builder()
                .orderDetailId(orderDetail.getId())
                .orderId(order.getId())
                .quantity(orderDetail.getQuantity())
                .unitPrice(orderDetail.getUnitPrice())
                .totalPrice(orderDetail.getTotalPrice())
                .productVariant(ProductVariantResponse.fromProductVariant(orderDetail.getProductVariant()))
                .customerName(customerName)
                .customerPhone(customerPhone)
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(paymentMethodNames)
                .paymentStatus(paymentStatus)
                .orderStatus(order.getOrderStatus().getStatusName())
                .createTime(order.getCreatedAt())
                .updateTime(order.getUpdatedAt())
                .tax(order.getTaxAmount())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalPrice())
                .imageUrl(productImage)
                .build();
    }
}

