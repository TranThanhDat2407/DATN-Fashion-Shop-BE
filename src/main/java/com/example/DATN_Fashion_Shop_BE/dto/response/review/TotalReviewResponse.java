package com.example.DATN_Fashion_Shop_BE.dto.response.review;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductMediaResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductTranslationResponse;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TotalReviewResponse {
   private Long totalReviews;
}
