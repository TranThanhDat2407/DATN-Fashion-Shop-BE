package com.example.DATN_Fashion_Shop_BE.dto.response.review;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvgRatingResponse {
   private Double avgRating;
}
