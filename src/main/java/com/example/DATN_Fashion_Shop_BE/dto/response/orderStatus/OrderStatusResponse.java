package com.example.DATN_Fashion_Shop_BE.dto.response.orderStatus;

import com.example.DATN_Fashion_Shop_BE.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusResponse {

    private Long id;
    private String statusName;

    public static OrderStatusResponse fromOrderStatus(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return null;
        }
        return OrderStatusResponse.builder()
                .id(orderStatus.getId())
                .statusName(orderStatus.getStatusName())
                .build();
    }
}
