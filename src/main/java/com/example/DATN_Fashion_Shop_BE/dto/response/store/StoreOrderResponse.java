package com.example.DATN_Fashion_Shop_BE.dto.response.store;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.coupon.CouponResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderStatus.OrderStatusResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.payment.PaymentMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.UserResponse;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreOrderResponse extends BaseResponse {
    private Long orderId;
    private Double totalPrice;
    private Double totalAmount;
    private String shippingAddress;
    private Double shippingFee;
    private Double taxAmount;
    private String transactionId;

    private OrderStatusResponse orderStatus;
    private UserResponse user;
    private PaymentMethodResponse paymentMethod;
    private StoreCouponResponse coupon;
    private ShippingMethodResponse shippingMethod;
    private List<StoreOrderDetailResponse> orderDetails;

    public static StoreOrderResponse fromOrder(Order order,String languageCode) {
        StoreOrderResponse response = StoreOrderResponse.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress() != null ? order.getShippingAddress() : null)
                .shippingFee(order.getShippingFee())
                .taxAmount(order.getTaxAmount())
                .transactionId(order.getTransactionId())
                .coupon(order.getCoupon() != null ? StoreCouponResponse.fromCoupon(order.getCoupon()) : null)

                .orderStatus(OrderStatusResponse.fromOrderStatus(order.getOrderStatus()))
                .user(order.getUser() != null ? UserResponse.fromUser(order.getUser()) : null)
                .paymentMethod(order.getPayments().isEmpty() ? null
                        : PaymentMethodResponse.fromPaymentMethod(order.getPayments().get(0).getPaymentMethod()))
                .shippingMethod(order.getShippingMethod() == null ? null
                        : ShippingMethodResponse.fromShippingMethod(order.getShippingMethod()))

                .orderDetails(order.getOrderDetails().stream()
                        .map(item -> StoreOrderDetailResponse.fromOrderDetail(item,languageCode))
                        .collect(Collectors.toList()))
                .build();
        response.setCreatedAt(order.getCreatedAt());
        response.setCreatedBy(order.getCreatedBy());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setUpdatedBy(order.getUpdatedBy());

        return response;
    }
}
