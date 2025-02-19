package com.example.DATN_Fashion_Shop_BE.dto.request.inventory_transfer;

import com.example.DATN_Fashion_Shop_BE.model.TransferStatus;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTransferCreateRequest {
    private Long warehouseId;
    private Long storeId;
    private Long productVariantId;
    private Integer quantity;
}
