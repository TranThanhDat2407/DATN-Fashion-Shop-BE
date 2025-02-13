package com.example.DATN_Fashion_Shop_BE.dto.response.cart;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductMediaResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductTranslationResponse;
import com.example.DATN_Fashion_Shop_BE.model.CartItem;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productVariantId;
    private Integer quantity;

    public static CartItemResponse fromCartItem(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productVariantId(cartItem.getProductVariant().getId())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
