package com.example.DATN_Fashion_Shop_BE.specification;

import org.springframework.data.jpa.domain.Specification;
import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.CouponTranslation;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CouponSpecification {
    public static Specification<Coupon> filterCoupons(String code, LocalDateTime expirationDate,
                                                      Float discountValue, Float minOrderValue,
                                                      String languageCode) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 🔹 Tìm kiếm theo mã giảm giá (LIKE)
            if (code != null && !code.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("code"), "%" + code + "%"));
            }

            // 🔹 Tìm kiếm theo ngày hết hạn
            if (expirationDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("expirationDate"), expirationDate));
            }

            // 🔹 Tìm kiếm theo giá trị giảm giá
            if (discountValue != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountValue"), discountValue));
            }

            // 🔹 Tìm kiếm theo giá trị đơn hàng tối thiểu
            if (minOrderValue != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("minOrderValue"), minOrderValue));
            }

            // 🔹 Lọc theo ngôn ngữ (JOIN với CouponTranslation)
            if (languageCode != null && !languageCode.isEmpty()) {
                Join<Coupon, CouponTranslation> translationJoin = root.join("translations"); // Join với bảng dịch
                predicates.add(criteriaBuilder.equal(translationJoin.get("language").get("code"), languageCode));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
