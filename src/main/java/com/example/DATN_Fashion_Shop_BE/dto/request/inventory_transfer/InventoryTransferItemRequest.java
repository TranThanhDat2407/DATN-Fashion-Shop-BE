package com.example.DATN_Fashion_Shop_BE.dto.request.inventory_transfer;

import com.example.DATN_Fashion_Shop_BE.model.TransferStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTransferItemRequest {
    @NotNull(message = "Product variant ID is required")
    private Long productVariantId;

    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
}
