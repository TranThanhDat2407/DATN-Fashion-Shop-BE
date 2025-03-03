package com.example.DATN_Fashion_Shop_BE.dto.response.inventory;

import com.example.DATN_Fashion_Shop_BE.dto.response.audit.AuditResponse;
import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import com.example.DATN_Fashion_Shop_BE.model.InventoryTransfer;
import com.example.DATN_Fashion_Shop_BE.model.TransferStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryAudResponse extends AuditResponse {
    private Long id;
    private Long warehouseId;
    private Long storeId;
    private Long productVariantId;
    private Integer quantity;

    public static InventoryAudResponse fromInventory(Inventory inventory,
                                                     DefaultRevisionEntity revEntity,
                                                     RevisionType revType) {
        return InventoryAudResponse.builder()
                .id(inventory.getId())
                .warehouseId(inventory.getWarehouse().getId())
                .storeId(inventory.getStore().getId())
                .productVariantId(inventory.getProductVariant().getId())
                .quantity(inventory.getQuantityInStock())
                .revision(revEntity.getId())
                .revisionType(revType.name())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .createdBy(inventory.getCreatedBy())
                .updatedBy(inventory.getUpdatedBy())
                .build();
    }
}
