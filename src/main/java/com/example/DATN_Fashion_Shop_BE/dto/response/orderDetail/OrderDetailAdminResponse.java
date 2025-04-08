package com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail;

import com.example.DATN_Fashion_Shop_BE.dto.response.payment.PaymentMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductVariantResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.userAddressResponse.UserAddressResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime updateTime;
    private Double couponPrice;
    private String storeName;
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
        User user = order.getUser();
        List<UserAddressResponse> userAddressResponses = (user != null && user.getUserAddresses() != null)
                ? user.getUserAddresses().stream()
                .map(UserAddressResponse::fromUserAddress)
                .collect(Collectors.toList())
                : List.of();

        UserAddressResponse defaultAddress = (!userAddressResponses.isEmpty())
                ? userAddressResponses.stream()
                .filter(UserAddressResponse::getIsDefault)
                .findFirst()
                .orElse(userAddressResponses.get(0))
                : null;


        String customerName = (defaultAddress != null)
                ? defaultAddress.getFirstName() + " " + defaultAddress.getLastName()
                : "Guest";
        String customerPhone = (defaultAddress != null)
                ? defaultAddress.getPhone()
                : "N/A";


        // 🛠️ Xử lý phương thức thanh toán
        List<PaymentMethodResponse> paymentMethods = (order.getPayments() != null)
                ? order.getPayments().stream()
                .map(payment -> PaymentMethodResponse.fromPaymentMethod(payment.getPaymentMethod()))
                .collect(Collectors.toList())
                : List.of();

        String paymentMethodNames = (!paymentMethods.isEmpty())
                ? paymentMethods.stream().map(PaymentMethodResponse::getMethodName).collect(Collectors.joining(", "))
                : "Thanh toán khi nhận hàng";

        String[] paymentMethodsArray = paymentMethodNames.split(", ");
        Set<String> uniquePaymentMethods = new HashSet<>(Arrays.asList(paymentMethodsArray));
        paymentMethodNames = String.join(", ", uniquePaymentMethods);

        String paymentStatus = (order.getPayments() != null && !order.getPayments().isEmpty())
                ? order.getPayments().stream()
                .map(Payment::getStatus)
                .distinct() // Đảm bảo không có trạng thái trùng lặp
                .findFirst()
                .orElse("Chưa thanh toán")
                : "Chưa thanh toán";

        return OrderDetailAdminResponse.builder()
                .orderDetailId(orderDetail.getId())
                .orderId(order.getId())
                .quantity(orderDetail.getQuantity())
                .unitPrice(orderDetail.getUnitPrice())
                .totalPrice(orderDetail.getTotalPrice())
                .productVariant(ProductVariantResponse.fromProductVariant(orderDetail.getProductVariant()))
                .customerName(customerName)
                .customerPhone(customerPhone)
                .shippingAddress(order.getShippingAddress() != null ? order.getShippingAddress() : "Không có địa chỉ")
                .paymentMethod(paymentMethodNames)
                .paymentStatus(paymentStatus)
                .orderStatus(order.getOrderStatus().getStatusName())
                .createTime(order.getCreatedAt())
                .updateTime(order.getUpdatedAt())
                .tax(order.getTaxAmount())
                .storeName(order.getStore() != null ? order.getStore().getName() : null)
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalPrice())
                .imageUrl(productImage)
                .build();
    }
}

