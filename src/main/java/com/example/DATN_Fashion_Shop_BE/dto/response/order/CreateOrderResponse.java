package com.example.DATN_Fashion_Shop_BE.dto.response.order;

import com.example.DATN_Fashion_Shop_BE.dto.response.coupon.CouponResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.payment.PaymentMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.UserResponse;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import com.example.DATN_Fashion_Shop_BE.model.OrderDetail;
import com.example.DATN_Fashion_Shop_BE.model.OrderStatus;
import com.example.DATN_Fashion_Shop_BE.model.Payment;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse {
    private Long orderId;
    private Long userId;
    private Long couponId;
    private String shippingMethodName;
    private String shippingAddress;
    private String paymentMethodName; // Thêm thông tin chi tiết Payment Method
    private String orderStatusName;

    public static CreateOrderResponse fromOrder(Order order) {
        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .couponId(order.getCoupon() != null ? order.getCoupon().getId() : null)
                .shippingMethodName(order.getShippingMethod() != null ? order.getShippingMethod().getMethodName() : null)
                .shippingAddress(order.getShippingAddress())
                .paymentMethodName(
                        order.getPayments().stream()
                                .findFirst()
                                .map(payment -> payment.getPaymentMethod().getMethodName())
                                .orElse(null)
                )
                .orderStatusName(order.getOrderStatus() != null ? order.getOrderStatus().getStatusName() : null)
                .build();
    }

}
