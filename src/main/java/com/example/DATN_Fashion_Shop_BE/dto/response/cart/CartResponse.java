package com.example.DATN_Fashion_Shop_BE.dto.response.cart;
import com.example.DATN_Fashion_Shop_BE.model.Cart;
import com.example.DATN_Fashion_Shop_BE.model.CartItem;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long id;
    private Long userId;
    private String sessionId;
    private List<CartItemResponse> cartItems;
    private Double totalPrice;

    public static CartResponse fromCart(Cart cart) {
        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                .map(CartItemResponse::fromCartItem)
                .collect(Collectors.toList());

        Double totalPrice = cart.getCartItems().stream()
                .mapToDouble(cartItem -> cartItem.getProductVariant().getAdjustedPrice() * cartItem.getQuantity())
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .sessionId(cart.getSessionId())
                .cartItems(cartItemResponses)
                .totalPrice(totalPrice)
                .build();
    }
}
