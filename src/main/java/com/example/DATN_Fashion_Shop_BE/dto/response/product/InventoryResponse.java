package com.example.DATN_Fashion_Shop_BE.dto.response.product;
import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponse {
    private Long productVariantId;
    private String colorName;
    private String sizeName;
    private int quantityInStock;
}
