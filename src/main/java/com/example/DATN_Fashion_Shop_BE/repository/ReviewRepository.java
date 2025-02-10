package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import com.example.DATN_Fashion_Shop_BE.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    long countByProductId(Long productId);

    @Query("SELECT AVG(CAST(r.reviewRate AS double)) FROM Review r WHERE r.product.id = :productId")
    Double findAverageReviewRateByProductId(Long productId);

    Page<Review> findByProductId(Long productId, Pageable pageable);
}
