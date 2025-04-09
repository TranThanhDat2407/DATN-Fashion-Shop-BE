package com.example.DATN_Fashion_Shop_BE.dto.response.store;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.CreateOrderResponse;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StorePaymentResponse extends BaseResponse {

    private Long orderId;
    private Long userId; // Thêm thông tin chi tiết User
    private String customerName;
    private Long couponId; // Thêm thông tin chi tiết Coupon
    private String shippingMethodName; // Thêm thông tin chi tiết Shipping Method
    private String shippingAddress;
    private Double tax_amount;
    private Double totalPrice;
    private String paymentMethodName; // Thêm thông tin chi tiết Payment Method
    private String orderStatusName;
    private String payUrl;

    public static StorePaymentResponse fromOrder(Order order) {
        StorePaymentResponse response = StorePaymentResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .customerName(order.getUser() != null ? order.getUser().getFirstName()
                        + " " +order.getUser().getLastName() : null)
                .couponId(order.getCoupon() != null ? order.getCoupon().getId() : null)
                .shippingMethodName(order.getShippingMethod() != null ? order.getShippingMethod().getMethodName() : null)
                .shippingAddress(order.getShippingAddress())
                .totalPrice(order.getTotalPrice())
                .tax_amount(order.getTaxAmount())
                .paymentMethodName(
                        (order.getPayments() != null && !order.getPayments().isEmpty()) ?
                                order.getPayments().getFirst().getPaymentMethod().getMethodName() : null
                )
                .orderStatusName(order.getOrderStatus() != null ? order.getOrderStatus().getStatusName() : null)
                .build();

        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setCreatedBy(order.getCreatedBy());
        response.setUpdatedBy(order.getUpdatedBy());

        return response;
    }
}
