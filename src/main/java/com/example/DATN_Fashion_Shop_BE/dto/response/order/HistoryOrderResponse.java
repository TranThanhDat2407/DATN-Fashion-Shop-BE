package com.example.DATN_Fashion_Shop_BE.dto.response.order;

import com.example.DATN_Fashion_Shop_BE.dto.request.order.OrderRequest;
import com.example.DATN_Fashion_Shop_BE.model.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HistoryOrderResponse {
    private Long orderId;
    private Double totalPrice;
    private Double totalAmount;
    private String shippingAddress;
    private String orderStatus;
    private String shippingMethodName;
    private String paymentMethodName;
    private Double shippingFee;
    private Double taxAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static HistoryOrderResponse fromHistoryOrder(Order order) {
        return HistoryOrderResponse.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().getStatusName())
                .shippingMethodName(order.getShippingMethod() != null ? order.getShippingMethod().getMethodName() : null)
                .shippingAddress(order.getShippingAddress())
                .paymentMethodName(
                order.getPayments().stream()
                        .findFirst()
                        .map(payment -> payment.getPaymentMethod().getMethodName())
                        .orElse(null)
        )
                .shippingFee(order.getShippingFee())
                .taxAmount(order.getTaxAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
