package com.example.DATN_Fashion_Shop_BE.dto.response.order;

import com.example.DATN_Fashion_Shop_BE.dto.response.cart.CartItemResponse;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private Long productVariantId;
    private Integer quantity;

    public static OrderItemDTO fromCartItem(CartItemResponse cartItem) {
        return OrderItemDTO.builder()
                .productVariantId(cartItem.getProductVariantId())
                .quantity(cartItem.getQuantity())
                .build();
    }

}
