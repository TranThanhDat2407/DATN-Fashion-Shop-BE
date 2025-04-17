package com.example.DATN_Fashion_Shop_BE.dto.response.revenue;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountReviews {
    private Long productId;
    private String productName;
    private Long totalReviews;
    private Double avgRating;
    private Long oneStar;
    private Long twoStars;
    private Long threeStars;
    private Long fourStars;
    private Long fiveStars;
    private Long fitTight;
    private Long fitSlightlyTight;
    private Long fitTrueToSize;
    private Long fitLoose;
    private Long fitSlightlyLoose;

}
