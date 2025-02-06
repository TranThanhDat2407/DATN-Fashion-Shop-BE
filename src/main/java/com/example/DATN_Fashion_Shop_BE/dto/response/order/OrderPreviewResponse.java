package com.example.DATN_Fashion_Shop_BE.dto.response.order;

import com.example.DATN_Fashion_Shop_BE.model.Order;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderPreviewResponse {
    private double totalAmount;
    private double discount;
    private double taxAmount;
    private double shippingFee;
    private double finalAmount;


    public static OrderPreviewResponse fromOrder(Order order) {
        OrderPreviewResponse response = new OrderPreviewResponse();
        response.setTotalAmount(order.getTotalPrice());
        response.setDiscount(order.getCoupon().getDiscountValue());
        response.setTaxAmount(order.getTaxAmount());
        response.setShippingFee(order.getShippingFee());
        response.setFinalAmount(order.getTotalAmount());
        return response;
    }
}
