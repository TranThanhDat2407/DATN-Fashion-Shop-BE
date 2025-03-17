package com.example.DATN_Fashion_Shop_BE.dto.request.inventory_transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTransferRequest {
    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Store ID is required")
    private Long storeId;

    private String message;

    @NotNull(message = "Transfer items cannot be empty")
    private List<InventoryTransferItemRequest> transferItems;
}
