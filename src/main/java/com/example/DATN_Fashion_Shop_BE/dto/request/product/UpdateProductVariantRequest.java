package com.example.DATN_Fashion_Shop_BE.dto.request.product;

import lombok.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductVariantRequest {
    private Long colorValueId;  // Nếu cập nhật màu sắc
    private Long sizeValueId;   // Nếu cập nhật kích thước
    private Double salePrice;   // Giá mới cho biến thể
}
