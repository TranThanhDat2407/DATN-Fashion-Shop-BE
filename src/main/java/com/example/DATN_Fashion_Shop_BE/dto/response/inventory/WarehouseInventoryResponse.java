package com.example.DATN_Fashion_Shop_BE.dto.response.inventory;
import com.example.DATN_Fashion_Shop_BE.dto.PromotionDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseInventoryResponse extends BaseResponse {
   private Long id;
   private Long warehouseId;
   private Long productVariantId;
   private Integer quantityInStock;

   public static WarehouseInventoryResponse fromInventory(Inventory inventory) {
      return new WarehouseInventoryResponse(
              inventory.getId(),
              inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null,
              inventory.getProductVariant().getId(),
              inventory.getQuantityInStock()
      );
   }
}
