package com.example.DATN_Fashion_Shop_BE.dto.response.wishlist;
import com.example.DATN_Fashion_Shop_BE.model.WishListItem;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WishlistItemResponse {
   private Long id;
   private Long productId;
   private Long colorId;
   private String colorName;

   public static WishlistItemResponse fromWishlistItem(WishListItem item) {
      return WishlistItemResponse.builder()
              .id(item.getId())
              .productId(item.getProductVariant().getProduct().getId())
              .colorId(item.getProductVariant().getColorValue().getId())
              .colorName(item.getProductVariant().getColorValue().getValueName())
              .build();
   }
}
