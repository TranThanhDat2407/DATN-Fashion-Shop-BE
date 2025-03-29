package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.CountReviews;
import com.example.DATN_Fashion_Shop_BE.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Long countByProductId(Long productId);

    @Query("SELECT AVG(CAST(r.reviewRate AS double)) FROM Review r WHERE r.product.id = :productId")
    Double findAverageReviewRateByProductId(Long productId);

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Long countByProductIdAndReviewRate(Long productId, String reviewRate);


    @Query("""
    SELECT new com.example.DATN_Fashion_Shop_BE.dto.response.revenue.CountReviews(
        p.id,
        COALESCE(pt.name, ''),
        COUNT(r.id) AS totalReviews,
        COALESCE(AVG(CAST(NULLIF(r.reviewRate, '') AS double)), 0) AS avgRating,
        SUM(CASE WHEN CAST(NULLIF(r.reviewRate, '') AS INTEGER) = 1 THEN 1 ELSE 0 END) AS oneStar,
        SUM(CASE WHEN CAST(NULLIF(r.reviewRate, '') AS INTEGER) = 2 THEN 1 ELSE 0 END) AS twoStars,
        SUM(CASE WHEN CAST(NULLIF(r.reviewRate, '') AS INTEGER) = 3 THEN 1 ELSE 0 END) AS threeStars,
        SUM(CASE WHEN CAST(NULLIF(r.reviewRate, '') AS INTEGER) = 4 THEN 1 ELSE 0 END) AS fourStars,
        SUM(CASE WHEN CAST(NULLIF(r.reviewRate, '') AS INTEGER) = 5 THEN 1 ELSE 0 END) AS fiveStars,
        SUM(CASE WHEN TRIM(UPPER(COALESCE(NULLIF(r.fit, ''), ''))) = 'CHẬT' THEN 1 ELSE 0 END) AS fitTight,
        SUM(CASE WHEN TRIM(UPPER(COALESCE(NULLIF(r.fit, ''), ''))) = 'HƠI CHẬT' THEN 1 ELSE 0 END) AS fitSlightlyTight,
        SUM(CASE WHEN TRIM(UPPER(COALESCE(NULLIF(r.fit, ''), ''))) = 'ĐÚNG VỚI KÍCH THƯỚC' THEN 1 ELSE 0 END) AS fitTrueToSize,
        SUM(CASE WHEN TRIM(UPPER(COALESCE(NULLIF(r.fit, ''), ''))) = 'HƠI RỘNG' THEN 1 ELSE 0 END) AS fitLoose,
        SUM(CASE WHEN TRIM(UPPER(COALESCE(NULLIF(r.fit, ''), ''))) = 'RỘNG' THEN 1 ELSE 0 END) AS fitSlightlyLoose
    )
    FROM Product p
    LEFT JOIN Review r ON r.product.id = p.id
    LEFT JOIN ProductsTranslation pt ON pt.product = p AND pt.language.code = :languageCode
    WHERE p.isActive = true
    GROUP BY p.id, pt.name
""")
    Page<CountReviews> getProductReviewStatistics(@Param("languageCode") String languageCode, Pageable pageable);






}
