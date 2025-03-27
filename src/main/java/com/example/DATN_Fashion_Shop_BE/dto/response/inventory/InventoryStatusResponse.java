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
public class InventoryStatusResponse {
   private Long productVariantId;
   private String productName;
   private String productImage;
   private String colorValue;
   private String colorImage;
   private String sizeValue;
   private Integer quantityInStock;
   private String storeName;
   private Integer daysUnsold;
}
