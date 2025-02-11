package com.example.DATN_Fashion_Shop_BE.dto.request.cart;

import com.example.DATN_Fashion_Shop_BE.dto.BannerTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartRequest {
    private Long productVariantId;
    private int quantity;
}
