package com.example.DATN_Fashion_Shop_BE.dto.response.order;

import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseMail {
    private Long orderId;
    private String shippingAddress;
    private Double totalPrice;
    private String status;
    private String paymentMethod;
    private String shippingMethod;
    private LocalDateTime create_at;
    private LocalDateTime update_at;
    private List<OrderDetailResponse> orderDetails;

    public static OrderResponseMail fromOrder(Order order, List<OrderDetailResponse> orderDetails) {
        return OrderResponseMail.builder()
                .orderId(order.getId())
                .shippingAddress(order.getShippingAddress())
                .totalPrice(order.getTotalPrice())
                .status(String.valueOf(order.getOrderStatus()))
                .paymentMethod(order.getPayments().toString())
                .shippingMethod(String.valueOf(order.getShippingMethod()))
                .create_at(order.getCreatedAt())
                .update_at(order.getUpdatedAt())
                .orderDetails(orderDetails)
                .build();
    }
}
