package com.example.DATN_Fashion_Shop_BE.dto.response.review;
import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.Review;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse extends BaseResponse {
   private Long id;
   private String title;
   private String comment;
   private String purchasedSize;
   private String fit;
   private String nickname;
   private String gender;
   private String ageGroup;
   private String height;
   private String weight;
   private String reviewRate;
   private String location;
   private String shoeSize;
   private Long productId;

   public static ReviewResponse fromReview(Review review) {
      ReviewResponse response = ReviewResponse.builder()
              .id(review.getId())
              .title(review.getTitle())
              .comment(review.getComment())
              .purchasedSize(review.getPurchasedSize())
              .fit(review.getFit())
              .nickname(review.getNickname())
              .gender(review.getGender())
              .ageGroup(review.getAgeGroup())
              .height(review.getHeight())
              .weight(review.getWeight())
              .reviewRate(review.getReviewRate())
              .shoeSize(review.getShoeSize())
              .location(review.getLocation())
              .productId(review.getProduct() != null ? review.getProduct().getId() : null)
              .build();

      response.setCreatedAt(review.getCreatedAt());
      response.setUpdatedAt(review.getUpdatedAt());
      response.setCreatedBy(review.getCreatedBy());
      response.setUpdatedBy(review.getUpdatedBy());

      return response;
   }
}
