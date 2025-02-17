package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.review.CreateReviewRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.review.AvgRatingResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.review.ReviewResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.review.TotalReviewResponse;
import com.example.DATN_Fashion_Shop_BE.model.Attribute;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.Review;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final LocalizationUtils localizationUtils;

    public TotalReviewResponse totalReview (Long productId){
        Long totalReviews = reviewRepository.countByProductId(productId);
        return TotalReviewResponse.builder()
                .totalReviews(totalReviews)
                .build();
    }

    public AvgRatingResponse avgRating (Long productId){
        Double avgRating = reviewRepository.findAverageReviewRateByProductId(productId);

        if (avgRating == null) {
            avgRating = 0.0; // Tránh null pointer
        }

        // Làm tròn đến 0.5 gần nhất
        double roundedAvgRating = Math.round(avgRating * 2) / 2.0;

        return AvgRatingResponse.builder()
                .avgRating(roundedAvgRating)
                .build();
    }

    public TotalReviewResponse countReviewByRating (Long productId, String reviewRating){
        Long totalReview = reviewRepository.countByProductIdAndReviewRate(productId, reviewRating);

        if (totalReview == null) {
            totalReview = 0L;
        }

        return TotalReviewResponse.builder()
                .totalReviews(totalReview)
                .build();
    }

    public Page<ReviewResponse> getReviewsByProduct(Long productId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);

        // Chuyển đổi từ Page<Review> sang Page<ReviewResponse>
        List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
                .map(ReviewResponse::fromReview)
                .collect(Collectors.toList());

        return new PageImpl<>(reviewResponses, pageable, reviewPage.getTotalElements());
    }

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Review review = Review.builder()
                .title(request.getTitle())
                .comment(request.getComment())
                .purchasedSize(request.getPurchasedSize())
                .fit(request.getFit())
                .nickname(request.getNickname())
                .gender(request.getGender())
                .ageGroup(request.getAgeGroup())
                .height(request.getHeight())
                .weight(request.getWeight())
                .shoeSize(request.getShoeSize())
                .reviewRate(String.valueOf(request.getReviewRate()))
                .location(request.getLocation())
                .product(product)
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.fromReview(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, CreateReviewRequest request) {

        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        existingReview.setTitle(request.getTitle());
        existingReview.setComment(request.getComment());
        existingReview.setPurchasedSize(request.getPurchasedSize());
        existingReview.setFit(request.getFit());
        existingReview.setNickname(request.getNickname());
        existingReview.setGender(request.getGender());
        existingReview.setAgeGroup(request.getAgeGroup());
        existingReview.setHeight(request.getHeight());
        existingReview.setWeight(request.getWeight());
        existingReview.setShoeSize(request.getShoeSize());
        existingReview.setReviewRate(String.valueOf(request.getReviewRate())); // Convert Integer -> String
        existingReview.setLocation(request.getLocation());
        existingReview.setProduct(product);

        Review updatedReview = reviewRepository.save(existingReview);

        return ReviewResponse.fromReview(updatedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Review not found with id: " + reviewId));

        reviewRepository.deleteById(reviewId);
    }

}
