package com.example.DATN_Fashion_Shop_BE.dto.response.store;
import com.example.DATN_Fashion_Shop_BE.model.Store;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreInventoryResponse {
   private Integer quantityInStock;
}
