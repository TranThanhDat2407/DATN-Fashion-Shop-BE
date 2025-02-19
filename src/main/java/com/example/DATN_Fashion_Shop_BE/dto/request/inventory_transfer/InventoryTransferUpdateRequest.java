package com.example.DATN_Fashion_Shop_BE.dto.request.inventory_transfer;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTransferUpdateRequest {
    private Long id;
    private Long warehouseId;
    private Long storeId;
    private Long productVariantId;
    private Integer quantity;
    private Boolean isReturn;
}
