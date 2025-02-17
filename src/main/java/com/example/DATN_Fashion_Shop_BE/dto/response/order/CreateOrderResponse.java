package com.example.DATN_Fashion_Shop_BE.dto.response.order;

import com.example.DATN_Fashion_Shop_BE.dto.response.coupon.CouponResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.payment.PaymentMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod.ShippingMethodResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.UserResponse;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import com.example.DATN_Fashion_Shop_BE.model.OrderStatus;
import com.example.DATN_Fashion_Shop_BE.model.Payment;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse {
    private Long orderId;
    private UserResponse user; // Thêm thông tin chi tiết User
    private CouponResponse coupon; // Thêm thông tin chi tiết Coupon
    private ShippingMethodResponse shippingMethod; // Thêm thông tin chi tiết Shipping Method
    private String shippingAddress;
    private PaymentMethodResponse paymentMethod; // Thêm thông tin chi tiết Payment Method
    private OrderStatus orderStatus;



    public static CreateOrderResponse fromOrder(Order order) {
        UserResponse userResponse = UserResponse.fromUser(order.getUser());
        CouponResponse couponResponse = (order.getCoupon() != null)
                ? CouponResponse.fromCoupon(order.getCoupon())
                : null;
        ShippingMethodResponse shippingMethodResponse = ShippingMethodResponse.fromShippingMethod(order.getShippingMethod());
        OrderStatus orderStatusResponse = OrderStatus.fromOrderStatus(order.getOrderStatus());

        // Lấy Payment tương ứng với Order
        Payment payment = order.getPayments().stream().findFirst().orElse(null);
        PaymentMethodResponse paymentMethodResponse = (payment != null)
                ? PaymentMethodResponse.fromPaymentMethod(payment.getPaymentMethod())
                : null;

        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .user(userResponse)
                .coupon(couponResponse)
                .shippingMethod(shippingMethodResponse)
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(paymentMethodResponse)  // Trả về thông tin phương thức thanh toán
                .orderStatus(orderStatusResponse)  // Lấy trạng thái từ OrderStatus
                .build();
    }

}
