package com.example.DATN_Fashion_Shop_BE.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Comment cannot be empty")
    private String comment;

    @NotBlank(message = "Purchased size is required")
    private String purchasedSize;

    private String fit;
    private String nickname;
    private String gender;
    private String ageGroup;
    private String height;
    private String weight;
    private String shoeSize;
    private String location;

    @NotNull(message = "Review rate is required")
    @Min(value = 1, message = "Review rate must be at least 1")
    @Max(value = 5, message = "Review rate cannot be more than 5")
    private Integer reviewRate;
}
