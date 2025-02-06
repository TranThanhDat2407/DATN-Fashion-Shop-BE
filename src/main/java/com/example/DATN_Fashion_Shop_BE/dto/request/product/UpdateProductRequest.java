package com.example.DATN_Fashion_Shop_BE.dto.request.product;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {
    private String status;
    private Double basePrice;
    private Boolean isActive;
    private List<CreateProductTranslationRequest> translations;
}
