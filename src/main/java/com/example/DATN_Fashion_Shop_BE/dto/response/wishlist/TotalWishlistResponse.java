package com.example.DATN_Fashion_Shop_BE.dto.response.wishlist;
import com.example.DATN_Fashion_Shop_BE.model.WishListItem;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TotalWishlistResponse {
   private Integer totalWishlist;

}
