package com.example.DATN_Fashion_Shop_BE.dto.request.product;

import lombok.*;

import java.util.List;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductVariantRequest {
    private Long productId;
    private Long colorValueId;  // Chọn màu cụ thể
    private Long sizeValueId;   // Chọn kích thước cụ thể
    private Double salePrice;
}
