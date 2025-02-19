package com.example.DATN_Fashion_Shop_BE.dto.response.inventory_transfer;

import com.example.DATN_Fashion_Shop_BE.model.InventoryTransfer;
import com.example.DATN_Fashion_Shop_BE.model.TransferStatus;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTransferResponse {
    private Long id;
    private Long warehouseId;
    private Long storeId;
    private Long productVariantId;
    private Integer quantity;
    private TransferStatus status;
    private Boolean isReturn;

    public static InventoryTransferResponse fromInventoryTransfer(InventoryTransfer inventoryTransfer) {
        return InventoryTransferResponse.builder()
                .id(inventoryTransfer.getId())
                .warehouseId(inventoryTransfer.getWarehouse().getId())
                .storeId(inventoryTransfer.getStore().getId())
                .productVariantId(inventoryTransfer.getProductVariant().getId())
                .quantity(inventoryTransfer.getQuantity())
                .status(inventoryTransfer.getStatus())
                .isReturn(inventoryTransfer.getIsReturn())
                .build();
    }
}
